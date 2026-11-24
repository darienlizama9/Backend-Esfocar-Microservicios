package cl.esfocar.backend_esfocar.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    // RUT único cuando no es nulo (obligatorio para Estudiante)
    @Column(unique = true)
    private String rut;

    @Column(nullable = false)
    private String fechaNacimiento;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String contrasena;

    // "Admin", "Docente" o "Estudiante"
    @Column(nullable = false)
    private String rol = "Docente";

    // false por defecto; Admin lo cambia a true
    @Column(nullable = false)
    private Boolean cuentaActiva = false;
}
