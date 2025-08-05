package com.example.majorproject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    //TODO: Move these variables to application.properties
    private static final Integer AMOUNT = 10;
    private static final String USER_CREATE_TOPIC = "user_create";

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserCacheRepository userCacheRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public User getUser(String userId) {
        User user = userCacheRepository.getUser(userId);
        if (user == null) {
            user = userRepository.findById(userId);
            userCacheRepository.addUser(user);
        }
        return user;
    }

    public void addUser(User user) throws JsonProcessingException {
        userRepository.save(user);

        //save it in the Redis cache
        userCacheRepository.addUser(user);

        //Publish kafka event for USER_CREATE
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", user.getUserId());
        jsonObject.put("amount", AMOUNT);
        kafkaTemplate.send(USER_CREATE_TOPIC, objectMapper.writeValueAsString(jsonObject));
    }
}
