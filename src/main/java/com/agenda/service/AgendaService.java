package com.agenda.service;

import com.agenda.dao.IPersonaDAO;
import com.agenda.model.Persona;

import java.util.List;

public class AgendaService {
    private final IPersonaDAO personaDAO;
    public AgendaService(
            IPersonaDAO personaDAO
    ) {
        this.personaDAO = personaDAO;
    }
    public List<Persona> obtenerPersonas() {
        return personaDAO.obtenerTodas();
    }
    public void agregarPersona(
            Persona persona
    ) {
        validarPersona(persona);
        personaDAO.insertar(persona);
    }
    public void actualizarPersona(
            Persona persona
    ) {
        validarPersona(persona);
        if (persona.getId() <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la persona no es válido."
            );
        }
        personaDAO.actualizar(persona);
    }
    public void eliminarPersona(
            int id
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID no es válido."
            );
        }
        personaDAO.eliminar(id);
    }
    private void validarPersona(
            Persona persona
    ) {
        if (persona == null) {
            throw new IllegalArgumentException(
                    "La persona no puede ser nula."
            );
        }
        if (persona.getNombre() == null ||
                persona.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }
    }
}