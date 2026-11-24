package cl.esfocar.backend_esfocar.config;

import cl.esfocar.backend_esfocar.model.Usuario;
import cl.esfocar.backend_esfocar.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Inicializador de datos: crea el usuario administrador maestro al arrancar si no existe.
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdminUser(UsuarioRepository usuarioRepository) {
        return args -> {
            String adminCorreo = "admin@esfocar.cl";
            if (!usuarioRepository.existsByCorreo(adminCorreo)) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setApellido("ESFOCAR");
                admin.setRut(null);                      // RUT opcional para Admin
                admin.setFechaNacimiento("01/01/1990");
                admin.setCorreo(adminCorreo);
                admin.setContrasena("123456");           // En producción usar BCrypt
                admin.setRol("Admin");
                admin.setCuentaActiva(true);
                usuarioRepository.save(admin);
                System.out.println(">>> [DataInitializer] Admin maestro creado: " + adminCorreo);
            } else {
                System.out.println(">>> [DataInitializer] Admin maestro ya existe.");
            }
        };
    }
}
