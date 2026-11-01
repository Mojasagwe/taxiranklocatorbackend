package com.taxirank.backend.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.taxirank.backend.models.Profile;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;

/**
 * The rank admin journey from the mockups: sign up, apply (existing rank or a new
 * one), review (request info, link, reject, approve), and the rank account's first login.
 */
public class ApplicationFlowIntegrationTest extends IntegrationTestSupport {

    private static final String PROPOSAL = "{\"proposedRank\":{\"name\":\"Bree Street Taxi Rank\","
            + "\"address\":\"Cnr Bree & Joubert St\",\"city\":\"Johannesburg\",\"province\":\"Gauteng\","
            + "\"latitude\":-26.2020,\"longitude\":28.0450,\"operatingHours\":\"04:30-21:00\",\"capacity\":40},"
            + "\"designation\":\"Rank marshal\",\"motivation\":\"Marshal here for 6 years\"}";

    private User systemAdmin;
    private User applicant;
    private TaxiRank central;

    @BeforeEach
    void setUp() {
        systemAdmin = person("admin@test.local", "0710000002", Profile.SYSTEM_ADMIN);
        applicant = person("sipho@test.local", "0725550142", Profile.COMMUTER);
        central = rank("CTR001", "Central Taxi Rank", -26.2041, 28.0473);
    }

