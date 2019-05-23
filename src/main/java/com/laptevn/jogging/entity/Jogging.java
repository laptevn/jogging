package com.laptevn.jogging.entity;

import com.laptevn.auth.entity.User;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Jogging {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Version
    @Column(nullable = false)
    private Integer version;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer distance;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private String location;

    private String averageTemperature;
    private String weatherCondition;

    public Integer getId() {
        return id;
    }

    public Jogging setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public Jogging setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Jogging setUser(User user) {
        this.user = user;
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public Jogging setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public Integer getDistance() {
        return distance;
    }

    public Jogging setDistance(Integer distance) {
        this.distance = distance;
        return this;
    }

    public LocalTime getTime() {
        return time;
    }

    public Jogging setTime(LocalTime time) {
        this.time = time;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public Jogging setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getAverageTemperature() {
        return averageTemperature;
    }

    public Jogging setAverageTemperature(String averageTemperature) {
        this.averageTemperature = averageTemperature;
        return this;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public Jogging setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
        return this;
    }

    @Override
    public String toString() {
        return "Jogging{" +
                "id=" + id +
                ", version=" + version +
                ", date=" + date +
                ", distance=" + distance +
                ", time=" + time +
                ", location='" + location + '\'' +
                ", averageTemperature='" + averageTemperature + '\'' +
                ", weatherCondition='" + weatherCondition + '\'' +
                '}';
    }
}