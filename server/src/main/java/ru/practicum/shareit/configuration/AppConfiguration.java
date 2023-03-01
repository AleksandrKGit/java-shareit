package ru.practicum.shareit.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;

@Configuration
@PropertySource(value = "classpath:application.properties")
public class AppConfiguration {
    @Bean
    public DefaultLocaleMessageSource messageSource() {
        DefaultLocaleMessageSource messageSource = new DefaultLocaleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}