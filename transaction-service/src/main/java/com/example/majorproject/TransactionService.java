package com.example.majorproject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionService {

    private static final String TOPIC_TRANSACTION_INITIATED = "transaction_initiated";
    private static final String TOPIC_WALLET_UPDATE = "wallet_update";

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public String initiateTransaction(TransactionRequest transactionRequest) throws JsonProcessingException {

        Transaction transaction = Transaction.builder()
                .amount(transactionRequest.getAmount())
                .fromUser(transactionRequest.getFromUser())
                .toUser(transactionRequest.getToUser())
                .purpose(transactionRequest.getPurpose())
                .transactionId(UUID.randomUUID().toString())
                .transactionStatus(TransactionStatus.PENDING)
                .build();

        transactionRepository.save(transaction);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fromUser", transaction.getFromUser());
        jsonObject.put("toUser", transaction.getToUser());
        jsonObject.put("amount", transaction.getAmount());
        jsonObject.put("transactionId", transaction.getTransactionId());

        kafkaTemplate.send(TOPIC_TRANSACTION_INITIATED, objectMapper.writeValueAsString(jsonObject));

        return transaction.getTransactionId();
    }

    @KafkaListener(topics = {TOPIC_WALLET_UPDATE}, groupId = "wallet-update-group")
    public void updateTransaction(String msg) throws JsonProcessingException {
        JSONObject jsonObject = objectMapper.readValue(msg, JSONObject.class);

        String transactionId = (String) jsonObject.get("transactionId");
        String status = (String) jsonObject.get("status");

        TransactionStatus transactionStatus = TransactionStatus.valueOf(status);
        transactionRepository.updateTransactionByStatus(transactionStatus, transactionId);
    }

}
