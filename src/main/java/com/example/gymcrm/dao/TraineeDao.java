package com.example.gymcrm.dao;

import com.example.gymcrm.domain.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    Trainee create(Trainee trainee);

    Trainee update(Trainee trainee);

    void deleteById(Long id);

    Optional<Trainee> findById(Long id);

    List<Trainee> findAll();
}
