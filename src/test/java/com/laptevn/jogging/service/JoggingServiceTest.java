package com.laptevn.jogging.service;

import com.laptevn.exception.IntegrityException;
import com.laptevn.PaginationFactory;
import com.laptevn.auth.entity.User;
import com.laptevn.auth.repository.UserRepository;
import com.laptevn.jogging.entity.Jogging;
import com.laptevn.jogging.entity.JoggingDto;
import com.laptevn.jogging.repository.JoggingRepository;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JoggingServiceTest {
    @Test(expected = IntegrityException.class)
    public void createJoggingWithoutUser() {
        new JoggingService(null, createUserRepository(Optional.empty()), null)
                .createJogging(new JoggingDto(), null);
    }

    @Test(expected = IntegrityException.class)
    public void createJoggingWithIntegrityViolation() {
        JoggingRepository joggingRepository = EasyMock.mock(JoggingRepository.class);
        EasyMock.expect(joggingRepository.save(EasyMock.anyObject()))
                .andThrow(new DataIntegrityViolationException("Test exception"));
        EasyMock.replay(joggingRepository);

        new JoggingService(joggingRepository, createUserRepository(Optional.of(new User())), null)
                .createJogging(new JoggingDto(), null);
    }

    @Test
    public void createJogging() {
        JoggingRepository joggingRepository = EasyMock.mock(JoggingRepository.class);
        int expectedId = 999;
        EasyMock.expect(joggingRepository.save(EasyMock.anyObject()))
                .andAnswer((IAnswer<Jogging>) () -> ((Jogging) EasyMock.getCurrentArguments()[0]).setId(expectedId));
        EasyMock.replay(joggingRepository);

        int id = new JoggingService(joggingRepository, createUserRepository(Optional.of(new User())), null)
                .createJogging(new JoggingDto(), null);
        assertEquals(expectedId, id);
    }

    private static UserRepository createUserRepository(Optional<User> user) {
        UserRepository userRepository = EasyMock.mock(UserRepository.class);
        EasyMock.expect(userRepository.findByNameIgnoreCase(EasyMock.anyString())).andReturn(user);
        EasyMock.replay(userRepository);
        return userRepository;
    }

    @Test
    public void getNotExistingJogging() {
        Optional<JoggingDto> joggingDto = new JoggingService(
                createJoggingRepository(Optional.empty()), createUserRepository(Optional.of(new User())), null)
                .getJogging(0, null);
        assertFalse(joggingDto.isPresent());
    }

    @Test
    public void getExistingJogging() {
        Jogging jogging = new Jogging()
                .setTime(LocalTime.now())
                .setLocation("Moscow")
                .setDistance(3000)
                .setDate(LocalDate.now());

        Optional<JoggingDto> joggingDto = new JoggingService(
                createJoggingRepository(Optional.of(jogging)), createUserRepository(Optional.of(new User())), null)
                .getJogging(0, null);
        assertTrue("Jogging wasn't found", joggingDto.isPresent());
        assertEquals("Invalid date", jogging.getDate(), joggingDto.get().getDate());
        assertEquals("Invalid time", jogging.getTime(), joggingDto.get().getTime());
        assertEquals("Invalid location", jogging.getLocation(), joggingDto.get().getLocation());
        assertEquals("Invalid distance", jogging.getDistance(), joggingDto.get().getDistance());
    }

    private static JoggingRepository createJoggingRepository(Optional<Jogging> jogging) {
        JoggingRepository repository = EasyMock.mock(JoggingRepository.class);
        EasyMock.expect(repository.findByIdAndUser(EasyMock.anyInt(), EasyMock.anyObject())).andReturn(jogging);
        EasyMock.expect(repository.save(EasyMock.anyObject()))
                .andAnswer((IAnswer<Jogging>) () -> ((Jogging) EasyMock.getCurrentArguments()[0]).setId(0));
        EasyMock.replay(repository);
        return repository;
    }

    @Test
    public void deleteNotExistingJogging() {
        JoggingRepository repository = EasyMock.mock(JoggingRepository.class);
        EasyMock.expect(repository.deleteByIdAndUser(EasyMock.anyInt(), EasyMock.anyObject())).andReturn(0);
        EasyMock.replay(repository);

        assertFalse(new JoggingService(repository, createUserRepository(Optional.of(new User())), null)
                .deleteJogging(0, null));
    }

    @Test
    public void deleteExistingJogging() {
        JoggingRepository repository = EasyMock.mock(JoggingRepository.class);
        EasyMock.expect(repository.deleteByIdAndUser(EasyMock.anyInt(), EasyMock.anyObject())).andReturn(1);
        EasyMock.replay(repository);

        assertTrue(new JoggingService(repository, createUserRepository(Optional.of(new User())), null)
                .deleteJogging(0, null));
    }

    @Test
    public void updateExistingJogging() {
        Jogging jogging = new Jogging()
                .setDate(LocalDate.now())
                .setLocation("test");
        JoggingDto joggingDto = new JoggingDto()
                .setDate(LocalDate.now())
                .setLocation("test");

        Optional<Integer> newId = new JoggingService(
                createJoggingRepository(Optional.of(jogging)), createUserRepository(Optional.of(new User())), null)
                .updateJogging(0, joggingDto, null);
        assertFalse(newId.isPresent());
    }

    @Test
    public void updateNotExistingJogging() {
        Optional<Integer> newId = new JoggingService(
                createJoggingRepository(Optional.empty()), createUserRepository(Optional.of(new User())), null)
                .updateJogging(0, new JoggingDto(), null);
        assertTrue("No id retrieved", newId.isPresent());
        assertEquals("Id is invalid", 0, (int) newId.get());
    }

    @Test
    public void getAllWithoutPagination() {
        List<Jogging> joggings = Arrays.asList(
                new Jogging()
                        .setTime(LocalTime.now())
                        .setLocation("Kiev")
                        .setDistance(1000)
                        .setDate(LocalDate.now()),
                new Jogging()
                        .setTime(LocalTime.now())
                        .setLocation("London")
                        .setDistance(50000)
                        .setDate(LocalDate.now()));

        JoggingRepository repository = EasyMock.mock(JoggingRepository.class);
        EasyMock.expect(repository.findByUser(EasyMock.anyObject())).andReturn(joggings);
        EasyMock.replay(repository);

        List<JoggingDto> foundJoggins = (List<JoggingDto>) new JoggingService(
                repository, createUserRepository(Optional.of(new User())), new PaginationFactory())
                .getAllJoggings(null, null, null, null);

        assertEquals("Not all joggins were found", joggings.size(), foundJoggins.size());
        assertEquals("Found joggin has invalid location", joggings.get(0).getLocation(), foundJoggins.get(0).getLocation());
        assertEquals("Found joggin has invalid distance", joggings.get(1).getDistance(), foundJoggins.get(1).getDistance());
    }

    @Test
    public void getAllWithPagination() {
        List<Jogging> joggings = Arrays.asList(
                new Jogging()
                        .setTime(LocalTime.now())
                        .setLocation("San Fransisco")
                        .setDistance(10000)
                        .setDate(LocalDate.now()));

        JoggingRepository repository = EasyMock.mock(JoggingRepository.class);
        EasyMock.expect(repository.findByUser(EasyMock.anyObject(), EasyMock.anyObject())).andReturn(joggings);
        EasyMock.replay(repository);

        List<JoggingDto> foundJoggins = (List<JoggingDto>) new JoggingService(
                repository, createUserRepository(Optional.of(new User())), new PaginationFactory())
                .getAllJoggings(null, 1, 2, null);

        assertEquals("Not all joggins were found", joggings.size(), foundJoggins.size());
        assertEquals("Found joggin has invalid location", joggings.get(0).getLocation(), foundJoggins.get(0).getLocation());
    }
}