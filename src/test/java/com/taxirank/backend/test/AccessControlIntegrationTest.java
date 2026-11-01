package com.taxirank.backend.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.models.Profile;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.Terminal;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.TerminalRepository;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Who may do what. Covers the 2026-11-01 audit findings (A privilege escalation,
 * B unauthenticated writes, C public super admin creation, F suspended accounts,
 * K rank admins editing other ranks) under the profiles model.
 */
public class AccessControlIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private TerminalRepository terminalRepository;

    private User commuter;
    private User systemAdmin;
    private TaxiRank central;
    private TaxiRank sandton;
    private User centralAccount;

    @BeforeEach
    void setUp() {
        commuter = person("rider@test.local", "0710000001", Profile.COMMUTER);
        systemAdmin = person("admin@test.local", "0710000002", Profile.SYSTEM_ADMIN);
        central = rank("CTR001", "Central Taxi Rank", -26.2041, 28.0473);
        sandton = rank("SCR001", "Sandton City Rank", -26.1067, 28.0567);
        centralAccount = rankAccount(central);
    }

    // --- A: privilege escalation ---------------------------------------------------

    @Test
    void commuterCannotUseTheAdminUserApi() throws Exception {
        mockMvc.perform(as(commuter, get("/api/admin/users"))).andExpect(status().isForbidden());
        mockMvc.perform(as(commuter, put("/api/admin/users/" + commuter.getId()), "{\"profileCode\":\"SYSTEM_ADMIN\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(as(commuter, post("/api/admin/users/" + commuter.getId() + "/status"), "{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isForbidden());

        assertThat(userRepository.findById(commuter.getId()).orElseThrow().hasProfile(Profile.COMMUTER)).isTrue();
    }

    @Test
    void selfUpdateIgnoresPrivilegedFields() throws Exception {
        mockMvc.perform(as(commuter, put("/api/users/me"),
                "{\"firstName\":\"Renamed\",\"profileCode\":\"SYSTEM_ADMIN\",\"accountStatus\":\"ACTIVE\"}"))
                .andExpect(status().isOk());

        User updated = userRepository.findById(commuter.getId()).orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo("Renamed");
        assertThat(updated.hasProfile(Profile.COMMUTER)).isTrue();
    }

    @Test
    void oldUserEndpointsAreGone() throws Exception {
        mockMvc.perform(as(commuter, put("/api/users/" + commuter.getId()), "{\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(as(commuter, get("/api/users"))).andExpect(status().is4xxClientError());
    }

    @Test
    void rankAccountCannotManageUsers() throws Exception {
        mockMvc.perform(as(centralAccount, get("/api/admin/users"))).andExpect(status().isForbidden());
        mockMvc.perform(as(centralAccount, post("/api/admin/users/" + commuter.getId() + "/reset-password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void systemAdminCannotLockThemselvesOut() throws Exception {
        mockMvc.perform(as(systemAdmin, put("/api/admin/users/" + systemAdmin.getId()), "{\"profileCode\":\"COMMUTER\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(systemAdmin, post("/api/admin/users/" + systemAdmin.getId() + "/status"),
                "{\"status\":\"SUSPENDED\",\"reason\":\"oops\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(systemAdmin, post("/api/admin/users/" + systemAdmin.getId() + "/reset-password")))
                .andExpect(status().isBadRequest());
    }

    // --- B: unauthenticated and under-privileged writes ----------------------------

    @Test
    void ranksAndRoutesAreReadableWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/ranks")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].hasAccount").exists());
        mockMvc.perform(get("/api/routes")).andExpect(status().isOk());
    }

    @Test
    void anonymousCannotWriteRanksOrRoutes() throws Exception {
        mockMvc.perform(delete("/api/ranks/all")).andExpect(status().is4xxClientError());
        mockMvc.perform(json(post("/api/ranks"), rankJson("FAKE01", "Fake rank"))).andExpect(status().is4xxClientError());
        mockMvc.perform(json(post("/api/taxi-ranks"), rankJson("FAKE01", "Fake rank"))).andExpect(status().isForbidden());
        mockMvc.perform(json(post("/api/routes"), "{}")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/routes/1")).andExpect(status().isForbidden());

        assertThat(taxiRankRepository.count()).isEqualTo(2);
    }

    @Test
    void onlySystemAdminsCreateRanksAndRoutes() throws Exception {
        mockMvc.perform(as(commuter, post("/api/taxi-ranks"), rankJson("NEW001", "New rank"))).andExpect(status().isForbidden());
        mockMvc.perform(as(centralAccount, post("/api/taxi-ranks"), rankJson("NEW001", "New rank"))).andExpect(status().isForbidden());
        mockMvc.perform(as(centralAccount, post("/api/routes"), "{}")).andExpect(status().isForbidden());

        mockMvc.perform(as(systemAdmin, post("/api/taxi-ranks"), rankJson("NEW001", "New rank"))).andExpect(status().isCreated());
    }

    // --- K: rank accounts only manage their own rank --------------------------------

    @Test
    void rankAccountEditsOnlyItsOwnRank() throws Exception {
        mockMvc.perform(as(centralAccount, put("/api/taxi-ranks/" + central.getId()), rankJson("CTR001", "Central Taxi Rank (Main)")))
                .andExpect(status().isOk());
        mockMvc.perform(as(centralAccount, put("/api/taxi-ranks/" + sandton.getId()), rankJson("SCR001", "Hijacked")))
                .andExpect(status().isForbidden());
        mockMvc.perform(as(commuter, put("/api/taxi-ranks/" + central.getId()), rankJson("CTR001", "Hijacked")))
                .andExpect(status().isForbidden());

        assertThat(taxiRankRepository.findById(sandton.getId()).orElseThrow().getName()).isEqualTo("Sandton City Rank");
    }

    @Test
    void rankCodeCannotBeChanged() throws Exception {
        mockMvc.perform(as(systemAdmin, put("/api/taxi-ranks/" + central.getId()), rankJson("NEWCODE", "Central Taxi Rank")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rankAccountManagesOnlyItsOwnTerminals() throws Exception {
        String terminal = "{\"name\":\"Soweto\",\"fare\":18.5,\"travelTime\":\"40 min\",\"distance\":\"25 km\"}";
        mockMvc.perform(as(centralAccount, post("/api/taxi-ranks/" + central.getId() + "/terminals"), terminal))
                .andExpect(status().isCreated());
        mockMvc.perform(as(centralAccount, post("/api/taxi-ranks/" + sandton.getId() + "/terminals"), terminal))
                .andExpect(status().isForbidden());

        Terminal sandtonTerminal = new Terminal();
        sandtonTerminal.setName("Alexandra");
        sandtonTerminal.setFare(12.0);
        sandtonTerminal.setTravelTime("15 min");
        sandtonTerminal.setDistance("6 km");
        sandtonTerminal.setTaxiRank(sandton);
        sandtonTerminal = terminalRepository.save(sandtonTerminal);

        // Another rank's terminal addressed through our own rank's path
        mockMvc.perform(as(centralAccount, delete("/api/taxi-ranks/" + central.getId() + "/terminals/" + sandtonTerminal.getId())))
                .andExpect(status().isNotFound());
        assertThat(terminalRepository.existsById(sandtonTerminal.getId())).isTrue();
    }

    // --- C: super admin creation ----------------------------------------------------

    @Test
    void legacySuperAdminEndpointsAreGone() throws Exception {
        String body = "{\"firstName\":\"Evil\",\"lastName\":\"User\",\"email\":\"evil@test.local\","
                + "\"password\":\"Passw0rd!\",\"phoneNumber\":\"0710000099\"}";
        mockMvc.perform(json(post("/api/admin/register/super"), body)).andExpect(status().is4xxClientError());
        mockMvc.perform(json(post("/api/auth/register/super-admin"), body)).andExpect(status().is4xxClientError());

        assertThat(userRepository.findByEmailIgnoreCase("evil@test.local")).isEmpty();
    }

    @Test
    void bootstrapNeedsTokenAndOnlyWorksWithoutASystemAdmin() throws Exception {
        String body = "{\"firstName\":\"Owner\",\"lastName\":\"User\",\"email\":\"owner@test.local\","
                + "\"password\":\"Passw0rd!\",\"phoneNumber\":\"0710000010\"}";

        // A System Administrator already exists in setUp
        mockMvc.perform(json(post("/api/auth/bootstrap").header("X-Bootstrap-Token", "test-bootstrap-token"), body))
                .andExpect(status().isConflict());
        mockMvc.perform(json(post("/api/auth/bootstrap").header("X-Bootstrap-Token", "wrong"), body))
                .andExpect(status().isForbidden());

        userRepository.delete(systemAdmin);
        mockMvc.perform(json(post("/api/auth/bootstrap"), body)).andExpect(status().isForbidden());
        mockMvc.perform(json(post("/api/auth/bootstrap").header("X-Bootstrap-Token", "test-bootstrap-token"), body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.profileCode").value("SYSTEM_ADMIN"));
    }

    // --- F: account status ----------------------------------------------------------

    @Test
    void suspendedAccountsCantLogInOrUseOldTokens() throws Exception {
        commuter.setAccountStatus(AccountStatus.SUSPENDED);
        commuter.setStatusReason("Abuse reports");
        userRepository.save(commuter);

        mockMvc.perform(as(commuter, get("/api/auth/me"))).andExpect(status().isForbidden());
        mockMvc.perform(login("rider@test.local", PASSWORD)).andExpect(status().isForbidden());
        // Wrong password gets the generic answer, so the suspension isn't revealed
        mockMvc.perform(login("rider@test.local", "wrong-password")).andExpect(status().isUnauthorized());
    }

    @Test
    void flaggedAccountsStillWork() throws Exception {
        commuter.setAccountStatus(AccountStatus.FLAGGED);
        commuter.setStatusReason("Review");
        userRepository.save(commuter);

        mockMvc.perform(login("rider@test.local", PASSWORD)).andExpect(status().isOk());
        mockMvc.perform(as(commuter, get("/api/users/me"))).andExpect(status().isOk());
    }

    @Test
    void temporaryPasswordOnlyAllowsChangingIt() throws Exception {
        centralAccount.setMustChangePassword(true);
        userRepository.save(centralAccount);

        mockMvc.perform(as(centralAccount, get("/api/dashboard/rank")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("PASSWORD_CHANGE_REQUIRED"));
        mockMvc.perform(as(centralAccount, get("/api/auth/me"))).andExpect(status().isOk());

        mockMvc.perform(as(centralAccount, post("/api/auth/change-password"),
                "{\"currentPassword\":\"Passw0rd!\",\"newPassword\":\"short-pass\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(centralAccount, post("/api/auth/change-password"),
                "{\"currentPassword\":\"Passw0rd!\",\"newPassword\":\"Central-Rank-2026\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(as(centralAccount, get("/api/dashboard/rank"))).andExpect(status().isOk());
    }
}
