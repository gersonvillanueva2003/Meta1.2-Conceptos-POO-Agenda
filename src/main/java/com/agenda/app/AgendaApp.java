package com.agenda.app;

import com.agenda.dao.PersonaDAO;
import com.agenda.model.Direccion;
import com.agenda.model.Persona;
import com.agenda.service.AgendaService;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class AgendaApp extends Application {
    private final AgendaService service =
            new AgendaService(
                    new PersonaDAO()
            );
    private final TableView<Persona> tabla =
            new TableView<>();
    private final TextField txtId =
            new TextField();
    private final TextField txtNombre =
            new TextField();
    private final TextField txtTels =
            new TextField();
    private final TextArea txtDirs =
            new TextArea();

    @Override
    public void start(Stage stage) {
        configurarTabla();
        GridPane formulario =
                crearFormulario();
        BorderPane root =
                new BorderPane();
        root.setCenter(tabla);
        root.setRight(formulario);
        actualizarTabla();
        stage.setScene(
                new Scene(
                        root,
                        1100,
                        500
                )
        );
        stage.setTitle(
                "Gestión de Agenda"
        );
        stage.show();
    }

    private void configurarTabla() {
        TableColumn<Persona, String> colId =
                new TableColumn<>("ID");
        colId.setCellValueFactory(
                c ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        c.getValue().getId()
                                )
                        )
        );
        TableColumn<Persona, String> colNombre =
                new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(
                c ->
                        new SimpleStringProperty(
                                c.getValue()
                                        .getNombre()
                        )
        );
        TableColumn<Persona, String> colTelefonos =
                new TableColumn<>("Teléfonos");
        colTelefonos.setCellValueFactory(
                c ->
                        new SimpleStringProperty(
                                c.getValue()
                                        .getTelefonosComoString()
                        )
        );
        TableColumn<Persona, String> colDirecciones =
                new TableColumn<>("Direcciones");
        colDirecciones.setCellValueFactory(
                c ->
                        new SimpleStringProperty(
                                c.getValue()
                                        .getDireccionesComoString()
                        )
        );
        tabla.getColumns().addAll(
                colId,
                colNombre,
                colTelefonos,
                colDirecciones
        );
        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, anterior, persona) -> {
                            if (persona != null) {
                                cargarPersonaEnFormulario(
                                        persona
                                );
                            }
                        }
                );
    }

    private GridPane crearFormulario() {
        GridPane form =
                new GridPane();
        form.setPadding(
                new Insets(10)
        );
        form.setVgap(10);
        form.setHgap(10);

        txtId.setDisable(true);
        txtTels.setPromptText(
                "Ej: 555-111, 555-222"
        );
        txtDirs.setPromptText(
                "Una dirección por línea"
        );
        txtDirs.setPrefRowCount(5);
        form.add(
                new Label("ID:"),
                0,
                0
        );
        form.add(
                txtId,
                1,
                0
        );
        form.add(
                new Label("Nombre:"),
                0,
                1
        );
        form.add(
                txtNombre,
                1,
                1
        );
        form.add(
                new Label("Teléfonos:"),
                0,
                2
        );
        form.add(
                txtTels,
                1,
                2
        );
        form.add(
                new Label("Direcciones:"),
                0,
                3
        );
        form.add(
                txtDirs,
                1,
                3
        );

        Button btnAgregar =
                new Button("Agregar");
        Button btnActualizar =
                new Button("Actualizar");
        Button btnEliminar =
                new Button("Eliminar");
        Button btnLimpiar =
                new Button("Limpiar");
        btnAgregar.setOnAction(
                e -> agregarPersona()
        );
        btnActualizar.setOnAction(
                e -> actualizarPersona()
        );
        btnEliminar.setOnAction(
                e -> eliminarPersona()
        );
        btnLimpiar.setOnAction(
                e -> limpiar()
        );
        HBox botones =
                new HBox(
                        10,
                        btnAgregar,
                        btnActualizar,
                        btnEliminar,
                        btnLimpiar
                );
        form.add(
                botones,
                0,
                4,
                2,
                1
        );
        return form;
    }

    private void agregarPersona() {
        try {
            Persona persona =
                    obtenerPersonaFormulario(0);
            service.agregarPersona(
                    persona
            );
            actualizarTabla();
            limpiar();
        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }

    private void actualizarPersona() {
        if (txtId.getText().isBlank()) {
            mostrarError(
                    "Seleccione una persona."
            );
            return;
        }
        try {
            int id =
                    Integer.parseInt(
                            txtId.getText()
                    );
            Persona persona =
                    obtenerPersonaFormulario(id);
            service.actualizarPersona(
                    persona
            );
            actualizarTabla();
            limpiar();

        } catch (IllegalArgumentException e) {
            mostrarError(
                    e.getMessage()
            );
        }
    }

    private void eliminarPersona() {
        if (txtId.getText().isBlank()) {
            mostrarError(
                    "Seleccione una persona."
            );

            return;
        }
        try {

            int id =
                    Integer.parseInt(
                            txtId.getText()
                    );
            service.eliminarPersona(id);
            actualizarTabla();
            limpiar();
        } catch (IllegalArgumentException e) {
            mostrarError(
                    e.getMessage()
            );
        }
    }

    private Persona obtenerPersonaFormulario(
            int id
    ) {
        List<String> telefonos =
                Arrays.stream(
                                txtTels.getText()
                                        .split(",")
                        )
                        .map(String::trim)
                        .filter(t -> !t.isEmpty())
                        .toList();
        List<Direccion> direcciones =
                Arrays.stream(
                                txtDirs.getText()
                                        .split("\\R")
                        )
                        .map(String::trim)
                        .filter(d -> !d.isEmpty())
                        .map(d ->
                                new Direccion(
                                        0,
                                        d
                                )
                        )
                        .toList();
        return new Persona(
                id,
                txtNombre.getText().trim(),
                telefonos,
                direcciones
        );
    }

    private void cargarPersonaEnFormulario(
            Persona persona
    ) {
        txtId.setText(
                String.valueOf(
                        persona.getId()
                )
        );
        txtNombre.setText(
                persona.getNombre()
        );
        txtTels.setText(
                persona.getTelefonosComoString()
        );
        String direcciones =
                persona.getDirecciones()
                        .stream()
                        .map(
                                Direccion::getDireccion
                        )
                        .reduce(
                                (a, b) ->
                                        a + "\n" + b
                        )
                        .orElse("");
        txtDirs.setText(
                direcciones
        );
    }

    private void actualizarTabla() {
        tabla.setItems(
                FXCollections.observableArrayList(
                        service.obtenerPersonas()
                )
        );
    }

    private void limpiar() {
        txtId.clear();
        txtNombre.clear();
        txtTels.clear();
        txtDirs.clear();
        tabla.getSelectionModel()
                .clearSelection();
    }

    private void mostrarError(
            String mensaje
    ) {
        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );
        alert.setTitle(
                "Error"
        );
        alert.setHeaderText(
                null
        );
        alert.setContentText(
                mensaje
        );
        alert.showAndWait();
    }
    public static void main(
            String[] args
    ) {
        launch(args);
    }
}