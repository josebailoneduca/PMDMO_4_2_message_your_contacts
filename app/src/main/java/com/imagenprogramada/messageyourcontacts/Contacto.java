package com.imagenprogramada.messageyourcontacts;

/**
 * Dto para contacto
 */
public class Contacto {

    private int id;
    private String nombre;
    private String telefono;
    int imagen;

    /**
     *Constructor
     * @param id Id del contacto
     * @param nombre Nombre
     * @param telefono Numero de telefono
     * @param imagen id de la imagen
     */
    public Contacto(int id, String nombre, String telefono, int imagen) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.imagen = imagen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getImagen() {
        return imagen;
    }

    public void setImagen(int imagen) {
        this.imagen = imagen;
    }
}
