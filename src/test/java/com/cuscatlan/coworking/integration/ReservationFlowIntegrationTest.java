package com.cuscatlan.coworking.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cuscatlan.coworking.dto.request.RegisterRequest;
import com.cuscatlan.coworking.dto.request.ReservationRequest;
import com.cuscatlan.coworking.dto.request.SpaceRequest;
import com.cuscatlan.coworking.dto.response.AuthResponse;
import com.cuscatlan.coworking.dto.response.PaymentValidationResult;
import com.cuscatlan.coworking.entity.Role;
import com.cuscatlan.coworking.entity.SpaceType;
import com.cuscatlan.coworking.entity.User;
import com.cuscatlan.coworking.repository.UserRepository;
import com.cuscatlan.coworking.service.PaymentValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private PaymentValidationService paymentValidationService;

    private String adminToken;
    private String userToken;
    private Long spaceId;

    private void setUpAdminAndSpace() throws Exception {
        when(paymentValidationService.validate(anyLong())).thenReturn(PaymentValidationResult.success());

        // Registra un usuario y lo promueve a ADMIN directamente en la BD de
        // test (más simple que exponer un endpoint de promoción solo para tests).
        RegisterRequest adminRequest = new RegisterRequest("Admin Test", "admin-flow@test.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRequest)));

        User admin = userRepository.findByEmail("admin-flow@test.com").orElseThrow();
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.cuscatlan.coworking.dto.request.LoginRequest("admin-flow@test.com", "password123"))))
                .andReturn();
        adminToken = objectMapper.readValue(loginResult.getResponse().getContentAsString(), AuthResponse.class).token();

        RegisterRequest userRequest = new RegisterRequest("User Test", "user-flow@test.com", "password123");
        MvcResult userRegisterResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andReturn();
        userToken = objectMapper.readValue(userRegisterResult.getResponse().getContentAsString(), AuthResponse.class).token();

        SpaceRequest spaceRequest = new SpaceRequest("Sala Test", SpaceType.MEETING_ROOM, 5, "Piso 1", BigDecimal.valueOf(10));
        MvcResult spaceResult = mockMvc.perform(post("/api/spaces")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spaceRequest)))
                .andReturn();
        String spaceJson = spaceResult.getResponse().getContentAsString();
        spaceId = objectMapper.readTree(spaceJson).get("id").asLong();
    }

    @Test
    void fullReservationFlow_createConfirmAndPreventOverlap() throws Exception {
        setUpAdminAndSpace();

        ReservationRequest reservationRequest = new ReservationRequest(
                spaceId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));

        // 1. USER crea una reserva -> como el pago está mockeado a approved,
        // debe confirmarse inmediatamente.
        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // 2. Intentar reservar el MISMO espacio con horario solapado  (409).
        ReservationRequest overlappingRequest = new ReservationRequest(
                spaceId, LocalDateTime.now().plusDays(1).plusHours(1), LocalDateTime.now().plusDays(1).plusHours(3));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlappingRequest)))
                .andExpect(status().isConflict());

        // 3. USER consulta mis reservas y debe ver exactamente 1.
        mockMvc.perform(get("/api/reservations/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void nonAdminUser_cannotCreateSpace() throws Exception {
        setUpAdminAndSpace();

        SpaceRequest spaceRequest = new SpaceRequest("Sala No Autorizada", SpaceType.DESK, 2, "Piso 3", BigDecimal.TEN);

        // Un USER normal no ADMIN intentando crear un espacio  (403 Forbidden).
        mockMvc.perform(post("/api/spaces")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spaceRequest)))
                .andExpect(status().isForbidden());
    }
}