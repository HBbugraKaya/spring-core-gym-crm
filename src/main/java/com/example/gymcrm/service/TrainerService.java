package com.example.gymcrm.service;

import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.service.command.CreateTrainerCommand;
import com.example.gymcrm.service.command.UpdateTrainerCommand;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer create(CreateTrainerCommand command);

    Trainer update(Long id, UpdateTrainerCommand command);

    Optional<Trainer> findById(Long id);

    List<Trainer> findAll();
}
