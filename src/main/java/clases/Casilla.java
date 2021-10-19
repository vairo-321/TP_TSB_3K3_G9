package clases;

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

}
