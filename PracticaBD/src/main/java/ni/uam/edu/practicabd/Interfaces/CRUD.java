package ni.uam.edu.practicabd.Interfaces;

import java.util.List;

public interface CRUD<T> {
    void guardar(T entidad);
    List<T> listar();
    void eliminar(T entidad);
    void actualizar(T entidad);
}
