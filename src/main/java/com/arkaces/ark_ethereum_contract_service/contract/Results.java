package com.arkaces.ark_ethereum_contract_service.contract;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class Results {
    private String contractAbiJson;
    private String contractCode;
    private String contractParamsJson;
    private Long gasLimit;

    private String arkSmartBridge;
    private String serviceArkAddress;
    private String returnArkAddress;
    private BigDecimal arkPerEthExchangeRate;
    private BigInteger estimatedGasCost;
    private BigDecimal estimatedEthCost;
    private BigDecimal deploymentArkCost;
    private BigDecimal arkFlatFee;
    private BigDecimal arkFeePercent;
    private BigDecimal arkFeeTotal;
    private BigDecimal requiredArk;

    private String arkPaymentTransactionId;
    private BigDecimal arkPaymentAmount;
    private BigDecimal gasUsed;

    private String returnArkTransactionId;
    private String returnArkTransactionUrl;
    private BigDecimal returnArkAmount;
    private String ethContractTransactionId;
    private String ethContractTransactionUrl;
}