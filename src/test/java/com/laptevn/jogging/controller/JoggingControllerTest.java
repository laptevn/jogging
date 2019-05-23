package com.laptevn.jogging.controller;

import com.laptevn.exception.IntegrityException;
import com.laptevn.jogging.service.JoggingService;
import com.laptevn.jogging.entity.JoggingDto;
import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.management.remote.JMXPrincipal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class JoggingControllerTest {
    private final static Principal USER = new JMXPrincipal("test");

    @Test
    public void createJoggingFailed() {
        JoggingService service = EasyMock.mock(JoggingService.class);
        EasyMock.expect(service.createJogging(EasyMock.anyObject(), EasyMock.anyString()))
                .andThrow(new IntegrityException("test exception"));
        EasyMock.replay(service);

        ResponseEntity response = new JoggingController(service).createJogging(null, USER);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    public void createJogging() {
        JoggingService service = EasyMock.mock(JoggingService.class);
        EasyMock.expect(service.createJogging(EasyMock.anyObject(), EasyMock.anyString())).andReturn(1);
        EasyMock.replay(service);

        ResponseEntity response = new JoggingController(service).createJogging(null, USER);
        assertEquals("Invalid status", HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Invalid location header", "/joggings/1", response.getHeaders().getFirst("Location"));
    }

    @Test
    public void getExistingJogging() {
        JoggingDto jogging = new JoggingDto();
        JoggingService service = EasyMock.mock(JoggingService.class);
        EasyMock.expect(service.getJogging(EasyMock.anyInt(), EasyMock.anyString()))
                .andReturn(Optional.of(jogging));
        EasyMock.replay(service);

        ResponseEntity response = new JoggingController(service).getJogging(0, USER);
        assertEquals("Invalid status", HttpStatus.OK, response.getStatusCode());
        assertSame("Invalid jogging was found", jogging, response.getBody());
    }

    @Test
    public void getNotExistingJogging() {
        JoggingService service = EasyMock.mock(JoggingService.class);
        EasyMock.expect(service.getJogging(EasyMock.anyInt(), EasyMock.anyString()))
                .andReturn(Optional.empty());
        EasyMock.replay(service);

        ResponseEntity response = new JoggingController(service).getJogging(0, USER);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void deleteExistingJogging() {
        JoggingService service = EasyMock.mock(JoggingService.class);
        EasyMock.expect(service.deleteJogging(EasyMock.anyInt(), EasyMock.anyString()))
                .andReturn(true);
        EasyMock.replay(service);

        ResponseEntity response = new JoggingController(service).deleteJogging(0, USER);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void deleteNotExistingJogging() {
        JoggingService service = EasyMock.mock(JoggingService.class);
        EasyMock.expect(service.deleteJogging(EasyMock.anyInt(), EasyMock.anyString()))
                .andReturn(false);
        EasyMock.replay(service);

        ResponseEntity response = new JoggingController(service).deleteJogging(0, USER);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void updateExistingJogging() {
        JoggingService service = EasyMock.mock(JoggingService.class);
        EasyMock.expect(service.updateJogging(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyString()))
                .andReturn(Optional.empty());
        EasyMock.replay(service);

        ResponseEntity response = new JoggingController(service).updateJogging(0, null, USER);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void updateNotExistingJogging() {
        JoggingService service = EasyMock.mock(JoggingService.class);
        EasyMock.expect(service.updateJogging(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyString()))
                .andReturn(Optional.of(1));
        EasyMock.replay(service);

        ResponseEntity response = new JoggingController(service).updateJogging(0, null, USER);
        assertEquals("Invalid status", HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Invalid location header", "/joggings/1", response.getHeaders().getFirst("Location"));
    }

    @Test
    public void updateJoggingFailed() {
        JoggingService service = EasyMock.mock(JoggingService.class);
        EasyMock.expect(service.updateJogging(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyString()))
                .andThrow(new IntegrityException("test"));
        EasyMock.replay(service);

        ResponseEntity response = new JoggingController(service).updateJogging(0, null, USER);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    public void getAllJoggingsFailed() {
        JoggingService service = EasyMock.mock(JoggingService.class);
        EasyMock.expect(service.getAllJoggings(
                EasyMock.anyString(), EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyString()))
                .andThrow(new IntegrityException("test"));
        EasyMock.replay(service);

        ResponseEntity response = new JoggingController(service).getAllJoggings(null, null, null, USER);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getAllJoggings() {
        List<JoggingDto> joggings = Arrays.asList(
                new JoggingDto()
                        .setTime(LocalTime.now())
                        .setLocation("Munich")
                        .setDistance(100)
                        .setDate(LocalDate.now()));

        JoggingService service = EasyMock.mock(JoggingService.class);
        EasyMock.expect(service.getAllJoggings(
                EasyMock.anyString(), EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyString()))
                .andReturn(joggings);
        EasyMock.replay(service);

        ResponseEntity response = new JoggingController(service).getAllJoggings(null, null, null, USER);
        assertEquals("Invalid status", HttpStatus.OK, response.getStatusCode());
        assertSame("Invalid status", joggings, response.getBody());
    }
}