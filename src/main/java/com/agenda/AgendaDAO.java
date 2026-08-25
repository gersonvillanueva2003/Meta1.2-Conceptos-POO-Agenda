package com.agenda;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class AgendaDAO {
    private static final String URL = "jdbc:mariadb://localhost:3306/agenda";
    private static final String USER = "usuario1";
    private static final String PASS = "superpassword";

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
    public List<Persona> obtenerTodas() {

        List<Persona> personas = new ArrayList<>();

        String query = """
                SELECT
                    p.id,
                    p.nombre,
                    GROUP_CONCAT(DISTINCT t.telefono) AS telefonos,
                    GROUP_CONCAT(DISTINCT d.id) AS direccionIds,
                    GROUP_CONCAT(DISTINCT d.direccion SEPARATOR '|||') AS direcciones
                FROM Personas p
                LEFT JOIN Telefonos t
                    ON p.id = t.personaId
                LEFT JOIN PersonaDireccion pd
                    ON p.id = pd.personaId
                LEFT JOIN Direcciones d
                    ON pd.direccionId = d.id
                GROUP BY p.id, p.nombre
                """;
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                List<String> tels = new ArrayList<>();
                String telsStr = rs.getString("telefonos");
                if (telsStr != null) {
                    tels = Arrays.stream(telsStr.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
                }
                List<Direccion> direcciones = new ArrayList<>();
                String idsStr = rs.getString("direccionIds");
                String dirsStr = rs.getString("direcciones");
                if (idsStr != null && dirsStr != null) {
                    String[] ids = idsStr.split(",");
                    String[] dirs = dirsStr.split("\\|\\|\\|");
                    for (int i = 0; i < ids.length; i++) {
                        direcciones.add(
                                new Direccion(
                                        Integer.parseInt(ids[i]),
                                        dirs[i]
                                )
                        );
                    }
                }
                personas.add(
                        new Persona(
                                rs.getInt("id"),
                                rs.getString("nombre"),
                                tels,
                                direcciones
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return personas;
    }
    public void insertarPersona(Persona p) {

        String sqlPersona =
                "INSERT INTO Personas (nombre) VALUES (?)";
        try (Connection conn = conectar()) {
            conn.setAutoCommit(false);
            try {
                int personaId;
                try (PreparedStatement pst =
                             conn.prepareStatement(
                                     sqlPersona,
                                     Statement.RETURN_GENERATED_KEYS)) {
                    pst.setString(1, p.getNombre());
                    pst.executeUpdate();
                    try (ResultSet rs = pst.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se pudo obtener el ID de la persona."
                            );
                        }
                        personaId = rs.getInt(1);
                    }
                }
                insertarTelefonos(conn, personaId, p.getTelefonos());
                insertarDirecciones(
                        conn,
                        personaId,
                        p.getDirecciones()
                );
                conn.commit();
            } catch (SQLException ex) {

                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void actualizarPersona(Persona p) {
        String sql =
                "UPDATE Personas SET nombre=? WHERE id=?";
        try (Connection conn = conectar()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pst =
                             conn.prepareStatement(sql)) {
                    pst.setString(1, p.getNombre());
                    pst.setInt(2, p.getId());
                    pst.executeUpdate();
                }
                try (PreparedStatement pst =
                             conn.prepareStatement(
                                     "DELETE FROM Telefonos WHERE personaId=?")) {
                    pst.setInt(1, p.getId());
                    pst.executeUpdate();
                }
                insertarTelefonos(
                        conn,
                        p.getId(),
                        p.getTelefonos()
                );
                try (PreparedStatement pst =
                             conn.prepareStatement(
                                     "DELETE FROM PersonaDireccion WHERE personaId=?")) {
                    pst.setInt(1, p.getId());
                    pst.executeUpdate();
                }
                insertarDirecciones(
                        conn,
                        p.getId(),
                        p.getDirecciones()
                );
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void eliminarPersona(int id) {
        String sql =
                "DELETE FROM Personas WHERE id=?";
        try (Connection conn = conectar();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void insertarTelefonos(
            Connection conn,
            int personaId,
            List<String> telefonos) throws SQLException {
        if (telefonos == null) {
            return;
        }
        String sql =
                "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";
        try (PreparedStatement pst =
                     conn.prepareStatement(sql)) {
            for (String tel : telefonos) {
                if (tel == null || tel.trim().isEmpty()) {
                    continue;
                }
                pst.setInt(1, personaId);
                pst.setString(2, tel.trim());
                pst.executeUpdate();
            }
        }
    }
    private void insertarDirecciones(
            Connection conn,
            int personaId,
            List<Direccion> direcciones) throws SQLException {
        if (direcciones == null) {
            return;
        }
        for (Direccion direccion : direcciones) {
            if (direccion == null ||
                    direccion.getDireccion() == null ||
                    direccion.getDireccion().trim().isEmpty()) {

                continue;
            }
            String texto =
                    direccion.getDireccion().trim();
            int direccionId =
                    obtenerOCrearDireccion(conn, texto);
            String verificar =
                    "SELECT COUNT(*) FROM PersonaDireccion " +
                            "WHERE personaId=? AND direccionId=?";
            boolean existe;
            try (PreparedStatement pst =
                         conn.prepareStatement(verificar)) {
                pst.setInt(1, personaId);
                pst.setInt(2, direccionId);
                try (ResultSet rs = pst.executeQuery()) {
                    rs.next();
                    existe = rs.getInt(1) > 0;
                }
            }
            if (!existe) {
                String insertar =
                        "INSERT INTO PersonaDireccion " +
                                "(personaId, direccionId) VALUES (?, ?)";
                try (PreparedStatement pst =
                             conn.prepareStatement(insertar)) {
                    pst.setInt(1, personaId);
                    pst.setInt(2, direccionId);

                    pst.executeUpdate();
                }
            }
        }
    }
    private int obtenerOCrearDireccion(
            Connection conn,
            String direccion) throws SQLException {

        String buscar =
                "SELECT id FROM Direcciones WHERE direccion=?";

        try (PreparedStatement pst =
                     conn.prepareStatement(buscar)) {
            pst.setString(1, direccion);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        String insertar =
                "INSERT INTO Direcciones (direccion) VALUES (?)";
        try (PreparedStatement pst =
                     conn.prepareStatement(
                             insertar,
                             Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, direccion);
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
}