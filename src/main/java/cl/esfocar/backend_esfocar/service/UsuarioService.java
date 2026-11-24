package cl.esfocar.backend_esfocar.service;

import cl.esfocar.backend_esfocar.dto.*;
import cl.esfocar.backend_esfocar.model.Usuario;
import cl.esfocar.backend_esfocar.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Lógica de negocio para autenticación y administración de usuarios.
 */
@Service
public class UsuarioService {

    private static final Pattern RUT_PATTERN =
            Pattern.compile("^[0-9]{7,8}-[0-9kK]$");

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ── Registro ─────────────────────────────────────────────────────────────

    public UsuarioResponse registrar(RegistroRequest req) {
        String rol = req.rol();

        // Solo se permite registrar Docente o Estudiante vía este endpoint
        if (!rol.equals("Docente") && !rol.equals("Estudiante")) {
            throw new IllegalArgumentException("Solo se pueden registrar roles 'Docente' o 'Estudiante'.");
        }

        // Si es Estudiante el RUT es obligatorio y debe tener formato chileno
        if (rol.equals("Estudiante")) {
            if (req.rut() == null || req.rut().isBlank()) {
                throw new IllegalArgumentException("El RUT es obligatorio para el rol Estudiante.");
            }
            if (!RUT_PATTERN.matcher(req.rut()).matches()) {
                throw new IllegalArgumentException(
                        "El RUT no tiene un formato válido. Use el formato 12345678-9.");
            }
        }

        // Verificar correo único
        if (usuarioRepository.existsByCorreo(req.correo())) {
            throw new IllegalArgumentException("El correo ya está registrado: " + req.correo());
        }

        // Verificar RUT único (solo cuando se proporciona)
        if (req.rut() != null && !req.rut().isBlank()
                && usuarioRepository.existsByRut(req.rut())) {
            throw new IllegalArgumentException("El RUT ya está registrado: " + req.rut());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(req.nombre());
        usuario.setApellido(req.apellido());
        usuario.setRut((req.rut() != null && req.rut().isBlank()) ? null : req.rut());
        usuario.setFechaNacimiento(req.fechaNacimiento());
        usuario.setCorreo(req.correo());
        usuario.setContrasena(req.contrasena());   // En producción: BCrypt
        usuario.setRol(rol);
        usuario.setCuentaActiva(false);             // Pendiente de aprobación

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponse(guardado);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public UsuarioResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByCorreo(req.correo())
                .orElseThrow(() -> new IllegalArgumentException("Correo o contraseña incorrectos."));

        if (!usuario.getContrasena().equals(req.contrasena())) {
            throw new IllegalArgumentException("Correo o contraseña incorrectos.");
        }

        // Bloquear cuentas inactivas que no sean Admin
        if (!Boolean.TRUE.equals(usuario.getCuentaActiva())
                && !usuario.getRol().equals("Admin")) {
            throw new IllegalStateException(
                    "Cuenta pendiente de aprobación por el Administrador.");
        }

        return toResponse(usuario);
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<UsuarioResponse> listarPendientes() {
        return usuarioRepository.findByCuentaActivaFalse()
                .stream().map(this::toResponse).toList();
    }

    public List<UsuarioResponse> listarPorRol(String rol) {
        return usuarioRepository.findByRol(rol)
                .stream().map(this::toResponse).toList();
    }

    // ── Administración ────────────────────────────────────────────────────────

    public UsuarioResponse aprobarCuenta(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        usuario.setCuentaActiva(true);
        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse actualizarRol(Long id, ActualizarRolRequest req) {
        String nuevoRol = req.rol();
        if (!nuevoRol.equals("Admin") && !nuevoRol.equals("Docente") && !nuevoRol.equals("Estudiante")) {
            throw new IllegalArgumentException("Rol inválido. Use: Admin, Docente o Estudiante.");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        usuario.setRol(nuevoRol);
        return toResponse(usuarioRepository.save(usuario));
    }

    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // ── Mapeo ─────────────────────────────────────────────────────────────────

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getApellido(),
                u.getRut(),
                u.getFechaNacimiento(),
                u.getCorreo(),
                u.getRol(),
                u.getCuentaActiva()
        );
    }
}
