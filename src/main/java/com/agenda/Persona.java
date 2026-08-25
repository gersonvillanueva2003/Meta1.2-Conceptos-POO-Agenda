package com.agenda;

import java.util.List;

public class Persona {

    private int id;
    private String nombre;
    private List<String> telefonos;
    private List<Direccion> direcciones;

    public Persona(int id, String nombre, List<String> telefonos, List<Direccion> direcciones) {
        this.id = id;
        this.nombre = nombre;
        this.telefonos = telefonos;
        this.direcciones = direcciones;
    }
    public int getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public List<String> getTelefonos() {
        return telefonos;
    }
    public List<Direccion> getDirecciones() {
        return direcciones;
    }
    public String getTelefonosComoString() {
        if (telefonos == null || telefonos.isEmpty()) {
            return "";
        }
        return String.join(", ", telefonos);
    }
    public String getDireccionesComoString() {
        if (direcciones == null || direcciones.isEmpty()) {
            return "";
        }
        return direcciones.stream()
                .map(Direccion::getDireccion)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}