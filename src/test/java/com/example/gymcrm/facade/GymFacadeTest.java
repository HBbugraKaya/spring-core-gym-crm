package com.example.gymcrm.facade;

import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.Training;
import com.example.gymcrm.domain.TrainingType;
import com.example.gymcrm.service.TraineeService;
import com.example.gymcrm.service.TrainerService;
import com.example.gymcrm.service.TrainingService;
import com.example.gymcrm.service.command.CreateTraineeCommand;
import com.example.gymcrm.service.command.CreateTrainerCommand;
import com.example.gymcrm.service.command.CreateTrainingCommand;
import com.example.gymcrm.service.command.UpdateTraineeCommand;
import com.example.gymcrm.service.command.UpdateTrainerCommand;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GymFacadeTest {
    @Test
    void delegatesEveryPublicOperation() {
        TraineeService traineeService = mock(TraineeService.class);
        TrainerService trainerService = mock(TrainerService.class);
        TrainingService trainingService = mock(TrainingService.class);
        GymFacade facade = new GymFacade(traineeService, trainerService, trainingService);

        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        Training training = new Training();
        CreateTraineeCommand createTrainee = new CreateTraineeCommand("A", "B", LocalDate.now(), "C", true);
        UpdateTraineeCommand updateTrainee = new UpdateTraineeCommand("A", "B", LocalDate.now(), "C", true);
        CreateTrainerCommand createTrainer = new CreateTrainerCommand("A", "B", TrainingType.YOGA, true);
        UpdateTrainerCommand updateTrainer = new UpdateTrainerCommand("A", "B", TrainingType.YOGA, true);
        CreateTrainingCommand createTraining = new CreateTrainingCommand(
                1L, 1L, "T", TrainingType.YOGA, LocalDate.now(), 10);

        when(traineeService.create(createTrainee)).thenReturn(trainee);
        when(traineeService.update(1L, updateTrainee)).thenReturn(trainee);
        when(traineeService.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeService.findAll()).thenReturn(List.of(trainee));
        when(trainerService.create(createTrainer)).thenReturn(trainer);
        when(trainerService.update(1L, updateTrainer)).thenReturn(trainer);
        when(trainerService.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerService.findAll()).thenReturn(List.of(trainer));
        when(trainingService.create(createTraining)).thenReturn(training);
        when(trainingService.findById(1L)).thenReturn(Optional.of(training));
        when(trainingService.findAll()).thenReturn(List.of(training));

        assertThat(facade.createTrainee(createTrainee)).isSameAs(trainee);
        assertThat(facade.updateTrainee(1L, updateTrainee)).isSameAs(trainee);
        assertThat(facade.findTrainee(1L)).containsSame(trainee);
        assertThat(facade.findAllTrainees()).containsExactly(trainee);
        facade.deleteTrainee(1L);
        verify(traineeService).delete(1L);

        assertThat(facade.createTrainer(createTrainer)).isSameAs(trainer);
        assertThat(facade.updateTrainer(1L, updateTrainer)).isSameAs(trainer);
        assertThat(facade.findTrainer(1L)).containsSame(trainer);
        assertThat(facade.findAllTrainers()).containsExactly(trainer);

        assertThat(facade.createTraining(createTraining)).isSameAs(training);
        assertThat(facade.findTraining(1L)).containsSame(training);
        assertThat(facade.findAllTrainings()).containsExactly(training);
    }
}
