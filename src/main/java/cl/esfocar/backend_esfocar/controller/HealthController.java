package cl.esfocar.backend_esfocar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Endpoint liviano de monitoreo de salud para Render y UptimeRobot.
 * No realiza consultas pesadas a la base de datos para responder de inmediato (<5ms)
 * y permitir mantener despierto el contenedor en Render sin forzar el cómputo de Neon.
 */
@Tag(name = "Health", description = "Monitoreo de estado y disponibilidad del servicio")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Operation(summary = "Comprobar estado del servicio", description = "Responde 200 OK con timestamp y estado UP")
    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Backend-Esfocar",
                "timestamp", Instant.now().toString()
        ));
    }
}
