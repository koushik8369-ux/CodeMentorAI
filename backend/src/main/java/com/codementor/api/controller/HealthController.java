package com.codementor.api.controller;

import com.codementor.api.dto.HealthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;

    @Autowired
    public HealthController(@Autowired(required = false) DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ResponseEntity<HealthResponse> getHealth() {
        String dbStatus = "DOWN";

        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                if (conn.isValid(2)) {
                    dbStatus = "PostgreSQL Ready";
                }
            } catch (Exception ignored) {
                dbStatus = "DOWN";
            }
        }

        return ResponseEntity.ok(new HealthResponse("UP", "CodeMentor AI", dbStatus));
    }
}
