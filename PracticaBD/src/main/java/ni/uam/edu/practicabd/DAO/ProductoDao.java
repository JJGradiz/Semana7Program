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

public class ProductoDao implements CRUD<Producto> {

    @Override
    public void guardar(Producto entidad) {
        String sql = "INSERT INTO producto (codigo, nombre, categoria_id, precio_venta, existencia, ruta_imagen, activo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, entidad.getCodigo());
            ps.setString(2, entidad.getNombre());
            ps.setInt(3, entidad.getCategoria() != null ? entidad.getCategoria().getId() : 0);
            ps.setBigDecimal(4, entidad.getPrecioVenta());
            ps.setInt(5, entidad.getExistencia());
            ps.setString(6, entidad.getRutaImagen());
            ps.setBoolean(7, entidad.isActivo());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error de base de datos al guardar producto: " + e.getMessage());
        }
    }

    @Override
    public List<Producto> listar() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.codigo, p.nombre, p.categoria_id, p.precio_venta, p.existencia, p.ruta_imagen, p.activo, " +
                "c.nombre AS categoria_nombre, c.activa AS categoria_activa " +
                "FROM producto p " +
                "INNER JOIN categoria c ON p.categoria_id = c.id " +
                "ORDER BY p.nombre";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Categoria cat = new Categoria(
                        rs.getInt("categoria_id"),
                        rs.getString("categoria_nombre"),
                        rs.getBoolean("categoria_activa")
                );

                Producto p = new Producto(
                        null,
                        rs.getString("nombre"),
                        rs.getString("codigo"),
                        cat,
                        rs.getBigDecimal("precio_venta"),
                        rs.getInt("existencia"),
                        rs.getString("ruta_imagen"),
                        rs.getBoolean("activo")
                );
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al listar productos: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public void eliminar(Producto entidad) {
        String sql = "DELETE FROM producto WHERE codigo = ?";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entidad.getCodigo());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void actualizar(Producto entidad) {
        String sql = "UPDATE producto SET nombre = ?, categoria_id = ?, precio_venta = ?, existencia = ?, ruta_imagen = ?, activo = ? " +
                "WHERE codigo = ?";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());
            ps.setInt(2, entidad.getCategoria() != null ? entidad.getCategoria().getId() : 0);
            ps.setBigDecimal(3, entidad.getPrecioVenta());
            ps.setInt(4, entidad.getExistencia());
            ps.setString(5, entidad.getRutaImagen());
            ps.setBoolean(6, entidad.isActivo());
            ps.setString(7, entidad.getCodigo());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean existeCodigo(String codigo) {
        String sql = "SELECT 1 FROM producto WHERE codigo = ?";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<Producto> buscarPorVariosCriterios(String criterio, String texto) {
        List<Producto> lista = new ArrayList<>();

        String sql = "SELECT p.codigo, p.nombre, p.categoria_id, p.precio_venta, p.existencia, p.ruta_imagen, p.activo, " +
                "c.nombre AS categoria_nombre, c.activa AS categoria_activa " +
                "FROM producto p " +
                "INNER JOIN categoria c ON p.categoria_id = c.id " +
                "WHERE ";

        boolean todosLosCampos = "Todos los campos".equals(criterio);
        switch (criterio) {
            case "Todos los campos":
                sql += "(p.codigo ILIKE ? OR p.nombre ILIKE ? OR c.nombre ILIKE ? " +
                        "OR CAST(p.precio_venta AS TEXT) ILIKE ? OR CAST(p.existencia AS TEXT) ILIKE ? " +
                        "OR COALESCE(p.ruta_imagen, '') ILIKE ? OR CAST(p.activo AS TEXT) ILIKE ?)";
                break;
            case "Código":
                sql += "p.codigo ILIKE ?";
                break;
            case "Nombre":
                sql += "p.nombre ILIKE ?";
                break;
            case "Categoría":
                sql += "c.nombre ILIKE ?";
                break;
            case "Precio":
                sql += "CAST(p.precio_venta AS TEXT) ILIKE ?";
                break;
            case "Existencia":
                sql += "CAST(p.existencia AS TEXT) ILIKE ?";
                break;
            default:
                sql += "p.nombre ILIKE ?";
        }

        sql += " ORDER BY p.nombre";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            String busqueda = "%" + texto + "%";
            if (todosLosCampos) {
                for (int i = 1; i <= 7; i++) {
                    ps.setString(i, busqueda);
                }
            } else {
                ps.setString(1, busqueda);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Categoria cat = new Categoria(rs.getInt("categoria_id"), rs.getString("categoria_nombre"), rs.getBoolean("categoria_activa"));
                    Producto p = new Producto(null, rs.getString("nombre"), rs.getString("codigo"), cat, rs.getBigDecimal("precio_venta"), rs.getInt("existencia"), rs.getString("ruta_imagen"), rs.getBoolean("activo"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
