package com.example.gymcrm.config;

import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.Training;
import com.example.gymcrm.domain.TrainingType;
import com.example.gymcrm.facade.GymFacade;
import com.example.gymcrm.service.command.CreateTraineeCommand;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SpringContextIntegrationTest {
    @Test
    @SuppressWarnings("unchecked")
    void contextLoadsSeparateStoragesAndWiresFacade() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            Map<Long, Trainee> trainees = context.getBean("traineeStorage", Map.class);
            Map<Long, Trainer> trainers = context.getBean("trainerStorage", Map.class);
            Map<Long, Training> trainings = context.getBean("trainingStorage", Map.class);
            GymFacade facade = context.getBean(GymFacade.class);

            assertThat(trainees).hasSize(1);
            assertThat(trainers).hasSize(1);
            assertThat(trainings).hasSize(1);
            assertThat((Object) trainees).isNotSameAs(trainers).isNotSameAs(trainings);
            assertThat(facade.findAllTrainees()).hasSize(1);
            assertThat(facade.findAllTrainers()).hasSize(1);
            assertThat(facade.findAllTrainings()).hasSize(1);

            Trainee duplicateName = facade.createTrainee(new CreateTraineeCommand(
                    "John", "Smith", LocalDate.of(2001, 1, 1), "Address", true));
            assertThat(duplicateName.getId()).isEqualTo(2L);
            assertThat(duplicateName.getUsername()).isEqualTo("John.Smith1");
            assertThat(duplicateName.getPassword()).hasSize(10);
        }
    }

    @Test
    void storageConfigCreatesIndependentConcurrentMaps() {
        StorageConfig config = new StorageConfig();

        assertThat(config.traineeStorage()).isEmpty();
        assertThat(config.trainerStorage()).isEmpty();
        assertThat(config.trainingStorage()).isEmpty();
        assertThat(config.traineeStorage().getClass().getSimpleName()).isEqualTo("ConcurrentHashMap");
    }

    @Test
    void configurationProvidesPlaceholderConfigurer() {
        assertThat(AppConfig.propertySourcesPlaceholderConfigurer()).isNotNull();
        assertThat(TrainingType.values()).contains(TrainingType.FITNESS, TrainingType.STRETCHING);
    }
}
