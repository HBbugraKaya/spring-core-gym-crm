package com.example.gymcrm.dao.impl;

import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.Training;
import com.example.gymcrm.domain.TrainingType;
import com.example.gymcrm.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapDaoTest {
    @Test
    void traineeDaoSupportsFullCrudAndContinuesSeededSequence() {
    // Given
        Map<Long, Trainee> storage = new HashMap<>();
        storage.put(5L, trainee(5L, "Seed"));
        TraineeDaoImpl dao = new TraineeDaoImpl();
        dao.setStorage(storage);

    // When
        Trainee created = dao.create(trainee(null, "New"));

    // Then
        assertThat(created.getId()).isEqualTo(6L);
        assertThat(dao.findById(6L)).containsSame(created);
        assertThat(dao.findAll()).extracting(Trainee::getId).containsExactly(5L, 6L);

    // When
        created.setAddress("Updated");

    // Then
        assertThat(dao.update(created).getAddress()).isEqualTo("Updated");

    // When
        dao.deleteById(6L);

    // Then
        assertThat(dao.findById(6L)).isEmpty();
    }

    @Test
    void traineeDaoRejectsMissingUpdatesAndDeletes() {
    // Given
        TraineeDaoImpl dao = new TraineeDaoImpl();
        dao.setStorage(new HashMap<>());

    // When / Then
        assertThatThrownBy(() -> dao.update(trainee(99L, "Missing")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee");
        assertThatThrownBy(() -> dao.deleteById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void trainerDaoSupportsCreateUpdateFindAndList() {
    // Given
        TrainerDaoImpl dao = new TrainerDaoImpl();
        dao.setStorage(new HashMap<>());

        Trainer trainer = new Trainer(null, "Jane", "Doe", "Jane.Doe", "abcdefghij",
                true, TrainingType.YOGA);

    // When
        Trainer created = dao.create(trainer);
    trainer.setSpecialization(TrainingType.CARDIO);

    // Then
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(dao.update(trainer).getSpecialization()).isEqualTo(TrainingType.CARDIO);
        assertThat(dao.findById(1L)).containsSame(trainer);
        assertThat(dao.findAll()).containsExactly(trainer);
    }

    @Test
    void trainingDaoSupportsCreateFindAndList() {
    // Given
        TrainingDaoImpl dao = new TrainingDaoImpl();
        dao.setStorage(new HashMap<>());
        Training training = new Training(null, 1L, 2L, "Cardio", TrainingType.CARDIO,
                LocalDate.of(2026, 6, 22), 45);

    // When
        Training created = dao.create(training);

    // Then
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(dao.findById(1L)).containsSame(training);
        assertThat(dao.findAll()).containsExactly(training);
    }

    @Test
    void daoRequiresStorageInjection() {
    // Given
        TraineeDaoImpl dao = new TraineeDaoImpl();

    // When / Then
        assertThatThrownBy(dao::findAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("storage");
    }

    private static Trainee trainee(Long id, String firstName) {
        return new Trainee(id, firstName, "User", firstName + ".User", "abcdefghij",
                true, LocalDate.of(2000, 1, 1), "Address");
    }
}
