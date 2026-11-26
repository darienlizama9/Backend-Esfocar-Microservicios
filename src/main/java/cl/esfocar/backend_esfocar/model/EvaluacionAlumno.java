package cl.esfocar.backend_esfocar.model;

import cl.esfocar.backend_esfocar.util.RutUtils;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class EvaluacionAlumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia a la plantilla usada
    @Column(nullable = false)
    private Long plantillaRubricaId;

    @JsonAlias({"titulo", "tituloRubrica"})
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
    @JsonAlias({"nota", "notaFinal"})
    @Column(nullable = false)
    private Double notaFinal;

    // "APROBADO" (nota >= 4.0) o "REPROBADO" (nota < 4.0)
    @Column(nullable = false)
    private String estado;

    // Comentario del docente visible para el alumno
    @JsonAlias({"feedback", "feedbackDocente"})
    @Column(length = 1000)
    private String feedbackDocente;

    @Column(nullable = false)
    private LocalDateTime fechaEvaluacion;

    // ── Campos de compatibilidad bidireccional con AppEsfocar (modelo Rubrica) ──

    @Transient
    @JsonProperty("titulo")
    private String titulo;

    @Transient
    @JsonProperty("tipo")
    private String tipo;

    @Transient
    @JsonProperty("nota")
    private Double nota;

    @Transient
    @JsonProperty("feedback")
    private String feedback;

    public void setRutAlumno(String rutAlumno) {
        this.rutAlumno = RutUtils.normalizarRut(rutAlumno);
    }

    public String getTitulo() {
        return (titulo != null && !titulo.isBlank()) ? titulo : tituloRubrica;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
        if (this.tituloRubrica == null || this.tituloRubrica.isBlank()) {
            this.tituloRubrica = titulo;
        }
    }

    public void setTituloRubrica(String tituloRubrica) {
        this.tituloRubrica = tituloRubrica;
        if (this.titulo == null || this.titulo.isBlank()) {
            this.titulo = tituloRubrica;
        }
    }

    public String getTipo() {
        return (tipo != null && !tipo.isBlank()) ? tipo : "Individual";
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getNota() {
        return (nota != null) ? nota : notaFinal;
    }

    public void setNota(Double nota) {
        this.nota = nota;
        if (this.notaFinal == null) {
            this.notaFinal = nota;
        }
    }

    public void setNotaFinal(Double notaFinal) {
        this.notaFinal = notaFinal;
        if (this.nota == null) {
            this.nota = notaFinal;
        }
    }

    public String getFeedback() {
        return (feedback != null && !feedback.isBlank()) ? feedback : feedbackDocente;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
        if (this.feedbackDocente == null || this.feedbackDocente.isBlank()) {
            this.feedbackDocente = feedback;
        }
    }

    public void setFeedbackDocente(String feedbackDocente) {
        this.feedbackDocente = feedbackDocente;
        if (this.feedback == null || this.feedback.isBlank()) {
            this.feedback = feedbackDocente;
        }
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaEvaluacion == null) {
            this.fechaEvaluacion = LocalDateTime.now();
        }
        if (this.rutAlumno != null) {
            this.rutAlumno = RutUtils.normalizarRut(this.rutAlumno);
        }
        if (this.tituloRubrica == null || this.tituloRubrica.isBlank()) {
            this.tituloRubrica = (this.titulo != null && !this.titulo.isBlank()) ? this.titulo : "Rúbrica de Evaluación";
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.rutAlumno != null) {
            this.rutAlumno = RutUtils.normalizarRut(this.rutAlumno);
        }
    }
}
