package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.controller.ControllerErrorHandler;
import ru.practicum.shareit.user.dto.UserDtoFromClient;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.stream.Stream;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static ru.practicum.shareit.tools.factories.UserFactory.createUserDtoFromClient;
import static ru.practicum.shareit.tools.factories.UserFactory.*;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserControllerIntegrationTests {
    MockMvc mockMvc;

    @Autowired
    UserController controller;

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ControllerErrorHandler controllerErrorHandler;

    final Long notExistingId = 1000L;

    final String name = "userName";

    final String email = "user@email.com";

    final UserDtoFromClient requestUserDto = createUserDtoFromClient(name, email);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(controllerErrorHandler)
                .build();
    }

    @Test
    void create_withNotUniqueEmail_shouldReturnStatusConflict() throws Exception {
        User userWithSameEmail = createUser(null, "userWithSameEmailName", email);
        em.persist(userWithSameEmail);
        em.flush();

        mockMvc.perform(post("/users/")
                        .content(objectMapper.writeValueAsString(requestUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void create_shouldReturnStatusOkAndDtoOfCreatedUser() throws Exception {
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(requestUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(notNullValue()), Long.class),
                        jsonPath("$.name", is(requestUserDto.getName())),
                        jsonPath("$.email", is(requestUserDto.getEmail())));

        User createdUser = em.createQuery("Select u from User u", User.class).getSingleResult();

        assertThat(createdUser, allOf(
                hasProperty("id", is(notNullValue())),
                hasProperty("name", equalTo(requestUserDto.getName())),
                hasProperty("email", equalTo(requestUserDto.getEmail()))
        ));
    }

    @Test
    void readAll_shouldReturnDtoListOfAllUsers() throws Exception {
        User[] users = new User[] {
                createUser(null, "n1", "e1@email.com"),
                createUser(null, "n2", "e2@email.com")
        };
        Stream.of(users).forEach(em::persist);
        em.flush();

        mockMvc.perform(get("/users"))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].id", is(users[0].getId()), Long.class),
                        jsonPath("$[0].name", is(users[0].getName())),
                        jsonPath("$[0].email", is(users[0].getEmail())),
                        jsonPath("$[1].id", is(users[1].getId()), Long.class),
                        jsonPath("$[1].name", is(users[1].getName())),
                        jsonPath("$[1].email", is(users[1].getEmail())));
    }

    @Test
    void readById_withNotExistingId_shouldReturnStatusNotFound() throws Exception {
        mockMvc.perform(get("/users/" + notExistingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void readById_shouldReturnStatusOkAndDtoOfSelectedUser() throws Exception {
        User existingUser = createUser(null, name, email);
        em.persist(existingUser);
        em.flush();
        Long existingId = existingUser.getId();

        mockMvc.perform(get("/users/" + existingId))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(existingUser.getId()), Long.class),
                        jsonPath("$.name", equalTo(existingUser.getName())),
                        jsonPath("$.email", equalTo(existingUser.getEmail())));
    }

    @Test
    void update_withNotExistingId_shouldReturnStatusNotFound() throws Exception {
        mockMvc.perform(patch("/users/" + notExistingId)
                        .content(objectMapper.writeValueAsString(requestUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withExistingIdAndNotUniqueEmail_shouldReturnStatusConflict() throws Exception {
        User userWithSameEmail = createUser(null, "userWithSameEmailName", email);
        em.persist(userWithSameEmail);
        User existingUser = createUser(null, "oldName", "old@email.com");
        em.persist(existingUser);
        em.flush();
        Long id = existingUser.getId();

        mockMvc.perform(patch("/users/" + id)
                        .content(objectMapper.writeValueAsString(requestUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void update_shouldReturnStatusOkAndDtoOfUpdatedUser() throws Exception {
        User existingUser = createUser(null, "oldName", "old@email.com");
        em.persist(existingUser);
        em.flush();
        Long existingId = existingUser.getId();

        mockMvc.perform(patch("/users/" + existingId)
                        .content(objectMapper.writeValueAsString(requestUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", equalTo(existingId), Long.class),
                        jsonPath("$.name", equalTo(requestUserDto.getName())),
                        jsonPath("$.email", equalTo(requestUserDto.getEmail())));

        assertThat(em.contains(existingUser), is(true));
        assertThat(existingUser, allOf(
                hasProperty("id", equalTo(existingId)),
                hasProperty("name", equalTo(requestUserDto.getName())),
                hasProperty("email", equalTo(requestUserDto.getEmail()))
        ));
    }

    @Test
    void delete_withNotExistingId_shouldReturnStatusNotFound() throws Exception {
        mockMvc.perform(delete("/users/" + notExistingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldDeleteUserAndReturnStatusOk() throws Exception {
        User existingUser = createUser(null, name, email);
        em.persist(existingUser);
        em.flush();
        Long existingId = existingUser.getId();

        mockMvc.perform(delete("/users/" + existingId))
                .andExpect(status().isOk());

        assertThat(em.contains(existingUser), is(false));
    }
}