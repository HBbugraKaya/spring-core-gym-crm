package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TraineeDao;
import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.exception.EntityNotFoundException;
import com.example.gymcrm.generator.PasswordGenerator;
import com.example.gymcrm.generator.UsernameGenerator;
import com.example.gymcrm.service.TraineeService;
import com.example.gymcrm.service.command.CreateTraineeCommand;
import com.example.gymcrm.service.command.UpdateTraineeCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.gymcrm.service.ValidationSupport.requireNonNull;
import static com.example.gymcrm.service.ValidationSupport.requirePositive;
import static com.example.gymcrm.service.ValidationSupport.requireText;

@Service
public class TraineeServiceImpl implements TraineeService {
    private TraineeDao traineeDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
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
    public Trainee create(CreateTraineeCommand command) {
        requireNonNull(command, "command");
        String firstName = requireText(command.firstName(), "firstName");
        String lastName = requireText(command.lastName(), "lastName");
        LocalDate dateOfBirth = requireNonNull(command.dateOfBirth(), "dateOfBirth");
        String address = requireText(command.address(), "address");

        Trainee trainee = new Trainee(null, firstName, lastName,
                usernameGenerator.generate(firstName, lastName), passwordGenerator.generate(),
                command.active(), dateOfBirth, address);
        return traineeDao.create(trainee);
    }

    @Override
    public Trainee update(Long id, UpdateTraineeCommand command) {
        requirePositive(id, "id");
        requireNonNull(command, "command");
        String firstName = requireText(command.firstName(), "firstName");
        String lastName = requireText(command.lastName(), "lastName");
        LocalDate dateOfBirth = requireNonNull(command.dateOfBirth(), "dateOfBirth");
        String address = requireText(command.address(), "address");

        Trainee trainee = traineeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee", id));
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.setActive(command.active());
        return traineeDao.update(trainee);
    }

    @Override
    public void delete(Long id) {
        traineeDao.deleteById(requirePositive(id, "id"));
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return traineeDao.findById(requirePositive(id, "id"));
    }

    @Override
    public List<Trainee> findAll() {
        return traineeDao.findAll();
    }
}
