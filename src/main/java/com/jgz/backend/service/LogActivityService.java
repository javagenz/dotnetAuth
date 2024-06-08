package com.jgz.backend.service;

import com.jgz.backend.repository.UserActivityRepository;
import com.jgz.backend.entity.UserActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogActivityService {

    @Autowired
    private UserActivityRepository userActivityRepository;

    public void recordActivity(Long userId, String email, String activityType) {
        UserActivity userActivity = new UserActivity();
        userActivity.setUserId(userId);
        userActivity.setEmail(email);
        userActivity.setActivityType(activityType);
        userActivityRepository.save(userActivity);
    }

    public void recordActivity(String email, String activityType) {
        UserActivity userActivity = new UserActivity();
        userActivity.setEmail(email);
        userActivity.setActivityType(activityType);
        userActivityRepository.save(userActivity);
    }
}

