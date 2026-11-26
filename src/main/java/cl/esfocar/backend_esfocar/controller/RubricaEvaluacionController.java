package cl.esfocar.backend_esfocar.controller;

import cl.esfocar.backend_esfocar.model.EvaluacionAlumno;
import cl.esfocar.backend_esfocar.model.PlantillaRubrica;
import cl.esfocar.backend_esfocar.service.RubricaEvaluacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST para el módulo de Rúbricas y Evaluaciones.
 * Base path: /api/rubricas
 */
@RestController
@RequestMapping("/api/rubricas")
public class RubricaEvaluacionController {

    private final RubricaEvaluacionService service;

    public RubricaEvaluacionController(RubricaEvaluacionService service) {
        this.service = service;
    }

    // ── CRUD Plantillas ───────────────────────────────────────────────────────

    /** POST /api/rubricas/plantillas — Crear plantilla de rúbrica */
    @PostMapping("/plantillas")
    public ResponseEntity<PlantillaRubrica> crearPlantilla(
            @RequestBody PlantillaRubrica plantilla) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crearPlantilla(plantilla));
    }

    /** GET /api/rubricas/plantillas — Listar todas las plantillas */
    @GetMapping("/plantillas")
    public ResponseEntity<List<PlantillaRubrica>> listarPlantillas() {
        return ResponseEntity.ok(service.listarPlantillas());
    }

    /** GET /api/rubricas/plantillas/{id} — Obtener plantilla por ID */
    @GetMapping("/plantillas/{id}")
    public ResponseEntity<PlantillaRubrica> obtenerPlantilla(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPlantillaPorId(id));
    }

    /** PUT /api/rubricas/plantillas/{id} — Editar plantilla */
    @PutMapping("/plantillas/{id}")
    public ResponseEntity<PlantillaRubrica> actualizarPlantilla(
            @PathVariable Long id,
            @RequestBody PlantillaRubrica datos) {
        return ResponseEntity.ok(service.actualizarPlantilla(id, datos));
    }

    /** DELETE /api/rubricas/plantillas/{id} — Eliminar plantilla */
    @DeleteMapping("/plantillas/{id}")
    public ResponseEntity<Void> eliminarPlantilla(@PathVariable Long id) {
        service.eliminarPlantilla(id);
        return ResponseEntity.noContent().build();
    }

    // ── Evaluaciones de Alumnos ───────────────────────────────────────────────

    /**
     * POST /api/rubricas/evaluar — Evaluar alumno.
     * Admite tanto el formato completo de EvaluacionAlumno como el modelo simple
     * Rubrica enviado desde AppEsfocar (Android).
     */
    @PostMapping("/evaluar")
    public ResponseEntity<EvaluacionAlumno> evaluarAlumno(
            @RequestBody EvaluacionAlumno evaluacion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.evaluarAlumno(evaluacion));
    }

    /**
     * GET /api/rubricas/evaluaciones — Listar todas las evaluaciones (vista docente)
     */
    @GetMapping("/evaluaciones")
    public ResponseEntity<List<EvaluacionAlumno>> listarEvaluaciones() {
        return ResponseEntity.ok(service.listarEvaluaciones());
    }

    /**
     * GET /api/rubricas/evaluaciones/{id} — Obtener detalle de evaluación por ID
     */
    @GetMapping("/evaluaciones/{id}")
    public ResponseEntity<EvaluacionAlumno> obtenerEvaluacion(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerEvaluacionPorId(id));
    }

    /** PUT /api/rubricas/evaluaciones/{id} — Editar feedback o puntajes */
    @PutMapping("/evaluaciones/{id}")
    public ResponseEntity<EvaluacionAlumno> actualizarEvaluacion(
            @PathVariable Long id,
            @RequestBody EvaluacionAlumno datos) {
        return ResponseEntity.ok(service.actualizarEvaluacion(id, datos));
    }

    /**
     * GET /api/rubricas/alumno/{rut} — El alumno ingresa su RUT y ve
     * todas sus evaluaciones y feedback en su cuenta (tolerante a formato).
     */
    @GetMapping("/alumno/{rut}")
    public ResponseEntity<List<EvaluacionAlumno>> verMisEvaluaciones(
            @PathVariable String rut) {
        return ResponseEntity.ok(service.listarPorRutAlumno(rut));
    }
}
