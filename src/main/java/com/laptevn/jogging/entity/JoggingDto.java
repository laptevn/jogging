package com.laptevn.jogging.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.laptevn.ErrorMessages;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public class JoggingDto {
    private final static String ISO_DATE_FORMAT = "yyyy-MM-dd";
    private final static String ISO_TIME_FORMAT = "HH:mm:ss";
    private final static String NO_DATA = "No data provided. Please repeat your request later.";

    private Integer id;

    @NotNull
    @JsonFormat(pattern = ISO_DATE_FORMAT)
    private LocalDate date;

    @NotNull
    @Min(1)
    private Integer distance;

    @NotNull
    @JsonFormat(pattern = ISO_TIME_FORMAT)
    private LocalTime time;

    @NotNull
    @Size(min = 1, message = ErrorMessages.EMPTY_LOCATION)
    private String location;

    private String averageTemperature;
    private String weatherCondition;

    public LocalDate getDate() {
        return date;
    }

    public JoggingDto setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public Integer getDistance() {
        return distance;
    }

    public JoggingDto setDistance(Integer distance) {
        this.distance = distance;
        return this;
    }

    public LocalTime getTime() {
        return time;
    }

    public JoggingDto setTime(LocalTime time) {
        this.time = time;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public JoggingDto setLocation(String location) {
        this.location = location;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public JoggingDto setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getAverageTemperature() {
        return averageTemperature;
    }

    public JoggingDto setAverageTemperature(String averageTemperature) {
        this.averageTemperature = averageTemperature;
        return this;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public JoggingDto setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
        return this;
    }

    @Override
    public String toString() {
        return "JoggingDto{" +
                "id=" + id +
                ", date=" + date +
                ", distance=" + distance +
                ", time=" + time +
                ", location='" + location + '\'' +
                ", averageTemperature='" + averageTemperature + '\'' +
                ", weatherCondition='" + weatherCondition + '\'' +
                '}';
    }

    public static JoggingDto create(Jogging jogging) {
        return new JoggingDto()
                .setId(jogging.getId())
                .setLocation(jogging.getLocation())
                .setDate(jogging.getDate())
                .setDistance(jogging.getDistance())
                .setTime(jogging.getTime())
                .setAverageTemperature(jogging.getAverageTemperature() == null ? NO_DATA : jogging.getAverageTemperature())
                .setWeatherCondition(jogging.getWeatherCondition() == null ? NO_DATA : jogging.getWeatherCondition());
    }
}