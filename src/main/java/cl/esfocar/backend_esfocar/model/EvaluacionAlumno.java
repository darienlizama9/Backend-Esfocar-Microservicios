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

    @Pattern(regexp = "^[0-9]{7,8}-[0-9kK]$", message = "El RUT no tiene un formato válido. Use el formato 12345678-9.")
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