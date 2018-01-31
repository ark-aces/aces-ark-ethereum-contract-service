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

    private BigInteger gasLimit;

    private String arkSmartBridge;

    private String serviceArkAddress;

    private String returnArkAddress;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkPerEthExchangeRate;

    private BigInteger estimatedGasCost;

    @Column(precision = 20, scale = 8)
    private BigDecimal estimatedEthCost;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkFlatFee;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkFeePercent;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkFeeTotal;

    @Column(precision = 20, scale = 8)
    private BigDecimal requiredArk;

    private String arkPaymentTransactionId;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkPaymentAmount;

    private BigInteger gasUsed;

    @Column(precision = 20, scale = 8)
    private BigDecimal deploymentArkCost;

    private String returnArkTransactionId;

    @Column(precision = 20, scale = 8)
    private BigDecimal returnArkAmount;

    private String ethContractTransactionId;
}
