package com.example.gymcrm.dao.impl;

import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.Training;
import com.example.gymcrm.domain.TrainingType;
import com.example.gymcrm.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapDaoTest {
    @Test
    void traineeDaoSupportsFullCrudAndContinuesSeededSequence() {
        Map<Long, Trainee> storage = new ConcurrentHashMap<>();
        storage.put(5L, trainee(5L, "Seed"));
        TraineeDaoImpl dao = new TraineeDaoImpl();
        dao.setStorage(storage);

        Trainee created = dao.create(trainee(null, "New"));
        assertThat(created.getId()).isEqualTo(6L);
        assertThat(dao.findById(6L)).containsSame(created);
        assertThat(dao.findAll()).extracting(Trainee::getId).containsExactly(5L, 6L);

        created.setAddress("Updated");
        assertThat(dao.update(created).getAddress()).isEqualTo("Updated");
        dao.deleteById(6L);
        assertThat(dao.findById(6L)).isEmpty();
    }

    @Test
    void traineeDaoRejectsMissingUpdatesAndDeletes() {
        TraineeDaoImpl dao = new TraineeDaoImpl();
        dao.setStorage(new ConcurrentHashMap<>());

        assertThatThrownBy(() -> dao.update(trainee(99L, "Missing")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee");
        assertThatThrownBy(() -> dao.deleteById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void trainerDaoSupportsCreateUpdateFindAndList() {
        TrainerDaoImpl dao = new TrainerDaoImpl();
        dao.setStorage(new ConcurrentHashMap<>());

        Trainer trainer = new Trainer(null, "Jane", "Doe", "Jane.Doe", "abcdefghij",
                true, TrainingType.YOGA);
        assertThat(dao.create(trainer).getId()).isEqualTo(1L);
        trainer.setSpecialization(TrainingType.CARDIO);
        assertThat(dao.update(trainer).getSpecialization()).isEqualTo(TrainingType.CARDIO);
        assertThat(dao.findById(1L)).containsSame(trainer);
        assertThat(dao.findAll()).containsExactly(trainer);
    }

    @Test
    void trainingDaoSupportsCreateFindAndList() {
        TrainingDaoImpl dao = new TrainingDaoImpl();
        dao.setStorage(new ConcurrentHashMap<>());
        Training training = new Training(null, 1L, 2L, "Cardio", TrainingType.CARDIO,
                LocalDate.of(2026, 6, 22), 45);

        assertThat(dao.create(training).getId()).isEqualTo(1L);
        assertThat(dao.findById(1L)).containsSame(training);
        assertThat(dao.findAll()).containsExactly(training);
    }

    @Test
    void daoRequiresStorageInjection() {
        TraineeDaoImpl dao = new TraineeDaoImpl();

        assertThatThrownBy(dao::findAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("storage");
    }

    private static Trainee trainee(Long id, String firstName) {
        return new Trainee(id, firstName, "User", firstName + ".User", "abcdefghij",
                true, LocalDate.of(2000, 1, 1), "Address");
    }
}
