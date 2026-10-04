package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TraineeDao;
import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.dao.TrainingDao;
import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.Training;
import com.example.gymcrm.domain.TrainingType;
import com.example.gymcrm.exception.EntityNotFoundException;
import com.example.gymcrm.exception.ValidationException;
import com.example.gymcrm.service.command.CreateTrainingCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    @Mock
    private TrainingDao trainingDao;
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;

    private TrainingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TrainingServiceImpl();
        service.setTrainingDao(trainingDao);
        service.setTraineeDao(traineeDao);
        service.setTrainerDao(trainerDao);
    }

    @Test
    void createsTrainingWhenReferencesExist() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(2L)).thenReturn(Optional.of(new Trainer()));
        when(trainingDao.create(any())).thenAnswer(invocation -> invocation.getArgument(0, Training.class));
        CreateTrainingCommand command = command();
        ArgumentCaptor<Training> trainingCaptor = ArgumentCaptor.forClass(Training.class);

        Training result = service.create(command);

        verify(traineeDao).findById(1L);
        verify(trainerDao).findById(2L);
        verify(trainingDao).create(trainingCaptor.capture());
        assertThat(result).isSameAs(trainingCaptor.getValue());
        assertThat(result.getTraineeId()).isEqualTo(1L);
        assertThat(result.getTrainerId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Session");
        assertThat(result.getTrainingType()).isEqualTo(TrainingType.FITNESS);
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 6, 22));
        assertThat(result.getDurationMinutes()).isEqualTo(45);
    }

    @Test
    void rejectsMissingTraineeBeforeCheckingTrainerOrCreatingTraining() {
        when(traineeDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(command()))
                .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("Trainee");

        verify(traineeDao).findById(1L);
        verify(trainerDao, never()).findById(any());
        verifyNoInteractions(trainingDao);
    }

    @Test
    void rejectsMissingTrainerBeforeCreatingTraining() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(command()))
                .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("Trainer");

        verify(traineeDao).findById(1L);
        verify(trainerDao).findById(2L);
        verifyNoInteractions(trainingDao);
    }

    @Test
    void rejectsInvalidTrainingBeforeCallingCollaborators() {
        assertThatThrownBy(() -> service.create(new CreateTrainingCommand(
                1L, 2L, "Session", TrainingType.FITNESS, LocalDate.now(), 0)))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.create(null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.create(new CreateTrainingCommand(
                1L, 2L, " ", TrainingType.FITNESS, LocalDate.now(), 45)))
                .isInstanceOf(ValidationException.class);

        verifyNoInteractions(traineeDao, trainerDao, trainingDao);
    }

    @Test
    void delegatesQueries() {
        Training training = new Training();
        when(trainingDao.findById(1L)).thenReturn(Optional.of(training));
        when(trainingDao.findAll()).thenReturn(List.of(training));

        assertThat(service.findById(1L)).containsSame(training);
        assertThat(service.findAll()).containsExactly(training);
        assertThatThrownBy(() -> service.findById(0L)).isInstanceOf(ValidationException.class);

        verify(trainingDao).findById(1L);
        verify(trainingDao).findAll();
    }

    private static CreateTrainingCommand command() {
        return new CreateTrainingCommand(1L, 2L, " Session ", TrainingType.FITNESS,
                LocalDate.of(2026, 6, 22), 45);
    }
}
