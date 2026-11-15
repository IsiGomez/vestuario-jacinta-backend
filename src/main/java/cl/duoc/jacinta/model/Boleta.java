package cl.duoc.jacinta.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boleta")
@Getter @Setter @NoArgsConstructor
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoBoleta estado = EstadoBoleta.PENDIENTE;

    @Column(nullable = false)
    private Boolean stockReservado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 50)
    private String clienteNombre;

    @Column(nullable = false, length = 100)
    private String clienteApellidos;

    @Column(nullable = false, length = 100)
    private String clienteCorreo;

    @Column(nullable = false, length = 300)
    private String entregaCalle;

    @Column(length = 50)
    private String entregaDepartamento;

    @Column(nullable = false, length = 100)
    private String entregaRegion;

    @Column(nullable = false, length = 100)
    private String entregaComuna;

    @Column(length = 500)
    private String entregaIndicaciones;

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleBoleta> detalles = new ArrayList<>();

    @PrePersist
    void antesDeGuardar() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    public void agregarDetalle(DetalleBoleta detalle) {
        detalle.setBoleta(this);
        detalles.add(detalle);
    }

    public int calcularTotal() {
        return detalles.stream()
                .mapToInt(d -> d.getPrecioUnitario() * d.getCantidad())
                .sum();
    }
}