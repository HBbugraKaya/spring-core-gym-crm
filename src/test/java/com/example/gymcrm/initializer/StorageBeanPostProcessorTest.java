package com.example.gymcrm.initializer;

import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.Training;
import com.example.gymcrm.exception.StorageInitializationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageBeanPostProcessorTest {
    @TempDir
    Path tempDirectory;

    @Test
    void populatesAllKnownStorageBeansFromOneFile() throws IOException {
        Path dataFile = write("valid.json", validJson());
        StorageBeanPostProcessor processor = processor(dataFile);
        Map<Long, Trainee> trainees = new HashMap<>();
        Map<Long, Trainer> trainers = new HashMap<>();
        Map<Long, Training> trainings = new HashMap<>();

        assertThat(processor.postProcessAfterInitialization(trainees, "traineeStorage")).isSameAs(trainees);
        processor.postProcessAfterInitialization(trainers, "trainerStorage");
        processor.postProcessAfterInitialization(trainings, "trainingStorage");

        assertThat(trainees).containsKey(1L);
        assertThat(trainers).containsKey(2L);
        assertThat(trainings).containsKey(3L);
        Object arbitrary = new Object();
        assertThat(processor.postProcessAfterInitialization(arbitrary, "anything")).isSameAs(arbitrary);
        Map<Long, String> unknownMap = new HashMap<>();
        assertThat(processor.postProcessAfterInitialization(unknownMap, "otherStorage")).isSameAs(unknownMap);
    }

    @Test
    void failsFastForMissingOrMalformedFile() throws IOException {
        StorageBeanPostProcessor missing = processor(tempDirectory.resolve("missing.json"));
        assertThatThrownBy(() -> missing.postProcessAfterInitialization(
                new HashMap<>(), "traineeStorage"))
                .isInstanceOf(StorageInitializationException.class)
                .hasMessageContaining("does not exist");

        StorageBeanPostProcessor malformed = processor(write("malformed.json", "{not-json}"));
        assertThatThrownBy(() -> malformed.postProcessAfterInitialization(
                new HashMap<>(), "traineeStorage"))
                .isInstanceOf(StorageInitializationException.class)
                .hasMessageContaining("Cannot read");
    }

    @Test
    void rejectsDuplicateAndNonPositiveIds() throws IOException {
        String duplicateJson = """
                {"trainees":[
                  {"id":1,"firstName":"A","lastName":"B","username":"A.B","password":"abcdefghij","active":true,"dateOfBirth":"2000-01-01","address":"Address"},
                  {"id":1,"firstName":"C","lastName":"D","username":"C.D","password":"abcdefghij","active":true,"dateOfBirth":"2000-01-01","address":"Address"}
                ]}
                """;
        StorageBeanPostProcessor duplicate = processor(write("duplicate.json", duplicateJson));
        assertThatThrownBy(() -> duplicate.postProcessAfterInitialization(
                new HashMap<>(), "traineeStorage"))
                .isInstanceOf(StorageInitializationException.class)
                .hasMessageContaining("Duplicate");

        String invalidIdJson = """
                {"trainers":[
                  {"id":0,"firstName":"A","lastName":"B","username":"A.B","password":"abcdefghij","active":true,"specialization":"YOGA"}
                ]}
                """;
        StorageBeanPostProcessor invalid = processor(write("invalid-id.json", invalidIdJson));
        assertThatThrownBy(() -> invalid.postProcessAfterInitialization(
                new HashMap<>(), "trainerStorage"))
                .isInstanceOf(StorageInitializationException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void rejectsMissingRequiredUserFields() throws IOException {
        String missingFieldJson = """
                {"trainees":[
                  {"id":1,"lastName":"B","username":"A.B","password":"abcdefghij","active":true,"dateOfBirth":"2000-01-01","address":"Address"}
                ]}
                """;
        StorageBeanPostProcessor processor = processor(write("missing-field.json", missingFieldJson));

        assertThatThrownBy(() -> processor.postProcessAfterInitialization(
                new HashMap<>(), "traineeStorage"))
                .isInstanceOf(StorageInitializationException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void initialDataTreatsMissingSectionsAsEmptyLists() {
        InitialData data = new InitialData(null, null, null);

        assertThat(data.trainees()).isEmpty();
        assertThat(data.trainers()).isEmpty();
        assertThat(data.trainings()).isEmpty();
    }

    private StorageBeanPostProcessor processor(Path path) {
        StorageBeanPostProcessor processor = new StorageBeanPostProcessor();
        processor.setDataPath(path.toString());
        return processor;
    }

    private Path write(String fileName, String content) throws IOException {
        return Files.writeString(tempDirectory.resolve(fileName), content);
    }

    private static String validJson() {
        return """
                {
                  "trainees":[{"id":1,"firstName":"A","lastName":"B","username":"A.B","password":"abcdefghij","active":true,"dateOfBirth":"2000-01-01","address":"Address"}],
                  "trainers":[{"id":2,"firstName":"C","lastName":"D","username":"C.D","password":"abcdefghij","active":true,"specialization":"YOGA"}],
                  "trainings":[{"id":3,"traineeId":1,"trainerId":2,"name":"Yoga","trainingType":"YOGA","date":"2026-06-22","durationMinutes":30}]
                }
                """;
    }
}
