package cl.esfocar.backend_esfocar.repository;

import cl.esfocar.backend_esfocar.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByRut(String rut);

    List<Usuario> findByCuentaActivaFalse();

    List<Usuario> findByRol(String rol);
}
