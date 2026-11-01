package com.taxirank.backend.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.security.JwtTokenProvider;

/**
 * Regression tests for the P0 access-control holes found in the 2026-11-01 audit:
 * A) self-service privilege escalation via PUT /api/users/{id},
 * B) unauthenticated writes to /api/ranks and /api/routes,
 * C) public super admin creation.
 * Runs the real security chain (JWT filter, URL rules, @PreAuthorize) against H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.bootstrap.super-admin-token=test-bootstrap-token")
@Transactional
public class AccessControlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaxiRankRepository taxiRankRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private User rider;
    private User otherRider;
    private User admin;

    @BeforeEach
    void setUp() {
        rider = createUser("rider@test.local", "0710000001", UserRole.RIDER);
        otherRider = createUser("other@test.local", "0710000002", UserRole.RIDER);
        admin = createUser("admin@test.local", "0710000003", UserRole.ADMIN);

        TaxiRank rank = new TaxiRank();
        rank.setName("Central");
        rank.setCode("CTR001");
        rank.setAddress("1 Main St");
        taxiRankRepository.save(rank);
    }

    // --- A: privilege escalation -------------------------------------------------

    @Test
    void riderCannotPromoteThemselves() throws Exception {
        mockMvc.perform(as(rider, put("/api/users/" + rider.getId()), "{\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isForbidden());

        assertThat(userRepository.findById(rider.getId()).orElseThrow().getRole()).isEqualTo(UserRole.RIDER);
    }

    @Test
    void riderCannotChangeOwnAccountStatusOrVerification() throws Exception {
        mockMvc.perform(as(rider, put("/api/users/" + rider.getId()), "{\"isVerified\":true}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(as(rider, put("/api/users/" + rider.getId()), "{\"accountStatus\":\"ACTIVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void riderCanEditOwnProfileAndPasswordIsHashed() throws Exception {
        mockMvc.perform(as(rider, put("/api/users/" + rider.getId()), "{\"firstName\":\"Renamed\",\"password\":\"NewPass123\"}"))
                .andExpect(status().isOk());

        User updated = userRepository.findById(rider.getId()).orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo("Renamed");
        assertThat(updated.getPassword()).isNotEqualTo("NewPass123");
        assertThat(passwordEncoder.matches("NewPass123", updated.getPassword())).isTrue();
    }

    @Test
    void riderCannotEditAnotherUser() throws Exception {
        mockMvc.perform(as(rider, put("/api/users/" + otherRider.getId()), "{\"firstName\":\"Hacked\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rankAdminCannotTakeOverOtherAccounts() throws Exception {
        User superAdmin = createUser("super@test.local", "0710000004", UserRole.SUPER_ADMIN);

        mockMvc.perform(as(admin, put("/api/users/" + superAdmin.getId()), "{\"password\":\"Owned123\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(as(admin, post("/api/users"), "{\"email\":\"x@test.local\",\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(as(admin, delete("/api/users/" + otherRider.getId()), null))
                .andExpect(status().isForbidden());
        mockMvc.perform(as(admin, post("/api/admin/register"), registerJson("new-admin@test.local", "0710000005")))
                .andExpect(status().isForbidden());
    }

    @Test
    void superAdminCanChangeRoles() throws Exception {
        User superAdmin = createUser("super@test.local", "0710000004", UserRole.SUPER_ADMIN);

        mockMvc.perform(as(superAdmin, put("/api/users/" + rider.getId()), "{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk());

        assertThat(userRepository.findById(rider.getId()).orElseThrow().getRole()).isEqualTo(UserRole.ADMIN);
    }

    // --- B: unauthenticated / under-privileged writes ----------------------------

    @Test
    void ranksAreReadableWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/ranks")).andExpect(status().isOk());
    }

    @Test
    void anonymousCannotDeleteAllRanks() throws Exception {
        mockMvc.perform(delete("/api/ranks/all")).andExpect(status().is4xxClientError());

        assertThat(taxiRankRepository.count()).isEqualTo(1);
    }

    @Test
    void onlyAdminsCanCreateRanks() throws Exception {
        String rank = "{\"name\":\"Fake\",\"code\":\"FAKE01\",\"address\":\"x\"}";

        mockMvc.perform(post("/api/ranks").contentType(MediaType.APPLICATION_JSON).content(rank))
                .andExpect(status().isForbidden());
        mockMvc.perform(as(rider, post("/api/ranks"), rank))
                .andExpect(status().isForbidden());
        mockMvc.perform(as(admin, post("/api/ranks"), rank))
                .andExpect(status().isOk());
    }

    @Test
    void routesAreReadableButNotWritableWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/routes")).andExpect(status().isOk());

        mockMvc.perform(post("/api/routes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/routes/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/routes/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void ridersCannotWriteRoutesAndAdminsCannotDeleteThem() throws Exception {
        mockMvc.perform(as(rider, post("/api/routes"), "{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(as(admin, delete("/api/routes/1"), null))
                .andExpect(status().isForbidden());
    }

    // --- C: super admin creation ---------------------------------------------------

    @Test
    void legacyPublicSuperAdminEndpointIsGone() throws Exception {
        mockMvc.perform(post("/api/admin/register/super").contentType(MediaType.APPLICATION_JSON)
                .content(registerJson("evil@test.local", "0710000009")))
                .andExpect(status().is4xxClientError());

        assertThat(userRepository.findByRole(UserRole.SUPER_ADMIN)).isEmpty();
    }

    @Test
    void firstSuperAdminRequiresBootstrapToken() throws Exception {
        String body = registerJson("owner@test.local", "0710000010");

        mockMvc.perform(post("/api/auth/register/super-admin").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/auth/register/super-admin").header("X-Bootstrap-Token", "wrong")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        assertThat(userRepository.findByRole(UserRole.SUPER_ADMIN)).isEmpty();

        mockMvc.perform(post("/api/auth/register/super-admin").header("X-Bootstrap-Token", "test-bootstrap-token")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        assertThat(userRepository.findByRole(UserRole.SUPER_ADMIN)).hasSize(1);
    }

    @Test
    void bootstrapTokenIsUselessOnceASuperAdminExists() throws Exception {
        createUser("super@test.local", "0710000004", UserRole.SUPER_ADMIN);

        mockMvc.perform(post("/api/auth/register/super-admin").header("X-Bootstrap-Token", "test-bootstrap-token")
                .contentType(MediaType.APPLICATION_JSON).content(registerJson("second@test.local", "0710000011")))
                .andExpect(status().isForbidden());
        mockMvc.perform(as(admin, post("/api/auth/register/super-admin"), registerJson("third@test.local", "0710000012")))
                .andExpect(status().isForbidden());

        assertThat(userRepository.findByRole(UserRole.SUPER_ADMIN)).hasSize(1);
    }

    // --- helpers -------------------------------------------------------------------

    private User createUser(String email, String phone, UserRole role) {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName(role.name());
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setPassword(passwordEncoder.encode("Passw0rd!"));
        user.setRole(role);
        return userRepository.save(user);
    }

    private MockHttpServletRequestBuilder as(User user, MockHttpServletRequestBuilder request, String json) {
        request.header("Authorization", "Bearer " + tokenProvider.generateToken(user.getId()));
        if (json != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return request;
    }

    private static String registerJson(String email, String phone) {
        return "{\"firstName\":\"New\",\"lastName\":\"User\",\"email\":\"" + email
                + "\",\"password\":\"Passw0rd!\",\"phoneNumber\":\"" + phone + "\"}";
    }
}
