package com.example.gymcrm.config;

import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.Training;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class StorageConfig {
    @Bean(StorageBeanNames.TRAINEE_STORAGE)
    public Map<Long, Trainee> traineeStorage() {
        return new HashMap<>();
    }

    @Bean(StorageBeanNames.TRAINER_STORAGE)
    public Map<Long, Trainer> trainerStorage() {
        return new HashMap<>();
    }

    @Bean(StorageBeanNames.TRAINING_STORAGE)
    public Map<Long, Training> trainingStorage() {
        return new HashMap<>();
    }
}
