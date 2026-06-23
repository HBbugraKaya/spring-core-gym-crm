package com.example.gymcrm.generator;

import com.example.gymcrm.config.StorageBeanNames;
import com.example.gymcrm.domain.Trainee;
import com.example.gymcrm.domain.Trainer;
import com.example.gymcrm.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Component
public class UniqueUsernameGenerator implements UsernameGenerator {
    private final Set<String> inFlightUsernames = new HashSet<>();
    private final Map<String, NamePair> inFlightPairsByUsername = new HashMap<>();
    private final Map<NamePair, Integer> inFlightPairCounts = new HashMap<>();
    private Map<Long, Trainee> traineeStorage;
    private Map<Long, Trainer> trainerStorage;

    @Autowired
    public void setTraineeStorage(@Qualifier(StorageBeanNames.TRAINEE_STORAGE) Map<Long, Trainee> traineeStorage) {
        this.traineeStorage = traineeStorage;
    }

    @Autowired
    public void setTrainerStorage(@Qualifier(StorageBeanNames.TRAINER_STORAGE) Map<Long, Trainer> trainerStorage) {
        this.trainerStorage = trainerStorage;
    }

    @Override
    public synchronized String generate(String firstName, String lastName) {
        requireStorage();
        String trimmedFirstName = firstName.trim();
        String trimmedLastName = lastName.trim();
        String base = trimmedFirstName + "." + trimmedLastName;
        NamePair namePair = NamePair.of(trimmedFirstName, trimmedLastName);
        Set<String> unavailable = unavailableUsernames();

        long suffix = existingNamePairCount(namePair) + inFlightPairCounts.getOrDefault(namePair, 0);
        String candidate = suffix == 0 ? base : base + suffix;
        while (unavailable.contains(normalize(candidate))) {
            suffix++;
            candidate = base + suffix;
        }
        reserve(candidate, namePair);
        return candidate;
    }

    @Override
    public synchronized void confirm(String username) {
        clearInFlight(username);
    }

    @Override
    public synchronized void release(String username) {
        clearInFlight(username);
    }

    private Set<String> unavailableUsernames() {
        Set<String> unavailable = new HashSet<>(inFlightUsernames);
        users().map(User::getUsername).map(this::normalize).forEach(unavailable::add);
        return unavailable;
    }

    private long existingNamePairCount(NamePair namePair) {
        return users().filter(user -> namePair.equals(NamePair.of(user.getFirstName(), user.getLastName()))).count();
    }

    private Stream<? extends User> users() {
        return Stream.concat(traineeStorage.values().stream(), trainerStorage.values().stream());
    }

    private void reserve(String username, NamePair namePair) {
        String normalizedUsername = normalize(username);
        inFlightUsernames.add(normalizedUsername);
        inFlightPairsByUsername.put(normalizedUsername, namePair);
        inFlightPairCounts.merge(namePair, 1, Integer::sum);
    }

    private void clearInFlight(String username) {
        String normalizedUsername = normalize(username);
        if (!inFlightUsernames.remove(normalizedUsername)) {
            return;
        }
        NamePair namePair = inFlightPairsByUsername.remove(normalizedUsername);
        if (namePair != null) {
            inFlightPairCounts.computeIfPresent(namePair, (ignored, count) -> count == 1 ? null : count - 1);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private void requireStorage() {
        if (traineeStorage == null || trainerStorage == null) {
            throw new IllegalStateException("User storages have not been injected");
        }
    }

    private record NamePair(String firstName, String lastName) {
        private static NamePair of(String firstName, String lastName) {
            return new NamePair(normalizeName(firstName), normalizeName(lastName));
        }

        private static String normalizeName(String value) {
            return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        }
    }
}
