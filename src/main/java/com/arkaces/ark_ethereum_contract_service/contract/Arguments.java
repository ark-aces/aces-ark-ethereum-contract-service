package com.arkaces.ark_ethereum_contract_service.contract;

import lombok.Data;

@Data
public class Arguments {
    private String contractAbiJson;
    private String contractCode;
    private String gasLimit;
}
