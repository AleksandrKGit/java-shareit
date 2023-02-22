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
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.practicum.shareit.controller.ControllerErrorHandler;
import ru.practicum.shareit.item.dto.CommentDtoFromClient;
import ru.practicum.shareit.item.dto.CommentDtoToClient;
import ru.practicum.shareit.item.dto.ItemDtoFromClient;
import ru.practicum.shareit.item.dto.ItemDtoToClient;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.tools.configuration.AppTestConfiguration;
import ru.practicum.shareit.tools.matchers.CommentDtoFromClientMatcher;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.tools.factories.ItemFactory.*;
import static ru.practicum.shareit.tools.factories.ItemFactory.createCommentDtoToClient;
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
    ItemService service;

    @SpyBean
    ControllerErrorHandler controllerErrorHandler;

    final Map<String, String> mockedErrors = Map.of("error", "mocked");

    static final long validUserId = 1L;

    static final long validId = 3L;

    static final String validItemJson = "{\"name\":\"name\",\"description\":\"desc\",\"available\":true}";

    static final String validCommentJson = "{\"text\":\"some text\"}";

    final ItemDtoFromClient validDto = createItemDtoFromClient("item", "desc", true,
            4L);

    final ItemDtoToClient resultDto = createItemDtoToClient(validId,"item", "desc", true,
            5L, null, null, null);

    final List<ItemDtoToClient> resultListDto = List.of(resultDto);

    final Integer validFrom = 2;

    final Integer validSize = 10;

    private static Stream<Arguments> incorrectRequest() {
        String incorrectId = "a";
        String incorrectUserId = "a";
        String incorrectJson = "}";

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
                                .contentType(MediaType.IMAGE_PNG)),

                Arguments.of("create without X-Sharer-User-Id header",
                        post("/items")
                                .content(validItemJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("createComment without X-Sharer-User-Id header",
                        post("/items/" + validId + "/comment")
                                .content(validCommentJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("readByOwner without X-Sharer-User-Id header",
                        get("/items")),

                Arguments.of("readByQuery without X-Sharer-User-Id header",
                        get("/items/search")),

                Arguments.of("readById without X-Sharer-User-Id header",
                        get("/items/" + validId)),

                Arguments.of("update without X-Sharer-User-Id header",
                        patch("/items/" + validId)
                                .content(validItemJson)
                                .contentType(MediaType.APPLICATION_JSON)),

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

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", validUserId)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(resultDto)))
                .andExpect(status().isOk());
    }

    @Test
    void request_withCreateCommentEndPoint_shouldReturnStatusOkAndServiceCreateCommentMethodResult()
            throws Exception {
        CommentDtoFromClient validCommentDto = createCommentDtoFromClient("comment");
        CommentDtoToClient resultDto = createCommentDtoToClient(1L, "comment", LocalDateTime.now(),
                "userName");
        doReturn(resultDto).when(service).createComment(eq(validUserId), eq(validId),
                argThat(CommentDtoFromClientMatcher.equalToDto(validCommentDto)));

        mockMvc.perform(post("/items/" + validId + "/comment")
                        .header("X-Sharer-User-Id", validUserId)
                        .content(objectMapper.writeValueAsString(validCommentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(resultDto)))
                .andExpect(status().isOk());
    }

    @Test
    void request_withReadByOwnerEndPoint_shouldReturnStatusOkAndServiceReadByOwnerMethodResult()
            throws Exception {
        doReturn(resultListDto).when(service).readByOwner(validUserId, validFrom, validSize);

        mockMvc.perform(get("/items?from=" + validFrom + "&size=" + validSize)
                        .header("X-Sharer-User-Id", validUserId))
                .andExpect(content().json(objectMapper.writeValueAsString(resultListDto)))
                .andExpect(status().isOk());
    }

    @Test
    void request_withReadByQueryEndPoint_shouldReturnStatusOkAndServiceReadByQueryMethodResult()
            throws Exception {
        String query = "query";
        doReturn(resultListDto).when(service).readByQuery(validUserId, query, validFrom, validSize);

        mockMvc.perform(get("/items/search?text=" + query + "&from=" + validFrom + "&size=" + validSize)
                        .header("X-Sharer-User-Id", validUserId))
                .andExpect(content().json(objectMapper.writeValueAsString(resultListDto)))
                .andExpect(status().isOk());
    }

    @Test
    void request_withReadByIdEndPoint_shouldReturnStatusOkAndServiceReadByIdMethodResult()
            throws Exception {
        doReturn(resultDto).when(service).readById(validUserId, validId);

        mockMvc.perform(get("/items/" + validId).header("X-Sharer-User-Id", validUserId))
                .andExpect(content().json(objectMapper.writeValueAsString(resultDto)))
                .andExpect(status().isOk());
    }

    @Test
    void request_withUpdateEndPoint_shouldReturnStatusOkAndServiceUpdateMethodResult()
            throws Exception {
        doReturn(resultDto).when(service).update(eq(validUserId), eq(validId), argThat(equalToDto(validDto)));

        mockMvc.perform(patch("/items/" + validId)
                        .header("X-Sharer-User-Id", validUserId)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(resultDto)))
                .andExpect(status().isOk());
    }
}