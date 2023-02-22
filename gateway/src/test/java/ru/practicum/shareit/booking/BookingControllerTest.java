package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.controller.ControllerErrorHandler;
import ru.practicum.shareit.tools.configuration.AppTestConfiguration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.tools.factories.DtoFactory.createBookingDtoFromClient;
import static ru.practicum.shareit.tools.matchers.BookingDtoFromClientMatcher.equalToDto;

@WebMvcTest(BookingController.class)
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient client;

    @SpyBean
    private ControllerErrorHandler controllerErrorHandler;

    @SpyBean
    private LocalValidatorFactoryBean validator;

    final Map<String, String> mockedErrors = Map.of("error", "mocked");

    static final long validUserId = 1L;

    static final long validId = 3L;

    static final LocalDateTime start = LocalDateTime.now().plusSeconds(10);

    static final LocalDateTime end = LocalDateTime.now().plusSeconds(20);

    static final String validBookingJson = "{\"start\":\"" + start + "\", \"end\":\"" + end + "\", " +
            "\"itemId\":1}";

    final BookingDtoFromClient validDto = createBookingDtoFromClient(4L, start, end);

    final ResponseEntity<Object> result = ResponseEntity.ok("OK");

    final BookingState state = BookingState.ALL;

    final Integer validFrom = 5;

    final Integer validSize = 10;

    private static Stream<Arguments> httpMediaTypeNotSupportedRequests() {
        return Stream.of(
                Arguments.of("create without media type",
                        post("/bookings")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validBookingJson)),

                Arguments.of("create with incorrect media type",
                        post("/bookings")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validBookingJson)
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
                        post("/bookings")
                                .content(validBookingJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("readByBooker",
                        get("/bookings")),

                Arguments.of("readByOwner",
                        get("/bookings/owner")),

                Arguments.of("readById",
                        get("/bookings/" + validId)),

                Arguments.of("approve",
                        patch("/bookings/" + validId + "?approved=true"))
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
        String incorrectApproved = "a";
        String incorrectFrom = "a";
        String incorrectSize = "a";
        String incorrectId = "a";
        String incorrectUserId = "a";
        String incorrectJson = "}";

        return Stream.of(
                Arguments.of("create with incorrect userId",
                        post("/bookings")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(incorrectJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("create with incorrect userId",
                        post("/bookings")
                                .header("X-Sharer-User-Id", incorrectUserId)
                                .content(validBookingJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("readByBooker with incorrect userId",
                        get("/bookings").header("X-Sharer-User-Id", incorrectUserId)),

                Arguments.of("readByBooker with incorrect size",
                        get("/bookings?size=" + incorrectSize).header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByBooker with incorrect from",
                        get("/bookings?from=" + incorrectFrom).header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByOwner with incorrect userId",
                        get("/bookings/owner").header("X-Sharer-User-Id", incorrectUserId)),

                Arguments.of("readByOwner with incorrect size",
                        get("/bookings/owner?size=" + incorrectSize)
                                .header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByOwner with incorrect from",
                        get("/bookings/owner?from=" + incorrectFrom).header("X-Sharer-User-Id",
                                validUserId)),

                Arguments.of("readById with incorrect userId",
                        get("/bookings/" + validId).header("X-Sharer-User-Id", incorrectUserId)),

                Arguments.of("approve with incorrect userId",
                        patch("/bookings/" + validId + "?approved=true")
                                .header("X-Sharer-User-Id", incorrectUserId)),

                Arguments.of("approve with incorrect id",
                        patch("/bookings/" + incorrectId + "?approved=true")
                                .header("X-Sharer-User-Id", validUserId)),

                Arguments.of("approve with incorrect approved",
                        patch("/bookings/" + validId + "?approved=" + incorrectApproved)
                                .header("X-Sharer-User-Id", validUserId))
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
                        get("/bookings/owner?from=-1").header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByOwner with zero size",
                        get("/bookings/owner?size=0").header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByOwner with negative size",
                        get("/bookings/owner?size=-1").header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByBooker with negative from",
                        get("/bookings?from=-1").header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByBooker with zero size",
                        get("/bookings?size=0").header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByBooker with negative size",
                        get("/bookings?size=-1").header("X-Sharer-User-Id", validUserId))
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

    private static Stream<Arguments> invalidDataRequests() {
        String invalidState = "unknown";
        String invalidEndEqualsStart = "{\"start\":\"" + start + "\", \"end\":\"" + start + "\", " +
                "\"itemId\":1}";
        String invalidEndEqualsBeforeStart = "{\"start\":\"" + start + "\", \"end\":\"" + start.minusSeconds(1)
                + "\", " + "\"itemId\":1}";

        return Stream.of(
                Arguments.of("create with end equal start",
                        post("/bookings")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(invalidEndEqualsStart)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("create with end before start",
                        post("/bookings")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(invalidEndEqualsBeforeStart)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("readByOwner with invalid state",
                        get("/bookings/owner?state=" + invalidState)
                                .header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readByBooker with invalid state",
                        get("/bookings?state=" + invalidState)
                                .header("X-Sharer-User-Id", validUserId))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDataRequests")
    void request_withNotValidData_shouldReturnStatusBadRequestAndJsonWithErrors(
            String testName, MockHttpServletRequestBuilder request) throws Exception {
        doReturn(mockedErrors).when(controllerErrorHandler).handleValidationException(any());

        mockMvc.perform(request)
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void request_withApproveEndPointMissingApproveParameter_shouldReturnStatusBadRequestAndJsonWithErrors()
            throws Exception {
        doReturn(mockedErrors).when(controllerErrorHandler).handleMissingServletRequestParameterException(any());

        mockMvc.perform(patch("/bookings/" + validId).header("X-Sharer-User-Id", validUserId))
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void request_withCreateEndPointAndInvalidDto_shouldReturnStatusBadRequestAndJsonWithErrors() throws Exception {
        BookingDtoFromClient invalidDto = createBookingDtoFromClient(null, null, null);
        doReturn(mockedErrors).when(controllerErrorHandler).handleMethodArgumentNotValidException(any());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", validUserId)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void request_withCreateEndPoint_shouldReturnClientCreateMethodResult()
            throws Exception {
        doReturn(result).when(client).create(eq(validUserId), argThat(equalToDto(validDto)));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", validUserId)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());

        verify(validator, times(1)).validate(argThat(equalToDto(validDto)), (Errors) any());
    }

    @Test
    void request_withReadByBookerEndPoint_shouldReturnClientReadByBookerMethodResult()
            throws Exception {
        doReturn(result).when(client).readByBooker(validUserId, state, validFrom, validSize);

        mockMvc.perform(get("/bookings?state=" + state + "&from=" + validFrom + "&size=" + validSize)
                        .header("X-Sharer-User-Id", validUserId))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());
    }

    @Test
    void request_withReadByOwnerEndPoint_shouldReturnClientReadByOwnerMethodResult()
            throws Exception {
        doReturn(result).when(client).readByOwner(validUserId, state, validFrom, validSize);

        mockMvc.perform(get("/bookings/owner?state=" + state + "&from=" + validFrom + "&size=" + validSize)
                        .header("X-Sharer-User-Id", validUserId))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());
    }

    @Test
    void request_withReadByIdEndPoint_shouldReturnClientReadByIdMethodResult()
            throws Exception {
        doReturn(result).when(client).readById(validUserId, validId);

        mockMvc.perform(get("/bookings/" + validId).header("X-Sharer-User-Id", validUserId))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());
    }

    @Test
    void request_withApproveEndPoint_shouldReturnClientApproveMethodResult()
            throws Exception {
        doReturn(result).when(client).approve(validUserId, validId, true);

        mockMvc.perform(patch("/bookings/" + validId + "?approved=true")
                        .header("X-Sharer-User-Id", validUserId))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());
    }
}