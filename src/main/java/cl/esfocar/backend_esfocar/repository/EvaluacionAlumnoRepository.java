package cl.esfocar.backend_esfocar.repository;

import cl.esfocar.backend_esfocar.model.EvaluacionAlumno;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface EvaluacionAlumnoRepository
        extends JpaRepository<EvaluacionAlumno, Long> {
    // Permite al alumno ver sus notas por RUT
    List<EvaluacionAlumno> findByRutAlumno(String rutAlumno);
}
