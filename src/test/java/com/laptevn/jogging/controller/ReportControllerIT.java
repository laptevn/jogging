package com.laptevn.jogging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laptevn.jogging.entity.JoggingDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class ReportControllerIT {
    private MockMvc client;
    private ObjectMapper objectMapper;

    @Autowired
    public void setClient(MockMvc client) {
        this.client = client;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void reportForbidden() throws Exception {
        client.perform(get("/reports/").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user1")
    public void reportNonEmpty() throws Exception {
        LocalDate now = LocalDate.now();

        List<JoggingDto> joggings = Arrays.asList(
                new JoggingDto()
                        .setTime(LocalTime.parse("00:00:10"))
                        .setLocation("Tokyo")
                        .setDistance(100)
                        .setDate(now.minus(Period.ofDays(8))),
                new JoggingDto()
                        .setTime(LocalTime.parse("00:04:44"))
                        .setLocation("New York")
                        .setDistance(1000)
                        .setDate(now.minus(Period.ofDays(6))),
                new JoggingDto()
                        .setTime(LocalTime.parse("00:41:50"))
                        .setLocation("Florida")
                        .setDistance(5000)
                        .setDate(now.minus(Period.ofDays(3))),
                new JoggingDto()
                        .setTime(LocalTime.parse("01:15:54"))
                        .setLocation("Tokyo")
                        .setDistance(10000)
                        .setDate(now),
                new JoggingDto()
                        .setTime(LocalTime.parse("04:43:38"))
                        .setLocation("Moscow")
                        .setDistance(50000)
                        .setDate(now.plus(Period.ofDays(1))));

        List<String> locations = joggings
                .stream()
                .map(joggingDto -> {
                    try {
                        return JoggingControllerIT.createJogging(joggingDto, client, objectMapper);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        try {
            client.perform(get("/reports/").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.averageSpeed").value(2.57))
                    .andExpect(jsonPath("$.averageDistance").value(5333.33));
        } finally {
            locations.forEach(location -> {
                try {
                    JoggingControllerIT.deleteJogging(location, client);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Test
    @WithMockUser(username = "user1")
    public void reportEmpty() throws Exception {
        client.perform(get("/reports/").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSpeed").value(0))
                .andExpect(jsonPath("$.averageDistance").value(0));
    }
}