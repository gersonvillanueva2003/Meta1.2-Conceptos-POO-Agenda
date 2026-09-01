package com.agenda;

import org.junit.jupiter.api.*;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgendaDAOTest {
    private static AgendaDAO dao;
    private static int idPrueba = -1;

    @BeforeAll
    public static void setup() {
        dao = new AgendaDAO();
    }

    @Test
    @Order(1)
    public void testInsertar() {
        int inicial = dao.obtenerTodas().size();
        dao.insertarPersona(new Persona(0, "Prueba Test", "Dir Test", Arrays.asList("123", "456")));
        List<Persona> todas = dao.obtenerTodas();

        assertEquals(inicial + 1, todas.size(), "Debería haber un registro más");

        Persona p = todas.stream().filter(x -> x.getNombre().equals("Prueba Test")).findFirst().get();
        assertEquals(2, p.getTelefonos().size(), "Deberían guardarse 2 teléfonos");
        idPrueba = p.getId();
    }

    @Test
    @Order(2)
    public void testActualizar() {
        dao.actualizarPersona(new Persona(idPrueba, "Modificado", "Dir Mod", Arrays.asList("999")));
        Persona p = dao.obtenerTodas().stream().filter(x -> x.getId() == idPrueba).findFirst().get();

        assertEquals("Modificado", p.getNombre(), "El nombre debió cambiar");
        assertEquals(1, p.getTelefonos().size(), "Solo debería quedar 1 teléfono");
    }

    @Test
    @Order(3)
    public void testEliminar() {
        int antes = dao.obtenerTodas().size();
        dao.eliminarPersona(idPrueba);
        assertEquals(antes - 1, dao.obtenerTodas().size(), "Debería haber un registro menos");
    }
}