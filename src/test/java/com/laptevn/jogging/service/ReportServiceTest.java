package com.laptevn.jogging.service;

import com.laptevn.auth.entity.User;
import com.laptevn.auth.repository.UserRepository;
import com.laptevn.jogging.entity.Jogging;
import com.laptevn.jogging.entity.ReportDto;
import com.laptevn.jogging.repository.JoggingRepository;
import org.easymock.EasyMock;
import org.junit.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class ReportServiceTest {
    @Test
    public void reportForNoEntries() {
        ReportDto report = createService(new ArrayList<>()).generateReport(null);
        assertEquals(0, report.getAverageDistance(), 0);
        assertEquals(0, report.getAverageSpeed(), 0);
    }

    @Test
    public void report() {
        ReportDto report = createService(Arrays.asList(
                new Jogging()
                        .setDistance(1000)
                        .setTime(LocalTime.parse("00:03:14")),
                new Jogging()
                        .setDistance(3000)
                        .setTime(LocalTime.parse("00:32:54")),
                new Jogging()
                        .setDistance(2600)
                        .setTime(LocalTime.parse("00:26:20"))
        )).generateReport(null);

        assertEquals(2200, report.getAverageDistance(), 0);
        assertEquals(2.77, report.getAverageSpeed(), 0.01);
    }

    private static ReportService createService(List<Jogging> joggings) {
        UserRepository userRepository = EasyMock.mock(UserRepository.class);
        EasyMock.expect(userRepository.findByNameIgnoreCase(EasyMock.anyString())).andReturn(Optional.of(new User()));

        JoggingRepository joggingRepository = EasyMock.mock(JoggingRepository.class);
        EasyMock.expect(joggingRepository.findByDateBetweenAndUser(EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject()))
                .andReturn(joggings);

        EasyMock.replay(userRepository, joggingRepository);
        return new ReportService(joggingRepository, userRepository);
    }
}