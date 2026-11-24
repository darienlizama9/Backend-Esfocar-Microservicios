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
