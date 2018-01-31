package com.arkaces.ark_ethereum_contract_service.ark_event_handler;

import ark_java_client.ArkClient;
import com.arkaces.aces_server.aces_service.contract.ContractStatus;
import com.arkaces.ark_ethereum_contract_service.Constants;
import com.arkaces.ark_ethereum_contract_service.ServiceArkAccountSettings;
import com.arkaces.ark_ethereum_contract_service.ServiceEthAccountSettings;
import com.arkaces.ark_ethereum_contract_service.ark.ArkSatoshiService;
import com.arkaces.ark_ethereum_contract_service.contract.ContractEntity;
import com.arkaces.ark_ethereum_contract_service.contract.ContractRepository;
import com.arkaces.ark_ethereum_contract_service.ethereum.EthereumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ArkEventHandler {

    private final ContractRepository contractRepository;
    private final ArkSatoshiService arkSatoshiService;
    private final ServiceEthAccountSettings serviceEthAccountSettings;
    private final EthereumService ethereumService;
    private final ArkClient arkClient;
    private final ServiceArkAccountSettings serviceArkAccountSettings;

    @PostMapping("/arkEvents")
    public ResponseEntity<Void> handleArkEvent(@RequestBody ArkEventPayload eventPayload) {
        String arkTransactionId = eventPayload.getTransactionId();
        ArkTransaction transaction = eventPayload.getTransaction();

        log.info("Received ark event: {} -> {}", arkTransactionId, transaction.toString());

        ContractEntity contractEntity = contractRepository.findOneByArkSmartBridge(transaction.getVendorField());
        if (contractEntity != null && contractEntity.getStatus().equals(ContractStatus.PENDING_PAYMENT)) {
            log.info("Matched event for contract id {}, ark transaction id {}", contractEntity.getId(), arkTransactionId);

            // Get ARK amount from transaction
            Long arkSatoshis = transaction.getAmount();
            BigDecimal arkAmount = arkSatoshiService.toArk(arkSatoshis);
            contractEntity.setArkPaymentAmount(arkAmount.setScale(8, BigDecimal.ROUND_UP));
            contractEntity.setArkPaymentTransactionId(arkTransactionId);

            contractEntity.setStatus("executing");
            contractRepository.save(contractEntity);

            BigDecimal totalEthCost = contractEntity.getEstimatedEthCost();

            // Check that service has enough eth to send
            BigDecimal serviceAvailableEth = ethereumService.getBalance(serviceEthAccountSettings.getAddress());
            if (totalEthCost.compareTo(serviceAvailableEth) < 0) {

                BigDecimal paymentArk = arkSatoshiService.toArk(transaction.getAmount());
                if (paymentArk.compareTo(contractEntity.getRequiredArk()) > 0) {
                    // Create ethereum smart contract
                    String ethTransactionId = ethereumService.createSmartContract(
                            contractEntity.getContractCode(),
                            contractEntity.getGasLimit()
                    );

                    // Check if eth transaction was successful
                    if (ethTransactionId != null) {
                        contractEntity.setEthContractTransactionId(ethTransactionId);

                        contractEntity.setStatus(ContractStatus.EXECUTED);

                        log.info("Successfully deployed eth smart contract " + ethTransactionId
                                + " for service contract " + contractEntity.getId());

                        // return change
                        Long change = transaction.getAmount() - arkSatoshiService.toSatoshi(contractEntity.getRequiredArk());
                        if (change > arkSatoshiService.toSatoshi(Constants.ARK_TRANSACTION_FEE)) {
                            returnArk(contractEntity, transaction.getSenderId(), change);
                        }
                    }
                    else {
                        log.error("Failed to send ETH transaction, ark transaction id {}", arkTransactionId);
                        contractEntity.setStatus("failed");

                        returnArk(contractEntity, transaction.getSenderId(), transaction.getAmount());
                    }
                } else {
                    log.error("Payment insufficient for " + contractEntity.getId()
                            + " and ark transaction id " + transaction.getId());
                    contractEntity.setStatus("failed");
                }
            }
            else {
                log.error("Service account has insufficient eth to deploy contract " + contractEntity.getId());
                contractEntity.setStatus("failed");

                returnArk(contractEntity, transaction.getSenderId(), transaction.getAmount());
            }

            contractRepository.save(contractEntity);
        }

        return ResponseEntity.ok().build();
    }

    private void returnArk(ContractEntity contractEntity, String arkAddress, Long arkSatoshis) {
        BigDecimal returnArkAmount = arkSatoshiService.toArk(arkSatoshis);
        contractEntity.setReturnArkAmount(returnArkAmount);

        String returnArkTransactionId = arkClient.broadcastTransaction(
                arkAddress,
            arkSatoshis,
            contractEntity.getArkSmartBridge(),
            serviceArkAccountSettings.getPassphrase(),
            10
        );
        contractEntity.setReturnArkAddress(returnArkTransactionId);

        log.info("Returned " + returnArkAmount + " ark to " + arkAddress + " for contract " + contractEntity.getId());
    }
}
