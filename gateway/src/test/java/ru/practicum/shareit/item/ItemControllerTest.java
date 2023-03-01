package ru.practicum.shareit.item;

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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.practicum.shareit.controller.ControllerErrorHandler;
import ru.practicum.shareit.item.dto.CommentDtoFromClient;
import ru.practicum.shareit.item.dto.ItemDtoFromClient;
import ru.practicum.shareit.tools.configuration.AppTestConfiguration;
import ru.practicum.shareit.tools.matchers.CommentDtoFromClientMatcher;
import ru.practicum.shareit.validation.groups.OnCreate;
import ru.practicum.shareit.validation.groups.OnUpdate;
import java.util.Map;
import java.util.stream.Stream;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.tools.factories.DtoFactory.createCommentDtoFromClient;
import static ru.practicum.shareit.tools.factories.DtoFactory.createItemDtoFromClient;
import static ru.practicum.shareit.tools.matchers.ItemDtoFromClientMatcher.equalToDto;

@WebMvcTest(ItemController.class)
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemClient client;

    @SpyBean
    ControllerErrorHandler controllerErrorHandler;

    @SpyBean
    LocalValidatorFactoryBean validator;

    final Map<String, String> mockedErrors = Map.of("error", "mocked");

    static final long validUserId = 1L;

    static final long validId = 3L;

    static final String validItemJson = "{\"name\":\"name\",\"description\":\"desc\",\"available\":true}";

    static final String validCommentJson = "{\"text\":\"some text\"}";

    final ItemDtoFromClient validDto = createItemDtoFromClient("item", "desc", true,
            4L);

    final ResponseEntity<Object> result = ResponseEntity.ok("OK");

    final Integer validFrom = 2;

    final Integer validSize = 10;

    private static Stream<Arguments> httpMediaTypeNotSupportedRequests() {
        return Stream.of(
                Arguments.of("create without media type",
                        post("/items")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validItemJson)),

                Arguments.of("create with incorrect media type",
                        post("/items")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validItemJson)
                                .contentType(MediaType.IMAGE_PNG)),

                Arguments.of("createComment without media type",
                        post("/items/" + validId + "/comment")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validCommentJson)),

                Arguments.of("createComment with incorrect media type",
                        post("/items/" + validId + "/comment")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validCommentJson)
                                .contentType(MediaType.IMAGE_PNG)),

                Arguments.of("update without media type",
                        patch("/items/" + validId)
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validItemJson)),

                Arguments.of("update with incorrect media type",
                        patch("/items/" + validId)
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validItemJson)
                                .contentType(MediaType.IMAGE_PNG))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("httpMediaTypeNotSupportedRequests")
    void request_withNotSupportedMediaType_shouldReturnStatusUnsupportedMediaTypeAndJsonWithErrors(
            String testName, MockHttpServletRequestBuilder request) throws Exception {
        doReturn(mockedErrors).when(controllerErrorHandler).handleHttpMediaTypeNotSupportedException(any());

        mockMvc.perform(request)
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isUnsupportedMediaType());
    }

    private static Stream<Arguments> missingXSharerUserIdHeaderRequests() {
        return Stream.of(
                Arguments.of("create",
                        post("/items")
                                .content(validItemJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("createComment",
                        post("/items/" + validId + "/comment")
                                .content(validCommentJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("readByOwner",
                        get("/items")),

                Arguments.of("readByQuery",
                        get("/items/search")),

                Arguments.of("readById",
                        get("/items/" + validId)),

                Arguments.of("update",
                        patch("/items/" + validId)
                                .content(validItemJson)
                                .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("missingXSharerUserIdHeaderRequests")
    void request_withMissingXSharerUserIdHeader_shouldReturnStatusBadRequestAndJsonWithErrors(
            String testName, MockHttpServletRequestBuilder request) throws Exception {
        doReturn(mockedErrors).when(controllerErrorHandler).handleMissingRequestHeaderException(any());

        mockMvc.perform(request)
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isBadRequest());
    }


    private static Stream<Arguments> httpMessageNotReadableRequests() {
        String incorrectId = "a";
        String incorrectUserId = "a";
        String incorrectJson = "}";

        return Stream.of(
                Arguments.of("create with incorrect userId",
                        post("/items")
                                .header("X-Sharer-User-Id", incorrectUserId)
                                .content(validItemJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("create with incorrect json",
                        post("/items")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(incorrectJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("createComment with incorrect userId",
                        post("/items/" + validId + "/comment")
                                .header("X-Sharer-User-Id", incorrectUserId)
                                .content(validCommentJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("createComment with incorrect id",
                        post("/items/" + incorrectId + "/comment")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validCommentJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("createComment with incorrect json",
                        post("/items/" + validId + "/comment")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(incorrectJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("readByOwner with incorrect userId",
                        get("/items").header("X-Sharer-User-Id", incorrectUserId)),

                Arguments.of("readByQuery with incorrect userId",
                        get("/items/search").header("X-Sharer-User-Id", incorrectUserId)),

                Arguments.of("readById with incorrect userId",
                        get("/items/" + validId).header("X-Sharer-User-Id", incorrectUserId)),

                Arguments.of("readById with incorrect id",
                        get("/items/" + incorrectId).header("X-Sharer-User-Id", validUserId)),

                Arguments.of("update with incorrect userId",
                        patch("/items/" + validId)
                                .header("X-Sharer-User-Id", incorrectUserId)
                                .content(validItemJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("update with incorrect id",
                        patch("/items/" + incorrectId)
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validItemJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("update with incorrect json",
                        patch("/items/" + validId)
                                .header("X-Sharer-User-Id", validUserId)
                                .content(incorrectJson)
                                .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("httpMessageNotReadableRequests")
    void request_withNotReadableMessage_shouldReturnStatusBadRequestAndJsonWithErrors(
            String testName, MockHttpServletRequestBuilder request) throws Exception {
        doReturn(mockedErrors).when(controllerErrorHandler).handleHttpMessageNotReadableException(any());

        mockMvc.perform(request)
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> constraintViolationRequests() {
        return Stream.of(
                Arguments.of("readByOwner with negative from",
                        get("/items?from=-1").header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByOwner with zero size",
                        get("/items?size=0").header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByOwner with negative size",
                        get("/items?size=-1").header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByQuery with negative from",
                        get("/items/search?from=-1").header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByQuery with zero size",
                        get("/items/search?size=0").header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByQuery with negative size",
                        get("/items/search?size=-1").header("X-Sharer-User-Id", validUserId))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("constraintViolationRequests")
    void request_withConstraintViolation_shouldReturnStatusBadRequestAndJsonWithErrors(
            String testName, MockHttpServletRequestBuilder request) throws Exception {
        doReturn(mockedErrors).when(controllerErrorHandler).handleConstraintViolationException(any());

        mockMvc.perform(request)
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidRequestBodyRequests() {
        String invalidItemJson = "{\"name\":\"\"}";
        String invalidCommentJson = "{}";

        return Stream.of(
                Arguments.of("create with empty name",
                        post("/items")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(invalidItemJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("createComment with null text",
                        post("/items/" + validId + "/comment")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(invalidCommentJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("update with empty name",
                        patch("/items/" + validId)
                                .header("X-Sharer-User-Id", validUserId)
                                .content(invalidItemJson)
                                .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRequestBodyRequests")
    void request_withInvalidRequestBody_shouldReturnStatusBadRequestAndJsonWithErrors(
            String testName, MockHttpServletRequestBuilder request) throws Exception {
        doReturn(mockedErrors).when(controllerErrorHandler).handleMethodArgumentNotValidException(any());

        mockMvc.perform(request)
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void request_withCreateEndPoint_shouldReturnClientCreateMethodResult()
            throws Exception {
        doReturn(result).when(client).create(eq(validUserId), argThat(equalToDto(validDto)));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", validUserId)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());

        verify(validator, times(1))
                .validate(argThat(equalToDto(validDto)), (Errors) any(), eq(OnCreate.class));
    }

    @Test
    void request_withCreateCommentEndPoint_shouldReturnClientCreateCommentMethodResult()
            throws Exception {
        CommentDtoFromClient validCommentDto = createCommentDtoFromClient("comment");
        doReturn(result).when(client).createComment(eq(validUserId), eq(validId),
                argThat(CommentDtoFromClientMatcher.equalToDto(validCommentDto)));

        mockMvc.perform(post("/items/" + validId + "/comment")
                        .header("X-Sharer-User-Id", validUserId)
                        .content(objectMapper.writeValueAsString(validCommentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());

        verify(validator, times(1))
                .validate(argThat(CommentDtoFromClientMatcher.equalToDto(validCommentDto)), (Errors) any());
    }

    @Test
    void request_withReadByOwnerEndPoint_shouldReturnClientReadByOwnerMethodResult()
            throws Exception {
        doReturn(result).when(client).readByOwner(validUserId, validFrom, validSize);

        mockMvc.perform(get("/items?from=" + validFrom + "&size=" + validSize)
                        .header("X-Sharer-User-Id", validUserId))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());
    }

    @Test
    void request_withReadByQueryEndPoint_shouldReturnClientReadByQueryMethodResult()
            throws Exception {
        String query = "query";
        doReturn(result).when(client).readByQuery(validUserId, query, validFrom, validSize);

        mockMvc.perform(get("/items/search?text=" + query + "&from=" + validFrom + "&size=" + validSize)
                        .header("X-Sharer-User-Id", validUserId))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());
    }

    @Test
    void request_withReadByIdEndPoint_shouldReturnClientReadByIdMethodResult()
            throws Exception {
        doReturn(result).when(client).readById(validUserId, validId);

        mockMvc.perform(get("/items/" + validId).header("X-Sharer-User-Id", validUserId))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());
    }

    @Test
    void request_withUpdateEndPoint_shouldReturnClientUpdateMethodResult()
            throws Exception {
        doReturn(result).when(client).update(eq(validUserId), eq(validId), argThat(equalToDto(validDto)));

        mockMvc.perform(patch("/items/" + validId)
                        .header("X-Sharer-User-Id", validUserId)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());

        verify(validator, times(1))
                .validate(argThat(equalToDto(validDto)), (Errors) any(), eq(OnUpdate.class));
    }
}