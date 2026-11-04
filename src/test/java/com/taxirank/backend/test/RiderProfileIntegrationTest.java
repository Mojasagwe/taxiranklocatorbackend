package com.taxirank.backend.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.enums.ApplicationStatus;
import com.taxirank.backend.enums.TripStatus;
import com.taxirank.backend.models.Profile;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.Terminal;
import com.taxirank.backend.models.Trip;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.RankApplicationRepository;
import com.taxirank.backend.repositories.TerminalRepository;
import com.taxirank.backend.repositories.TripRepository;
import com.taxirank.backend.storage.PhotoStorage;

/**
 * The rider's own account: details, password and sessions, photo, notification settings,
 * trip history and deleting the account.
 */
class RiderProfileIntegrationTest extends IntegrationTestSupport {

    @Autowired
    TerminalRepository terminalRepository;

    @Autowired
    TripRepository tripRepository;

    @Autowired
    RankApplicationRepository rankApplicationRepository;

    @Autowired
    PhotoStorage photoStorage;

    private User rider;
    private User otherRider;
    private User systemAdmin;
    private TaxiRank central;
    private Terminal soweto;

    @BeforeEach
    void setUp() {
        rider = person("rider@test.local", "0821110001", Profile.COMMUTER);
        otherRider = person("other@test.local", "0821110002", Profile.COMMUTER);
        systemAdmin = person("admin@test.local", "0821110003", Profile.SYSTEM_ADMIN);
        central = rank("CTR001", "Central Taxi Rank", -26.2041, 28.0473);
        soweto = terminal(central, "Soweto · Bara", 18.0, true);
    }

    // --- Password and sessions

