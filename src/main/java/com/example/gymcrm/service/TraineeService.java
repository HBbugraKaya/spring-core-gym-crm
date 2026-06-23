package com.example.gymcrm.service;

import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.service.command.CreateTraineeCommand;
import com.example.gymcrm.service.command.UpdateTraineeCommand;

import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee create(CreateTraineeCommand command);

    Trainee update(Long id, UpdateTraineeCommand command);

    void delete(Long id);

    Optional<Trainee> findById(Long id);

    List<Trainee> findAll();
}
