package com.laptevn.jogging.controller;

import com.laptevn.ErrorDto;
import com.laptevn.exception.IntegrityException;
import com.laptevn.auth.RoleSpringConverter;
import com.laptevn.jogging.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@Validated
@Secured({RoleSpringConverter.USER_ROLE, RoleSpringConverter.ADMIN_ROLE})
public class ReportController {
    private final static Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @RequestMapping(value = "/reports/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity generateReport(Principal principal) {
        logger.info("Generating a report for '{}' user", principal.getName());

        try {
            return ResponseEntity.ok(reportService.generateReport(principal.getName()));
        } catch (IntegrityException e) {
            logger.info(e.getMessage());
            return ResponseEntity.unprocessableEntity().body(
                    new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage()));
        }
    }
}