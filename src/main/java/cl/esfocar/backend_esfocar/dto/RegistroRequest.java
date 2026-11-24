package cl.esfocar.backend_esfocar.dto;

import jakarta.validation.constraints.*;

/**
 * DTO para registro de Docente o Estudiante.
 * Si el rol es "Estudiante", el RUT es obligatorio y debe tener formato chileno.
 */
public record RegistroRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        // RUT: validado en el service según el rol
        String rut,

        @NotBlank(message = "La fecha de nacimiento es obligatoria")
        String fechaNacimiento,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String contrasena,

        // "Docente" o "Estudiante"
        @NotBlank(message = "El rol es obligatorio")
        String rol
) {}
