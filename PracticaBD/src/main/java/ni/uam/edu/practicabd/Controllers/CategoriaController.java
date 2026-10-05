package ni.uam.edu.practicabd.Controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.uam.edu.practicabd.DAO.CategoriaDao;
import ni.uam.edu.practicabd.Modelos.Categoria;

import java.util.Optional;

public class CategoriaController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDatoFiltrar;
    @FXML private CheckBox chkActiva;
    @FXML private Button btnGuardar;
    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, Boolean> colActiva;

    private CategoriaDao categoriaDao = new CategoriaDao();
    private Categoria categoriaSeleccionada = null;

    @FXML
    public void initialize() {
        if (chkActiva != null) {
            chkActiva.setSelected(true);
        }
        if (tblCategorias != null) {
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            colActiva.setCellValueFactory(new PropertyValueFactory<>("activa"));
            configurarContextMenu();
            cargarCategorias();
        }
    }

    private void configurarContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem itemActualizar = new MenuItem("Actualizar");
        MenuItem itemEliminar = new MenuItem("Eliminar");

        itemActualizar.setOnAction(e -> prepararActualizar());
        itemEliminar.setOnAction(e -> eliminarCategoria());

        contextMenu.getItems().addAll(itemActualizar, itemEliminar);
        tblCategorias.setContextMenu(contextMenu);
    }



    private void prepararActualizar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección", "Debe seleccionar una categoría de la tabla.");
            return;
        }
        categoriaSeleccionada = seleccionada;
        txtNombre.setText(seleccionada.getNombre());
        chkActiva.setSelected(seleccionada.isActiva());
        if (btnGuardar != null) {
            btnGuardar.setText("Actualizar");
        }
    }

    private void eliminarCategoria() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección", "Debe seleccionar una categoría de la tabla.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar la categoría \"" + seleccionada.getNombre() + "\"?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> respuesta = confirm.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
            try {
                categoriaDao.eliminar(seleccionada);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría eliminada correctamente.");
                limpiar();
                cargarCategorias();
            } catch (IllegalArgumentException | IllegalStateException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al eliminar", e.getMessage());
            }
        }
    }

    private void cargarCategorias() {
        if (tblCategorias != null) {
            try {
                tblCategorias.setItems(FXCollections.observableArrayList(categoriaDao.listar()));
            } catch (IllegalStateException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de base de datos", e.getMessage());
            }
        }
    }



    @FXML
    public void guardar(ActionEvent event) {
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "El nombre de la categoría es obligatorio.");
            txtNombre.requestFocus();
            return;
        }

        try {
            if (categoriaSeleccionada == null) {
                Categoria categoria = new Categoria();
                categoria.setNombre(nombre);
                categoria.setActiva(chkActiva != null && chkActiva.isSelected());
                categoriaDao.guardar(categoria);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría guardada correctamente.");
            } else {
                categoriaSeleccionada.setNombre(nombre);
                categoriaSeleccionada.setActiva(chkActiva != null && chkActiva.isSelected());
                categoriaDao.actualizar(categoriaSeleccionada);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría actualizada correctamente.");
            }

            limpiar();
            cargarCategorias();

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (IllegalStateException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de base de datos", e.getMessage());
        }
    }

    private void limpiar() {
        txtNombre.clear();
        if (chkActiva != null) {
            chkActiva.setSelected(true);
        }
        categoriaSeleccionada = null;
        if (btnGuardar != null) {
            btnGuardar.setText("Guardar");
        }
    }
    @FXML
    public void filtarDatosCategoria(){
        String filtrar = txtDatoFiltrar.getText() == null ? "" : txtDatoFiltrar.getText().trim();
        if (filtrar.isEmpty()) {
            cargarCategorias();
        }
        else{
            try {
                tblCategorias.setItems(FXCollections.observableArrayList(categoriaDao.buscarPorNombre(filtrar)));
            } catch (IllegalStateException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de base de datos", e.getMessage());
            }
        }
    }

    @FXML
    public void cerrar(ActionEvent event) {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
