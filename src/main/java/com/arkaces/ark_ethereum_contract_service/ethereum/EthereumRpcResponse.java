package com.arkaces.ark_ethereum_contract_service.ethereum;

import lombok.Data;

@Data
public class EthereumRpcResponse<T> {

    private Integer id;
    private String jsonrpc;
    private T result;
    private RpcError error;
}
