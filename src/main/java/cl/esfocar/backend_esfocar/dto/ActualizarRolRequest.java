package cl.esfocar.backend_esfocar.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para actualizar el rol de un usuario.
 */
public record ActualizarRolRequest(

        @NotBlank(message = "El rol es obligatorio")
        String rol
) {}
