# GUIA_JEAN.md — Módulo de Rúbricas y Evaluaciones 🎓

> **Autor del módulo:** Jean  
> **Proyecto:** Backend ESFOCAR — Sistema de Evaluación Docente  
> **Stack:** Java 21 · Spring Boot 4.2.0 · PostgreSQL (Neon) · Jakarta Persistence

---

## ¿Qué debes completar?

Darien dejó **5 archivos vacíos** con el comentario `// TODO`. Tu tarea es reemplazar el contenido de cada uno con el código que se muestra a continuación, **uno por uno**, y después hacer el commit individual indicado.

Al terminar tendrás exactamente **5 commits propios** en el historial.

---

## Archivo 1 — `model/PlantillaRubrica.java`

**Ruta:** `src/main/java/cl/esfocar/backend_esfocar/model/PlantillaRubrica.java`

```java
package cl.esfocar.backend_esfocar.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plantillas_rubrica")
public class PlantillaRubrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    // "Individual" o "Grupal"
    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String criterio1Nombre;

    @Column(nullable = false)
    private String criterio2Nombre;

    @Column(nullable = false)
    private String criterio3Nombre;

    @Column(nullable = false)
    private String criterio4Nombre;

    // Puntaje máximo posible (suma de los 4 criterios)
    @Column(nullable = false)
    private Double puntajeMaximo;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
```

**Commit individual:**
```bash
git add src/main/java/cl/esfocar/backend_esfocar/model/PlantillaRubrica.java
git commit -m "feat(rubrica): agregar entidad JPA PlantillaRubrica con 4 criterios y puntaje maximo"
```

---

## Archivo 2 — `model/EvaluacionAlumno.java`

**Ruta:** `src/main/java/cl/esfocar/backend_esfocar/model/EvaluacionAlumno.java`

```java
package cl.esfocar.backend_esfocar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "evaluaciones_alumno")
public class EvaluacionAlumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia a la plantilla usada
    @Column(nullable = false)
    private Long plantillaRubricaId;

    @Column(nullable = false)
    private String tituloRubrica;

    // Datos del alumno evaluado
    @Column(nullable = false)
    private String nombreAlumno;

    @Column(nullable = false)
    private String apellidoAlumno;

    @Pattern(regexp = "^[0-9]{7,8}-[0-9kK]$",
             message = "El RUT no tiene un formato válido. Use el formato 12345678-9.")
    @Column(nullable = false)
    private String rutAlumno;

    // Puntajes por criterio (ingresados por el docente)
    @Column(nullable = false)
    private Double puntaje1;

    @Column(nullable = false)
    private Double puntaje2;

    @Column(nullable = false)
    private Double puntaje3;

    @Column(nullable = false)
    private Double puntaje4;

    // Calculada automáticamente en el service (escala 1.0 – 7.0, 2 decimales)
    @Column(nullable = false)
    private Double notaFinal;

    // "APROBADO" (nota >= 4.0) o "REPROBADO" (nota < 4.0)
    @Column(nullable = false)
    private String estado;

    // Comentario del docente visible para el alumno
    @Column(length = 1000)
    private String feedbackDocente;

    @Column(nullable = false)
    private LocalDateTime fechaEvaluacion;

    @PrePersist
    public void prePersist() {
        this.fechaEvaluacion = LocalDateTime.now();
    }
}
```

**Commit individual:**
```bash
git add src/main/java/cl/esfocar/backend_esfocar/model/EvaluacionAlumno.java
git commit -m "feat(rubrica): agregar entidad JPA EvaluacionAlumno con validacion de RUT chileno"
```

---

## Archivo 3 — `repository/RubricaRepositories.java`

**Ruta:** `src/main/java/cl/esfocar/backend_esfocar/repository/RubricaRepositories.java`

```java
package cl.esfocar.backend_esfocar.repository;

import cl.esfocar.backend_esfocar.model.EvaluacionAlumno;
import cl.esfocar.backend_esfocar.model.PlantillaRubrica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorios JPA para el módulo de Rúbricas y Evaluaciones.
 */
public class RubricaRepositories {

    @Repository
    public interface PlantillaRubricaRepository
            extends JpaRepository<PlantillaRubrica, Long> {
        // Búsqueda por tipo ("Individual" / "Grupal")
        List<PlantillaRubrica> findByTipo(String tipo);
    }

    @Repository
    public interface EvaluacionAlumnoRepository
            extends JpaRepository<EvaluacionAlumno, Long> {
        // Permite al alumno ver sus notas por RUT
        List<EvaluacionAlumno> findByRutAlumno(String rutAlumno);
    }
}
```

**Commit individual:**
```bash
git add src/main/java/cl/esfocar/backend_esfocar/repository/RubricaRepositories.java
git commit -m "feat(rubrica): agregar repositorios JPA PlantillaRubrica y EvaluacionAlumno con findByRutAlumno"
```

---

## Archivo 4 — `service/RubricaEvaluacionService.java`

**Ruta:** `src/main/java/cl/esfocar/backend_esfocar/service/RubricaEvaluacionService.java`

