package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.practicum.shareit.controller.ControllerErrorHandler;
import ru.practicum.shareit.request.dto.ItemRequestDtoFromClient;
import ru.practicum.shareit.request.dto.ItemRequestDtoToClient;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.tools.configuration.AppTestConfiguration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.*;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.createItemDtoToClient;
import static ru.practicum.shareit.tools.matchers.ItemRequestDtoFromClientMatcher.equalToDto;

@WebMvcTest(ItemRequestController.class)
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemRequestService service;

    @SpyBean
    ControllerErrorHandler controllerErrorHandler;

    final Map<String, String> mockedErrors = Map.of("error", "mocked");

    static final long validUserId = 1L;

    static final long validId = 2L;

    static final String validItemRequestJson = "{\"description\":\"text\"}";

    final ItemRequestDtoFromClient validDto = createItemRequestDtoFromClient("description");

    final ItemRequestDtoToClient resultDto =
            createItemRequestDtoToClient(1L, "desc", LocalDateTime.now().minusDays(10), List.of(
                    createItemDtoToClient(2L, "itemName", "itemDesc", true, 1L)
            ));

    final List<ItemRequestDtoToClient> resultListDto = List.of(resultDto);

    private static Stream<Arguments> incorrectRequest() {
        String incorrectFrom = "a";
        String incorrectSize = "a";
        String incorrectUserId = "a";
        String incorrectId = "a";
        String incorrectJson = "}";

        return Stream.of(
                Arguments.of("create without media type",
                        post("/requests")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validItemRequestJson)),

                Arguments.of("create with incorrect media type",
                        post("/requests")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validItemRequestJson)
                                .contentType(MediaType.IMAGE_PNG)),

                Arguments.of("create without X-Sharer-User-Id header",
                        post("/requests")
                                .content(validItemRequestJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("readByUser without X-Sharer-User-Id header",
                        get("/requests")),

                Arguments.of("readAll without X-Sharer-User-Id header",
                        get("/requests/all")),

                Arguments.of("readById without X-Sharer-User-Id header",
                        get("/requests/" + validId)),

                Arguments.of("create with incorrect userId",
                        post("/requests")
                                .header("X-Sharer-User-Id", incorrectUserId)
                                .content(validItemRequestJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("create with incorrect json",
                        post("/requests")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(incorrectJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("readByUser with incorrect userId",
                        get("/requests")
                                .header("X-Sharer-User-Id", incorrectUserId)),

                Arguments.of("readAll with incorrect userId",
                        get("/requests/all")
                                .header("X-Sharer-User-Id", incorrectUserId)),

                Arguments.of("readAll with incorrect size",
                        get("/requests/all?size=" + incorrectSize)
                                .header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readAll with incorrect from",
                        get("/requests/all?from=" + incorrectFrom)
                                .header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readById with incorrect user id",
                        get("/requests/" + validId)
                                .header("X-Sharer-User-Id", incorrectUserId)),

                Arguments.of("readById with incorrect id",
                        get("/requests/" + incorrectId)
                                .header("X-Sharer-User-Id", validUserId))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("incorrectRequest")
    void request_withIncorrectRequest_shouldReturnStatusInternalServerErrorAndJsonWithErrors(
            String testName, MockHttpServletRequestBuilder request) throws Exception {
        doReturn(mockedErrors).when(controllerErrorHandler).handleException(any());

        mockMvc.perform(request)
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void request_withCreateEndPoint_shouldReturnStatusOkAndServiceCreateMethodResult()
            throws Exception {
        doReturn(resultDto).when(service).create(eq(validUserId), argThat(equalToDto(validDto)));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", validUserId)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(resultDto)))
                .andExpect(status().isOk());
    }

    @Test
    void request_withReadByUserEndPoint_shouldReturnStatusOkAndServiceReadByUserMethodResult()
            throws Exception {
        doReturn(resultListDto).when(service).readByUser(validUserId);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", validUserId))
                .andExpect(content().json(objectMapper.writeValueAsString(resultListDto)))
                .andExpect(status().isOk());
    }

    @Test
    void request_withReadAllEndPoint_shouldReturnStatusOkAndServiceReadAllMethodResult()
            throws Exception {
        Integer validFrom = 2;
        Integer validSize = 10;
        doReturn(resultListDto).when(service).readAll(validUserId, validFrom, validSize);

        mockMvc.perform(get("/requests/all?from=" + validFrom + "&size=" + validSize)
                        .header("X-Sharer-User-Id", validUserId))
                .andExpect(content().json(objectMapper.writeValueAsString(resultListDto)))
                .andExpect(status().isOk());
    }

    @Test
    void request_withReadByIdEndPoint_shouldReturnStatusOkAndServiceReadByIdMethodResult()
            throws Exception {
        doReturn(resultDto).when(service).readById(validUserId, validId);

        mockMvc.perform(get("/requests/" + validId)
                        .header("X-Sharer-User-Id", validUserId))
                .andExpect(content().json(objectMapper.writeValueAsString(resultDto)))
                .andExpect(status().isOk());
    }
}