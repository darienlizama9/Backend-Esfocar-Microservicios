package cl.esfocar.backend_esfocar.controller;

import cl.esfocar.backend_esfocar.dto.*;
import cl.esfocar.backend_esfocar.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST para gestión de usuarios ESFOCAR.
 * Base path: /api/usuarios
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ── POST /api/usuarios/registro ───────────────────────────────────────────
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody RegistroRequest request) {
        UsuarioResponse response = usuarioService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── POST /api/usuarios/login ──────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(
            @Valid @RequestBody LoginRequest request) {
        UsuarioResponse response = usuarioService.login(request);
        return ResponseEntity.ok(response);
    }

    // ── GET /api/usuarios/ ────────────────────────────────────────────────────
    @GetMapping("/")
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // ── GET /api/usuarios/pendientes ──────────────────────────────────────────
    @GetMapping("/pendientes")
    public ResponseEntity<List<UsuarioResponse>> listarPendientes() {
        return ResponseEntity.ok(usuarioService.listarPendientes());
    }

    // ── GET /api/usuarios/rol/{rol} ───────────────────────────────────────────
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<UsuarioResponse>> listarPorRol(
            @PathVariable String rol) {
        return ResponseEntity.ok(usuarioService.listarPorRol(rol));
    }

    // ── PUT /api/usuarios/{id}/aprobar ────────────────────────────────────────
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<UsuarioResponse> aprobar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.aprobarCuenta(id));
    }

    // ── PUT /api/usuarios/{id}/rol ────────────────────────────────────────────
    @PutMapping("/{id}/rol")
    public ResponseEntity<UsuarioResponse> actualizarRol(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarRolRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarRol(id, request));
    }

    // ── DELETE /api/usuarios/{id} ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
