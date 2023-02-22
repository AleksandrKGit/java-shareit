package ru.practicum.shareit.tools.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;

@TestConfiguration
public class AppTestConfiguration {
    @Bean
    public DefaultLocaleMessageSource messageSource() {
        DefaultLocaleMessageSource messageSource = new DefaultLocaleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
