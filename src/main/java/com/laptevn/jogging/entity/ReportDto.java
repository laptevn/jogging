package com.laptevn.jogging.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class ReportDto {
    @JsonSerialize(using = DoubleSerializer.class)
    private final double averageSpeed;

    @JsonSerialize(using = DoubleSerializer.class)
    private final double averageDistance;

    public ReportDto(double averageSpeed, double averageDistance) {
        this.averageSpeed = averageSpeed;
        this.averageDistance = averageDistance;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public double getAverageDistance() {
        return averageDistance;
    }

    @Override
    public String toString() {
        return "ReportDto{" +
                "averageSpeed=" + averageSpeed +
                ", averageDistance=" + averageDistance +
                '}';
    }
}