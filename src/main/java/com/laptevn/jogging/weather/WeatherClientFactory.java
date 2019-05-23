package com.laptevn.jogging.weather;

import feign.Feign;
import feign.gson.GsonDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class WeatherClientFactory {
    @Bean
    public WeatherClient createWeatherClient(@Value("${weather.provider.url}") String providerUrl) {
        return Feign.builder()
                .decoder(new GsonDecoder())
                .target(WeatherClient.class, providerUrl);
    }
}