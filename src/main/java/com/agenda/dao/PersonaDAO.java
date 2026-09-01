package com.agenda.dao;

import com.agenda.database.ConexionDB;
import com.agenda.model.Direccion;
import com.agenda.model.Persona;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PersonaDAO implements IPersonaDAO {
    @Override
    public List<Persona> obtenerTodas() {
        List<Persona> personas = new ArrayList<>();
        String sql = """
                SELECT
                    p.id,
                    p.nombre,
                    GROUP_CONCAT(DISTINCT t.telefono) AS telefonos,
                    GROUP_CONCAT(
                        DISTINCT d.direccion
                        SEPARATOR '|||'
                    ) AS direcciones
                FROM Personas p
                LEFT JOIN Telefonos t
                    ON p.id = t.personaId
                LEFT JOIN PersonaDireccion pd
                    ON p.id = pd.personaId
                LEFT JOIN Direcciones d
                    ON pd.direccionId = d.id
                GROUP BY p.id, p.nombre
                """;
        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                List<String> telefonos =
                        convertirTelefonos(
                                rs.getString("telefonos")
                        );
                List<Direccion> direcciones =
                        convertirDirecciones(
                                rs.getString("direcciones")
                        );
                Persona persona =
                        new Persona(
                                rs.getInt("id"),
                                rs.getString("nombre"),
                                telefonos,
                                direcciones
                        );
                personas.add(persona);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return personas;
    }
    @Override
    public void insertar(Persona persona) {
        String sqlPersona =
                "INSERT INTO Personas (nombre) VALUES (?)";
        try (Connection conn = ConexionDB.conectar()) {
            conn.setAutoCommit(false);
            try {
                int personaId;
                try (PreparedStatement pst =
                             conn.prepareStatement(
                                     sqlPersona,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {
                    pst.setString(
                            1,
                            persona.getNombre()
                    );
                    pst.executeUpdate();
                    try (ResultSet rs =
                                 pst.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se pudo obtener el ID de la persona."
                            );
                        }
                        personaId =
                                rs.getInt(1);
                    }
                }
                insertarTelefonos(
                        conn,
                        personaId,
                        persona.getTelefonos()
                );
                insertarDirecciones(
                        conn,
                        personaId,
                        persona.getDirecciones()
                );
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void actualizar(Persona persona) {
        String sqlPersona =
                "UPDATE Personas SET nombre=? WHERE id=?";
        try (Connection conn = ConexionDB.conectar()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pst =
                             conn.prepareStatement(
                                     sqlPersona
                             )) {
                    pst.setString(
                            1,
                            persona.getNombre()
                    );
                    pst.setInt(
                            2,
                            persona.getId()
                    );
                    pst.executeUpdate();
                }
                eliminarTelefonos(
                        conn,
                        persona.getId()
                );
                insertarTelefonos(
                        conn,
                        persona.getId(),
                        persona.getTelefonos()
                );
                eliminarRelacionesDirecciones(
                        conn,
                        persona.getId()
                );
                insertarDirecciones(
                        conn,
                        persona.getId(),
                        persona.getDirecciones()
                );
                conn.commit();
            } catch (SQLException e) {

                conn.rollback();

                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void eliminar(int id) {
        String sql =
                "DELETE FROM Personas WHERE id=?";

        try (Connection conn =
                     ConexionDB.conectar();

             PreparedStatement pst =
                     conn.prepareStatement(sql)) {

            pst.setInt(
                    1,
                    id
            );
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void insertarTelefonos(
            Connection conn,
            int personaId,
            List<String> telefonos
    ) throws SQLException {
        if (telefonos == null) {
            return;
        }
        String sql =
                "INSERT INTO Telefonos " +
                        "(personaId, telefono) VALUES (?, ?)";
        try (PreparedStatement pst =
                     conn.prepareStatement(sql)) {
            for (String telefono : telefonos) {
                if (telefono == null ||
                        telefono.isBlank()) {
                    continue;
                }
                pst.setInt(
                        1,
                        personaId
                );
                pst.setString(
                        2,
                        telefono.trim()
                );
                pst.executeUpdate();
            }
        }
    }
    private void insertarDirecciones(
            Connection conn,
            int personaId,
            List<Direccion> direcciones
    ) throws SQLException {

        if (direcciones == null) {
            return;
        }
        for (Direccion direccion : direcciones) {
            if (direccion == null ||
                    direccion.getDireccion() == null ||
                    direccion.getDireccion().isBlank()) {
                continue;
            }
            String texto =
                    direccion.getDireccion().trim();
            int direccionId =
                    obtenerOCrearDireccion(
                            conn,
                            texto
                    );
            insertarRelacionDireccion(
                    conn,
                    personaId,
                    direccionId
            );
        }
    }
    private int obtenerOCrearDireccion(
            Connection conn,
            String direccion
    ) throws SQLException {
        String buscar =
                "SELECT id FROM Direcciones " +
                        "WHERE direccion=?";
        try (PreparedStatement pst =
                     conn.prepareStatement(buscar)) {
            pst.setString(
                    1,
                    direccion
            );
            try (ResultSet rs =
                         pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        String insertar =
                "INSERT INTO Direcciones " +
                        "(direccion) VALUES (?)";
        try (PreparedStatement pst =
                     conn.prepareStatement(
                             insertar,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            pst.setString(
                    1,
                    direccion
            );
            pst.executeUpdate();
            try (ResultSet rs =
                         pst.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException(
                "No se pudo obtener el ID de la dirección."
        );
    }
    private void insertarRelacionDireccion(
            Connection conn,
            int personaId,
            int direccionId
    ) throws SQLException {
        String sql = """
                INSERT IGNORE INTO PersonaDireccion
                (personaId, direccionId)
                VALUES (?, ?)
                """;
        try (PreparedStatement pst =
                     conn.prepareStatement(sql)) {
            pst.setInt(
                    1,
                    personaId
            );
            pst.setInt(
                    2,
                    direccionId
            );
            pst.executeUpdate();
        }
    }
    private void eliminarTelefonos(
            Connection conn,
            int personaId
    ) throws SQLException {
        String sql =
                "DELETE FROM Telefonos " +
                        "WHERE personaId=?";
        try (PreparedStatement pst =
                     conn.prepareStatement(sql)) {
            pst.setInt(
                    1,
                    personaId
            );
            pst.executeUpdate();
        }
    }
    private void eliminarRelacionesDirecciones(
            Connection conn,
            int personaId
    ) throws SQLException {
        String sql =
                "DELETE FROM PersonaDireccion " +
                        "WHERE personaId=?";
        try (PreparedStatement pst =
                     conn.prepareStatement(sql)) {
            pst.setInt(
                    1,
                    personaId
            );
            pst.executeUpdate();
        }
    }
    private List<String> convertirTelefonos(
            String telefonos
    ) {
        if (telefonos == null ||
                telefonos.isBlank()) {

            return new ArrayList<>();
        }
        return Arrays.stream(
                        telefonos.split(",")
                )
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .toList();
    }
    private List<Direccion> convertirDirecciones(
            String direcciones
    ) {
        if (direcciones == null ||
                direcciones.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(
                        direcciones.split("\\|\\|\\|")
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
    }
}