> **Nota:** La nota final se convierte de puntaje a escala 1.0–7.0 con `BigDecimal` a 2 decimales exactos usando `RoundingMode.HALF_UP`.  
> Fórmula: `notaFinal = 1.0 + (puntajeObtenido / puntajeMaximo) * 6.0`

```java
package cl.esfocar.backend_esfocar.service;

import cl.esfocar.backend_esfocar.model.EvaluacionAlumno;
import cl.esfocar.backend_esfocar.model.PlantillaRubrica;
import cl.esfocar.backend_esfocar.repository.RubricaRepositories.EvaluacionAlumnoRepository;
import cl.esfocar.backend_esfocar.repository.RubricaRepositories.PlantillaRubricaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Lógica de negocio para Rúbricas y Evaluaciones.
 */
@Service
public class RubricaEvaluacionService {

    private final PlantillaRubricaRepository plantillaRepo;
    private final EvaluacionAlumnoRepository evaluacionRepo;

    public RubricaEvaluacionService(PlantillaRubricaRepository plantillaRepo,
                                     EvaluacionAlumnoRepository evaluacionRepo) {
        this.plantillaRepo = plantillaRepo;
        this.evaluacionRepo = evaluacionRepo;
    }

    // ── CRUD Plantillas ───────────────────────────────────────────────────────

    public PlantillaRubrica crearPlantilla(PlantillaRubrica plantilla) {
        return plantillaRepo.save(plantilla);
    }

    public List<PlantillaRubrica> listarPlantillas() {
        return plantillaRepo.findAll();
    }

    public PlantillaRubrica actualizarPlantilla(Long id, PlantillaRubrica datos) {
        PlantillaRubrica existente = plantillaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Plantilla no encontrada con id: " + id));
        existente.setTitulo(datos.getTitulo());
        existente.setTipo(datos.getTipo());
        existente.setCriterio1Nombre(datos.getCriterio1Nombre());
        existente.setCriterio2Nombre(datos.getCriterio2Nombre());
        existente.setCriterio3Nombre(datos.getCriterio3Nombre());
        existente.setCriterio4Nombre(datos.getCriterio4Nombre());
        existente.setPuntajeMaximo(datos.getPuntajeMaximo());
        return plantillaRepo.save(existente);
    }

    public void eliminarPlantilla(Long id) {
        if (!plantillaRepo.existsById(id)) {
            throw new EntityNotFoundException("Plantilla no encontrada con id: " + id);
        }
        plantillaRepo.deleteById(id);
    }

    // ── Evaluación de Alumnos ─────────────────────────────────────────────────

    /**
     * Crea una evaluación calculando la nota final a 2 decimales exactos
     * en escala 1.0 – 7.0 y asignando estado + feedback automático.
     */
    public EvaluacionAlumno evaluarAlumno(EvaluacionAlumno evaluacion) {
        PlantillaRubrica plantilla = plantillaRepo.findById(evaluacion.getPlantillaRubricaId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Plantilla no encontrada con id: " + evaluacion.getPlantillaRubricaId()));

        double puntajeObtenido = evaluacion.getPuntaje1()
                + evaluacion.getPuntaje2()
                + evaluacion.getPuntaje3()
                + evaluacion.getPuntaje4();

        // Nota en escala 1.0 – 7.0, redondeada a 2 decimales (HALF_UP)
        BigDecimal nota = BigDecimal.valueOf(1.0)
                .add(BigDecimal.valueOf(puntajeObtenido)
                        .divide(BigDecimal.valueOf(plantilla.getPuntajeMaximo()), 10, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(6.0)))
                .setScale(2, RoundingMode.HALF_UP);

        evaluacion.setNotaFinal(nota.doubleValue());
        evaluacion.setTituloRubrica(plantilla.getTitulo());

        // Estado
        if (nota.compareTo(BigDecimal.valueOf(4.0)) >= 0) {
            evaluacion.setEstado("APROBADO");
            if (evaluacion.getFeedbackDocente() == null
                    || evaluacion.getFeedbackDocente().isBlank()) {
                evaluacion.setFeedbackDocente("¡Buen trabajo! Nota: " + nota);
            }
        } else {
            evaluacion.setEstado("REPROBADO");
            if (evaluacion.getFeedbackDocente() == null
                    || evaluacion.getFeedbackDocente().isBlank()) {
                evaluacion.setFeedbackDocente("Debe reforzar los contenidos. Nota: " + nota);
            }
        }

        return evaluacionRepo.save(evaluacion);
    }

    public List<EvaluacionAlumno> listarEvaluaciones() {
        return evaluacionRepo.findAll();
    }

    public EvaluacionAlumno actualizarEvaluacion(Long id, EvaluacionAlumno datos) {
        EvaluacionAlumno existente = evaluacionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Evaluación no encontrada con id: " + id));
        existente.setFeedbackDocente(datos.getFeedbackDocente());
        existente.setPuntaje1(datos.getPuntaje1());
        existente.setPuntaje2(datos.getPuntaje2());
        existente.setPuntaje3(datos.getPuntaje3());
        existente.setPuntaje4(datos.getPuntaje4());

        // Recalcular nota
        PlantillaRubrica plantilla = plantillaRepo.findById(existente.getPlantillaRubricaId())
                .orElseThrow(() -> new EntityNotFoundException("Plantilla no encontrada."));
        double puntajeObtenido = existente.getPuntaje1() + existente.getPuntaje2()
                + existente.getPuntaje3() + existente.getPuntaje4();
        BigDecimal nota = BigDecimal.valueOf(1.0)
                .add(BigDecimal.valueOf(puntajeObtenido)
                        .divide(BigDecimal.valueOf(plantilla.getPuntajeMaximo()), 10, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(6.0)))
                .setScale(2, RoundingMode.HALF_UP);
        existente.setNotaFinal(nota.doubleValue());
        existente.setEstado(nota.compareTo(BigDecimal.valueOf(4.0)) >= 0 ? "APROBADO" : "REPROBADO");

        return evaluacionRepo.save(existente);
    }

    /** Permite al alumno ver sus evaluaciones usando su RUT. */
    public List<EvaluacionAlumno> listarPorRutAlumno(String rut) {
        return evaluacionRepo.findByRutAlumno(rut);
    }
}
```

