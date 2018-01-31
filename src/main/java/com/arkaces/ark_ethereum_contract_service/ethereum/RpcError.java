package com.arkaces.ark_ethereum_contract_service.ethereum;

import lombok.Data;

@Data
public class RpcError {
    private Integer code;
    private String message;
}
