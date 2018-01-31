package com.arkaces.ark_ethereum_contract_service.error;

import com.arkaces.aces_server.common.error.GeneralError;
import com.arkaces.ark_ethereum_contract_service.ethereum.EthereumRpcException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class AppErrorControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(EthereumRpcException.class)
    public GeneralError handleNotFoundException(EthereumRpcException ethereumRpcException) {
        log.error("Ethereum error occurred", ethereumRpcException);

        GeneralError generalError = new GeneralError();
        generalError.setCode("ethereumError");
        generalError.setMessage("Ethereum error occurred: " + ethereumRpcException.getEthErrorMessage()
            + " (code: " + ethereumRpcException.getEthErrorCode() + ")");

        return generalError;
    }
}