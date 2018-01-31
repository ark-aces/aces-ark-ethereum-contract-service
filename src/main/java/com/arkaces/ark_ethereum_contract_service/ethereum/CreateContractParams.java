package com.arkaces.ark_ethereum_contract_service.ethereum;

import lombok.Data;

@Data
public class CreateContractParams {
    private String from;
    private String data;
    private String gasPrice;
    private String gas;
}
