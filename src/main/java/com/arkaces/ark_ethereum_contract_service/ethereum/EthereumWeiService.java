package com.arkaces.ark_ethereum_contract_service.ethereum;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class EthereumWeiService {

    private final BigInteger WEI_PER_ETHER = new BigInteger("1000000000000000000");

    public BigInteger toWei(BigDecimal etherAmount) {
        return etherAmount
                .multiply(new BigDecimal(WEI_PER_ETHER))
                .toBigIntegerExact();
    }

    public BigDecimal toEther(BigInteger wei) {
        return new BigDecimal(wei)
                .setScale(18, BigDecimal.ROUND_UP)
                .divide(new BigDecimal(WEI_PER_ETHER), BigDecimal.ROUND_UP);
    }
}
