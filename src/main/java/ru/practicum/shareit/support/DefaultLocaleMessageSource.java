package ru.practicum.shareit.support;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import java.util.Locale;

public class DefaultLocaleMessageSource extends ReloadableResourceBundleMessageSource {
    public String get(String code) {
        return this.getMessage(code, null, Locale.getDefault());
    }
}
