package cl.esfocar.backend_esfocar.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plantillas_rubrica")
public class PlantillaRubrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    // "Individual" o "Grupal"
    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String criterio1Nombre;

    @Column(nullable = false)
    private String criterio2Nombre;

    @Column(nullable = false)
    private String criterio3Nombre;

    @Column(nullable = false)
    private String criterio4Nombre;

    // Puntaje máximo posible (suma de los 4 criterios)
    @Column(nullable = false)
    private Double puntajeMaximo;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.tipo == null || this.tipo.isBlank()) {
            this.tipo = "Individual";
        }
        if (this.puntajeMaximo == null || this.puntajeMaximo <= 0) {
            this.puntajeMaximo = 20.0;
        }
    }
}
