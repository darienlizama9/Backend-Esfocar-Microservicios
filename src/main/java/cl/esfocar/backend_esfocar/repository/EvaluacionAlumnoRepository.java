package cl.esfocar.backend_esfocar.repository;

import cl.esfocar.backend_esfocar.model.EvaluacionAlumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluacionAlumnoRepository
        extends JpaRepository<EvaluacionAlumno, Long> {

    // Permite al alumno ver sus notas por RUT exacto
    List<EvaluacionAlumno> findByRutAlumno(String rutAlumno);

    // Búsqueda insensible a mayúsculas/minúsculas
    List<EvaluacionAlumno> findByRutAlumnoIgnoreCase(String rutAlumno);

    // Búsqueda flexible con fallback para soportar registros legados con puntos, espacios o formato variable
    @Query("SELECT e FROM EvaluacionAlumno e WHERE " +
           "UPPER(REPLACE(REPLACE(e.rutAlumno, '.', ''), ' ', '')) = UPPER(:rutNormalizado) " +
           "OR UPPER(REPLACE(REPLACE(REPLACE(e.rutAlumno, '.', ''), ' ', ''), '-', '')) = UPPER(:rutSinPuntosNiGuion) " +
           "OR UPPER(e.rutAlumno) = UPPER(:rutOriginal)")
    List<EvaluacionAlumno> buscarPorRutFlexible(
            @Param("rutNormalizado") String rutNormalizado,
            @Param("rutSinPuntosNiGuion") String rutSinPuntosNiGuion,
            @Param("rutOriginal") String rutOriginal);
}
