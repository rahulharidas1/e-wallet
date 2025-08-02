package com.example.majorproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserCacheRepository userCacheRepository;

    public User getUser(String userId) {
        User user = userCacheRepository.getUser(userId);
        if (user == null) {
            user = userRepository.findById(userId);
            userCacheRepository.addUser(user);
        }
        return user;
    }

    public void addUser(User user) {
        userRepository.save(user);

        //save it in the Redis cache
        userCacheRepository.addUser(user);

        //Publish kafka event for USER_CREATE
    }
}
