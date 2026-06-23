package com.example.gymcrm.service;

import com.example.gymcrm.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationSupportTest {
    @Test
    void validatesAndNormalizesSupportedValues() {
        assertThat(ValidationSupport.requireText(" value ", "field")).isEqualTo("value");
        assertThat(ValidationSupport.requireNonNull("value", "field")).isEqualTo("value");
        assertThat(ValidationSupport.requirePositive(4L, "field")).isEqualTo(4L);
        assertThat(ValidationSupport.requirePositive(5, "field")).isEqualTo(5);
    }

    @Test
    void rejectsInvalidValues() {
        assertThatThrownBy(() -> ValidationSupport.requireText(" ", "name"))
                .isInstanceOf(ValidationException.class).hasMessageContaining("name");
        assertThatThrownBy(() -> ValidationSupport.requireNonNull(null, "value"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ValidationSupport.requirePositive(0L, "id"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ValidationSupport.requirePositive(-1, "duration"))
                .isInstanceOf(ValidationException.class);
    }
}
