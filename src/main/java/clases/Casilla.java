package clases;

import java.util.Objects;

public class Casilla <E>{
    private String estado;
    private E dato;




    public Casilla()
    {
        estado = "abierta";
        dato = null;
    }

    public void setEstado(String nombre)
    {
        this.estado = nombre;
    }

    public String getEstado()
    {
        return estado;
    }

    public void setDato(E dato)
    {
        this.dato = dato;
    }

    public E getDato()
    {
        return this.dato;
    }

    public  String toString()
    {
        if (dato == null) return "( ,  )";
        return dato.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        Casilla<E> cas = new Casilla();
        cas.setEstado(this.getEstado());
        cas.setDato(this.getDato());
        return cas;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.estado);
        if (this.estado == "cerrada")
        {
            hash += 61 * hash + Objects.hashCode(this.dato);
        }
        return hash;
    }
}
