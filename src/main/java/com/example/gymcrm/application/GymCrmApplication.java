package com.example.gymcrm.application;

import com.example.gymcrm.config.AppConfig;
import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.Training;
import com.example.gymcrm.domain.TrainingType;
import com.example.gymcrm.facade.GymFacade;
import com.example.gymcrm.service.command.CreateTraineeCommand;
import com.example.gymcrm.service.command.CreateTrainerCommand;
import com.example.gymcrm.service.command.CreateTrainingCommand;
import com.example.gymcrm.service.command.UpdateTraineeCommand;
import com.example.gymcrm.service.command.UpdateTrainerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.Arrays;

public final class GymCrmApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(GymCrmApplication.class);

    private GymCrmApplication() {
    }

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);
            LOGGER.info("Gym CRM started: trainees={}, trainers={}, trainings={}",
                    facade.findAllTrainees().size(),
                    facade.findAllTrainers().size(),
                    facade.findAllTrainings().size());

            if (Arrays.asList(args).contains("--demo")) {
                runDemo(facade);
            }
        }
    }

    private static void runDemo(GymFacade facade) {
        LOGGER.info("Demo scenario started");

        Trainee trainee = facade.createTrainee(new CreateTraineeCommand(
                "John", "Smith", LocalDate.of(2001, 1, 1), "Demo address", true));
        LOGGER.info("Demo created trainee id={}", trainee.getId());

        Trainee updatedTrainee = facade.updateTrainee(trainee.getId(), new UpdateTraineeCommand(
                "Jane", "Smith", LocalDate.of(2001, 1, 1), "Updated demo address", false));
        LOGGER.info("Demo updated trainee id={} active={}", updatedTrainee.getId(), updatedTrainee.isActive());

        facade.deleteTrainee(updatedTrainee.getId());
        LOGGER.info("Demo deleted trainee id={}", updatedTrainee.getId());

        Trainer trainer = facade.createTrainer(new CreateTrainerCommand(
                "Demo", "Trainer", TrainingType.YOGA, true));
        LOGGER.info("Demo created trainer id={}", trainer.getId());

        Trainer updatedTrainer = facade.updateTrainer(trainer.getId(), new UpdateTrainerCommand(
                "Demo", "Trainer", TrainingType.CARDIO, true));
        LOGGER.info("Demo updated trainer id={}", updatedTrainer.getId());

        Training training = facade.createTraining(new CreateTrainingCommand(
                1L, 1L, "Demo training", TrainingType.FITNESS, LocalDate.now(), 45));
        LOGGER.info("Demo created training id={}", training.getId());

        LOGGER.info("Demo final counts: trainees={}, trainers={}, trainings={}",
                facade.findAllTrainees().size(),
                facade.findAllTrainers().size(),
                facade.findAllTrainings().size());
        LOGGER.info("Demo scenario completed");
    }
}
