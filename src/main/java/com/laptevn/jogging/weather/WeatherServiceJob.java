package com.laptevn.jogging.weather;

import com.laptevn.exception.IntegrityException;
import com.laptevn.exception.OperationException;
import com.laptevn.jogging.entity.Jogging;
import com.laptevn.jogging.repository.JoggingRepository;
import feign.Response;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WeatherServiceJob {
    private final static Logger logger = LoggerFactory.getLogger(WeatherServiceJob.class);

    private final JoggingRepository joggingRepository;
    private final String weatherProviderKey;
    private final WeatherClient weatherClient;
    private final WeatherDataExtractor weatherDataExtractor;

    public WeatherServiceJob(
            JoggingRepository joggingRepository,
            @Value("${weather.provider.key}") String weatherProviderKey,
            WeatherClient weatherClient,
            WeatherDataExtractor weatherDataExtractor) {

        this.joggingRepository = joggingRepository;
        this.weatherProviderKey = weatherProviderKey;
        this.weatherClient = weatherClient;
        this.weatherDataExtractor = weatherDataExtractor;
    }

    @Scheduled(cron = "${weather.checkTime}")
    public void initializeWeatherData() {
        logger.info("Initializing weather data");

        List<Jogging> joggings = joggingRepository.findByWeatherConditionIsNull();
        logger.info("Found {} joggings to initialize", joggings.size());

        List<Jogging> updatedJoggings = new ArrayList<>();
        try {
            joggings
                    .stream()
                    .forEach(jogging -> {
                        Response weatherData = weatherClient.getWeather(
                                weatherProviderKey, jogging.getLocation(), jogging.getDate());
                        try {
                            weatherDataExtractor.extract(weatherData, jogging);
                            updatedJoggings.add(jogging);
                        } catch (OperationException e) {
                            logger.info(e.getMessage());
                        }
                    });
        } catch (IntegrityException | RetryableException e) {
            logger.error("Couldn't get weather data from a provider", e);
            return;
        }

        if (!updatedJoggings.isEmpty()) {
            joggingRepository.saveAll(updatedJoggings);
        }

        logger.info("Updated {} joggings", updatedJoggings.size());
    }
}