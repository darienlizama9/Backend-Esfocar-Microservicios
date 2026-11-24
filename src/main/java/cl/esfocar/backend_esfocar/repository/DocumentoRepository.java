package cl.esfocar.backend_esfocar.repository;

import cl.esfocar.backend_esfocar.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
}
