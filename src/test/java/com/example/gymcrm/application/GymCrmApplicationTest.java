package com.example.gymcrm.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class GymCrmApplicationTest {
    @Test
    void applicationStartsAndStopsCleanly() {
        assertThatCode(() -> GymCrmApplication.main(new String[0])).doesNotThrowAnyException();
    }

    @Test
    void applicationRunsDemoScenario() {
        assertThatCode(() -> GymCrmApplication.main(new String[]{"--demo"})).doesNotThrowAnyException();
    }
}
