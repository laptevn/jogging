package com.laptevn.jogging.controller;

import com.laptevn.exception.IntegrityException;
import com.laptevn.jogging.entity.ReportDto;
import com.laptevn.jogging.service.ReportService;
import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.management.remote.JMXPrincipal;

import static org.junit.Assert.assertEquals;

public class ReportControllerTest {
    @Test
    public void error() {
        ReportService service = EasyMock.mock(ReportService.class);
        EasyMock.expect(service.generateReport(EasyMock.anyString())).andThrow(new IntegrityException("test"));
        EasyMock.replay(service);

        ResponseEntity response = new ReportController(service).generateReport(new JMXPrincipal("test"));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    public void report() {
        ReportService service = EasyMock.mock(ReportService.class);
        EasyMock.expect(service.generateReport(EasyMock.anyString())).andReturn(new ReportDto(1, 2));
        EasyMock.replay(service);

        ResponseEntity response = new ReportController(service).generateReport(new JMXPrincipal("test"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}