package com.example.majorproject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

@Service
public class TransactionService {

    private static final String TOPIC_TRANSACTION_INITIATED = "transaction_initiated";
    private static final String TOPIC_WALLET_UPDATE = "wallet_update";
    private static final String TOPIC_TRANSACTION_COMPLETE = "transaction_complete";

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RestTemplate restTemplate;

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
        String fromUser = (String) jsonObject.get("fromUser");
        String toUser = (String) jsonObject.get("toUser");

        TransactionStatus transactionStatus = TransactionStatus.valueOf(status);
        transactionRepository.updateTransactionByStatus(transactionStatus, transactionId);

        // publish the transaction complete event, to be consumed by notification service
        URI uri = URI.create("http://localhost:9000/user?id=" + fromUser);
        JSONObject fromUserDetails = restTemplate.exchange(uri,
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                JSONObject.class).getBody();

        // publish the event
        JSONObject transactionCompleteEvent = new JSONObject();
        transactionCompleteEvent.put("email", fromUserDetails.get("email"));
        transactionCompleteEvent.put("message", "Hey " + fromUser + "!, your transaction with ID: " + transactionId + "got " + transactionStatus);

        kafkaTemplate.send(TOPIC_TRANSACTION_COMPLETE, objectMapper.writeValueAsString(transactionCompleteEvent));

        if(TransactionStatus.SUCCESS.equals(transactionStatus)) {
            // in case transaction is successful, send an event for the toUser as well
            int amount = (int) jsonObject.get("amount");

            uri = URI.create("http://localhost:9000/user?id=" + toUser);
            JSONObject toUserDetails = restTemplate.exchange(uri,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    JSONObject.class).getBody();

            transactionCompleteEvent = new JSONObject();
            transactionCompleteEvent.put("email", toUserDetails.get("email"));
            transactionCompleteEvent.put("message", "Hey " + toUser + "!, you have received amount: " + amount + "from " + fromUser + "Transaction ID: " + transactionId);

            kafkaTemplate.send(TOPIC_TRANSACTION_COMPLETE, objectMapper.writeValueAsString(transactionCompleteEvent));
        }
    }

}
