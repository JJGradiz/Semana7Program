package ni.uam.edu.practicabd.Modelos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter@AllArgsConstructor
@Setter@NoArgsConstructor
public class Categoria {
    private Integer id;
    private String nombre;
    private boolean activa;

    @Override
    public String toString() {
        return nombre != null ? nombre : "";
    }
}
