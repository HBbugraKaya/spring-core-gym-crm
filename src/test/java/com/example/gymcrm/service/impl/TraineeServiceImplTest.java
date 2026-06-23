package com.example.gymcrm.service.impl;

import com.example.gymcrm.dao.TraineeDao;
import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.exception.EntityNotFoundException;
import com.example.gymcrm.exception.ValidationException;
import com.example.gymcrm.generator.PasswordGenerator;
import com.example.gymcrm.generator.UsernameGenerator;
import com.example.gymcrm.service.command.CreateTraineeCommand;
import com.example.gymcrm.service.command.UpdateTraineeCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;

    private TraineeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TraineeServiceImpl();
        service.setTraineeDao(traineeDao);
        service.setUsernameGenerator(usernameGenerator);
        service.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void createsTraineeWithGeneratedCredentialsAndNormalizedFields() {
        when(usernameGenerator.generate("Alice", "Brown")).thenReturn("Alice.Brown");
        when(passwordGenerator.generate()).thenReturn("A1b2C3d4E5");
        when(traineeDao.create(any())).thenAnswer(invocation -> {
            Trainee trainee = invocation.getArgument(0);
            trainee.setId(1L);
            return trainee;
        });

        Trainee result = service.create(new CreateTraineeCommand(
                " Alice ", " Brown ", LocalDate.of(1995, 4, 12), " Address ", true));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Alice");
        assertThat(result.getLastName()).isEqualTo("Brown");
        assertThat(result.getAddress()).isEqualTo("Address");
        assertThat(result.getUsername()).isEqualTo("Alice.Brown");
        assertThat(result.getPassword()).isEqualTo("A1b2C3d4E5");
    }

    @Test
    void updatesProfileButPreservesCredentials() {
        Trainee current = trainee(2L);
        when(traineeDao.findById(2L)).thenReturn(Optional.of(current));
        when(traineeDao.update(current)).thenReturn(current);

        Trainee result = service.update(2L, new UpdateTraineeCommand(
                "Updated", "Name", LocalDate.of(1999, 2, 3), "New address", false));

        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("Name");
        assertThat(result.getUsername()).isEqualTo("Original.User");
        assertThat(result.getPassword()).isEqualTo("abcdefghij");
        assertThat(result.isActive()).isFalse();
    }

    @Test
    void rejectsMissingOrInvalidUpdates() {
        when(traineeDao.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(9L, new UpdateTraineeCommand(
                "Missing", "User", LocalDate.now(), "Address", true)))
                .isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> service.create(null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.create(new CreateTraineeCommand(
                " ", "User", LocalDate.now(), "Address", true)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void delegatesDeleteFindAndList() {
        Trainee trainee = trainee(1L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));

        assertThat(service.findById(1L)).containsSame(trainee);
        assertThat(service.findAll()).containsExactly(trainee);
        service.delete(1L);

        verify(traineeDao).deleteById(1L);
        assertThatThrownBy(() -> service.findById(0L)).isInstanceOf(ValidationException.class);
    }

    private static Trainee trainee(Long id) {
        return new Trainee(id, "Original", "User", "Original.User", "abcdefghij",
                true, LocalDate.of(1990, 1, 1), "Address");
    }
}
