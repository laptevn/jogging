package com.laptevn.jogging.weather;

import com.laptevn.exception.IntegrityException;
import com.laptevn.jogging.entity.Jogging;
import com.laptevn.jogging.repository.JoggingRepository;
import feign.Request;
import feign.RetryableException;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WeatherServiceJobTest {
    @Test
    public void nothingToUpdate() {
        JoggingRepository repository = EasyMock.mock(JoggingRepository.class);
        EasyMock.expect(repository.findByWeatherConditionIsNull()).andReturn(new ArrayList<>());
        EasyMock.replay(repository);

        new WeatherServiceJob(repository, null, null, null).initializeWeatherData();
    }

    @Test
    public void noConnectionToProvider() {
        JoggingRepository repository = EasyMock.mock(JoggingRepository.class);
        EasyMock.expect(repository.findByWeatherConditionIsNull()).andReturn(Arrays.asList(new Jogging()));

        WeatherClient weatherClient = EasyMock.mock(WeatherClient.class);
        EasyMock.expect(weatherClient.getWeather(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyObject()))
                .andThrow(new RetryableException(0, "test", Request.HttpMethod.GET, new Date()));

        EasyMock.replay(repository, weatherClient);

        new WeatherServiceJob(repository, null, weatherClient, null).initializeWeatherData();
    }

    @Test
    public void invalidProviderResponse() {
        JoggingRepository repository = EasyMock.mock(JoggingRepository.class);
        EasyMock.expect(repository.findByWeatherConditionIsNull()).andReturn(Arrays.asList(new Jogging()));

        WeatherClient weatherClient = EasyMock.mock(WeatherClient.class);
        EasyMock.expect(weatherClient.getWeather(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyObject()))
                .andReturn(null);

        WeatherDataExtractor extractor = EasyMock.mock(WeatherDataExtractor.class);
        extractor.extract(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expectLastCall().andThrow(new IntegrityException("test"));

        EasyMock.replay(repository, weatherClient, extractor);

        new WeatherServiceJob(repository, null, weatherClient, extractor).initializeWeatherData();
    }

    @Test
    public void correctUpdate() {
        JoggingRepository repository = EasyMock.mock(JoggingRepository.class);
        List<Jogging> joggings = Arrays.asList(new Jogging(), new Jogging());
        EasyMock.expect(repository.findByWeatherConditionIsNull()).andReturn(joggings);
        EasyMock.expect(repository.saveAll(EasyMock.anyObject())).andAnswer(() -> {
            List<Jogging> updatedJoggings = (List<Jogging>) EasyMock.getCurrentArguments()[0];
            assertEquals(joggings.size(), updatedJoggings.size());
            return updatedJoggings;
        });

        WeatherClient weatherClient = EasyMock.mock(WeatherClient.class);
        EasyMock.expect(weatherClient.getWeather(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyObject()))
                .andReturn(null).times(joggings.size());

        WeatherDataExtractor extractor = EasyMock.mock(WeatherDataExtractor.class);
        extractor.extract(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expectLastCall().times(joggings.size());

        EasyMock.replay(repository, weatherClient, extractor);

        new WeatherServiceJob(repository, null, weatherClient, extractor).initializeWeatherData();
    }
}