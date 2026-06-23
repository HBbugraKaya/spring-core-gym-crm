package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TraineeDao;
import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.dao.TrainingDao;
import com.example.gymcrm.domain.Training;
import com.example.gymcrm.domain.TrainingType;
import com.example.gymcrm.exception.EntityNotFoundException;
import com.example.gymcrm.service.TrainingService;
import com.example.gymcrm.service.command.CreateTrainingCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.gymcrm.service.ValidationSupport.requireNonNull;
import static com.example.gymcrm.service.ValidationSupport.requirePositive;
import static com.example.gymcrm.service.ValidationSupport.requireText;

@Service
public class TrainingServiceImpl implements TrainingService {
    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Override
    public Training create(CreateTrainingCommand command) {
        requireNonNull(command, "command");
        long traineeId = requirePositive(command.traineeId(), "traineeId");
        long trainerId = requirePositive(command.trainerId(), "trainerId");
        String name = requireText(command.name(), "name");
        TrainingType trainingType = requireNonNull(command.trainingType(), "trainingType");
        LocalDate date = requireNonNull(command.date(), "date");
        int durationMinutes = requirePositive(command.durationMinutes(), "durationMinutes");

        traineeDao.findById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee", traineeId));
        trainerDao.findById(trainerId)
                .orElseThrow(() -> new EntityNotFoundException("Trainer", trainerId));

        return trainingDao.create(new Training(null, traineeId, trainerId, name,
                trainingType, date, durationMinutes));
    }

    @Override
    public Optional<Training> findById(Long id) {
        return trainingDao.findById(requirePositive(id, "id"));
    }

    @Override
    public List<Training> findAll() {
        return trainingDao.findAll();
    }
}
