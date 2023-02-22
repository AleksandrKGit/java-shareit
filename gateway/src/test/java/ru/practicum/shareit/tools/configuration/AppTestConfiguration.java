package ru.practicum.shareit.tools.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
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

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }
}