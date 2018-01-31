package com.arkaces.ark_ethereum_contract_service.contract;

import com.arkaces.aces_server.aces_service.contract.Contract;
import com.arkaces.aces_server.aces_service.contract.ContractStatus;
import com.arkaces.aces_server.aces_service.contract.CreateContractRequest;
import com.arkaces.aces_server.aces_service.error.ServiceErrorCodes;
import com.arkaces.aces_server.common.error.NotFoundException;
import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import com.arkaces.ark_ethereum_contract_service.FeeSettings;
import com.arkaces.ark_ethereum_contract_service.ServiceArkAccountSettings;
import com.arkaces.ark_ethereum_contract_service.ethereum.EthereumGasService;
import com.arkaces.ark_ethereum_contract_service.ethereum.EthereumService;
import com.arkaces.ark_ethereum_contract_service.exchange_rate.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Transactional
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ContractController {
    
    private final IdentifierGenerator identifierGenerator;
    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final CreateContractRequestValidator createContractRequestValidator;
    private final FeeSettings feeSettings;
    private final ServiceArkAccountSettings serviceArkAccountSettings;
    private final ExchangeRateService exchangeRateService;
    private final EthereumService ethereumService;
    private final EthereumGasService ethereumGasService;

    @PostMapping("/contracts")
    public Contract<Results> postContract(@RequestBody CreateContractRequest<Arguments> createContractRequest) {
        createContractRequestValidator.validate(createContractRequest);

        LocalDateTime now = LocalDateTime.now();

        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setId(identifierGenerator.generate());
        contractEntity.setCorrelationId(createContractRequest.getCorrelationId());
        contractEntity.setStatus(ContractStatus.PENDING_PAYMENT);
        contractEntity.setCreatedAt(now);
        contractEntity.setExpiresAt(now.plus(1, ChronoUnit.HOURS));

        Arguments arguments = createContractRequest.getArguments();
        contractEntity.setContractAbiJson(arguments.getContractAbiJson());
        contractEntity.setContractCode(arguments.getContractCode());
        contractEntity.setGasLimit(new BigInteger(arguments.getGasLimit()));

        String arkSmartBridge = UUID.randomUUID().toString();

        contractEntity.setArkSmartBridge(arkSmartBridge);
        contractEntity.setServiceArkAddress(serviceArkAccountSettings.getAddress());

        BigDecimal arkPerEthExchangeRate = exchangeRateService.getRate("ETH", "ARK");
        contractEntity.setArkPerEthExchangeRate(arkPerEthExchangeRate.setScale(8, BigDecimal.ROUND_UP));

        BigInteger estimatedGas = ethereumService.estimateGas(contractEntity.getContractCode(), contractEntity.getGasLimit());
        contractEntity.setEstimatedGasCost(estimatedGas);

        BigDecimal estimatedEthCost = ethereumGasService.toEth(estimatedGas);
        contractEntity.setEstimatedEthCost(estimatedEthCost.setScale(8, BigDecimal.ROUND_UP));

        BigDecimal estimatedArkCost = estimatedEthCost.multiply(arkPerEthExchangeRate);
        contractEntity.setDeploymentArkCost(estimatedArkCost.setScale(8, BigDecimal.ROUND_UP));

        contractEntity.setArkFlatFee(feeSettings.getArkFlatFee().setScale(8, BigDecimal.ROUND_UP));
        contractEntity.setArkFeePercent(feeSettings.getArkPercentFee().setScale(8, BigDecimal.ROUND_UP));

        BigDecimal feeFraction = feeSettings.getArkPercentFee().divide(new BigDecimal(100), 8, RoundingMode.HALF_UP);
        BigDecimal arkPercentFee = estimatedArkCost.multiply(feeFraction);

        BigDecimal arkFeeTotal = arkPercentFee.add(arkPercentFee);
        contractEntity.setArkFeeTotal(arkFeeTotal.setScale(8, BigDecimal.ROUND_UP));

        BigDecimal requiredArk = estimatedArkCost.add(arkFeeTotal);
        contractEntity.setRequiredArk(requiredArk.setScale(8, BigDecimal.ROUND_UP));

        contractRepository.save(contractEntity);

        return contractMapper.map(contractEntity);
    }
    
    @GetMapping("/contracts/{contractId}")
    public Contract<Results> getContract(@PathVariable String contractId) {
        ContractEntity contractEntity = contractRepository.findOneById(contractId);
        if (contractEntity == null) {
            throw new NotFoundException(ServiceErrorCodes.CONTRACT_NOT_FOUND, "Contract not found with id = " + contractId);
        }
        return contractMapper.map(contractEntity);
    }
}
