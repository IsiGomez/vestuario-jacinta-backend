package cl.duoc.jacinta.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_boleta")
@Getter @Setter @NoArgsConstructor
public class DetalleBoleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "boleta_id", nullable = false)
    private Boleta boleta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 150)
    private String nombreProducto;

    @Column(nullable = false)
    private Integer precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(length = 10)
    private String talla;

    @Column(length = 50)
    private String color;
}