package cl.esfocar.backend_esfocar.repository;

import cl.esfocar.backend_esfocar.model.PlantillaRubrica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaRubricaRepository
        extends JpaRepository<PlantillaRubrica, Long> {
    // Búsqueda por tipo ("Individual" / "Grupal")
    List<PlantillaRubrica> findByTipo(String tipo);
}
