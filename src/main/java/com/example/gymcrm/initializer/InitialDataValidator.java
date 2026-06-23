package com.example.gymcrm.initializer;

import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.Training;
import com.example.gymcrm.domain.User;
import com.example.gymcrm.exception.StorageInitializationException;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

final class InitialDataValidator {
    void validate(InitialData data) {
        if (data == null) {
            throw new StorageInitializationException("Initial data must not be null");
        }

        Set<Long> traineeIds = validateTrainees(data.trainees());
        Set<Long> trainerIds = validateTrainers(data.trainers());
        validateTrainings(data.trainings(), traineeIds, trainerIds);
        validateUniqueUsernames(data);
    }

    private Set<Long> validateTrainees(List<Trainee> trainees) {
        Set<Long> ids = validateIds(trainees, Trainee::getId, "Trainee");
        for (Trainee trainee : trainees) {
            validateUser(trainee, "Trainee");
            requireNonNull(trainee.getDateOfBirth(), "Trainee dateOfBirth");
            requireText(trainee.getAddress(), "Trainee address");
        }
        return ids;
    }

    private Set<Long> validateTrainers(List<Trainer> trainers) {
        Set<Long> ids = validateIds(trainers, Trainer::getId, "Trainer");
        for (Trainer trainer : trainers) {
            validateUser(trainer, "Trainer");
            requireNonNull(trainer.getSpecialization(), "Trainer specialization");
        }
        return ids;
    }

    private void validateTrainings(List<Training> trainings, Set<Long> traineeIds, Set<Long> trainerIds) {
        validateIds(trainings, Training::getId, "Training");
        for (Training training : trainings) {
            requirePositive(training.getTraineeId(), "Training traineeId");
            requirePositive(training.getTrainerId(), "Training trainerId");
            requireText(training.getName(), "Training name");
            requireNonNull(training.getTrainingType(), "Training trainingType");
            requireNonNull(training.getDate(), "Training date");
            if (training.getDurationMinutes() <= 0) {
                throw new StorageInitializationException("Training durationMinutes must be positive");
            }
            if (!traineeIds.contains(training.getTraineeId())) {
                throw new StorageInitializationException("Training references unknown trainee id=" + training.getTraineeId());
            }
            if (!trainerIds.contains(training.getTrainerId())) {
                throw new StorageInitializationException("Training references unknown trainer id=" + training.getTrainerId());
            }
        }
    }

    private <T> Set<Long> validateIds(List<T> entities, Function<T, Long> idExtractor, String entityType) {
        Set<Long> ids = new HashSet<>();
        for (T entity : entities) {
            if (entity == null) {
                throw new StorageInitializationException(entityType + " entry must not be null");
            }
            Long id = idExtractor.apply(entity);
            requirePositive(id, entityType + " id");
            if (!ids.add(id)) {
                throw new StorageInitializationException("Duplicate " + entityType + " id=" + id);
            }
        }
        return ids;
    }

    private void validateUser(User user, String entityType) {
        requireText(user.getFirstName(), entityType + " firstName");
        requireText(user.getLastName(), entityType + " lastName");
        requireText(user.getUsername(), entityType + " username");
        String password = requireText(user.getPassword(), entityType + " password");
        if (!password.matches("[A-Za-z0-9]{10}")) {
            throw new StorageInitializationException(entityType + " password must be 10 alphanumeric characters");
        }
    }

    private void validateUniqueUsernames(InitialData data) {
        Set<String> usernames = new HashSet<>();
        for (Trainee trainee : data.trainees()) {
            addUsername(usernames, trainee.getUsername());
        }
        for (Trainer trainer : data.trainers()) {
            addUsername(usernames, trainer.getUsername());
        }
    }

    private void addUsername(Set<String> usernames, String username) {
        if (!usernames.add(username.trim().toLowerCase(Locale.ROOT))) {
            throw new StorageInitializationException("Duplicate username in initial data");
        }
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new StorageInitializationException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new StorageInitializationException(fieldName + " must not be null");
        }
    }

    private void requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new StorageInitializationException(fieldName + " must be positive");
        }
    }
}
