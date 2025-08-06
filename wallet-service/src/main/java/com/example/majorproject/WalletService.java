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
}
