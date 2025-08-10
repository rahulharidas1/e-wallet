package com.example.majorproject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    //TODO: Move these variables to application.properties
    private static final String USER_CREATE_TOPIC = "user_create";
    private static final String TOPIC_TRANSACTION_INITIATED = "transaction_initiated";
    private static final String WALLET_UPDATE = "wallet_update";

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @KafkaListener(topics = {USER_CREATE_TOPIC}, groupId = "user-create-group")
    public void createWallet(String msg) throws JsonProcessingException {
        JSONObject jsonObject = objectMapper.readValue(msg, JSONObject.class);

        String userId = (String) jsonObject.get("userId");
        int amount = (int) jsonObject.get("amount");

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .amount(amount)
                .build();

        //save wallet to DB
        walletRepository.save(wallet);
    }

    @KafkaListener(topics = {TOPIC_TRANSACTION_INITIATED}, groupId = "transaction-initiated-group")
    public void updateWalletsForTransaction(String msg) throws JsonProcessingException {
        JSONObject jsonObject = objectMapper.readValue(msg, JSONObject.class);
        String fromUser = (String) jsonObject.get("fromUser");
        String toUser = (String) jsonObject.get("toUser");
        int amount = (int) jsonObject.get("amount");
        String transactionId = (String) jsonObject.get("transactionId");

        JSONObject transactionUpdateRequest = new JSONObject();
        Wallet fromWallet = walletRepository.findByUserId(fromUser);

        transactionUpdateRequest.put("transactionId", transactionId);
        if (fromWallet == null || fromWallet.getAmount() - amount < 0) {
            transactionUpdateRequest.put("status", "FAILED");
        } else {
            walletRepository.updateWallet(fromUser, 0 - amount);
            walletRepository.updateWallet(toUser, amount);

            transactionUpdateRequest.put("status", "SUCCESS");
        }

        // publish wallet_update event for transaction service to consume and mark transaction status
        kafkaTemplate.send(WALLET_UPDATE, objectMapper.writeValueAsString(transactionUpdateRequest));
    }
}
