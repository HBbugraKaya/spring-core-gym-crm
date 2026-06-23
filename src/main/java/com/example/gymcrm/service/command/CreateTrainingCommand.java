package com.example.gymcrm.service.command;

import com.example.gymcrm.domain.TrainingType;

import java.time.LocalDate;

public record CreateTrainingCommand(
        Long traineeId,
        Long trainerId,
        String name,
        TrainingType trainingType,
        LocalDate date,
        int durationMinutes) {
}
