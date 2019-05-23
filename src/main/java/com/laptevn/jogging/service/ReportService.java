package com.laptevn.jogging.service;

import com.laptevn.auth.repository.UserRepository;
import com.laptevn.jogging.entity.Jogging;
import com.laptevn.jogging.entity.ReportDto;
import com.laptevn.jogging.repository.JoggingRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Component
public class ReportService {
    private final JoggingRepository joggingRepository;
    private final UserRepository userRepository;

    public ReportService(JoggingRepository joggingRepository, UserRepository userRepository) {
        this.joggingRepository = joggingRepository;
        this.userRepository = userRepository;
    }

    public ReportDto generateReport(String userName) {
        LocalDate rightBound = LocalDate.now();
        LocalDate leftBound = rightBound.minus(Period.ofWeeks(1));
        List<Jogging> weeklyJoggings = joggingRepository.findByDateBetweenAndUser(
                leftBound, rightBound, JoggingService.getUser(userRepository, userName));

        return new ReportDto(calculateAverageSpeed(weeklyJoggings), calculateAverageDistance(weeklyJoggings));
    }

    private static double calculateAverageDistance(List<Jogging> joggings) {
        return joggings
                .stream()
                .mapToDouble(Jogging::getDistance)
                .average()
                .orElse(0);
    }

    private static double calculateAverageSpeed(List<Jogging> joggings) {
        return joggings
                .stream()
                .mapToDouble(jogging -> (double) jogging.getDistance() / jogging.getTime().toSecondOfDay())
                .average()
                .orElse(0);
    }
}