package ru.practicum.shareit.tools.support;

import ru.practicum.shareit.support.DefaultLocaleMessageSource;

import java.util.Locale;

public class MockedMessageSource extends DefaultLocaleMessageSource {
    @Override
    public String getMessageInternal(String code, Object[] args, Locale locale) {
        return super.getMessageInternal(code, args, locale);
    }
}