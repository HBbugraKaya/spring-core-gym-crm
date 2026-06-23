package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.TrainingType;
import com.example.gymcrm.exception.EntityNotFoundException;
import com.example.gymcrm.exception.ValidationException;
import com.example.gymcrm.generator.PasswordGenerator;
import com.example.gymcrm.generator.UsernameGenerator;
import com.example.gymcrm.service.command.CreateTrainerCommand;
import com.example.gymcrm.service.command.UpdateTrainerCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;

    private TrainerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TrainerServiceImpl();
        service.setTrainerDao(trainerDao);
        service.setUsernameGenerator(usernameGenerator);
        service.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void createsTrainerWithGeneratedCredentials() {
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generate()).thenReturn("A1b2C3d4E5");
        when(trainerDao.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer result = service.create(new CreateTrainerCommand(
                " John ", " Smith ", TrainingType.FITNESS, true));

        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getUsername()).isEqualTo("John.Smith");
        assertThat(result.getPassword()).isEqualTo("A1b2C3d4E5");
        assertThat(result.getSpecialization()).isEqualTo(TrainingType.FITNESS);
    }

    @Test
    void updatesTrainerAndPreservesCredentials() {
        Trainer current = trainer(2L);
        when(trainerDao.findById(2L)).thenReturn(Optional.of(current));
        when(trainerDao.update(current)).thenReturn(current);

        Trainer result = service.update(2L,
                new UpdateTrainerCommand("Updated", "Trainer", TrainingType.YOGA, false));

        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getUsername()).isEqualTo("John.Smith");
        assertThat(result.getPassword()).isEqualTo("abcdefghij");
        assertThat(result.getSpecialization()).isEqualTo(TrainingType.YOGA);
        assertThat(result.isActive()).isFalse();
    }

    @Test
    void handlesMissingAndInvalidProfiles() {
        when(trainerDao.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(8L,
                new UpdateTrainerCommand("Missing", "Trainer", TrainingType.YOGA, true)))
                .isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> service.create(new CreateTrainerCommand(
                "John", "Smith", null, true))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.findById(null)).isInstanceOf(ValidationException.class);
    }

    @Test
    void delegatesFindAndList() {
        Trainer trainer = trainer(1L);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerDao.findAll()).thenReturn(List.of(trainer));

        assertThat(service.findById(1L)).containsSame(trainer);
        assertThat(service.findAll()).containsExactly(trainer);
    }

    private static Trainer trainer(Long id) {
        return new Trainer(id, "John", "Smith", "John.Smith", "abcdefghij",
                true, TrainingType.FITNESS);
    }
}
