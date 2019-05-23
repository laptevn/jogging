package com.laptevn.jogging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laptevn.ErrorDto;
import com.laptevn.jogging.entity.JoggingDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class JoggingControllerIT {
    private final static JoggingDto PREDEFINED_JOGGING = new JoggingDto()
            .setTime(LocalTime.parse("00:14:44"))
            .setLocation("Paris")
            .setDistance(100)
            .setDate(LocalDate.parse("2019-03-28"));

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
    public void getJoggingsForbidden() throws Exception {
        client.perform(get("/joggings/").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "unknown_user")
    public void getJoggingWithUnknownUser() throws Exception {
        ErrorDto errorDto = new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, "'unknown_user' user doesn't exist");

        client.perform(get("/joggings/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(objectMapper.writeValueAsString(errorDto)));
    }

    @Test
    @WithMockUser(username = "user1")
    public void getNotExistingJogging() throws Exception {
        client.perform(get("/joggings/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user1")
    public void getExistingJogging() throws Exception {
        JoggingDto joggingDto = new JoggingDto()
                .setTime(LocalTime.now())
                .setLocation("Istanbul")
                .setDistance(5000)
                .setDate(LocalDate.now());

        String joggingLocation = createJogging(joggingDto);

        try {
            client.perform(get(joggingLocation).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.location").value(joggingDto.getLocation()));
        } finally {
            deleteJogging(joggingLocation);
        }
    }

    @Test
    @WithMockUser(username = "user1")
    public void createJoggingWithInvalidDate() throws Exception {
        createJoggingWithInvalidContent("{\n" +
                "\t\"date\": \"2019-03-28T20:09:29+00:00\",\n" +
                "\t\"distance\": 2000,\n" +
                "\t\"time\": \"00:24:44\",\n" +
                "\t\"location\": \"London\"\n" +
                "}");
    }

    @Test
    @WithMockUser(username = "user1")
    public void createJoggingWithInvalidTime() throws Exception {
        createJoggingWithInvalidContent("{\n" +
                "\t\"date\": \"2019-03-28\",\n" +
                "\t\"distance\": 2000,\n" +
                "\t\"time\": \"00:24:44.123\",\n" +
                "\t\"location\": \"London\"\n" +
                "}");
    }

    @Test
    @WithMockUser(username = "user1")
    public void createJoggingWithInvalidDistance() throws Exception {
        createJoggingWithInvalidContent("{\n" +
                "\t\"date\": \"2019-03-28\",\n" +
                "\t\"distance\": 0,\n" +
                "\t\"time\": \"00:24:44\",\n" +
                "\t\"location\": \"London\"\n" +
                "}");
    }

    @Test
    @WithMockUser(username = "user1")
    public void createJoggingWithInvalidLocation() throws Exception {
        createJoggingWithInvalidContent("{\n" +
                "\t\"date\": \"2019-03-28\",\n" +
                "\t\"distance\": 100,\n" +
                "\t\"time\": \"00:24:44\",\n" +
                "\t\"location\": \"\"\n" +
                "}");
    }

    @Test
    @WithMockUser(username = "user1")
    public void deleteNotExistingJogging() throws Exception {
        client.perform(
                delete("/joggings/777"))
                .andExpect(status().isNotFound());
    }

    private void createJoggingWithInvalidContent(String jogging) throws Exception {
        client.perform(
                post("/joggings/")
                        .content(jogging)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private String createJogging(JoggingDto joggingDto) throws Exception {
        return createJogging(joggingDto, client, objectMapper);
    }

    public static String createJogging(JoggingDto joggingDto, MockMvc client, ObjectMapper objectMapper) throws Exception {
        MvcResult result = client.perform(
                post("/joggings/")
                        .content(objectMapper.writeValueAsString(joggingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String joggingLocation = result.getResponse().getHeader("Location");
        assertNotNull(joggingLocation);
        assertFalse(joggingLocation.isEmpty());
        return joggingLocation;
    }

    private void deleteJogging(String joggingLocation) throws Exception {
        deleteJogging(joggingLocation, client);
    }

    public static void deleteJogging(String joggingLocation, MockMvc client) throws Exception {
        client.perform(
                delete(joggingLocation))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user1")
    public void updateJogging() throws Exception {
        JoggingDto joggingDto = new JoggingDto()
                .setTime(LocalTime.now())
                .setLocation("Tokyo")
                .setDistance(1000)
                .setDate(LocalDate.now());

        MvcResult result = client.perform(
                put("/joggings/666")
                        .content(objectMapper.writeValueAsString(joggingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String joggingLocation = result.getResponse().getHeader("Location");
        assertNotNull(joggingLocation);
        assertFalse(joggingLocation.isEmpty());

        try {
            joggingDto.setLocation("Sahalin");
            client.perform(
                    put(joggingLocation)
                            .content(objectMapper.writeValueAsString(joggingDto))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            client.perform(get(joggingLocation).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.location").value(joggingDto.getLocation()));
        } finally {
            deleteJogging(joggingLocation);
        }
    }

    @Test
    @WithMockUser(username = "user1")
    public void getJoggingsInvalidPageIndex() throws Exception {
        client.perform(get("/joggings/?page=0").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user1")
    public void getJoggingsInvalidPageSize() throws Exception {
        client.perform(get("/joggings/?per_page=0").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user2")
    public void getPredefinedJoggingByOwner() throws Exception {
        client.perform(get("/joggings/99999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value(PREDEFINED_JOGGING.getLocation()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void getPredefinedJoggingByAdmin() throws Exception {
        client.perform(get("/joggings/99999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value(PREDEFINED_JOGGING.getLocation()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deletePredefinedJoggingByAdmin() throws Exception {
        deleteJogging("/joggings/99998");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteNotExistingJoggingByAdmin() throws Exception {
        client.perform(
                delete("/joggings/666"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updatePredefinedJoggingByAdmin() throws Exception {
        JoggingDto joggingDto = new JoggingDto()
                .setLocation(PREDEFINED_JOGGING.getLocation() + "!")
                .setDate(PREDEFINED_JOGGING.getDate())
                .setDistance(PREDEFINED_JOGGING.getDistance())
                .setTime(PREDEFINED_JOGGING.getTime());

        client.perform(
                put("/joggings/99998")
                        .content(objectMapper.writeValueAsString(joggingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void getJoggingsByAdmin() throws Exception {
        client.perform(
                get("/joggings/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(PREDEFINED_JOGGING.getLocation())));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void getJoggingsByAdminWithPaging() throws Exception {
        client.perform(
                get("/joggings/?per_page=2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(PREDEFINED_JOGGING.getLocation())));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void getJoggingsByAdminWithFiltering() throws Exception {
        client.perform(
                get("/joggings/")
                        .content("location eq 'Paris'")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[*]['location']").value(PREDEFINED_JOGGING.getLocation()));
    }

    @Test
    @WithMockUser(username = "user1")
    public void getAllJoggings() throws Exception {
        List<JoggingDto> joggings = Arrays.asList(
                new JoggingDto()
                        .setTime(LocalTime.parse("00:14:44"))
                        .setLocation("Tokyo")
                        .setDistance(100)
                        .setDate(LocalDate.parse("2019-03-28")),
                new JoggingDto()
                        .setTime(LocalTime.parse("00:03:00"))
                        .setLocation("Kongo")
                        .setDistance(1000)
                        .setDate(LocalDate.parse("2015-12-01")),
                new JoggingDto()
                        .setTime(LocalTime.parse("01:03:15"))
                        .setLocation("New York")
                        .setDistance(10000)
                        .setDate(LocalDate.parse("2019-01-01")));

        List<String> locations = joggings
                .stream()
                .map(joggingDto -> {
                    try {
                        return createJogging(joggingDto);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        try {
            client.perform(get("/joggings/").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(joggings.size()))
                    .andExpect(jsonPath("$[*]['location']", containsInAnyOrder(
                            joggings
                                    .stream()
                                    .map(jogging -> jogging.getLocation())
                                    .toArray(String[]::new))));

            client.perform(get("/joggings/?per_page=1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[*]['location']").value(joggings.get(0).getLocation()));

            client.perform(get("/joggings/?page=2&per_page=1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[*]['location']").value(joggings.get(1).getLocation()));

            client.perform(get("/joggings/?page=10&per_page=1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        } finally {
            locations.forEach(location -> {
                try {
                    deleteJogging(location);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Test
    @WithMockUser(username = "user1")
    public void getJoggingsInvalidDateFilter() throws Exception {
        client.perform(get("/joggings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("date eq '2018-20-20'"))
                .andExpect(status().isBadRequest());

        client.perform(get("/joggings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("date eq '2018-021-10'"))
                .andExpect(status().isBadRequest());

        client.perform(get("/joggings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("date eq 2018-02-10"))
                .andExpect(status().isBadRequest());

        client.perform(get("/joggings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("date eq '20180210'"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user1")
    public void getJoggingsInvalidTimeFilter() throws Exception {
        client.perform(get("/joggings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("time eq '10:05:06.564'"))
                .andExpect(status().isBadRequest());

        client.perform(get("/joggings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("time eq '10:05'"))
                .andExpect(status().isBadRequest());

        client.perform(get("/joggings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("time eq 10:05:06"))
                .andExpect(status().isBadRequest());

        client.perform(get("/joggings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("time eq '100506'"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user1")
    public void getJoggingsDoubleFiltering() throws Exception {
        client.perform(get("/joggings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("averageTemperature eq '29.5'"))
                .andExpect(status().isOk());

        client.perform(get("/joggings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("averageTemperature eq '-29.5'"))
                .andExpect(status().isOk());

        client.perform(get("/joggings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("weatherCondition eq 'partially, cloudy'"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1")
    public void getAllJoggingsWithFiltering() throws Exception {
        List<JoggingDto> joggings = Arrays.asList(
                new JoggingDto()
                        .setTime(LocalTime.parse("00:14:44"))
                        .setLocation("Tokyo")
                        .setDistance(100)
                        .setDate(LocalDate.parse("2019-03-28")),
                new JoggingDto()
                        .setTime(LocalTime.parse("01:14:44"))
                        .setLocation("Kyoto")
                        .setDistance(10000)
                        .setDate(LocalDate.parse("2019-03-28")),
                new JoggingDto()
                        .setTime(LocalTime.parse("10:00:03"))
                        .setLocation("Kyoto")
                        .setDistance(5000)
                        .setDate(LocalDate.parse("2019-03-28")),
                new JoggingDto()
                        .setTime(LocalTime.parse("00:03:00"))
                        .setLocation("Kongo")
                        .setDistance(1000)
                        .setDate(LocalDate.parse("2015-12-01")),
                new JoggingDto()
                        .setTime(LocalTime.parse("01:03:15"))
                        .setLocation("New York")
                        .setDistance(10000)
                        .setDate(LocalDate.parse("2019-01-01")));

        List<String> locations = joggings
                .stream()
                .map(joggingDto -> {
                    try {
                        return createJogging(joggingDto);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        try {
            client.perform(get("/joggings/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("(date eq '2019-03-28') AND ((distance gt 1000) AND (distance lt 10000))"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[*]['location']").value(joggings.get(2).getLocation()));

            client.perform(get("/joggings/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("(date eq '2019-03-28') AND ((distance gt 1000) OR (distance lt 10000))"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[*]['location']", containsInAnyOrder(
                            joggings.get(0).getLocation(), joggings.get(1).getLocation(), joggings.get(2).getLocation())));

            client.perform(get("/joggings/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("date ne '2019-03-28'"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[*]['location']", containsInAnyOrder(
                            joggings.get(3).getLocation(), joggings.get(4).getLocation())));

            client.perform(get("/joggings/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("time lt '01:00:00'"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[*]['location']", containsInAnyOrder(
                            joggings.get(0).getLocation(), joggings.get(3).getLocation())));

            client.perform(get("/joggings/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("time gt '01:00:00'"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[*]['location']", containsInAnyOrder(
                            joggings.get(1).getLocation(), joggings.get(2).getLocation(), joggings.get(4).getLocation())));

            client.perform(get("/joggings/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("location eq 'Kyoto'"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[*]['location']", containsInAnyOrder(
                            joggings.get(1).getLocation(), joggings.get(2).getLocation())));
        } finally {
            locations.forEach(location -> {
                try {
                    deleteJogging(location);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}