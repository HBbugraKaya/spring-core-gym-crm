package com.example.gymcrm.service;

import com.example.gymcrm.domain.Training;
import com.example.gymcrm.service.command.CreateTrainingCommand;

import java.util.List;
import java.util.Optional;

public interface TrainingService {
    Training create(CreateTrainingCommand command);

    Optional<Training> findById(Long id);

    List<Training> findAll();
}
