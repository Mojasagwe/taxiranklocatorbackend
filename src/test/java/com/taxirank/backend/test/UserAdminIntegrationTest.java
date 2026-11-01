package com.taxirank.backend.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.models.Profile;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;

/**
 * System Administrator account management: create users and rank accounts,
 * reset passwords, set status, change responsible person, search.
 */
public class UserAdminIntegrationTest extends IntegrationTestSupport {

    private User systemAdmin;
    private User commuter;

    @BeforeEach
    void setUp() {
        systemAdmin = person("admin@test.local", "0710000002", Profile.SYSTEM_ADMIN);
        commuter = person("rider@test.local", "0710000001", Profile.COMMUTER);
    }

    @Test
    void createdPeopleGetATemporaryPasswordTheyMustChange() throws Exception {
        JsonNode issued = data(mockMvc.perform(as(systemAdmin, post("/api/admin/users"),
                "{\"firstName\":\"Naledi\",\"lastName\":\"Dube\",\"email\":\"Naledi@Test.local\","
                        + "\"phoneNumber\":\"0710000003\",\"profileCode\":\"SYSTEM_ADMIN\"}"))
                .andExpect(status().isCreated())
                .andReturn());

        assertThat(issued.get("loginName").asText()).isEqualTo("naledi@test.local");
        String temporaryPassword = issued.get("temporaryPassword").asText();
        assertThat(temporaryPassword).matches("[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}");

        mockMvc.perform(login("naledi@test.local", temporaryPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.mustChangePassword").value(true))
                .andExpect(jsonPath("$.data.user.permissions").isArray())
                .andExpect(jsonPath("$.data.user.password").doesNotExist());
    }

    @Test
    void rankAccountsLogInWithTheRankCode() throws Exception {
        TaxiRank rank = rank("PCR001", "Pretoria Central Rank", null, null);

        JsonNode issued = data(mockMvc.perform(as(systemAdmin, post("/api/admin/users"),
                "{\"profileCode\":\"RANK_ADMIN\",\"rankId\":" + rank.getId()
                        + ",\"responsiblePersonName\":\"Johan Pretorius\",\"responsiblePersonPhone\":\"0825550100\"}"))
                .andExpect(status().isCreated())
                .andReturn());

        assertThat(issued.get("loginName").asText()).isEqualTo("PCR001");
        mockMvc.perform(login("pcr001", issued.get("temporaryPassword").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.rank.code").value("PCR001"))
                .andExpect(jsonPath("$.data.user.profileCode").value("RANK_ADMIN"));

        // One account per rank
        mockMvc.perform(as(systemAdmin, post("/api/admin/users"),
                "{\"profileCode\":\"RANK_ADMIN\",\"rankId\":" + rank.getId()
                        + ",\"responsiblePersonName\":\"Someone Else\",\"responsiblePersonPhone\":\"0825550101\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void resetPasswordReplacesTheOldOne() throws Exception {
        JsonNode issued = data(mockMvc.perform(as(systemAdmin, post("/api/admin/users/" + commuter.getId() + "/reset-password")))
                .andExpect(status().isOk())
                .andReturn());

        mockMvc.perform(login("rider@test.local", PASSWORD)).andExpect(status().isUnauthorized());
        mockMvc.perform(login("rider@test.local", issued.get("temporaryPassword").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.mustChangePassword").value(true));
    }

    @Test
    void statusChangesNeedAReasonAndAreRecorded() throws Exception {
        mockMvc.perform(as(systemAdmin, post("/api/admin/users/" + commuter.getId() + "/status"), "{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(systemAdmin, post("/api/admin/users/" + commuter.getId() + "/status"),
                "{\"status\":\"SUSPENDED\",\"reason\":\"Abusive messages\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountStatus").value("SUSPENDED"))
                .andExpect(jsonPath("$.data.statusReason").value("Abusive messages"));

        mockMvc.perform(as(systemAdmin, get("/api/admin/users/" + commuter.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.history[0].action").value("STATUS_CHANGED"))
                .andExpect(jsonPath("$.data.history[0].actorName").value(systemAdmin.getDisplayName()));

        mockMvc.perform(as(systemAdmin, post("/api/admin/users/" + commuter.getId() + "/status"), "{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statusReason").doesNotExist());
        assertThat(userRepository.findById(commuter.getId()).orElseThrow().getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void responsiblePersonIsOnlyForRankAccounts() throws Exception {
        User account = rankAccount(rank("DBR001", "Durban Beach Rank", null, null));
        String person = "{\"name\":\"Sizwe Zulu\",\"phone\":\"0835550123\"}";

        mockMvc.perform(as(systemAdmin, put("/api/admin/users/" + account.getId() + "/responsible-person"), person))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.responsiblePersonName").value("Sizwe Zulu"));
        mockMvc.perform(as(systemAdmin, put("/api/admin/users/" + commuter.getId() + "/responsible-person"), person))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchFiltersByProfileStatusAndText() throws Exception {
        rankAccount(rank("CTS001", "Cape Town Station", null, null));

        mockMvc.perform(as(systemAdmin, get("/api/admin/users").param("profile", "RANK_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].loginName").value("CTS001"));
        mockMvc.perform(as(systemAdmin, get("/api/admin/users").param("q", "rider@")))
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(as(systemAdmin, get("/api/admin/users").param("status", "BLOCKED")))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void duplicateEmailOrPhoneIsRejected() throws Exception {
        mockMvc.perform(as(systemAdmin, post("/api/admin/users"),
                "{\"firstName\":\"Dup\",\"lastName\":\"User\",\"email\":\"RIDER@test.local\","
                        + "\"phoneNumber\":\"0710000077\",\"profileCode\":\"COMMUTER\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(json(post("/api/auth/register"),
                "{\"firstName\":\"Dup\",\"lastName\":\"User\",\"email\":\"new@test.local\","
                        + "\"password\":\"Passw0rd!\",\"phoneNumber\":\"0710000001\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void registrationIsValidated() throws Exception {
        mockMvc.perform(json(post("/api/auth/register"),
                "{\"firstName\":\"\",\"lastName\":\"User\",\"email\":\"not-an-email\",\"password\":\"123\",\"phoneNumber\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
