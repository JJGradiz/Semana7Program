package ni.uam.edu.practicabd.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ni.uam.edu.practicabd.DAO.CategoriaDao;
import ni.uam.edu.practicabd.DAO.ProductoDao;
import ni.uam.edu.practicabd.Modelos.Categoria;
import ni.uam.edu.practicabd.Modelos.Producto;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

public class ProductoController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtRuta;
    @FXML private TextField txtExistencia;
    @FXML private TextField txtDatoFiltrar;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<String> cmbCriterios;
    @FXML private CheckBox chkActivo;
    @FXML private Button btnGuardar;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, Boolean> colActivo;
    @FXML private TableColumn<Producto, String> colRuta;

    private ProductoDao productosDao = new ProductoDao();
    private CategoriaDao categoriaDao = new CategoriaDao();
    private Producto productoSeleccionado = null;

    @FXML
    public void initialize() {
        cargarCategorias();
        cargarCriterios();
        configurarTabla();
        configurarContextMenu();
        cargarProductos();
    }

    public void cargarCategorias() {
        if (cmbCategoria != null) {
            try {
                cmbCategoria.setItems(FXCollections.observableArrayList(categoriaDao.listar()));
            } catch (IllegalStateException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de base de datos", e.getMessage());
            }
        }
    }

    public void cargarCriterios(){
        if(cmbCriterios != null) {
            cmbCriterios.setItems(FXCollections.observableArrayList("Todos los campos", "Código", "Nombre",
                    "Categoría", "Precio", "Existencia"));
            cmbCriterios.getSelectionModel().selectFirst();
        }
    }
    private void configurarTabla() {
        if (tblProductos != null) {
            colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
            colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            colCategoria.setCellValueFactory(cell -> {
                Categoria cat = cell.getValue().getCategoria();
                String nombreCat = (cat != null && cat.getNombre() != null) ? cat.getNombre() : "";
                return new SimpleStringProperty(nombreCat);
            });
            colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
            colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
            colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
            colRuta.setCellValueFactory(new PropertyValueFactory<>("rutaImagen"));
        }
    }

    private void configurarContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem itemActualizar = new MenuItem("Actualizar");
        MenuItem itemEliminar = new MenuItem("Eliminar");

        itemActualizar.setOnAction(e -> prepararActualizar());
        itemEliminar.setOnAction(e -> eliminarProducto());

        contextMenu.getItems().addAll(itemActualizar, itemEliminar);
        tblProductos.setContextMenu(contextMenu);
    }

    private void prepararActualizar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección", "Debe seleccionar un producto de la tabla.");
            return;
        }

        productoSeleccionado = seleccionado;
        txtCodigo.setText(seleccionado.getCodigo());
        txtCodigo.setDisable(true);
        txtNombre.setText(seleccionado.getNombre());
        txtPrecio.setText(seleccionado.getPrecioVenta() != null ? seleccionado.getPrecioVenta().toString() : "");
        txtExistencia.setText(String.valueOf(seleccionado.getExistencia()));
        txtRuta.setText(seleccionado.getRutaImagen() != null ? seleccionado.getRutaImagen() : "");
        if (chkActivo != null) {
            chkActivo.setSelected(seleccionado.isActivo());
        }

        if (cmbCategoria != null && seleccionado.getCategoria() != null) {
            for (Categoria cat : cmbCategoria.getItems()) {
                if (cat.getId() != null && cat.getId().equals(seleccionado.getCategoria().getId())) {
                    cmbCategoria.setValue(cat);
                    break;
                }
            }
        }

        if (btnGuardar != null) {
            btnGuardar.setText("Actualizar Producto");
        }
    }

    private void eliminarProducto() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección", "Debe seleccionar un producto de la tabla.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar el producto \"" + seleccionado.getNombre() + "\"?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> respuesta = confirm.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
            try {
                productosDao.eliminar(seleccionado);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto eliminado correctamente.");
                limpiarFormulario();
                cargarProductos();
            } catch (IllegalArgumentException | IllegalStateException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al eliminar", e.getMessage());
            }
        }
    }

    public void cargarProductos() {
        if (tblProductos != null) {
            try {
                tblProductos.setItems(FXCollections.observableArrayList(productosDao.listar()));
            } catch (IllegalStateException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de base de datos", e.getMessage());
            }
        }
    }

    @FXML
    public void seleccionarRuta(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen del Producto");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.webp", "*.gif")
        );
        Stage stage = null;
        if (txtRuta != null && txtRuta.getScene() != null) {
            stage = (Stage) txtRuta.getScene().getWindow();
        }
        File archivo = fileChooser.showOpenDialog(stage);
        if (archivo != null) {
            txtRuta.setText(archivo.getAbsolutePath());
        }
    }

    @FXML
    public void btnGuardar() {
        if (!validaciones()) {
            return;
        }

        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        Categoria categoria = cmbCategoria.getSelectionModel().getSelectedItem();
        BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
        int existencia = Integer.parseInt(txtExistencia.getText().trim());
        String ruta = (txtRuta != null && txtRuta.getText() != null) ? txtRuta.getText().trim() : "";
        boolean activo = chkActivo != null && chkActivo.isSelected();

        Producto producto = new Producto(null, nombre, codigo, categoria, precio, existencia, ruta, activo);

        try {
            if (productoSeleccionado == null) {
                productosDao.guardar(producto);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto guardado correctamente.");
            } else {
                productosDao.actualizar(producto);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado correctamente.");
            }
            limpiarFormulario();
            cargarProductos();
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (IllegalStateException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de base de datos", e.getMessage());
        }
    }
    @FXML
    public void filtrarPorCriterio(){
        if (txtDatoFiltrar == null || cmbCriterios == null || tblProductos == null) {
            return;
        }

        String texto = txtDatoFiltrar.getText() == null ? "" : txtDatoFiltrar.getText().trim();
        String criterio = cmbCriterios.getValue();
        if (texto.isEmpty() || criterio == null) {
            cargarProductos();
            return;
        }

        try {
            tblProductos.setItems(FXCollections.observableArrayList(
                    productosDao.buscarPorVariosCriterios(criterio, texto)));
        } catch (IllegalStateException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de base de datos", e.getMessage());
        }
    }

    @FXML
    public void limpiarFiltro() {
        if (txtDatoFiltrar != null) {
            txtDatoFiltrar.clear();
        }
        if (cmbCriterios != null) {
            cmbCriterios.getSelectionModel().selectFirst();
        }
        cargarProductos();
    }
    @FXML
    public void abrirCategorias(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ni/uam/edu/practicabd/categoria-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Formulario Categoría");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            cargarCategorias();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir la vista de categoría: " + e.getMessage());
        }
    }

    private boolean validaciones() {
        String codigo = txtCodigo == null || txtCodigo.getText() == null
                ? "" : txtCodigo.getText().trim();
        String nombre = txtNombre == null || txtNombre.getText() == null
                ? "" : txtNombre.getText().trim();

        if (codigo.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "El código del producto es obligatorio.");
            if (txtCodigo != null) {
                txtCodigo.requestFocus();
            }
            return false;
        }

        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "El nombre del producto es obligatorio.");
            if (txtNombre != null) {
                txtNombre.requestFocus();
            }
            return false;
        }

        if (cmbCategoria == null || cmbCategoria.getSelectionModel().getSelectedItem() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Debe seleccionar una categoría.");
            if (cmbCategoria != null) {
                cmbCategoria.requestFocus();
            }
            return false;
        }

        if (txtPrecio == null || txtPrecio.getText() == null || txtPrecio.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo vacío", "Debe ingresar el precio del producto.");
            return false;
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarAlerta(Alert.AlertType.ERROR, "Precio incorrecto", "El precio de venta debe ser mayor que cero.");
                txtPrecio.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Precio incorrecto", "El precio debe contener únicamente valores numéricos.");
            txtPrecio.requestFocus();
            return false;
        }

        if (txtExistencia == null || txtExistencia.getText() == null || txtExistencia.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo vacío", "Debe ingresar un valor en existencia.");
            return false;
        }

        try {
            int existencia = Integer.parseInt(txtExistencia.getText().trim());
            if (existencia < 0) {
                mostrarAlerta(Alert.AlertType.ERROR, "Dato inválido", "La existencia no puede ser negativa.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Dato inválido", "La existencia debe ser un número entero.");
            txtExistencia.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    public void limpiarFormulario() {
        productoSeleccionado = null;
        if (txtCodigo != null) {
            txtCodigo.clear();
            txtCodigo.setDisable(false);
        }
        if (txtNombre != null) txtNombre.clear();
        if (txtPrecio != null) txtPrecio.clear();
        if (cmbCategoria != null) cmbCategoria.getSelectionModel().clearSelection();
        if (txtRuta != null) txtRuta.clear();
        if (txtExistencia != null) txtExistencia.clear();
        if (chkActivo != null) chkActivo.setSelected(true);
        if (btnGuardar != null) {
            btnGuardar.setText("Guardar Producto");
        }
    }

    public void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
