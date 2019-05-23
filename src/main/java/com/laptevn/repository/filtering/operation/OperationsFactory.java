package com.laptevn.repository.filtering.operation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class OperationsFactory {
    @Bean
    public Map<OperationType, Operation> createOperations() {
        return Arrays.asList(
                new EqualOperation(),
                new NotEqualOperation(),
                new GreaterThanOperation(),
                new LessThanOperation())
                .stream()
                .collect(Collectors.toMap(Operation::getOperationType, operation -> operation));
    }
}