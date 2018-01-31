package com.arkaces.ark_ethereum_contract_service.ethereum;

import com.arkaces.aces_server.common.json.NiceObjectMapper;
import com.arkaces.ark_ethereum_contract_service.ServiceEthAccountSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EthereumService {

    private final EthereumWeiService ethereumWeiService;
    private final NiceObjectMapper objectMapper = new NiceObjectMapper(new ObjectMapper());
    private final EthereumRpcRequestFactory ethereumRpcRequestFactory = new EthereumRpcRequestFactory();
    private final RestTemplate ethereumRpcRestTemplate;
    private final ServiceEthAccountSettings serviceEthAccountSettings;

    private final BigInteger gasPrice = new BigInteger("20000000000");
    private final Integer accountUnlockTimeoutSeconds = 30;

    public BigDecimal getBalance(String address) {
        HttpEntity<String> requestEntity = getRequestEntity("eth_getBalance", Arrays.asList(address, "latest"));
        EthereumRpcResponse<String> response = ethereumRpcRestTemplate
            .exchange(
                "/",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<EthereumRpcResponse<String>>() {}
            )
            .getBody();

        if (response.getError() != null) {
            RpcError rpcError = response.getError();
            throw new EthereumRpcException("Failed to get balance", rpcError.getCode(), rpcError.getMessage());
        }

        BigInteger wei = getBigIntegerFromHexString(response.getResult());

        return ethereumWeiService.toEther(wei);
    }

    public String createSmartContract(String data, BigInteger gasLimit) {
        EthereumRpcResponse<Boolean> unlockResponse = ethereumRpcRestTemplate
            .exchange(
                "/",
                HttpMethod.POST,
                getRequestEntity("personal_unlockAccount", Arrays.asList(
                        serviceEthAccountSettings.getAddress(),
                        serviceEthAccountSettings.getPassphrase(),
                        accountUnlockTimeoutSeconds
                )),
                new ParameterizedTypeReference<EthereumRpcResponse<Boolean>>() {}
            )
            .getBody();

        if (unlockResponse.getError() != null || unlockResponse.getResult() == null || ! unlockResponse.getResult()) {
            RpcError rpcError = unlockResponse.getError();
            throw new EthereumRpcException("Failed to unlock service account", rpcError.getCode(), rpcError.getMessage());
        }

        CreateContractParams createContractParams = new CreateContractParams();
        createContractParams.setFrom(serviceEthAccountSettings.getAddress());
        createContractParams.setData(data);
        createContractParams.setGasPrice(getHexString(gasPrice));
        createContractParams.setGas(getHexString(gasLimit));

        EthereumRpcResponse<String> response = ethereumRpcRestTemplate
            .exchange(
                "/",
                HttpMethod.POST,
                    getRequestEntity("eth_sendTransaction", Collections.singletonList(createContractParams)),
                new ParameterizedTypeReference<EthereumRpcResponse<String>>() {}
            )
            .getBody();

        if (response.getError() != null) {
            RpcError rpcError = response.getError();
            throw new EthereumRpcException("Failed to estimate gas", rpcError.getCode(), rpcError.getMessage());
        }

        return response.getResult();
    }

    public BigInteger estimateGas(String data, BigInteger gasLimit) {
        CreateContractParams createContractParams = new CreateContractParams();
        createContractParams.setFrom(serviceEthAccountSettings.getAddress());
        createContractParams.setData(data);
        createContractParams.setGasPrice(getHexString(gasPrice));
        createContractParams.setGas(getHexString(gasLimit));

        HttpEntity<String> requestEntity = getRequestEntity("eth_estimateGas", Collections.singletonList(createContractParams));
        EthereumRpcResponse<String> response = ethereumRpcRestTemplate
            .exchange(
                "/",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<EthereumRpcResponse<String>>() {}
            )
            .getBody();

        if (response.getError() != null) {
            RpcError rpcError = response.getError();
            throw new EthereumRpcException("Failed to estimate gas", rpcError.getCode(), rpcError.getMessage());
        }

        return getBigIntegerFromHexString(response.getResult());
    }

    private BigInteger getBigIntegerFromHexString(String hexString) {
       return new BigInteger(hexString.replaceFirst("0x", ""), 16);
    }

    private String getHexString(BigInteger input) {
        return "0x" + removeLeadingZeros(input.toString(16));
    }

    private String removeLeadingZeros(String s) {
        int index = findFirstNonZeroIndex(s);
        if (index == -1) {
            return "0";
        }
        return s.substring(index);
    }

    private int findFirstNonZeroIndex(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '0') {
                return i;
            }
        }
        return -1;
    }

    private HttpEntity<String> getRequestEntity(String method, List<Object> params) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");

        EthereumRpcRequest ethereumRpcRequest = ethereumRpcRequestFactory.create(method, params);
        String body = objectMapper.writeValueAsString(ethereumRpcRequest);

        return new HttpEntity<>(body, headers);
    }
}
