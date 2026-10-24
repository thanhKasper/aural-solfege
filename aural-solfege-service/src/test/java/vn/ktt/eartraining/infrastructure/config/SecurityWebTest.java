package vn.ktt.eartraining.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import vn.ktt.eartraining.application.access.NotFoundException;
import vn.ktt.eartraining.application.inbound.IExerciseCreationPort;
import vn.ktt.eartraining.application.inbound.IExerciseRetrievalPort;
import vn.ktt.eartraining.application.inbound.ISessionPort;
import vn.ktt.eartraining.infrastructure.controller.ApiExceptionHandler;
import vn.ktt.eartraining.infrastructure.controller.ExerciseController;
import vn.ktt.eartraining.infrastructure.controller.SessionController;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Wires the real {@link SecurityConfig}, controllers and {@link ApiExceptionHandler} against mocked
 * ports, so the HTTP-level rules (authentication, role gate, 404 mapping) are checked without a database.
 */
@SpringJUnitWebConfig(SecurityWebTest.TestConfig.class)
class SecurityWebTest {

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, ExerciseController.class, SessionController.class, ApiExceptionHandler.class})
    static class TestConfig {
        @Bean JwtDecoder jwtDecoder() { return Mockito.mock(JwtDecoder.class); }
        @Bean IExerciseCreationPort exerciseCreationPort() { return Mockito.mock(IExerciseCreationPort.class); }
        @Bean IExerciseRetrievalPort exerciseRetrievalPort() { return Mockito.mock(IExerciseRetrievalPort.class); }
        @Bean ISessionPort sessionPort() { return Mockito.mock(ISessionPort.class); }
    }

    @Autowired WebApplicationContext context;
    @Autowired ISessionPort sessionPort;
    @Autowired IExerciseRetrievalPort exerciseRetrievalPort;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void requestWithoutTokenIsRejectedWith401() throws Exception {
        mvc.perform(get("/api/exercises")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/sessions/{id}/advance", UUID.randomUUID())).andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserWithoutLearnerRoleIsForbidden() throws Exception {
        mvc.perform(get("/api/exercises").with(jwt())).andExpect(status().isForbidden());
    }

    @Test
    void learnerCanReachTheApi() throws Exception {
        when(exerciseRetrievalPort.getExercises(0, 10)).thenReturn(null);
        mvc.perform(get("/api/exercises").with(learner())).andExpect(status().isOk());
    }

    @Test
    void sessionOwnedBySomeoneElseSurfacesAs404() throws Exception {
        var sessionId = UUID.randomUUID();
        when(sessionPort.advanceToNextStep(sessionId)).thenThrow(new NotFoundException("Session not found: " + sessionId));
        when(sessionPort.concludeSession(sessionId)).thenThrow(new NotFoundException("Session not found: " + sessionId));

        mvc.perform(post("/api/sessions/{id}/advance", sessionId).with(learner())).andExpect(status().isNotFound());
        mvc.perform(post("/api/sessions/{id}/conclude", sessionId).with(learner())).andExpect(status().isNotFound());
    }

    @Test
    void exerciseOwnedBySomeoneElseSurfacesAs404() throws Exception {
        when(exerciseRetrievalPort.getExerciseById(any())).thenThrow(new NotFoundException("Exercise not found"));
        mvc.perform(get("/api/exercises/{id}", UUID.randomUUID()).with(learner())).andExpect(status().isNotFound());
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor learner() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_learner"));
    }
}
