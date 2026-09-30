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
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la categoría (es posible que tenga productos asociados).");
            }
        }
    }

    private void cargarCategorias() {
        if (tblCategorias != null) {
            tblCategorias.setItems(FXCollections.observableArrayList(categoriaDao.listar()));
        }
    }

    @FXML
    public void guardar(ActionEvent event) {
        String nombre = txtNombre.getText();
        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Debe ingresar el nombre de la categoría.");
            return;
        }

        if (categoriaSeleccionada == null) {
            Categoria c = new Categoria();
            c.setNombre(nombre.trim());
            c.setActiva(chkActiva != null && chkActiva.isSelected());
            categoriaDao.guardar(c);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría guardada correctamente en la base de datos.");
        } else {
            categoriaSeleccionada.setNombre(nombre.trim());
            categoriaSeleccionada.setActiva(chkActiva != null && chkActiva.isSelected());
            categoriaDao.actualizar(categoriaSeleccionada);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría actualizada correctamente.");
        }

        limpiar();
        cargarCategorias();
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
            tblCategorias.setItems(FXCollections.observableArrayList(categoriaDao.buscarPorNombre(filtrar)));
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
