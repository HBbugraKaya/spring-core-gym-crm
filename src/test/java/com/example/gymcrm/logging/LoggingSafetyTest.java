package com.example.gymcrm.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.gymcrm.dao.impl.TraineeDaoImpl;
import com.example.gymcrm.domain.Trainee;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingSafetyTest {
    @Test
    void traineeDaoLogsDoNotExposeSensitiveProfileFields() {
        Logger logger = (Logger) LoggerFactory.getLogger(TraineeDaoImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            TraineeDaoImpl dao = new TraineeDaoImpl();
            dao.setStorage(new HashMap<>());
            Trainee trainee = new Trainee(null, "Alice", "Brown", "Alice.Brown", "secret1234",
                    true, LocalDate.of(1995, 4, 12), "Private address");

            dao.create(trainee);
            dao.findById(trainee.getId());
            dao.findAll();

            String messages = appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .collect(Collectors.joining("\n"));

            assertThat(messages)
                    .doesNotContain("secret1234")
                    .doesNotContain("Private address")
                    .doesNotContain("1995-04-12")
                    .contains("Created Trainee id=", "Finding Trainee id=", "Listed Trainee count=");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
