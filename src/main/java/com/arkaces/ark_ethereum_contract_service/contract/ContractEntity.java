package com.arkaces.ark_ethereum_contract_service.contract;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "contracts")
public class ContractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;

    private String id;
    private String correlationId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @Column(columnDefinition="TEXT")
    private String contractAbiJson;

    @Column(columnDefinition="TEXT")
    private String contractCode;

    @Column(columnDefinition="TEXT")
    private String contractParamsJson;

    private BigInteger gasLimit;

    private String arkSmartBridge;
    private String serviceArkAddress;
    private String returnArkAddress;
    private BigDecimal arkPerEthExchangeRate;
    private BigInteger estimatedGasCost;
    private BigDecimal estimatedEthCost;
    private BigDecimal arkFlatFee;
    private BigDecimal arkFeePercent;
    private BigDecimal arkFeeTotal;
    private BigDecimal requiredArk;

    private String arkPaymentTransactionId;
    private BigDecimal arkPaymentAmount;

    private BigInteger gasUsed;
    private BigDecimal deploymentArkCost;
    private String returnArkTransactionId;
    private String returnArkTransactionUrl;
    private BigDecimal returnArkAmount;
    private String ethContractTransactionId;
    private String ethContractTransactionUrl;
}
