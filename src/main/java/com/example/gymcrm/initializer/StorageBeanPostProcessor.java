package com.example.gymcrm.initializer;

import com.example.gymcrm.config.StorageBeanNames;
import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.Training;
import com.example.gymcrm.exception.StorageInitializationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class StorageBeanPostProcessor implements BeanPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageBeanPostProcessor.class);

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private InitialData initialData;

    private String dataPath;

    @Value("${storage.initial-data.path}")
    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof Map<?, ?>)) {
            return bean;
        }

        switch (beanName) {
            case StorageBeanNames.TRAINEE_STORAGE -> populateTrainees(bean);
            case StorageBeanNames.TRAINER_STORAGE -> populateTrainers(bean);
            case StorageBeanNames.TRAINING_STORAGE -> populateTrainings(bean);
            default -> {
                return bean;
            }
        }
        return bean;
    }

    @SuppressWarnings("unchecked")
    private void populateTrainees(Object bean) {
        populate((Map<Long, Trainee>) bean, loadData().trainees(), Trainee::getId,
                trainee -> hasText(trainee.getFirstName()) && hasText(trainee.getLastName()), "Trainee");
    }

    @SuppressWarnings("unchecked")
    private void populateTrainers(Object bean) {
        populate((Map<Long, Trainer>) bean, loadData().trainers(), Trainer::getId,
                trainer -> hasText(trainer.getFirstName()) && hasText(trainer.getLastName())
                        && trainer.getSpecialization() != null, "Trainer");
    }

    @SuppressWarnings("unchecked")
    private void populateTrainings(Object bean) {
        populate((Map<Long, Training>) bean, loadData().trainings(), Training::getId,
                training -> hasText(training.getName()) && training.getTrainingType() != null
                        && training.getDate() != null && training.getDurationMinutes() > 0, "Training");
    }

    private <T> void populate(Map<Long, T> storage, List<T> entities,
                              Function<T, Long> idExtractor, Function<T, Boolean> validEntity,
                              String entityType) {
        for (T entity : entities) {
            Long id = idExtractor.apply(entity);
            if (id == null || id <= 0) {
                throw new StorageInitializationException(entityType + " id must be positive");
            }
            if (!validEntity.apply(entity)) {
                throw new StorageInitializationException(entityType + " initial data is invalid");
            }
            if (storage.putIfAbsent(id, entity) != null) {
                throw new StorageInitializationException("Duplicate " + entityType + " id=" + id);
            }
        }
        LOGGER.info("Initialized {} storage count={}", entityType, entities.size());
    }

    private InitialData loadData() {
        if (initialData != null) {
            return initialData;
        }

        Resource resource = dataPath.startsWith("classpath:")
                ? new ClassPathResource(dataPath.substring("classpath:".length()))
                : new FileSystemResource(dataPath);
        if (!resource.exists()) {
            throw new StorageInitializationException("Initial data file does not exist: " + dataPath);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            initialData = objectMapper.readValue(inputStream, InitialData.class);
            LOGGER.info("Loaded initial data file successfully");
            return initialData;
        } catch (IOException exception) {
            LOGGER.error("Failed to load initial data file");
            throw new StorageInitializationException("Cannot read initial data file", exception);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
