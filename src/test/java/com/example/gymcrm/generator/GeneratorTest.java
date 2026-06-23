package com.example.gymcrm.generator;

import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.TrainingType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneratorTest {
    @Test
    void usernameChecksBothStoragesCaseInsensitivelyAndAddsSuffixes() {
        Map<Long, Trainee> trainees = new HashMap<>();
        trainees.put(1L, new Trainee(1L, "John", "Smith", "john.smith", "abcdefghij",
                true, LocalDate.of(2000, 1, 1), "Address"));
        Map<Long, Trainer> trainers = new HashMap<>();
        trainers.put(1L, new Trainer(1L, "John", "Smith", "JOHN.SMITH1", "abcdefghij",
                true, TrainingType.FITNESS));

        UniqueUsernameGenerator generator = generator(trainees, trainers);

        assertThat(generator.generate(" John ", " Smith ")).isEqualTo("John.Smith2");
        assertThat(generator.generate("Jane", "Doe")).isEqualTo("Jane.Doe");
    }

    @Test
    void usernameSuffixIsBasedOnExistingNamePair() {
        Map<Long, Trainee> trainees = new HashMap<>();
        trainees.put(1L, new Trainee(1L, "Jane", "Doe", "Old.Username", "abcdefghij",
                true, LocalDate.of(2000, 1, 1), "Address"));
        Map<Long, Trainer> trainers = new HashMap<>();
        trainers.put(2L, new Trainer(2L, "Different", "Person", "Jane.Doe", "abcdefghij",
                true, TrainingType.FITNESS));

        UniqueUsernameGenerator generator = generator(trainees, trainers);

        assertThat(generator.generate("Jane", "Doe")).isEqualTo("Jane.Doe1");
    }

    @Test
    void usernameCanBeReusedAfterEntityIsRemovedFromStorage() {
        Map<Long, Trainee> trainees = new HashMap<>();
        UniqueUsernameGenerator generator = generator(trainees, new HashMap<>());

        String username = generator.generate("Deleted", "User");
        trainees.put(1L, new Trainee(1L, "Deleted", "User", username, "abcdefghij",
                true, LocalDate.of(2000, 1, 1), "Address"));
        trainees.remove(1L);

        assertThat(generator.generate("Deleted", "User")).isEqualTo("Deleted.User");
    }

    @Test
    void usernameGeneratorRequiresBothStorages() {
        UniqueUsernameGenerator generator = new UniqueUsernameGenerator();
        generator.setTraineeStorage(new HashMap<>());

        assertThatThrownBy(() -> generator.generate("John", "Smith"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void securePasswordHasExactLengthAndAllowedCharacters() {
        SecurePasswordGenerator generator = new SecurePasswordGenerator();

        Set<String> passwords = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            String password = generator.generate();
            assertThat(password).hasSize(10).matches("[A-Za-z0-9]{10}");
            passwords.add(password);
        }
        assertThat(passwords).hasSizeGreaterThan(1);
    }

    private static UniqueUsernameGenerator generator(Map<Long, Trainee> trainees,
                                                      Map<Long, Trainer> trainers) {
        UniqueUsernameGenerator generator = new UniqueUsernameGenerator();
        generator.setTraineeStorage(trainees);
        generator.setTrainerStorage(trainers);
        return generator;
    }
}
