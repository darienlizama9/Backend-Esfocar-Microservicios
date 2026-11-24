package cl.esfocar.backend_esfocar.dto;

/**
 * DTO de respuesta con los datos del usuario (sin contraseña).
 */
public record UsuarioResponse(
        Long id,
        String nombre,
        String apellido,
        String rut,
        String fechaNacimiento,
        String correo,
        String rol,
        Boolean cuentaActiva
) {}