    @Test
    void changingPasswordLogsOutOtherSessionsButNotThisOne() throws Exception {
        String otherPhone = token(rider);

        MvcResult result = mockMvc.perform(as(rider, post("/api/auth/change-password"),
                "{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"New-rider-pass-1\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String thisPhone = data(result).get("token").asText();

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + otherPhone))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + thisPhone))
                .andExpect(status().isOk());
        mockMvc.perform(login("rider@test.local", "New-rider-pass-1")).andExpect(status().isOk());
    }

    @Test
    void adminPasswordResetLogsTheUserOut() throws Exception {
        String riderToken = token(rider);
        mockMvc.perform(as(systemAdmin, post("/api/admin/users/" + rider.getId() + "/reset-password")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + riderToken))
                .andExpect(status().isForbidden());
    }

    // --- Details

    @Test
    void nameChangesNeedNoPasswordButContactChangesDo() throws Exception {
        mockMvc.perform(as(rider, put("/api/users/me"), "{\"firstName\":\"Lerato\",\"email\":\"RIDER@test.local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Lerato"));

        mockMvc.perform(as(rider, put("/api/users/me"), "{\"email\":\"new@test.local\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(rider, put("/api/users/me"), "{\"phoneNumber\":\"0829999999\",\"currentPassword\":\"wrong-pass\"}"))
                .andExpect(status().isBadRequest());
        assertThat(userRepository.findById(rider.getId()).orElseThrow().getPhoneNumber()).isEqualTo("0821110001");

        mockMvc.perform(as(rider, put("/api/users/me"),
                "{\"email\":\"new@test.local\",\"phoneNumber\":\"0829999999\",\"currentPassword\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("new@test.local"));
    }

    @Test
    void contactDetailsMustStayUnique() throws Exception {
        mockMvc.perform(as(rider, put("/api/users/me"),
                "{\"email\":\"other@test.local\",\"currentPassword\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void rankAccountsCantEditThemselves() throws Exception {
        User account = rankAccount(central);
        mockMvc.perform(as(account, put("/api/users/me"), "{\"firstName\":\"Renamed\"}"))
                .andExpect(status().isBadRequest());
    }

    // --- Notifications

    @Test
    void notificationSettingsAreSaved() throws Exception {
        mockMvc.perform(as(rider, get("/api/users/me/notifications")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationUpdates").value(true))
                .andExpect(jsonPath("$.data.rankChanges").value(true))
                .andExpect(jsonPath("$.data.news").value(false));

        mockMvc.perform(as(rider, put("/api/users/me/notifications"),
                "{\"applicationUpdates\":false,\"rankChanges\":true,\"news\":true}"))
                .andExpect(status().isOk());
        mockMvc.perform(as(rider, get("/api/users/me/notifications")))
                .andExpect(jsonPath("$.data.applicationUpdates").value(false))
                .andExpect(jsonPath("$.data.news").value(true));

        mockMvc.perform(as(rider, put("/api/users/me/notifications"), "{\"news\":true}"))
                .andExpect(status().isBadRequest());
    }

    // --- Photo

    @Test
    void photoIsCroppedSquareAndOnlyVisibleToOwnerAndAdmins() throws Exception {
        JsonNode user = data(mockMvc.perform(as(rider, multipart(HttpMethod.PUT, "/api/users/me/photo")
                .file(photo("photo.png", image(800, 600, "png")))))
                .andExpect(status().isOk())
                .andReturn());
        String photoUrl = user.get("photoUrl").asText();
        assertThat(photoUrl).startsWith("/api/users/" + rider.getId() + "/photo?v=");

        byte[] stored = mockMvc.perform(as(rider, get("/api/users/" + rider.getId() + "/photo")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andReturn().getResponse().getContentAsByteArray();
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(stored));
        assertThat(decoded.getWidth()).isEqualTo(512);
        assertThat(decoded.getHeight()).isEqualTo(512);

        mockMvc.perform(as(otherRider, get("/api/users/" + rider.getId() + "/photo"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/users/" + rider.getId() + "/photo")).andExpect(status().isForbidden());
        mockMvc.perform(as(systemAdmin, get("/api/users/" + rider.getId() + "/photo"))).andExpect(status().isOk());
    }

    @Test
    void newPhotoReplacesTheOldFileAndRemovingDeletesIt() throws Exception {
        mockMvc.perform(as(rider, multipart(HttpMethod.PUT, "/api/users/me/photo").file(photo("a.jpg", image(300, 300, "jpg")))))
                .andExpect(status().isOk());
        String firstKey = userRepository.findById(rider.getId()).orElseThrow().getProfilePicture();

        mockMvc.perform(as(rider, multipart(HttpMethod.PUT, "/api/users/me/photo").file(photo("b.jpg", image(300, 300, "jpg")))))
                .andExpect(status().isOk());
        String secondKey = userRepository.findById(rider.getId()).orElseThrow().getProfilePicture();
        assertThat(secondKey).isNotEqualTo(firstKey);
        assertThat(photoStorage.load(firstKey)).isEmpty();

        mockMvc.perform(as(rider, delete("/api/users/me/photo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.photoUrl").doesNotExist());
        assertThat(photoStorage.load(secondKey)).isEmpty();
        mockMvc.perform(as(rider, get("/api/users/" + rider.getId() + "/photo"))).andExpect(status().isNotFound());
    }

    @Test
    void onlyRealJpegOrPngPhotosAreAccepted() throws Exception {
        mockMvc.perform(as(rider, multipart(HttpMethod.PUT, "/api/users/me/photo")
                .file(photo("photo.jpg", "<svg onload=alert(1)>".getBytes()))))
                .andExpect(status().isBadRequest());
        // JPEG signature, garbage body
        mockMvc.perform(as(rider, multipart(HttpMethod.PUT, "/api/users/me/photo")
                .file(photo("photo.jpg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3, 4}))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(rider, multipart(HttpMethod.PUT, "/api/users/me/photo")))
                .andExpect(status().isBadRequest());
        assertThat(userRepository.findById(rider.getId()).orElseThrow().getProfilePicture()).isNull();
    }

    // --- Trips

    @Test
    void ridersLogListAndSummariseTheirTrips() throws Exception {
        Terminal pretoria = terminal(central, "Pretoria", 45.0, true);
        logTrip(rider, soweto, "TAKEN", null).andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.destination").value("Soweto · Bara"))
                .andExpect(jsonPath("$.data.fare").value(18.0))
                .andExpect(jsonPath("$.data.rank.name").value("Central Taxi Rank"));
        logTrip(rider, pretoria, "TAKEN", null).andExpect(status().isCreated());
        logTrip(rider, soweto, "PLANNED", LocalDateTime.now().plusDays(2)).andExpect(status().isCreated());
        logTrip(otherRider, soweto, "TAKEN", null).andExpect(status().isCreated());

        mockMvc.perform(as(rider, get("/api/trips"))).andExpect(jsonPath("$.data.length()").value(3));
        mockMvc.perform(as(rider, get("/api/trips").param("status", "PLANNED")))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("PLANNED"));
        mockMvc.perform(as(rider, get("/api/trips/summary")))
                .andExpect(jsonPath("$.data.tripsTaken").value(2))
                .andExpect(jsonPath("$.data.spentThisMonth").value(63.0))
                .andExpect(jsonPath("$.data.ranksUsed").value(1));
    }

    @Test
    void fareIsCopiedSoHistoryIgnoresLaterFareChanges() throws Exception {
        logTrip(rider, soweto, "TAKEN", null).andExpect(status().isCreated());
        soweto.setFare(25.0);
        terminalRepository.save(soweto);

        mockMvc.perform(as(rider, get("/api/trips"))).andExpect(jsonPath("$.data[0].fare").value(18.0));
    }

    @Test
    void tripsAreValidated() throws Exception {
        logTrip(rider, soweto, "TAKEN", LocalDateTime.now().plusDays(1)).andExpect(status().isBadRequest());
        logTrip(rider, soweto, "PLANNED", LocalDateTime.now().plusYears(2)).andExpect(status().isBadRequest());
        logTrip(rider, terminal(central, "Closed route", 10.0, false), "TAKEN", null).andExpect(status().isNotFound());
        mockMvc.perform(as(rider, post("/api/trips"), "{\"terminalId\":999999,\"status\":\"TAKEN\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(as(rider, post("/api/trips"), "{\"status\":\"TAKEN\"}")).andExpect(status().isBadRequest());
        assertThat(tripRepository.count()).isZero();
    }

    @Test
    void ridersOnlyTouchTheirOwnTrips() throws Exception {
        long tripId = data(logTrip(rider, soweto, "PLANNED", LocalDateTime.now().plusDays(1)).andReturn()).get("id").asLong();

        mockMvc.perform(as(otherRider, delete("/api/trips/" + tripId))).andExpect(status().isNotFound());
        mockMvc.perform(as(otherRider, post("/api/trips/" + tripId + "/taken"))).andExpect(status().isNotFound());
        assertThat(tripRepository.findById(tripId)).isPresent();

        mockMvc.perform(as(rider, post("/api/trips/" + tripId + "/taken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("TAKEN"));
        Trip taken = tripRepository.findById(tripId).orElseThrow();
        assertThat(taken.getTripDate()).isBeforeOrEqualTo(LocalDateTime.now());

        mockMvc.perform(as(rider, delete("/api/trips/" + tripId))).andExpect(status().isOk());
        assertThat(tripRepository.findById(tripId)).isEmpty();
    }

    @Test
    void tripsNeedTheTripsPermission() throws Exception {
        mockMvc.perform(get("/api/trips")).andExpect(status().isForbidden());
        mockMvc.perform(as(rankAccount(central), get("/api/trips"))).andExpect(status().isForbidden());
        mockMvc.perform(as(systemAdmin, post("/api/trips"), "{\"terminalId\":" + soweto.getId() + ",\"status\":\"TAKEN\"}"))
                .andExpect(status().isForbidden());
        assertThat(tripRepository.count()).isZero();
    }

    @Test
    void destinationsArePublicAndOnlyActive() throws Exception {
        terminal(central, "Closed route", 10.0, false);
        mockMvc.perform(get("/api/ranks/" + central.getId() + "/destinations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Soweto · Bara"))
                .andExpect(jsonPath("$.data[0].fare").value(18.0));
        mockMvc.perform(get("/api/ranks/999999/destinations")).andExpect(status().isNotFound());
    }

    // --- Delete account

    @Test
    void deletingNeedsTheWordDeleteAndThePassword() throws Exception {
        mockMvc.perform(as(rider, post("/api/users/me/delete"), "{\"password\":\"" + PASSWORD + "\",\"confirmation\":\"yes\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(rider, post("/api/users/me/delete"), "{\"password\":\"wrong-pass\",\"confirmation\":\"DELETE\"}"))
                .andExpect(status().isBadRequest());
        assertThat(userRepository.findById(rider.getId()).orElseThrow().getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void deletingErasesPersonalDataAndKeepsOnlyAnonymousRecords() throws Exception {
        mockMvc.perform(as(rider, multipart(HttpMethod.PUT, "/api/users/me/photo").file(photo("a.jpg", image(200, 200, "jpg")))))
                .andExpect(status().isOk());
        String photoKey = userRepository.findById(rider.getId()).orElseThrow().getProfilePicture();
        long tripId = data(logTrip(rider, soweto, "TAKEN", null).andReturn()).get("id").asLong();
        long applicationId = data(mockMvc.perform(as(rider, post("/api/applications"),
                "{\"rankId\":" + central.getId() + ",\"designation\":\"Rank marshal\",\"motivation\":\"I work here\"}"))
                .andExpect(status().isCreated()).andReturn()).get("id").asLong();
        String riderToken = token(rider);

        mockMvc.perform(as(rider, post("/api/users/me/delete"), "{\"password\":\"" + PASSWORD + "\",\"confirmation\":\"delete\"}"))
                .andExpect(status().isOk());

        User deleted = userRepository.findById(rider.getId()).orElseThrow();
        assertThat(deleted.getAccountStatus()).isEqualTo(AccountStatus.DELETED);
        assertThat(deleted.getEmail()).isNull();
        assertThat(deleted.getPhoneNumber()).isNull();
        assertThat(deleted.getLastName()).isNull();
        assertThat(deleted.getFirstName()).isEqualTo("Deleted user");
        assertThat(deleted.getProfilePicture()).isNull();
        assertThat(deleted.getDeletedAt()).isNotNull();
        assertThat(photoStorage.load(photoKey)).isEmpty();

        Trip trip = tripRepository.findById(tripId).orElseThrow();
        assertThat(trip.getUser()).isNull();
        assertThat(trip.getStatus()).isEqualTo(TripStatus.TAKEN);

        var application = rankApplicationRepository.findById(applicationId).orElseThrow();
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.WITHDRAWN);
        assertThat(application.getMotivation()).isNull();

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + riderToken)).andExpect(status().isForbidden());
        mockMvc.perform(login("rider@test.local", PASSWORD)).andExpect(status().isUnauthorized());

        // The same email and phone can sign up again
        mockMvc.perform(json(post("/api/auth/register"), "{\"firstName\":\"Lerato\",\"lastName\":\"Mokoena\","
                + "\"email\":\"rider@test.local\",\"phoneNumber\":\"0821110001\",\"password\":\"Another-pass-1\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void deletedAccountsCantBeRevivedByAnAdmin() throws Exception {
        mockMvc.perform(as(rider, post("/api/users/me/delete"), "{\"password\":\"" + PASSWORD + "\",\"confirmation\":\"DELETE\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(as(systemAdmin, post("/api/admin/users/" + rider.getId() + "/status"), "{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(as(systemAdmin, post("/api/admin/users/" + rider.getId() + "/reset-password")))
                .andExpect(status().isConflict());
        mockMvc.perform(as(systemAdmin, post("/api/admin/users/" + otherRider.getId() + "/status"),
                "{\"status\":\"DELETED\",\"reason\":\"spam\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void theLastSystemAdminCantDeleteThemselves() throws Exception {
        mockMvc.perform(as(systemAdmin, post("/api/users/me/delete"), "{\"password\":\"" + PASSWORD + "\",\"confirmation\":\"DELETE\"}"))
                .andExpect(status().isBadRequest());

        person("admin2@test.local", "0821110004", Profile.SYSTEM_ADMIN);
        mockMvc.perform(as(systemAdmin, post("/api/users/me/delete"), "{\"password\":\"" + PASSWORD + "\",\"confirmation\":\"DELETE\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void rankAccountsCantBeDeletedByThemselves() throws Exception {
        User account = rankAccount(central);
        mockMvc.perform(as(account, post("/api/users/me/delete"), "{\"password\":\"" + PASSWORD + "\",\"confirmation\":\"DELETE\"}"))
                .andExpect(status().isBadRequest());
        assertThat(userRepository.findById(account.getId()).orElseThrow().isDeleted()).isFalse();
    }

    // --- Helpers

    private Terminal terminal(TaxiRank rank, String name, double fare, boolean active) {
        Terminal terminal = new Terminal();
        terminal.setName(name);
        terminal.setFare(fare);
        terminal.setTravelTime("40 min");
        terminal.setDistance("25 km");
        terminal.setActive(active);
        terminal.setTaxiRank(rank);
        return terminalRepository.save(terminal);
    }

    private org.springframework.test.web.servlet.ResultActions logTrip(User user, Terminal terminal, String status,
            LocalDateTime when) throws Exception {
        String date = when == null ? "" : ",\"tripDate\":\"" + when.withNano(0) + "\"";
        return mockMvc.perform(as(user, post("/api/trips"),
                "{\"terminalId\":" + terminal.getId() + ",\"status\":\"" + status + "\"" + date + "}"));
    }

    private static MockMultipartFile photo(String name, byte[] bytes) {
        return new MockMultipartFile("photo", name, "application/octet-stream", bytes);
    }

    private static byte[] image(int width, int height, String format) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(0xE3B040));
        g.fillRect(0, 0, width, height);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out.toByteArray();
    }
}
