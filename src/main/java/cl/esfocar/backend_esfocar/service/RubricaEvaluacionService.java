package cl.esfocar.backend_esfocar.service;

import cl.esfocar.backend_esfocar.model.EvaluacionAlumno;
import cl.esfocar.backend_esfocar.model.PlantillaRubrica;
import cl.esfocar.backend_esfocar.repository.EvaluacionAlumnoRepository;
import cl.esfocar.backend_esfocar.repository.PlantillaRubricaRepository;
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