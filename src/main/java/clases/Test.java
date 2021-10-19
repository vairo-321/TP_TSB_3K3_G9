package clases;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Una clase con un main() simple para probar la clase TSBHashtable.
 * @author Ing. Valerio Frittelli.
 * @version Octubre de 2017.
 */
public class Test 
{
    public static void main(String args[])
    {
        // una tabla "corta" con factor de carga pequeño...
        TSBHashtableDA<Integer, String> ht1 = new TSBHashtableDA<>();
        System.out.println("Contenido inicial: " + ht1.toString());
        
        // algunas inserciones...
        ht1.put(1, "Argentina");
        ht1.put(12, "Brasil");
        ht1.put(23, "Chile");
        ht1.put(34, "Mexico");
        ht1.put(45, "Uruguay");
        System.out.println("Luego de algunas inserciones: " + ht1);
        System.out.println(ht1.mostrarVector());
        ht1.put(56, "Perú");
        ht1.put(7, "Colombia");
        ht1.put(8, "Ecuador");
        ht1.put(9, "Paraguay");
        System.out.println("Luego de algunas inserciones2: " + ht1);
        System.out.println(ht1.mostrarVector());
        ht1.put(10, "Bolivia");
        ht1.put(11, "Venezuela");
        ht1.put(13, "Estados Unidos");
        System.out.println("Luego de algunas inserciones3: " + ht1);
        System.out.println(ht1.mostrarVector());


        String cad1 = ht1.remove(1);
        String cad2 = ht1.remove(2);
        String cad3 = ht1.remove(9);
        System.out.println("Luego de algunas Deserciones: " + ht1);
        System.out.println(ht1.mostrarVector());





        //TSBHashtable<Integer, String> ht2 = new TSBHashtable<>(ht1);
        //System.out.println("Segunda tabla: " + ht2);
        
        System.out.println("Tabla 1 recorrida a partir de una vista: ");
        Set<Map.Entry<Integer, String>> se = ht1.entrySet();
        Iterator<Map.Entry<Integer, String>> it = se.iterator();
        while(it.hasNext())
        {
            Map.Entry<Integer, String> entry = it.next();
            System.out.println("Par: " + entry);
        }
    }
}
