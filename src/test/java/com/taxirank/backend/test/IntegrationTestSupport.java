package com.taxirank.backend.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxirank.backend.models.Profile;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.ProfileRepository;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.security.JwtTokenProvider;

/**
 * Base for end-to-end tests: full application context on H2 with the real security
 * chain (JWT filter, URL rules, @PreAuthorize). Profiles come from src/test/resources/data.sql.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.bootstrap.super-admin-token=test-bootstrap-token")
@Transactional
abstract class IntegrationTestSupport {

    static final String PASSWORD = "Passw0rd!";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    TaxiRankRepository taxiRankRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;

    User person(String email, String phone, String profileCode) {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName(profileCode);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setProfile(profileRepository.findByCode(profileCode).orElseThrow());
        return userRepository.save(user);
    }

    TaxiRank rank(String code, String name, Double latitude, Double longitude) {
        TaxiRank rank = new TaxiRank();
        rank.setName(name);
        rank.setCode(code);
        rank.setAddress("1 Main St");
        rank.setCity("Johannesburg");
        rank.setProvince("Gauteng");
        rank.setLatitude(latitude);
        rank.setLongitude(longitude);
        rank.setCapacity(30);
        rank.setOperatingHours("05:00-21:00");
        rank.setIsActive(true);
        return taxiRankRepository.save(rank);
    }

    User rankAccount(TaxiRank rank) {
        User account = new User();
        account.setFirstName(rank.getName());
        account.setUsername(rank.getCode());
        account.setRank(rank);
        account.setResponsiblePersonName("Thabo Molefe");
        account.setResponsiblePersonPhone("0825550199");
        account.setPassword(passwordEncoder.encode(PASSWORD));
        account.setProfile(profileRepository.findByCode(Profile.RANK_ADMIN).orElseThrow());
        return userRepository.save(account);
    }

    MockHttpServletRequestBuilder as(User user, MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + tokenProvider.generateToken(user.getId()));
    }

    MockHttpServletRequestBuilder as(User user, MockHttpServletRequestBuilder request, Object body) throws Exception {
        return json(as(user, request), body);
    }

    MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return request.contentType(MediaType.APPLICATION_JSON)
                .content(body instanceof String s ? s : objectMapper.writeValueAsString(body));
    }

    MockHttpServletRequestBuilder login(String identifier, String password) throws Exception {
        return json(post("/api/auth/login"), "{\"identifier\":\"" + identifier + "\",\"password\":\"" + password + "\"}");
    }

    JsonNode data(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }

    static String rankJson(String code, String name) {
        return "{\"name\":\"" + name + "\",\"code\":\"" + code + "\",\"address\":\"1 Main St\",\"city\":\"Johannesburg\","
                + "\"province\":\"Gauteng\",\"capacity\":30,\"operatingHours\":\"05:00-21:00\"}";
    }
}
