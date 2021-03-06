package com.arkaces.ark_ethereum_contract_service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "serviceArkAccount")
public class ServiceArkAccountSettings {
    private String address;
    private String passphrase;
}
