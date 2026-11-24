package cl.esfocar.backend_esfocar.service;

import cl.esfocar.backend_esfocar.dto.DocumentoRequest;
import cl.esfocar.backend_esfocar.dto.DocumentoResponse;
import cl.esfocar.backend_esfocar.model.Documento;
import cl.esfocar.backend_esfocar.repository.DocumentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lógica de negocio para la gestión de documentos (rúbricas/pautas PDF/DOCX).
 */
@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;

    public DocumentoService(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    public DocumentoResponse crear(DocumentoRequest req) {
        Documento doc = new Documento();
        doc.setNombreArchivo(req.nombreArchivo());
        doc.setTipoArchivo(req.tipoArchivo());
        doc.setRutaUri(req.rutaUri());
        return toResponse(documentoRepository.save(doc));
    }

    public List<DocumentoResponse> listarTodos() {
        return documentoRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DocumentoResponse actualizar(Long id, DocumentoRequest req) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con id: " + id));
        doc.setNombreArchivo(req.nombreArchivo());
        doc.setTipoArchivo(req.tipoArchivo());
        doc.setRutaUri(req.rutaUri());
        return toResponse(documentoRepository.save(doc));
    }

    public void eliminar(Long id) {
        if (!documentoRepository.existsById(id)) {
            throw new EntityNotFoundException("Documento no encontrado con id: " + id);
        }
        documentoRepository.deleteById(id);
    }

    private DocumentoResponse toResponse(Documento d) {
        return new DocumentoResponse(
                d.getId(),
                d.getNombreArchivo(),
                d.getTipoArchivo(),
                d.getRutaUri(),
                d.getFechaSubida()
        );
    }
}
