package ru.practicum.shareit.support;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import java.util.Locale;

public class DefaultLocaleMessageSource extends ReloadableResourceBundleMessageSource {
    public String get(String code) {
        return getMessageInternal(code, null, Locale.getDefault());
    }
}