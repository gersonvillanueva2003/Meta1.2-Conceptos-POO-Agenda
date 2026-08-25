package com.agenda;

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

    private final AgendaDAO dao = new AgendaDAO();
    private final TableView<Persona> tabla = new TableView<>();
    private final TextField txtId = new TextField();
    private final TextField txtNombre = new TextField();
    private final TextField txtTels = new TextField();
    private final TextArea txtDirs = new TextArea();

    @Override
    public void start(Stage stage) {
        TableColumn<Persona, String> colId =
                new TableColumn<>("ID");
        colId.setCellValueFactory(
                c -> new SimpleStringProperty(
                        String.valueOf(c.getValue().getId())
                )
        );
        TableColumn<Persona, String> colNom =
                new TableColumn<>("Nombre");
        colNom.setCellValueFactory(
                c -> new SimpleStringProperty(
                        c.getValue().getNombre()
                )
        );
        TableColumn<Persona, String> colTel =
                new TableColumn<>("Teléfonos");
        colTel.setCellValueFactory(
                c -> new SimpleStringProperty(
                        c.getValue().getTelefonosComoString()
                )
        );
        TableColumn<Persona, String> colDir =
                new TableColumn<>("Direcciones");
        colDir.setCellValueFactory(
                c -> new SimpleStringProperty(
                        c.getValue().getDireccionesComoString()
                )
        );
        tabla.getColumns().addAll(
                colId,
                colNom,
                colTel,
                colDir
        );
        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        actualizarTabla();
        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, nuevaPersona) -> {
                    if (nuevaPersona != null) {
                        txtId.setText(
                                String.valueOf(
                                        nuevaPersona.getId()
                                )
                        );
                        txtNombre.setText(
                                nuevaPersona.getNombre()
                        );

                        txtTels.setText(
                                nuevaPersona
                                        .getTelefonosComoString()
                        );
                        String direcciones =
                                nuevaPersona
                                        .getDirecciones()
                                        .stream()
                                        .map(Direccion::getDireccion)
                                        .reduce(
                                                (a, b) ->
                                                        a + "\n" + b
                                        )
                                        .orElse("");
                        txtDirs.setText(direcciones);
                    }
                });
        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
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
        form.add(new Label("ID:"), 0, 0);
        form.add(txtId, 1, 0);
        form.add(new Label("Nombre:"), 0, 1);
        form.add(txtNombre, 1, 1);
        form.add(new Label("Teléfonos:"), 0, 2);
        form.add(txtTels, 1, 2);
        form.add(new Label("Direcciones:"), 0, 3);
        form.add(txtDirs, 1, 3);
        Button btnAdd =
                new Button("Agregar");
        Button btnUpd =
                new Button("Actualizar");
        Button btnDel =
                new Button("Eliminar");
        Button btnClear =
                new Button("Limpiar");
        HBox botones =
                new HBox(
                        10,
                        btnAdd,
                        btnUpd,
                        btnDel,
                        btnClear
                );
        form.add(
                botones,
                0,
                4,
                2,
                1
        );
        btnAdd.setOnAction(e -> {
            List<String> telefonos =
                    Arrays.stream(
                                    txtTels.getText().split(",")
                            )
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
            List<Direccion> direcciones =
                    Arrays.stream(
                                    txtDirs.getText().split("\\R")
                            )
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(s -> new Direccion(0, s))
                            .toList();
            Persona persona =
                    new Persona(
                            0,
                            txtNombre.getText(),
                            telefonos,
                            direcciones
                    );
            dao.insertarPersona(persona);
            actualizarTabla();
            limpiar();
        });
        btnUpd.setOnAction(e -> {
            if (!txtId.getText().isEmpty()) {
                List<String> telefonos =
                        Arrays.stream(
                                        txtTels.getText().split(",")
                                )
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .toList();
                List<Direccion> direcciones =
                        Arrays.stream(
                                        txtDirs.getText().split("\\R")
                                )
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(s -> new Direccion(0, s))
                                .toList();
                Persona persona =
                        new Persona(
                                Integer.parseInt(
                                        txtId.getText()
                                ),
                                txtNombre.getText(),
                                telefonos,
                                direcciones
                        );
                dao.actualizarPersona(persona);
                actualizarTabla();
                limpiar();
            }
        });
        btnDel.setOnAction(e -> {
            if (!txtId.getText().isEmpty()) {

                dao.eliminarPersona(
                        Integer.parseInt(
                                txtId.getText()
                        )
                );
                actualizarTabla();
                limpiar();
            }
        });
        btnClear.setOnAction(
                e -> limpiar()
        );
        BorderPane root =
                new BorderPane();
        root.setCenter(tabla);
        root.setRight(form);
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
    private void actualizarTabla() {

        tabla.setItems(
                FXCollections.observableArrayList(
                        dao.obtenerTodas()
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
    public static void main(String[] args) {
        launch(args);
    }
}