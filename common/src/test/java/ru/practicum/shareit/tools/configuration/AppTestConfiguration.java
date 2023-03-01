package ru.practicum.shareit.tools.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.practicum.shareit.tools.support.MockedMessageSource;

@TestConfiguration
public class AppTestConfiguration {
    @Bean
    public MockedMessageSource messageSource() {
        MockedMessageSource messageSource = new MockedMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