    @Test
    void newRankProposalFromSignUpToFirstLogin() throws Exception {
        // Sign up as "Rank admin" step 1: an ordinary personal account
        JsonNode signUp = data(mockMvc.perform(json(post("/api/auth/register"),
                "{\"firstName\":\"Lerato\",\"lastName\":\"Khumalo\",\"email\":\"lerato@test.local\","
                        + "\"password\":\"Passw0rd!\",\"phoneNumber\":\"0725550188\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.profileCode").value("COMMUTER"))
                .andReturn());
        String token = "Bearer " + signUp.get("token").asText();

        // Step 2: the application
        JsonNode submitted = data(mockMvc.perform(json(post("/api/applications").header("Authorization", token), PROPOSAL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn());
        long id = submitted.get("id").asLong();

        // One open application at a time
        mockMvc.perform(json(post("/api/applications").header("Authorization", token), PROPOSAL))
                .andExpect(status().isConflict());

        // Reviewer sees it in the queue, with the nearby-rank warning
        mockMvc.perform(as(systemAdmin, get("/api/applications")))
                .andExpect(jsonPath("$.data[0].id").value(id));
        mockMvc.perform(as(systemAdmin, get("/api/applications/" + id)))
                .andExpect(jsonPath("$.data.nearbyRanks[0].code").value("CTR001"));

        // Request info → applicant sees the note → resubmits
        mockMvc.perform(as(systemAdmin, post("/api/applications/" + id + "/request-info"),
                "{\"note\":\"Please move the pin to the rank entrance\"}"))
                .andExpect(jsonPath("$.data.status").value("INFO_REQUESTED"));
        mockMvc.perform(get("/api/applications/mine").header("Authorization", token))
                .andExpect(jsonPath("$.data[0].reviewerNote").value("Please move the pin to the rank entrance"));
        mockMvc.perform(json(put("/api/applications/" + id).header("Authorization", token), PROPOSAL))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        // Approve: creates the rank and its account, shows the login once
        JsonNode approval = data(mockMvc.perform(as(systemAdmin, post("/api/applications/" + id + "/approve"), "{\"code\":\"BSR001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.application.status").value("APPROVED"))
                .andReturn());
        assertThat(approval.get("loginName").asText()).isEqualTo("BSR001");
        assertThat(taxiRankRepository.findByCode("BSR001")).isPresent();
        User account = userRepository.findByUsernameIgnoreCase("BSR001").orElseThrow();
        assertThat(account.getResponsiblePersonName()).isEqualTo("Lerato Khumalo");
        assertThat(account.isMustChangePassword()).isTrue();

        // The applicant keeps their own login, unchanged
        User person = userRepository.findByEmailIgnoreCase("lerato@test.local").orElseThrow();
        assertThat(person.hasProfile(Profile.COMMUTER)).isTrue();

        // First login with the rank code and temporary password
        mockMvc.perform(login("BSR001", approval.get("temporaryPassword").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.mustChangePassword").value(true));
    }

    @Test
    void applyingForAnExistingRank() throws Exception {
        String body = "{\"rankId\":" + central.getId() + ",\"designation\":\"Association secretary\"}";
        JsonNode submitted = data(mockMvc.perform(as(applicant, post("/api/applications"), body))
                .andExpect(status().isCreated())
                .andReturn());

        JsonNode approval = data(mockMvc.perform(as(systemAdmin, post("/api/applications/" + submitted.get("id").asLong() + "/approve")))
                .andExpect(status().isOk())
                .andReturn());
        assertThat(approval.get("loginName").asText()).isEqualTo("CTR001");

        // Rank now taken: nobody else can apply for it
        User other = person("other@test.local", "0725550111", Profile.COMMUTER);
        mockMvc.perform(as(other, post("/api/applications"), body)).andExpect(status().isConflict());
    }

    @Test
    void linkingAProposalToAnExistingRank() throws Exception {
        TaxiRank bree = rank("BRE001", "Bree Street", -26.2021, 28.0451);
        long id = data(mockMvc.perform(as(applicant, post("/api/applications"), PROPOSAL)).andReturn()).get("id").asLong();

        mockMvc.perform(as(systemAdmin, post("/api/applications/" + id + "/link"), "{\"rankId\":" + bree.getId() + "}"))
                .andExpect(jsonPath("$.data.rank.code").value("BRE001"));
        mockMvc.perform(as(systemAdmin, post("/api/applications/" + id + "/approve")))
                .andExpect(jsonPath("$.data.loginName").value("BRE001"));
        assertThat(taxiRankRepository.count()).isEqualTo(2);
    }

    @Test
    void rejectionNeedsAReasonAndIsFinal() throws Exception {
        long id = data(mockMvc.perform(as(applicant, post("/api/applications"), PROPOSAL)).andReturn()).get("id").asLong();

        mockMvc.perform(as(systemAdmin, post("/api/applications/" + id + "/reject"), "{\"note\":\"\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(systemAdmin, post("/api/applications/" + id + "/reject"), "{\"note\":\"Not a real rank\"}"))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
        mockMvc.perform(as(systemAdmin, post("/api/applications/" + id + "/approve")))
                .andExpect(status().isConflict());

        // A decided application frees the applicant to apply again
        mockMvc.perform(as(applicant, post("/api/applications"), PROPOSAL)).andExpect(status().isCreated());
    }

    @Test
    void applicantsOnlySeeTheirOwnAndCantDecide() throws Exception {
        long id = data(mockMvc.perform(as(applicant, post("/api/applications"), PROPOSAL)).andReturn()).get("id").asLong();
        User other = person("other@test.local", "0725550111", Profile.COMMUTER);

        mockMvc.perform(as(other, get("/api/applications/" + id))).andExpect(status().isNotFound());
        mockMvc.perform(as(other, put("/api/applications/" + id), PROPOSAL)).andExpect(status().isNotFound());
        mockMvc.perform(as(applicant, get("/api/applications"))).andExpect(status().isForbidden());
        mockMvc.perform(as(applicant, post("/api/applications/" + id + "/approve"))).andExpect(status().isForbidden());
        mockMvc.perform(as(applicant, get("/api/applications/" + id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nearbyRanks").doesNotExist());
    }

    @Test
    void applicantCanWithdraw() throws Exception {
        long id = data(mockMvc.perform(as(applicant, post("/api/applications"), PROPOSAL)).andReturn()).get("id").asLong();

        mockMvc.perform(as(applicant, post("/api/applications/" + id + "/withdraw")))
                .andExpect(jsonPath("$.data.status").value("WITHDRAWN"));
        mockMvc.perform(as(applicant, post("/api/applications/" + id + "/withdraw")))
                .andExpect(status().isConflict());
    }

    @Test
    void applicationMustNameOneRank() throws Exception {
        mockMvc.perform(as(applicant, post("/api/applications"), "{\"designation\":\"Marshal\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(applicant, post("/api/applications"),
                "{\"rankId\":" + central.getId() + "," + PROPOSAL.substring(1)))
                .andExpect(status().isBadRequest());
    }
}
