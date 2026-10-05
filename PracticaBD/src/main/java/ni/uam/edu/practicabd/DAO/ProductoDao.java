package ni.uam.edu.practicabd.DAO;

import ni.uam.edu.practicabd.Interfaces.CRUD;
import ni.uam.edu.practicabd.Modelos.Categoria;
import ni.uam.edu.practicabd.Modelos.Producto;
import ni.uam.edu.practicabd.Util.DataBaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDao implements CRUD<Producto> {

    @Override
    public void guardar(Producto entidad) {
        validarProducto(entidad);
        if (existeCodigo(entidad.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un producto con ese código.");
        }
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
            throw new IllegalStateException(mensajeError(e, "guardar el producto"), e);
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
            throw new IllegalStateException("No se pudieron cargar los productos. Verifique la conexión con la base de datos.", e);
        }
        return lista;
    }

    @Override
    public void eliminar(Producto entidad) {
        if (entidad == null || entidad.getCodigo() == null || entidad.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un producto para eliminar.");
        }
        if (!existeCodigo(entidad.getCodigo())) {
            throw new IllegalArgumentException("El producto seleccionado ya no existe.");
        }
        String sql = "DELETE FROM producto WHERE codigo = ?";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entidad.getCodigo());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("El producto seleccionado ya no existe.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(mensajeError(e, "eliminar el producto"), e);
        }
    }

    @Override
    public void actualizar(Producto entidad) {
        actualizar(entidad, entidad == null ? null : entidad.getCodigo());
    }

    public void actualizar(Producto entidad, String codigoOriginal) {
        validarProducto(entidad);
        if (codigoOriginal == null || codigoOriginal.trim().isEmpty() || !existeCodigo(codigoOriginal)) {
            throw new IllegalArgumentException("Debe seleccionar un producto existente para actualizar.");
        }
        if (existeCodigo(entidad.getCodigo(), codigoOriginal)) {
            throw new IllegalArgumentException("Ya existe un producto con ese código.");
        }
        String sql = "UPDATE producto SET codigo = ?, nombre = ?, categoria_id = ?, precio_venta = ?, existencia = ?, ruta_imagen = ?, activo = ? " +
                "WHERE codigo = ?";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entidad.getCodigo());
            ps.setString(2, entidad.getNombre());
            ps.setInt(3, entidad.getCategoria() != null ? entidad.getCategoria().getId() : 0);
            ps.setBigDecimal(4, entidad.getPrecioVenta());
            ps.setInt(5, entidad.getExistencia());
            ps.setString(6, entidad.getRutaImagen());
            ps.setBoolean(7, entidad.isActivo());
            ps.setString(8, codigoOriginal);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("El producto seleccionado ya no existe.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(mensajeError(e, "actualizar el producto"), e);
        }
    }

    public boolean existeCodigo(String codigo) {
        return existeCodigo(codigo, null);
    }

    private boolean existeCodigo(String codigo, String codigoExcluir) {
        String sql = "SELECT COUNT(*) FROM producto WHERE codigo = ?"
                + (codigoExcluir == null ? "" : " AND codigo <> ?");
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, codigo);
            if (codigoExcluir != null) {
                ps.setString(2, codigoExcluir);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo validar si el código del producto ya existe.", e);
        }
    }

    private void validarProducto(Producto entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("Los datos del producto son obligatorios.");
        }
        if (entidad.getCodigo() == null || entidad.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del producto es obligatorio.");
        }
        if (entidad.getNombre() == null || entidad.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (entidad.getCategoria() == null || entidad.getCategoria().getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoría válida.");
        }
        if (entidad.getPrecioVenta() == null || entidad.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero.");
        }
        if (entidad.getExistencia() < 0) {
            throw new IllegalArgumentException("La existencia no puede ser negativa.");
        }
    }



    private String mensajeError(SQLException e, String operacion) {
        if ("23505".equals(e.getSQLState())) {
            return "Ya existe un producto con ese código.";
        }
        if ("23503".equals(e.getSQLState())) {
            return "La categoría seleccionada no existe o está relacionada con otros registros.";
        }
        return "No se pudo " + operacion + ". Verifique la conexión y los datos de la base de datos.";
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
            throw new IllegalStateException("No se pudieron buscar los productos. Verifique la conexión con la base de datos.", e);
        }
        return lista;
    }
}
