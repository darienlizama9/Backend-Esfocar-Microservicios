package cl.esfocar.backend_esfocar.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para un Documento.
 */
public record DocumentoResponse(
        Long id,
        String nombreArchivo,
        String tipoArchivo,
        String rutaUri,
        LocalDateTime fechaSubida
) {}