**Commit individual:**
```bash
git add src/main/java/cl/esfocar/backend_esfocar/service/RubricaEvaluacionService.java
git commit -m "feat(rubrica): implementar logica de evaluacion con calculo de nota 1.0-7.0 a 2 decimales BigDecimal"
```

---

## Archivo 5 — `controller/RubricaEvaluacionController.java`

**Ruta:** `src/main/java/cl/esfocar/backend_esfocar/controller/RubricaEvaluacionController.java`

```java
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
     * El docente envía los 4 puntajes; el service calcula nota y estado.
     */
    @PostMapping("/evaluar")
    public ResponseEntity<EvaluacionAlumno> evaluarAlumno(
            @RequestBody EvaluacionAlumno evaluacion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.evaluarAlumno(evaluacion));
    }

    /** GET /api/rubricas/evaluaciones — Listar todas las evaluaciones (vista docente) */
    @GetMapping("/evaluaciones")
    public ResponseEntity<List<EvaluacionAlumno>> listarEvaluaciones() {
        return ResponseEntity.ok(service.listarEvaluaciones());
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
     * todas sus evaluaciones y feedback en su cuenta.
     */
    @GetMapping("/alumno/{rut}")
    public ResponseEntity<List<EvaluacionAlumno>> verMisEvaluaciones(
            @PathVariable String rut) {
        return ResponseEntity.ok(service.listarPorRutAlumno(rut));
    }
}
```

**Commit individual:**
```bash
git add src/main/java/cl/esfocar/backend_esfocar/controller/RubricaEvaluacionController.java
git commit -m "feat(rubrica): exponer endpoints REST CRUD de plantillas y evaluaciones con vista por RUT de alumno"
```

---

## Resumen de tus 5 commits

| # | Archivo | Mensaje de commit |
|---|---------|-------------------|
| 1 | `model/PlantillaRubrica.java` | `feat(rubrica): agregar entidad JPA PlantillaRubrica...` |
| 2 | `model/EvaluacionAlumno.java` | `feat(rubrica): agregar entidad JPA EvaluacionAlumno...` |
| 3 | `repository/RubricaRepositories.java` | `feat(rubrica): agregar repositorios JPA PlantillaRubrica...` |
| 4 | `service/RubricaEvaluacionService.java` | `feat(rubrica): implementar logica de evaluacion...` |
| 5 | `controller/RubricaEvaluacionController.java` | `feat(rubrica): exponer endpoints REST CRUD...` |

---

## Endpoints disponibles en tu módulo

| Método | URL | Descripción |
|--------|-----|-------------|
| `POST` | `/api/rubricas/plantillas` | Crear plantilla de rúbrica |
| `GET` | `/api/rubricas/plantillas` | Listar todas las plantillas |
| `PUT` | `/api/rubricas/plantillas/{id}` | Editar plantilla |
| `DELETE` | `/api/rubricas/plantillas/{id}` | Eliminar plantilla |
| `POST` | `/api/rubricas/evaluar` | Evaluar alumno (calcula nota automáticamente) |
| `GET` | `/api/rubricas/evaluaciones` | Listar todas las evaluaciones |
| `PUT` | `/api/rubricas/evaluaciones/{id}` | Editar evaluación |
| `GET` | `/api/rubricas/alumno/{rut}` | El alumno ve sus notas y feedback por RUT |

---

## Ejemplo de JSON para evaluar a un alumno

```json
POST /api/rubricas/evaluar
{
  "plantillaRubricaId": 1,
  "nombreAlumno": "Carlos",
  "apellidoAlumno": "González",
  "rutAlumno": "12345678-9",
  "puntaje1": 20.0,
  "puntaje2": 18.5,
  "puntaje3": 22.0,
  "puntaje4": 19.5,
  "feedbackDocente": "Excelente desempeño en criterio 3."
}
```

> La `notaFinal`, `estado` y `fechaEvaluacion` se calculan automáticamente.

---

*Generado por Darien · Backend ESFOCAR · Java 21 · Spring Boot 4.2.0*
