package com.agenda.dao;

import com.agenda.model.Persona;

import java.util.List;

public interface IPersonaDAO {
    List<Persona> obtenerTodas();
    void insertar(Persona persona);
    void actualizar(Persona persona);
    void eliminar(int id);
}