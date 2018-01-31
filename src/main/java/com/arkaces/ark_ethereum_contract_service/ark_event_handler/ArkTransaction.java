package com.arkaces.ark_ethereum_contract_service.ark_event_handler;

import lombok.Data;

import java.util.List;

@Data
public class ArkTransaction {

    private String id;
    private Integer height;
    private String blockId;
    private Integer type;
    private Integer timestamp;
    private String senderPublicKey;
    private String senderId;
    private String recipientId;
    private Long amount;
    private Long fee;
    private String signature;
    private List<String> signatures;
    private Integer confirmations;
    private String vendorField;
    private Object asset;
}
