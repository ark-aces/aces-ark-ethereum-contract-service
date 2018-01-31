package com.arkaces.ark_ethereum_contract_service.ethereum;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class EthereumGasService {

    private final BigInteger GAS_PER_ETH = new BigInteger("50000000");

    public BigDecimal toEth(BigInteger gas) {
        return new BigDecimal(gas)
            .divide(new BigDecimal(GAS_PER_ETH), 8, BigDecimal.ROUND_HALF_EVEN);
    }

    public BigInteger toGas(BigDecimal eth) {
        return eth
            .setScale(18, BigDecimal.ROUND_UP)
            .multiply(new BigDecimal(GAS_PER_ETH))
                .toBigInteger();
    }
}
