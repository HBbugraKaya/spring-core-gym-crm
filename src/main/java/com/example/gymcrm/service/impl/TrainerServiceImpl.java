package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.TrainingType;
import com.example.gymcrm.exception.EntityNotFoundException;
import com.example.gymcrm.generator.PasswordGenerator;
import com.example.gymcrm.generator.UsernameGenerator;
import com.example.gymcrm.service.TrainerService;
import com.example.gymcrm.service.command.CreateTrainerCommand;
import com.example.gymcrm.service.command.UpdateTrainerCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.example.gymcrm.service.ValidationSupport.requireNonNull;
import static com.example.gymcrm.service.ValidationSupport.requirePositive;
import static com.example.gymcrm.service.ValidationSupport.requireText;

@Service
public class TrainerServiceImpl implements TrainerService {
    private TrainerDao trainerDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public Trainer create(CreateTrainerCommand command) {
        requireNonNull(command, "command");
        String firstName = requireText(command.firstName(), "firstName");
        String lastName = requireText(command.lastName(), "lastName");
        TrainingType specialization = requireNonNull(command.specialization(), "specialization");

        Trainer trainer = new Trainer(null, firstName, lastName,
                usernameGenerator.generate(firstName, lastName), passwordGenerator.generate(),
                command.active(), specialization);
        return trainerDao.create(trainer);
    }

    @Override
    public Trainer update(Long id, UpdateTrainerCommand command) {
        requirePositive(id, "id");
        requireNonNull(command, "command");
        String firstName = requireText(command.firstName(), "firstName");
        String lastName = requireText(command.lastName(), "lastName");
        TrainingType specialization = requireNonNull(command.specialization(), "specialization");

        Trainer trainer = trainerDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer", id));
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization(specialization);
        trainer.setActive(command.active());
        return trainerDao.update(trainer);
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return trainerDao.findById(requirePositive(id, "id"));
    }

    @Override
    public List<Trainer> findAll() {
        return trainerDao.findAll();
    }
}
