package cl.esfocar.backend_esfocar.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para crear o editar un Documento.
 */
public record DocumentoRequest(

        @NotBlank(message = "El nombre del archivo es obligatorio")
        String nombreArchivo,

        @NotBlank(message = "El tipo de archivo es obligatorio")
        String tipoArchivo,

        @NotBlank(message = "La ruta URI es obligatoria")
        String rutaUri
) {}
