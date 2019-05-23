package com.laptevn.jogging.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laptevn.exception.IntegrityException;
import com.laptevn.exception.OperationException;
import com.laptevn.jogging.entity.Jogging;
import feign.Request;
import feign.Response;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class WeatherDataExtractorTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void notJson() throws IOException {
        expectedException.expect(IntegrityException.class);
        expectedException.expectMessage("Weather data is not JSON");

        extract("not json");
    }

    @Test
    public void errorWithoutMessage() throws IOException {
        verifyMissingPropertyException("message");

        extract("{\"error\" : {\"code\" : \"1\"}}");
    }

    @Test
    public void errorWithMessage() throws IOException {
        expectedException.expect(OperationException.class);
        expectedException.expectMessage("test error");

        extract("{\"error\" : {\"message\" : \"test error\"}}");
    }

    @Test
    public void noForecast() throws IOException {
        verifyMissingPropertyException("forecast");

        extract("{\"something\" : \"1\"}");
    }

    @Test
    public void noForecastDay() throws IOException {
        verifyMissingPropertyException("forecastday");

        extract("{\"forecast\" : {\"something\" : \"1\"}}");
    }

    @Test
    public void noExactDay() throws IOException {
        verifyMissingPropertyException("exact day");

        extract("{\"forecast\" : {\"forecastday\" : []}}");
    }

    @Test
    public void noDay() throws IOException {
        verifyMissingPropertyException("day");

        extract("{\"forecast\" : {\"forecastday\" : [{\"something\" : \"1\"}]}}");
    }

    @Test
    public void noTemperature() throws IOException {
        verifyMissingPropertyException("avgtemp_c");

        extract("{\"forecast\" : {\"forecastday\" : [{\"day\" : {\"something\" : \"1\"}}]}}");
    }

    @Test
    public void noCondition() throws IOException {
        verifyMissingPropertyException("condition");

        extract("{\"forecast\" : {\"forecastday\" : [{\"day\" : {\"avgtemp_c\" : \"1.2\"}}]}}");
    }

    @Test
    public void noText() throws IOException {
        verifyMissingPropertyException("text");

        extract("{\"forecast\" : {\"forecastday\" : [{\"day\" : {\"avgtemp_c\" : \"1.2\", \"condition\" : {}}}]}}");
    }

    @Test
    public void correctFormat() throws IOException {
        Jogging jogging = extract(
                "{\"forecast\" : {\"forecastday\" : [{\"day\" : {\"avgtemp_c\" : \"1.2\", \"condition\" : {\"text\" : \"snow\"}}}]}}");
        assertEquals("Invalid temperature", "1.2", jogging.getAverageTemperature());
        assertEquals("Invalid condition", "snow", jogging.getWeatherCondition());
    }

    private void verifyMissingPropertyException(String propertyName) {
        expectedException.expect(IntegrityException.class);
        expectedException.expectMessage(
                String.format("This JSON format is not supported. Missing '%s' property", propertyName));
    }

    private static Jogging extract(String content) throws IOException {
        Jogging jogging = new Jogging();
        new WeatherDataExtractor(new ObjectMapper()).extract(createResponse(content), jogging);
        return jogging;
    }

    private static Response createResponse(String content) throws IOException {
        return Response
                .builder()
                .request(Request.create(Request.HttpMethod.GET, "", new HashMap<>(), null))
                .body(content, Charset.defaultCharset())
                .build();
    }
}