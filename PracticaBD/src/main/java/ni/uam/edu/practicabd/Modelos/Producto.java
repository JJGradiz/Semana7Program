package ni.uam.edu.practicabd.Modelos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter@AllArgsConstructor
@Setter@NoArgsConstructor
public class Producto {
    private Integer id;
    private String nombre;
    private String codigo;
    private Categoria categoria;
    private BigDecimal precioVenta;
    private int existencia;
    private String rutaImagen;
    private boolean activo;
}
