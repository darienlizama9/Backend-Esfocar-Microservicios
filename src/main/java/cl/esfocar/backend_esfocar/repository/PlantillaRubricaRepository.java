package cl.esfocar.backend_esfocar.repository;

import cl.esfocar.backend_esfocar.model.PlantillaRubrica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantillaRubricaRepository
        extends JpaRepository<PlantillaRubrica, Long> {

    // Búsqueda por tipo ("Individual" / "Grupal")
    List<PlantillaRubrica> findByTipo(String tipo);

    // Búsqueda por título insensible a mayúsculas/minúsculas
    List<PlantillaRubrica> findByTituloIgnoreCase(String titulo);

    Optional<PlantillaRubrica> findFirstByTituloIgnoreCase(String titulo);
}
