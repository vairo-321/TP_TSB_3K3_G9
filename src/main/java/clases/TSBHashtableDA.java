package clases;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class TSBHashtableDA<K, V> extends AbstractMap<K, V> {
    //************************ Constantes (privadas o públicas).

    // el tamaño máximo que podrá tener el arreglo de soprte...
    private final static int MAX_SIZE = Integer.MAX_VALUE;

    //************************ Atributos privados (estructurales).

    // la tabla hash: el objeto Casilla contiene el estado de la casilla
    private Casilla<Map.Entry<K, V>> table[];

    // el tamaño inicial de la tabla (tamaño con el que fue creada)...
    private int initial_capacity;

    // la cantidad de objetos que contiene la tabla en TODAS sus listas...
    private int count;

    //************************ Atributos privados (para gestionar las vistas).

    /*
     * (Tal cual están definidos en la clase java.util.Hashtable)
     * Cada uno de estos campos se inicializa para contener una instancia de la
     * vista que sea más apropiada, la primera vez que esa vista es requerida.
     * La vista son objetos stateless (no se requiere que almacenen datos, sino
     * que sólo soportan operaciones), y por lo tanto no es necesario crear más
     * de una de cada una.
     */
    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K, V>> entrySet = null;
    private transient Collection<V> values = null;

    //************************ Constructores.

    public TSBHashtableDA() {
        this(11);
    }

    public TSBHashtableDA(int capacidad_inicial) {
        if (capacidad_inicial <= 1) {
            initial_capacity = 11;
        } else {
            if (capacidad_inicial > TSBHashtableDA.MAX_SIZE) {
                initial_capacity = TSBHashtableDA.MAX_SIZE; // 2147483647 es primo
            }
        }

        if (!esPrimo(capacidad_inicial))
        {
            capacidad_inicial = siguientePrimo(capacidad_inicial);
        }
        this.table = new Casilla[capacidad_inicial];
        for (int i = 0; i < table.length; i++) {
            table[i] = new Casilla<>();
        }

        this.initial_capacity = capacidad_inicial;
        this.count = 0;
    }


    //************************ Implementación de métodos especificados por Map.

    //Unico metodo abstracto de la clase AbstractMap<>
    public Set<Map.Entry<K, V>> entrySet() {

        return entrySet;
    }

    //************************ Redefinición de métodos heredados desde Object.


    //************************ Métodos específicos de la clase.


    //************************ Métodos privados.

    /*
     * Función hash. Toma una clave entera k y calcula y retorna un índice
     * válido para esa clave para entrar en la tabla.
     */
    private int h(int k) {
        return h(k, this.table.length);
    }

    /*
     * Función hash. Toma un objeto key que representa una clave y calcula y
     * retorna un índice válido para esa clave para entrar en la tabla.
     */
    private int h(K key) {
        return h(key.hashCode(), this.table.length);
    }

    /*
     * Función hash. Toma un objeto key que representa una clave y un tamaño de
     * tabla t, y calcula y retorna un índice válido para esa clave dedo ese
     * tamaño.
     */
    private int h(K key, int t) {
        return h(key.hashCode(), t);
    }

    /*
     * Función hash. Toma una clave entera k y un tamaño de tabla t, y calcula y
     * retorna un índice válido para esa clave dado ese tamaño.
     */
    private int h(int k, int t) {
        if (k < 0) k *= -1;
        return k % t;
    }


//************************ Clases Internas.


    //*************************** metodos sin clasificacion hechos x mi
    private static final int siguientePrimo(int n) {
        if (n % 2 == 0) n++;
        for (; !esPrimo(n); n += 2) ;
        return n;

    }

    private static boolean esPrimo(int n) {
        for (int i = 2; i < n; n++)
        {
            if (n % i == 0) return false;
        }
        return true;
    }


}