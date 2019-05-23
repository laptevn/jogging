package com.laptevn.jogging.weather;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;

import java.time.LocalDate;

interface WeatherClient {
    @RequestLine("GET /history.json?key={key}&q={location}&dt={date}")
    @Headers({"Content-Type: application/json"})
    Response getWeather(
            @Param("key") String key,
            @Param("location") String location,
            @Param("date") LocalDate date);
}