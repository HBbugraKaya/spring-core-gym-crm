package com.example.gymcrm.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DomainModelTest {
    @Test
    void userModelsExposeFieldsButNeverRenderCredentialsOrPersonalData() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setFirstName("Alice");
        trainee.setLastName("Brown");
        trainee.setUsername("Alice.Brown");
        trainee.setPassword("secret1234");
        trainee.setActive(true);
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 2));
        trainee.setAddress("Private address");

        assertThat(trainee.getId()).isEqualTo(1L);
        assertThat(trainee.getFirstName()).isEqualTo("Alice");
        assertThat(trainee.getLastName()).isEqualTo("Brown");
        assertThat(trainee.getUsername()).isEqualTo("Alice.Brown");
        assertThat(trainee.getPassword()).isEqualTo("secret1234");
        assertThat(trainee.isActive()).isTrue();
        assertThat(trainee.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 2));
        assertThat(trainee.getAddress()).isEqualTo("Private address");
        assertThat(trainee.toString()).doesNotContain("secret1234", "Private address", "Alice.Brown");

        Trainer trainer = new Trainer();
        trainer.setSpecialization(TrainingType.STRENGTH);
        assertThat(trainer.getSpecialization()).isEqualTo(TrainingType.STRENGTH);
    }

    @Test
    void trainingModelExposesFieldsAndSafeSummary() {
        Training training = new Training();
        training.setId(3L);
        training.setTraineeId(1L);
        training.setTrainerId(2L);
        training.setName("Private session name");
        training.setTrainingType(TrainingType.YOGA);
        training.setDate(LocalDate.of(2026, 1, 1));
        training.setDurationMinutes(30);

        assertThat(training.getId()).isEqualTo(3L);
        assertThat(training.getTraineeId()).isEqualTo(1L);
        assertThat(training.getTrainerId()).isEqualTo(2L);
        assertThat(training.getName()).isEqualTo("Private session name");
        assertThat(training.getTrainingType()).isEqualTo(TrainingType.YOGA);
        assertThat(training.getDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(training.getDurationMinutes()).isEqualTo(30);
        assertThat(training.toString()).doesNotContain("Private session name");
    }
}
