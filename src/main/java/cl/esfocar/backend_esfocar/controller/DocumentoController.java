package cl.esfocar.backend_esfocar.controller;

import cl.esfocar.backend_esfocar.dto.DocumentoRequest;
import cl.esfocar.backend_esfocar.dto.DocumentoResponse;
import cl.esfocar.backend_esfocar.service.DocumentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST para gestión de documentos (rúbricas/pautas PDF o Word).
 * Base path: /api/documentos
 */
@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    // ── POST /api/documentos ──────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<DocumentoResponse> crear(
            @Valid @RequestBody DocumentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentoService.crear(request));
    }

    // ── GET /api/documentos ───────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<DocumentoResponse>> listarTodos() {
        return ResponseEntity.ok(documentoService.listarTodos());
    }

    // ── PUT /api/documentos/{id} ──────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<DocumentoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DocumentoRequest request) {
        return ResponseEntity.ok(documentoService.actualizar(id, request));
    }

    // ── DELETE /api/documentos/{id} ───────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        documentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
