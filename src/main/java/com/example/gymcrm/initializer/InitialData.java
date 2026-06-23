package com.example.gymcrm.initializer;

import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.Training;

import java.util.List;

public record InitialData(List<Trainee> trainees, List<Trainer> trainers, List<Training> trainings) {
    public InitialData {
        trainees = trainees == null ? List.of() : List.copyOf(trainees);
        trainers = trainers == null ? List.of() : List.copyOf(trainers);
        trainings = trainings == null ? List.of() : List.copyOf(trainings);
    }
}
