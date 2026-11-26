package cl.esfocar.backend_esfocar.service;

import cl.esfocar.backend_esfocar.model.EvaluacionAlumno;
import cl.esfocar.backend_esfocar.model.PlantillaRubrica;
import cl.esfocar.backend_esfocar.repository.EvaluacionAlumnoRepository;
import cl.esfocar.backend_esfocar.repository.PlantillaRubricaRepository;
import cl.esfocar.backend_esfocar.util.RutUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Lógica de negocio robusta para Rúbricas y Evaluaciones.
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

    @Transactional
    public PlantillaRubrica crearPlantilla(PlantillaRubrica plantilla) {
        if (plantilla.getPuntajeMaximo() == null || plantilla.getPuntajeMaximo() <= 0) {
            plantilla.setPuntajeMaximo(20.0);
        }
        if (plantilla.getTipo() == null || plantilla.getTipo().isBlank()) {
            plantilla.setTipo("Individual");
        }
        return plantillaRepo.save(plantilla);
    }

    public List<PlantillaRubrica> listarPlantillas() {
        return plantillaRepo.findAll();
    }

    public PlantillaRubrica obtenerPlantillaPorId(Long id) {
        return plantillaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plantilla no encontrada con id: " + id));
    }

    @Transactional
    public PlantillaRubrica actualizarPlantilla(Long id, PlantillaRubrica datos) {
        PlantillaRubrica existente = plantillaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Plantilla no encontrada con id: " + id));

        if (datos.getTitulo() != null && !datos.getTitulo().isBlank()) {
            existente.setTitulo(datos.getTitulo().trim());
        }
        if (datos.getTipo() != null && !datos.getTipo().isBlank()) {
            existente.setTipo(datos.getTipo().trim());
        }
        if (datos.getCriterio1Nombre() != null) {
            existente.setCriterio1Nombre(datos.getCriterio1Nombre());
        }
        if (datos.getCriterio2Nombre() != null) {
            existente.setCriterio2Nombre(datos.getCriterio2Nombre());
        }
        if (datos.getCriterio3Nombre() != null) {
            existente.setCriterio3Nombre(datos.getCriterio3Nombre());
        }
        if (datos.getCriterio4Nombre() != null) {
            existente.setCriterio4Nombre(datos.getCriterio4Nombre());
        }
        if (datos.getPuntajeMaximo() != null && datos.getPuntajeMaximo() > 0) {
            existente.setPuntajeMaximo(datos.getPuntajeMaximo());
        }

        return plantillaRepo.save(existente);
    }

    @Transactional
    public void eliminarPlantilla(Long id) {
        if (!plantillaRepo.existsById(id)) {
            throw new EntityNotFoundException("Plantilla no encontrada con id: " + id);
        }
        plantillaRepo.deleteById(id);
    }

    // ── Evaluación de Alumnos ─────────────────────────────────────────────────

    /**
     * Registra una evaluación resolviendo de forma segura la plantilla,
     * normalizando el RUT chileno, protegiendo puntajes contra NullPointerException
     * y calculando la nota final exacta (escala 1.0 – 7.0 a 2 decimales).
     */
    @Transactional
    public EvaluacionAlumno evaluarAlumno(EvaluacionAlumno evaluacion) {
        // 1. Normalizar y validar RUT
        if (evaluacion.getRutAlumno() == null || evaluacion.getRutAlumno().isBlank()) {
            throw new IllegalArgumentException("El RUT del alumno es obligatorio para registrar la evaluación.");
        }
        String rutNormalizado = RutUtils.normalizarRut(evaluacion.getRutAlumno());
        evaluacion.setRutAlumno(rutNormalizado);

        // 2. Proteger nombres
        if (evaluacion.getNombreAlumno() == null || evaluacion.getNombreAlumno().isBlank()) {
            evaluacion.setNombreAlumno("Alumno");
        } else {
            evaluacion.setNombreAlumno(evaluacion.getNombreAlumno().trim());
        }

        if (evaluacion.getApellidoAlumno() == null || evaluacion.getApellidoAlumno().isBlank()) {
            evaluacion.setApellidoAlumno("");
        } else {
            evaluacion.setApellidoAlumno(evaluacion.getApellidoAlumno().trim());
        }

        // 3. Resolución segura de la plantilla de rúbrica
        String tituloRubrica = (evaluacion.getTituloRubrica() != null && !evaluacion.getTituloRubrica().isBlank())
                ? evaluacion.getTituloRubrica()
                : evaluacion.getTitulo();

        PlantillaRubrica plantilla = resolverPlantillaSegura(evaluacion.getPlantillaRubricaId(), tituloRubrica);
        evaluacion.setPlantillaRubricaId(plantilla.getId());
        evaluacion.setTituloRubrica(plantilla.getTitulo());
        evaluacion.setTitulo(plantilla.getTitulo());

        double puntajeMax = (plantilla.getPuntajeMaximo() != null && plantilla.getPuntajeMaximo() > 0)
                ? plantilla.getPuntajeMaximo()
                : 20.0;

        // 4. Manejo seguro de puntajes y notas entrantes
        boolean todosPuntajesNulos = (evaluacion.getPuntaje1() == null
                && evaluacion.getPuntaje2() == null
                && evaluacion.getPuntaje3() == null
                && evaluacion.getPuntaje4() == null);

        Double notaEntrante = evaluacion.getNotaFinal() != null ? evaluacion.getNotaFinal() : evaluacion.getNota();

        if (todosPuntajesNulos) {
            if (notaEntrante != null && notaEntrante >= 1.0) {
                // Deducir puntajes equitativos si la app sólo envió la nota (escala 1.0 a 7.0)
                double factor = Math.max(0.0, Math.min(1.0, (notaEntrante - 1.0) / 6.0));
                double puntajeTotalDeducido = puntajeMax * factor;
                double porCriterio = BigDecimal.valueOf(puntajeTotalDeducido / 4.0)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
                evaluacion.setPuntaje1(porCriterio);
                evaluacion.setPuntaje2(porCriterio);
                evaluacion.setPuntaje3(porCriterio);
                evaluacion.setPuntaje4(porCriterio);
            } else {
                evaluacion.setPuntaje1(0.0);
                evaluacion.setPuntaje2(0.0);
                evaluacion.setPuntaje3(0.0);
                evaluacion.setPuntaje4(0.0);
            }
        } else {
            if (evaluacion.getPuntaje1() == null) evaluacion.setPuntaje1(0.0);
            if (evaluacion.getPuntaje2() == null) evaluacion.setPuntaje2(0.0);
            if (evaluacion.getPuntaje3() == null) evaluacion.setPuntaje3(0.0);
            if (evaluacion.getPuntaje4() == null) evaluacion.setPuntaje4(0.0);
        }

        // 5. Cálculo exacto de nota final (1.0 – 7.0, HALF_UP a 2 decimales)
        BigDecimal notaCalculada;
        if (todosPuntajesNulos && notaEntrante != null && notaEntrante >= 1.0) {
            notaCalculada = BigDecimal.valueOf(notaEntrante).setScale(2, RoundingMode.HALF_UP);
        } else {
            double puntajeObtenido = evaluacion.getPuntaje1()
                    + evaluacion.getPuntaje2()
                    + evaluacion.getPuntaje3()
                    + evaluacion.getPuntaje4();

            notaCalculada = BigDecimal.valueOf(1.0)
                    .add(BigDecimal.valueOf(puntajeObtenido)
                            .divide(BigDecimal.valueOf(puntajeMax), 10, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(6.0)))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Limitar nota estrictamente entre 1.00 y 7.00
        if (notaCalculada.compareTo(BigDecimal.valueOf(7.0)) > 0) {
            notaCalculada = BigDecimal.valueOf(7.00);
        } else if (notaCalculada.compareTo(BigDecimal.valueOf(1.0)) < 0) {
            notaCalculada = BigDecimal.valueOf(1.00);
        }

        evaluacion.setNotaFinal(notaCalculada.doubleValue());
        evaluacion.setNota(notaCalculada.doubleValue());

        // 6. Asignar estado
        if (notaCalculada.compareTo(BigDecimal.valueOf(4.0)) >= 0) {
            evaluacion.setEstado("APROBADO");
        } else {
            evaluacion.setEstado("REPROBADO");
        }

        // 7. Feedback docente
        String feedback = (evaluacion.getFeedbackDocente() != null && !evaluacion.getFeedbackDocente().isBlank())
                ? evaluacion.getFeedbackDocente()
                : evaluacion.getFeedback();

        if (feedback == null || feedback.isBlank()) {
            if ("APROBADO".equals(evaluacion.getEstado())) {
                feedback = "¡Buen trabajo! Nota: " + notaCalculada;
            } else {
                feedback = "Debe reforzar los contenidos. Nota: " + notaCalculada;
            }
        }
        evaluacion.setFeedbackDocente(feedback);
        evaluacion.setFeedback(feedback);

        if (evaluacion.getFechaEvaluacion() == null) {
            evaluacion.setFechaEvaluacion(LocalDateTime.now());
        }

        return evaluacionRepo.save(evaluacion);
    }

    public List<EvaluacionAlumno> listarEvaluaciones() {
        return evaluacionRepo.findAll();
    }

    public EvaluacionAlumno obtenerEvaluacionPorId(Long id) {
        return evaluacionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evaluación no encontrada con id: " + id));
    }

    @Transactional
    public EvaluacionAlumno actualizarEvaluacion(Long id, EvaluacionAlumno datos) {
        EvaluacionAlumno existente = evaluacionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Evaluación no encontrada con id: " + id));

        if (datos.getNombreAlumno() != null && !datos.getNombreAlumno().isBlank()) {
            existente.setNombreAlumno(datos.getNombreAlumno().trim());
        }
        if (datos.getApellidoAlumno() != null) {
            existente.setApellidoAlumno(datos.getApellidoAlumno().trim());
        }
        if (datos.getRutAlumno() != null && !datos.getRutAlumno().isBlank()) {
            existente.setRutAlumno(datos.getRutAlumno());
        }

        // Actualizar feedback
        String nuevoFeedback = (datos.getFeedbackDocente() != null && !datos.getFeedbackDocente().isBlank())
                ? datos.getFeedbackDocente()
                : datos.getFeedback();
        if (nuevoFeedback != null && !nuevoFeedback.isBlank()) {
            existente.setFeedbackDocente(nuevoFeedback);
            existente.setFeedback(nuevoFeedback);
        }

        // Actualizar puntajes si se enviaron
        if (datos.getPuntaje1() != null) existente.setPuntaje1(datos.getPuntaje1());
        if (datos.getPuntaje2() != null) existente.setPuntaje2(datos.getPuntaje2());
        if (datos.getPuntaje3() != null) existente.setPuntaje3(datos.getPuntaje3());
        if (datos.getPuntaje4() != null) existente.setPuntaje4(datos.getPuntaje4());

        // Asegurar no nulos en existente
        if (existente.getPuntaje1() == null) existente.setPuntaje1(0.0);
        if (existente.getPuntaje2() == null) existente.setPuntaje2(0.0);
        if (existente.getPuntaje3() == null) existente.setPuntaje3(0.0);
        if (existente.getPuntaje4() == null) existente.setPuntaje4(0.0);

        // Resolver plantilla segura
        Long plantillaId = datos.getPlantillaRubricaId() != null
                ? datos.getPlantillaRubricaId()
                : existente.getPlantillaRubricaId();
        String tituloRubrica = datos.getTituloRubrica() != null
                ? datos.getTituloRubrica()
                : existente.getTituloRubrica();

        PlantillaRubrica plantilla = resolverPlantillaSegura(plantillaId, tituloRubrica);
        existente.setPlantillaRubricaId(plantilla.getId());
        existente.setTituloRubrica(plantilla.getTitulo());
        existente.setTitulo(plantilla.getTitulo());

        double puntajeMax = (plantilla.getPuntajeMaximo() != null && plantilla.getPuntajeMaximo() > 0)
                ? plantilla.getPuntajeMaximo()
                : 20.0;

        Double notaDirecta = datos.getNotaFinal() != null ? datos.getNotaFinal() : datos.getNota();
        boolean datosSinPuntajes = (datos.getPuntaje1() == null && datos.getPuntaje2() == null
                && datos.getPuntaje3() == null && datos.getPuntaje4() == null);

        BigDecimal nota;
        if (datosSinPuntajes && notaDirecta != null && notaDirecta >= 1.0) {
            nota = BigDecimal.valueOf(notaDirecta).setScale(2, RoundingMode.HALF_UP);
            double factor = Math.max(0.0, Math.min(1.0, (notaDirecta - 1.0) / 6.0));
            double porCriterio = BigDecimal.valueOf((puntajeMax * factor) / 4.0)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
            existente.setPuntaje1(porCriterio);
            existente.setPuntaje2(porCriterio);
            existente.setPuntaje3(porCriterio);
            existente.setPuntaje4(porCriterio);
        } else {
            double puntajeObtenido = existente.getPuntaje1()
                    + existente.getPuntaje2()
                    + existente.getPuntaje3()
                    + existente.getPuntaje4();

            nota = BigDecimal.valueOf(1.0)
                    .add(BigDecimal.valueOf(puntajeObtenido)
                            .divide(BigDecimal.valueOf(puntajeMax), 10, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(6.0)))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        if (nota.compareTo(BigDecimal.valueOf(7.0)) > 0) {
            nota = BigDecimal.valueOf(7.00);
        } else if (nota.compareTo(BigDecimal.valueOf(1.0)) < 0) {
            nota = BigDecimal.valueOf(1.00);
        }

        existente.setNotaFinal(nota.doubleValue());
        existente.setNota(nota.doubleValue());
        existente.setEstado(nota.compareTo(BigDecimal.valueOf(4.0)) >= 0 ? "APROBADO" : "REPROBADO");

        if (existente.getFeedbackDocente() == null || existente.getFeedbackDocente().isBlank()) {
            existente.setFeedbackDocente("APROBADO".equals(existente.getEstado())
                    ? "¡Buen trabajo! Nota: " + nota
                    : "Debe reforzar los contenidos. Nota: " + nota);
        }
        existente.setFeedback(existente.getFeedbackDocente());

        return evaluacionRepo.save(existente);
    }

    /**
     * Permite al alumno ver sus evaluaciones buscando por RUT con normalización
     * y fallback tolerante (con/sin puntos, mayúsculas/minúsculas o sin guion).
     */
    public List<EvaluacionAlumno> listarPorRutAlumno(String rut) {
        if (rut == null || rut.isBlank()) {
            return List.of();
        }

        String rutNormalizado = RutUtils.normalizarRut(rut);

        // 1. Búsqueda directa por RUT normalizado (ej: 12345678-K)
        List<EvaluacionAlumno> resultado = evaluacionRepo.findByRutAlumno(rutNormalizado);

        // 2. Fallback case-insensitive
        if (resultado.isEmpty()) {
            resultado = evaluacionRepo.findByRutAlumnoIgnoreCase(rutNormalizado);
        }

        // 3. Fallback tolerante para registros legados que contengan puntos, espacios o sin guion
        if (resultado.isEmpty()) {
            String rutSinPuntosNiGuion = RutUtils.limpiarSinPuntosNiGuion(rut);
            resultado = evaluacionRepo.buscarPorRutFlexible(rutNormalizado, rutSinPuntosNiGuion, rut.trim());
        }

        return resultado;
    }

    // ── Métodos Auxiliares ────────────────────────────────────────────────────

    /**
     * Resuelve de forma infalible una plantilla de rúbrica:
     * 1. Por ID si es > 0 y existe.
     * 2. Por título si se proporcionó.
     * 3. La primera plantilla existente en la BD.
     * 4. Si la tabla está vacía, crea una plantilla base predeterminada.
     */
    public PlantillaRubrica resolverPlantillaSegura(Long plantillaId, String tituloRubrica) {
        // 1. Buscar por id si viene especificado
        if (plantillaId != null && plantillaId > 0) {
            Optional<PlantillaRubrica> opt = plantillaRepo.findById(plantillaId);
            if (opt.isPresent()) {
                return opt.get();
            }
        }

        // 2. Buscar por título exacto o insensible a mayúsculas
        if (tituloRubrica != null && !tituloRubrica.isBlank()) {
            List<PlantillaRubrica> porTitulo = plantillaRepo.findByTituloIgnoreCase(tituloRubrica.trim());
            if (!porTitulo.isEmpty()) {
                return porTitulo.get(0);
            }
        }

        // 3. Buscar cualquier plantilla existente en la BD
        List<PlantillaRubrica> todas = plantillaRepo.findAll();
        if (!todas.isEmpty()) {
            return todas.get(0);
        }

        // 4. Crear plantilla base por defecto si la base de datos no tiene ninguna
        PlantillaRubrica base = new PlantillaRubrica();
        base.setTitulo(tituloRubrica != null && !tituloRubrica.isBlank()
                ? tituloRubrica.trim()
                : "Rúbrica Base de Evaluación");
        base.setTipo("Individual");
        base.setCriterio1Nombre("Criterio 1: Desempeño Técnico");
        base.setCriterio2Nombre("Criterio 2: Procedimiento y Seguridad");
        base.setCriterio3Nombre("Criterio 3: Disciplina y Presentación");
        base.setCriterio4Nombre("Criterio 4: Cumplimiento de Objetivos");
        base.setPuntajeMaximo(20.0);
        base.setFechaCreacion(LocalDateTime.now());

        return plantillaRepo.save(base);
    }
}
