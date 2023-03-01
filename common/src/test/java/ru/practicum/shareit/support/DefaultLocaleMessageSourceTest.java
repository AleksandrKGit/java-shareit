package ru.practicum.shareit.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.shareit.tools.configuration.AppTestConfiguration;
import ru.practicum.shareit.tools.support.MockedMessageSource;
import java.util.Locale;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringJUnitConfig({AppTestConfiguration.class})
@ExtendWith(MockitoExtension.class)
class DefaultLocaleMessageSourceTest {
    @SpyBean
    private MockedMessageSource messageSource;

    @Test
    void get_shouldReturnGetMessageInternalResultInvokedWithMessageCodeNullArgsAndDefaultLocale() {
        String messageCode = "messageCode";
        String source = "result";
        doReturn(source).when(messageSource).getMessageInternal(messageCode, null, Locale.getDefault());

        String target = messageSource.get(messageCode);

        assertThat(target, equalTo(source));
        verify(messageSource, times(1)).getMessageInternal(any(), any(), any());
    }
}