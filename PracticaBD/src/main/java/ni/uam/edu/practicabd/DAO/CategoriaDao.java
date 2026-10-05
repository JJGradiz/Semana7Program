package ni.uam.edu.practicabd.DAO;

import ni.uam.edu.practicabd.Interfaces.CRUD;
import ni.uam.edu.practicabd.Modelos.Categoria;
import ni.uam.edu.practicabd.Modelos.Producto;
import ni.uam.edu.practicabd.Util.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDao implements CRUD<Categoria> {

    @Override
    public void guardar(Categoria entidad) {
        validarCategoria(entidad, false);
        String sql = "INSERT INTO categoria (nombre, activa) VALUES (?, ?)";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());
            ps.setBoolean(2, entidad.isActiva());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(mensajeError(e, "guardar la categoría"), e);
        }
    }

    @Override
    public List<Categoria> listar() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, activa FROM categoria ORDER BY nombre";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getBoolean("activa")
                );
                lista.add(c);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron cargar las categorías. Verifique la conexión con la base de datos.", e);
        }
        return lista;
    }

    @Override
    public void eliminar(Categoria entidad) {
        if (entidad == null || entidad.getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoría para eliminar.");
        }
        if (!existeId(entidad.getId())) {
            throw new IllegalArgumentException("La categoría seleccionada ya no existe.");
        }
        if (tieneProductos(entidad.getId())) {
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene productos asociados.");
        }
        String sql = "DELETE FROM categoria WHERE id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, entidad.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("La categoría seleccionada ya no existe.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(mensajeError(e, "eliminar la categoría"), e);
        }
    }

    @Override
    public void actualizar(Categoria entidad) {
        validarCategoria(entidad, true);
        String sql = "UPDATE categoria SET nombre = ?, activa = ? WHERE id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());
            ps.setBoolean(2, entidad.isActiva());
            ps.setInt(3, entidad.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("La categoría seleccionada ya no existe.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(mensajeError(e, "actualizar la categoría"), e);
        }
    }

    public boolean existeNombre(String nombre, Integer idExcluir) {
        String sql = "SELECT 1 FROM categoria WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?))"
                + (idExcluir == null ? "" : " AND id <> ?");
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nombre);
            if (idExcluir != null) {
                ps.setInt(2, idExcluir);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo validar si el nombre de categoría ya existe.", e);
        }
    }

    private void validarCategoria(Categoria entidad, boolean requiereId) {
        if (entidad == null || entidad.getNombre() == null || entidad.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        }
        if (requiereId && entidad.getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoría para actualizar.");
        }
        if (requiereId && !existeId(entidad.getId())) {
            throw new IllegalArgumentException("La categoría seleccionada ya no existe.");
        }
        if (existeNombre(entidad.getNombre().trim(), requiereId ? entidad.getId() : null)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }
    }

    private boolean existeId(Integer id) {
        String sql = "SELECT 1 FROM categoria WHERE id = ?";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo validar la categoría seleccionada.", e);
        }
    }

    private boolean tieneProductos(Integer categoriaId) {
        String sql = "SELECT 1 FROM producto WHERE categoria_id = ?";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo comprobar si la categoría tiene productos asociados.", e);
        }
    }

    private String mensajeError(SQLException e, String operacion) {
        if ("23505".equals(e.getSQLState())) {
            return "Ya existe una categoría con ese nombre.";
        }
        if ("23503".equals(e.getSQLState())) {
            return "No se puede eliminar la categoría porque tiene productos asociados.";
        }
        return "No se pudo " + operacion + ". Verifique la conexión y los datos de la base de datos.";
    }

    public List<Categoria> buscarPorNombre(String texto) {
        List<Categoria> lista = new ArrayList<>();

        String sql = "SELECT id, nombre, activa FROM categoria WHERE nombre  ILIKE ? ORDER BY nombre";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Categoria c = new Categoria(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getBoolean("activa")
                    );
                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron buscar las categorías. Verifique la conexión con la base de datos.", e);
        }
        return lista;
    }
}
