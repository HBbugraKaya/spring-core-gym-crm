package com.example.gymcrm.service.command;

import com.example.gymcrm.domain.TrainingType;

public record UpdateTrainerCommand(
        String firstName,
        String lastName,
        TrainingType specialization,
        boolean active) {
}
