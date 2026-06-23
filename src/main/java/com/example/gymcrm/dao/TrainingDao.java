package com.example.gymcrm.dao;

import com.example.gymcrm.domain.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingDao {
    Training create(Training training);

    Optional<Training> findById(Long id);

    List<Training> findAll();
}
