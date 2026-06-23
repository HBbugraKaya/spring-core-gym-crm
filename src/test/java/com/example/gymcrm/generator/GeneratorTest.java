package com.example.gymcrm.generator;

import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.TrainingType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneratorTest {
    @Test
    void usernameChecksBothStoragesCaseInsensitivelyAndReservesSuffixes() {
        Map<Long, Trainee> trainees = new ConcurrentHashMap<>();
        trainees.put(1L, new Trainee(1L, "John", "Smith", "john.smith", "abcdefghij",
                true, LocalDate.of(2000, 1, 1), "Address"));
        Map<Long, Trainer> trainers = new ConcurrentHashMap<>();
        trainers.put(1L, new Trainer(1L, "John", "Smith", "JOHN.SMITH1", "abcdefghij",
                true, TrainingType.FITNESS));

        UniqueUsernameGenerator generator = generator(trainees, trainers);

        assertThat(generator.generate(" John ", " Smith ")).isEqualTo("John.Smith2");
        assertThat(generator.generate("John", "Smith")).isEqualTo("John.Smith3");
        assertThat(generator.generate("Jane", "Doe")).isEqualTo("Jane.Doe");
    }

    @Test
    void usernameSuffixIsBasedOnExistingNamePair() {
        Map<Long, Trainee> trainees = new ConcurrentHashMap<>();
        trainees.put(1L, new Trainee(1L, "Jane", "Doe", "Old.Username", "abcdefghij",
                true, LocalDate.of(2000, 1, 1), "Address"));
        Map<Long, Trainer> trainers = new ConcurrentHashMap<>();
        trainers.put(2L, new Trainer(2L, "Different", "Person", "Jane.Doe", "abcdefghij",
                true, TrainingType.FITNESS));

        UniqueUsernameGenerator generator = generator(trainees, trainers);

        assertThat(generator.generate("Jane", "Doe")).isEqualTo("Jane.Doe1");
    }

    @Test
    void releasedUsernameCanBeGeneratedAgain() {
        UniqueUsernameGenerator generator = generator(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());

        String username = generator.generate("Failed", "Create");
        generator.release(username);

        assertThat(generator.generate("Failed", "Create")).isEqualTo("Failed.Create");
    }

    @Test
    void confirmedUsernameCanBeReusedAfterEntityIsRemovedFromStorage() {
        Map<Long, Trainee> trainees = new ConcurrentHashMap<>();
        UniqueUsernameGenerator generator = generator(trainees, new ConcurrentHashMap<>());

        String username = generator.generate("Deleted", "User");
        trainees.put(1L, new Trainee(1L, "Deleted", "User", username, "abcdefghij",
                true, LocalDate.of(2000, 1, 1), "Address"));
        generator.confirm(username);
        trainees.remove(1L);

        assertThat(generator.generate("Deleted", "User")).isEqualTo("Deleted.User");
    }

    @Test
    void concurrentGenerationNeverReturnsDuplicates() throws Exception {
        UniqueUsernameGenerator generator = generator(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
        var executor = Executors.newFixedThreadPool(8);
        try {
            Set<Callable<String>> tasks = new HashSet<>();
            for (int i = 0; i < 40; i++) {
                tasks.add(() -> generator.generate("Concurrent", "User"));
            }
            Set<String> usernames = new HashSet<>();
            for (var future : executor.invokeAll(tasks)) {
                usernames.add(future.get());
            }
            assertThat(usernames).hasSize(40);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void usernameGeneratorRequiresBothStorages() {
        UniqueUsernameGenerator generator = new UniqueUsernameGenerator();
        generator.setTraineeStorage(new ConcurrentHashMap<>());

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
