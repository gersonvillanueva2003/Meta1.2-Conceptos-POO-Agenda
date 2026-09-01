package com.agenda.model;

public class Direccion {
    private int id;
    private String direccion;

    public Direccion(int id, String direccion) {
        this.id = id;
        this.direccion = direccion;
    }
    public int getId() {
        return id;
    }
    public String getDireccion() {
        return direccion;
    }
    @Override
    public String toString() {
        return direccion;
    }
}