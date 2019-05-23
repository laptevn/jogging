package com.laptevn.jogging.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laptevn.exception.IntegrityException;
import com.laptevn.exception.OperationException;
import com.laptevn.jogging.entity.Jogging;
import feign.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
class WeatherDataExtractor {
    private final static String NOT_SUPPORTED_JSON_FORMAT = "This JSON format is not supported. Missing '%s' property";
    private final static String FORECAST_PROPERTY = "forecast";
    private final static String FORECAST_DAY_PROPERTY = "forecastday";
    private final static String DAY_PROPERTY = "day";
    private final static String TEMPERATURE_PROPERTY = "avgtemp_c";
    private final static String CONDITION_PROPERTY = "condition";
    private final static String TEXT_PROPERTY = "text";
    private final static String ERROR_PROPERTY = "error";
    private final static String MESSAGE_PROPERTY = "message";

    private final ObjectMapper objectMapper;

    public WeatherDataExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void extract(Response weatherData, Jogging joggingToUpdate) {
        JsonNode rootNode;
        try (InputStream stream = weatherData.body().asInputStream()) {
            rootNode = objectMapper.readTree(stream);
        } catch (IOException e) {
            throw new IntegrityException("Weather data is not JSON");
        }

        JsonNode errorNode = rootNode.get(ERROR_PROPERTY);
        if (errorNode != null) {
            handleError(errorNode);
        }

        JsonNode forecastNode = rootNode.get(FORECAST_PROPERTY);
        if (forecastNode == null) {
            throw new IntegrityException(String.format(NOT_SUPPORTED_JSON_FORMAT, FORECAST_PROPERTY));
        }

        JsonNode forecastDayNode = forecastNode.get(FORECAST_DAY_PROPERTY);
        if (forecastDayNode == null) {
            throw new IntegrityException(String.format(NOT_SUPPORTED_JSON_FORMAT, FORECAST_DAY_PROPERTY));
        }

        JsonNode exactDayNode = forecastDayNode.get(0);
        if (exactDayNode == null) {
            throw new IntegrityException(String.format(NOT_SUPPORTED_JSON_FORMAT, "exact day"));
        }

        JsonNode dayNode = exactDayNode.get(DAY_PROPERTY);
        if (dayNode == null) {
            throw new IntegrityException(String.format(NOT_SUPPORTED_JSON_FORMAT, DAY_PROPERTY));
        }

        JsonNode temperatureNode = dayNode.get(TEMPERATURE_PROPERTY);
        if (temperatureNode == null || !temperatureNode.isValueNode()) {
            throw new IntegrityException(String.format(NOT_SUPPORTED_JSON_FORMAT, TEMPERATURE_PROPERTY));
        }

        joggingToUpdate.setAverageTemperature(temperatureNode.asText());

        JsonNode conditionNode = dayNode.get(CONDITION_PROPERTY);
        if (conditionNode == null) {
            throw new IntegrityException(String.format(NOT_SUPPORTED_JSON_FORMAT, CONDITION_PROPERTY));
        }

        JsonNode textNode = conditionNode.get(TEXT_PROPERTY);
        if (textNode == null || !textNode.isValueNode()) {
            throw new IntegrityException(String.format(NOT_SUPPORTED_JSON_FORMAT, TEXT_PROPERTY));
        }

        joggingToUpdate.setWeatherCondition(textNode.asText());
    }

    private static void handleError(JsonNode errorNode) {
        JsonNode messageNode = errorNode.get(MESSAGE_PROPERTY);
        if (messageNode == null || !messageNode.isValueNode()) {
            throw new IntegrityException(String.format(NOT_SUPPORTED_JSON_FORMAT, MESSAGE_PROPERTY));
        }

        throw new OperationException(messageNode.asText());
    }
}