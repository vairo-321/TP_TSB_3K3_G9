package clases;

import java.io.Serializable;
import java.util.*;

public class TSBHashtableDA<K, V> extends AbstractMap<K, V> implements Cloneable, Serializable
{
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

    //************************ Atributos protegidos (control de iteración).

    // conteo de operaciones de cambio de tamaño (fail-fast iterator).
    protected transient int modCount;

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

        if (!esPrimo(capacidad_inicial)) {
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

    /**
     * Elimina todo el contenido de la tabla, de forma de dejarla vacía. En esta
     * implementación además, el arreglo de soporte vuelve a tener el tamaño que
     * inicialmente tuvo al ser creado el objeto.
     */
    @Override
    public void clear() {
        this.table = new Casilla[this.initial_capacity];
        for (int i = 0; i < this.table.length; i++) {
            this.table[i] = new Casilla<>();
        }
        this.count = 0;
    }

    /**
     * Retorna la cantidad de elementos contenidos en la tabla.
     *
     * @return la cantidad de elementos de la tabla.
     */
    @Override
    public int size() {
        return this.count;
    }

    /**
     * Determina si la tabla está vacía (no contiene ningún elemento).
     *
     * @return true si la tabla está vacía.
     */
    @Override
    public boolean isEmpty() {
        return (this.count == 0);
    }

    /**
     * Determina si la clave key está en la tabla.
     *
     * @param key la clave a verificar.
     * @return true si la clave está en la tabla.
     * @throws NullPointerException si la clave es null.
     */
    @Override
    public boolean containsKey(Object key) {
        return (this.get((K) key) != null);
    }

    /**
     * Retorna el objeto al cual está asociada la clave key en la tabla, o null
     * si la tabla no contiene ningún objeto asociado a esa clave.
     *
     * @param key la clave que será buscada en la tabla.
     * @return el objeto asociado a la clave especificada (si existe la clave) o
     * null (si no existe la clave en esta tabla).
     * @throws NullPointerException si key es null.
     * @throws ClassCastException   si la clase de key no es compatible con la
     *                              tabla.
     */
    @Override
    public V get(Object key) {
        if (key == null) throw new NullPointerException("get(): parámetro null");

        int ib = this.h(key.hashCode());
        Map.Entry<K, V> x = this.buscarObjeto(ib, (K) key);
        return (x != null) ? x.getValue() : null;
    }

    /**
     * Determina si alguna clave de la tabla está asociada al objeto value que
     * entra como parámetro. Equivale a contains().
     *
     * @param value el objeto a buscar en la tabla.
     * @return true si alguna clave está asociada efectivamente a ese value.
     */
    @Override
    public boolean containsValue(Object value) {
        return this.contains(value);
    }

    /**
     * Asocia el valor (value) especificado, con la clave (key) especificada en
     * esta tabla. Si la tabla contenía previamente un valor asociado para la
     * clave, entonces el valor anterior es reemplazado por el nuevo (y en este
     * caso el tamaño de la tabla no cambia).
     *
     * @param key   la clave del objeto que se quiere agregar a la tabla.
     * @param value el objeto que se quiere agregar a la tabla.
     * @return el objeto anteriormente asociado a la clave si la clave ya
     * estaba asociada con alguno, o null si la clave no estaba antes
     * asociada a ningún objeto.
     * @throws NullPointerException si key es null o value es null.
     */
    @Override
    public V put(K key, V value) {
        if (key == null || value == null) throw new NullPointerException("put(): parámetro null");

        int indice = buscarObjeto(key);

        Casilla<Map.Entry<K, V>> x = table[indice];

        V objAntiguo = null;

        if (x.getDato() != null) {
            count--;
            objAntiguo = x.getDato().getValue();
        }

        x.setEstado("cerrada");
        Map.Entry<K, V> entry = new Entry<>(key, value);
        x.setDato(entry);
        count++;
        modCount++;

        if (reducirTabla()) this.rehash();

        return objAntiguo;

    }

    /**
     * Elimina de la tabla la clave key (y su correspondiente valor asociado).
     * El método no hace nada si la clave no está en la tabla.
     *
     * @param key la clave a eliminar.
     * @return El objeto al cual la clave estaba asociada, o null si la clave no
     * estaba en la tabla.
     * @throws NullPointerException - if the key is null.
     */
    @Override
    public V remove(Object key) {
        if (key == null) throw new NullPointerException("remove(): parámetro null");

        int indice = buscarObjeto((K) key);

        if (table[indice].getEstado() == "cerrada") {
            table[indice].setEstado("tumba");
            count--;
            modCount++;

            V objeto = table[indice].getDato().getValue();

            table[indice].setDato(null);

            return objeto;

        } else {
            return null;
        }

    }

    public V removeIndice(int indice) {
        if (indice < 0 || indice >= table.length) throw new NullPointerException("remove(): Rango no valido");

        if (table[indice].getEstado() == "cerrada") {
            table[indice].setEstado("tumba");
            count--;
            modCount++;

            V objeto = table[indice].getDato().getValue();

            table[indice].setDato(null);

            return objeto;

        } else {
            return null;
        }

    }


    //************************ Redefinición de métodos heredados desde Object.

    /**
     * Devuelve el contenido de la tabla en forma de String. Sólo por razones de
     * didáctica, se hace referencia explícita en esa cadena al contenido de
     * cada una de las listas de desborde o buckets de la tabla.
     *
     * @return una cadena con el contenido completo de la tabla.
     */
    @Override
    public String toString() {
        StringBuilder cad = new StringBuilder("");
        for (int i = 0; i < this.table.length; i++) {
            if (table[i].getEstado() == "cerrada") {
                cad.append("\n->  ").append(table[i].toString()).append("\n\t");
            }
        }
        return cad.toString();
    }

    /**
     * Retorna una copia superficial de la tabla.
     * @return una copia superficial de la tabla.
     * @throws java.lang.CloneNotSupportedException si la clase no implementa la
     *         interface Cloneable.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        TSBHashtableDA<K, V> t = (TSBHashtableDA<K, V>)super.clone();
        t.table = new Casilla[table.length];
        for (int i = table.length ; i-- > 0 ; )
        {
            t.table[i] = (Casilla<Map.Entry<K,V>>) table[i].clone();
        }
        t.keySet = null;
        t.entrySet = null;
        t.values = null;
        t.modCount = 0;
        return t;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        for (int i = 0; i < this.table.length; i++) {
            if (table[i].getEstado() == "cerrada") {
                hash += 61 * hash + Objects.hashCode(table[i]);
            }
        }
        return hash;
    }

    /**
     * Determina si esta tabla es igual al objeto espeficicado.
     * @param obj el objeto a comparar con esta tabla.
     * @return true si los objetos son iguales.
     */
    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Map)) { return false; }

        Map<K, V> t = (Map<K, V>) obj;
        if(t.size() != this.size()) { return false; }

        try
        {
            Iterator<Map.Entry<K,V>> i = this.entrySet().iterator();
            while(i.hasNext())
            {
                Map.Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if(t.get(key) == null) { return false; }
                else
                {
                    if(!value.equals(t.get(key))) { return false; }
                }
            }
        }

        catch (ClassCastException | NullPointerException e)
        {
            return false;
        }

        return true;
    }

    //************************ Métodos específicos de la clase.

    /**
     * Determina si alguna clave de la tabla está asociada al objeto value que
     * entra como parámetro. Equivale a containsValue().
     *
     * @param value el objeto a buscar en la tabla.
     * @return true si alguna clave está asociada efectivamente a ese value.
     */
    public boolean contains(Object value) {
        /* SE RECORRE LA TABLA SECUENCIALMENTE YA QUE NADA ME GARANTIZA ENCONTRAR
        EL OBJETO value Antes de ver hasta la ultima casilla, por lo tanto
        la complejidad es de la magnitud de O(n)
         */
        if (value == null) return false;

        for (Casilla<Map.Entry<K, V>> cas : this.table) {
            if (cas.getEstado() == "cerrada") {
                Map.Entry<K, V> entry = (Map.Entry<K, V>) cas.getDato();
                if (value.equals(entry.getValue())) return true;
            }
        }

        return false;
    }

    /**
     * Incrementa el tamaño de la tabla y reorganiza su contenido. Se invoca
     * automaticamente cuando se detecta que la cantidad Valores en la tabla supera
     * el 50% de la misma. Esto es asi para garantizar que todos los objetos encuentren
     * un lugar disponible en la tabla con el algoritmo para evitar las famosas islas de objetos
     */
    protected void rehash()  //posible error en inicializar Casillas
    {
        //Primero buscamos la nueva cantidad de indices

        int newLength = siguientePrimo(table.length);

        if (newLength > TSBHashtableDA.MAX_SIZE) {
            newLength = TSBHashtableDA.MAX_SIZE;
        }

        Casilla<Map.Entry<K, V>> tablaVieja[] = table;

        table = new Casilla[newLength];
        for (int i = 0; i < table.length; i++) {
            table[i] = new Casilla<>();
        }
        count = 0;

        for (int i = 0; i < tablaVieja.length; i++) {
            if (tablaVieja[i].getEstado() == "cerrada") {
                Map.Entry<K, V> entry = tablaVieja[i].getDato();
                this.put(entry.getKey(), entry.getValue());
            }
        }

        modCount++;
    }


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

    /*
     * Clase interna que representa los pares de objetos que se almacenan en la
     * tabla hash: son instancias de esta clase las que realmente se guardan en
     * en cada una de las listas del arreglo table que se usa como soporte de
     * la tabla. Lanzará una IllegalArgumentException si alguno de los dos
     * parámetros es null.
     */
    private class Entry<K, V> implements Map.Entry<K, V> {
        private K key;
        private V value;

        public Entry(K key, V value) {
            if (key == null || value == null) {
                throw new IllegalArgumentException("Entry(): parámetro null...");
            }
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            if (value == null) {
                throw new IllegalArgumentException("setValue(): parámetro null...");
            }

            V old = this.value;
            this.value = value;
            return old;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }

            final TSBHashtableDA.Entry other = (TSBHashtableDA.Entry) obj;
            if (!Objects.equals(this.key, other.key)) {
                return false;
            }
            if (!Objects.equals(this.value, other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "(" + key.toString() + ", " + value.toString() + ")";
        }
    }


    /*
     * Clase interna que representa una vista de todas los Claves mapeadas en la
     * tabla: si la vista cambia, cambia también la tabla que le da respaldo, y
     * viceversa. La vista es stateless: no mantiene estado alguno (es decir, no
     * contiene datos ella misma, sino que accede y gestiona directamente datos
     * de otra fuente), por lo que no tiene atributos y sus métodos gestionan en
     * forma directa el contenido de la tabla. Están soportados los metodos para
     * eliminar un objeto (remove()), eliminar todo el contenido (clear) y la
     * creación de un Iterator (que incluye el método Iterator.remove()).
     */
    private class KeySet extends AbstractSet<K>
    {
        @Override
        public Iterator<K> iterator()
        {
            return new TSBHashtableDA.KeySet.KeySetIterator();
        }

        @Override
        public int size()
        {
            return TSBHashtableDA.this.count;
        }

        @Override
        public boolean contains(Object o)
        {
            return TSBHashtableDA.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o)
        {
            return (TSBHashtableDA.this.remove(o) != null);
        }

        @Override
        public void clear()
        {
            TSBHashtableDA.this.clear();
        }

        private class KeySetIterator implements Iterator<K>
        {

            // índice de la lista anterior (si se requiere en remove())...
            private int casilla_anterior;

            // índice del elemento actual en el iterador (el que fue retornado
            // la última vez por next() y será eliminado por remove())...
            private int casilla_actual;

            private int casillasProcesadas;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public KeySetIterator()
            {
                casillasProcesadas = 0;
                casilla_actual = -1;
                casilla_anterior = -1;
                next_ok = false;
                expected_modCount = TSBHashtableDA.this.modCount;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext()
            {
                if (TSBHashtableDA.this.isEmpty()) return false;
                if (casillasProcesadas == count) return false;
                return true;

            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public K next()
            {
                // control: fail-fast iterator...
                if(TSBHashtableDA.this.modCount != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                Casilla<Map.Entry<K, V>> t[] = TSBHashtableDA.this.table;

                Map.Entry<K, V> objeto = null;

                for ( int i = casilla_actual ; i < t.length; i++) {
                    if (i == -1) continue;

                    if ( t[i].getEstado() == "cerrada")
                    {
                        objeto = t[i].getDato();
                        casilla_anterior = casilla_actual;
                        casilla_actual = i;
                        break;
                    }

                }

                // avisar que next() fue invocado con éxito...
                next_ok = true;
                casillasProcesadas ++;

                // y retornar la clave del elemento alcanzado...
                K key = objeto.getKey();
                return key;
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove()
            {
                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                // eliminar el objeto que retornó next() la última vez...
                TSBHashtableDA.this.removeIndice(casilla_actual);

                casilla_actual = casilla_anterior;

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                casillasProcesadas --;

                // la suma del modCount y la resta de Count, ya la hace el metodo remove()
                // fail_fast iterator: todo en orden...
                //TSBHashtableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }

    /*
     * Clase interna que representa una vista de todos los PARES mapeados en la
     * tabla: si la vista cambia, cambia también la tabla que le da respaldo, y
     * viceversa. La vista es stateless: no mantiene estado alguno (es decir, no
     * contiene datos ella misma, sino que accede y gestiona directamente datos
     * de otra fuente), por lo que no tiene atributos y sus métodos gestionan en
     * forma directa el contenido de la tabla. Están soportados los metodos para
     * eliminar un objeto (remove()), eliminar todo el contenido (clear) y la
     * creación de un Iterator (que incluye el método Iterator.remove()).
     */
    private class EntrySet extends AbstractSet<Map.Entry<K, V>>
    {

        @Override
        public Iterator<Map.Entry<K, V>> iterator()
        {
            return new TSBHashtableDA.EntrySet.EntrySetIterator();
        }

        /*
         * Verifica si esta vista (y por lo tanto la tabla) contiene al par
         * que entra como parámetro (que debe ser de la clase Entry).
         */
        @Override
        public boolean contains(Object o)
        {
            if(o == null) { return false; }
            if(!(o instanceof TSBHashtableDA.Entry)) { return false; }

            Map.Entry<K, V> entry = (Map.Entry<K,V>)o;
            K key = entry.getKey();
            int index = buscarObjeto(key);

            if (table[index].getEstado() == "cerrada")
            {
                return true;
            }
            else return false;
        }

        /*
         * Elimina de esta vista (y por lo tanto de la tabla) al par que entra
         * como parámetro (y que debe ser de tipo Entry).
         */
        @Override
        public boolean remove(Object o)
        {
            if(o == null) { throw new NullPointerException("remove(): parámetro null");}
            if(!(o instanceof TSBHashtableDA.Entry)) { return false; }

            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            K key = entry.getKey();
            V obj = TSBHashtableDA.this.remove(key);

            if (obj != null) return true;
            else return false;
        }

        @Override
        public int size()
        {
            return TSBHashtableDA.this.count;
        }

        @Override
        public void clear()
        {
            TSBHashtableDA.this.clear();
        }

        private class EntrySetIterator implements Iterator<Map.Entry<K, V>>
        {

            // índice de la lista anterior (si se requiere en remove())...
            private int casilla_anterior;

            // índice del elemento actual en el iterador (el que fue retornado
            // la última vez por next() y será eliminado por remove())...
            private int casilla_actual;

            private int casillasProcesadas;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public EntrySetIterator()
            {
                casillasProcesadas = 0;
                casilla_actual = -1;
                casilla_anterior = -1;
                next_ok = false;
                expected_modCount = TSBHashtableDA.this.modCount;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext()
            {
                if (TSBHashtableDA.this.isEmpty()) return false;
                if (casillasProcesadas == count) return false;
                return true;

            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public Map.Entry<K, V> next()
            {
                // control: fail-fast iterator...
                if(TSBHashtableDA.this.modCount != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                Casilla<Map.Entry<K, V>> t[] = TSBHashtableDA.this.table;

                Map.Entry<K, V> objeto = null;

                for ( int i = casilla_actual ; i < t.length; i++) {
                    if (i == -1) continue;

                    if ( t[i].getEstado() == "cerrada")
                    {
                        objeto = t[i].getDato();
                        casilla_anterior = casilla_actual;
                        casilla_actual = i;
                        break;
                    }

                }

                // avisar que next() fue invocado con éxito...
                next_ok = true;
                casillasProcesadas ++;

                // y retornar la clave del elemento alcanzado...
                return objeto;
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove()
            {
                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                // eliminar el objeto que retornó next() la última vez...
                TSBHashtableDA.this.removeIndice(casilla_actual);

                casilla_actual = casilla_anterior;

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                casillasProcesadas --;

                // la suma del modCount y la resta de Count, ya la hace el metodo remove()
                // fail_fast iterator: todo en orden...
                //TSBHashtableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }

    /*
     * Clase interna que representa una vista de todos los VALORES mapeados en
     * la tabla: si la vista cambia, cambia también la tabla que le da respaldo,
     * y viceversa. La vista es stateless: no mantiene estado alguno (es decir,
     * no contiene datos ella misma, sino que accede y gestiona directamente los
     * de otra fuente), por lo que no tiene atributos y sus métodos gestionan en
     * forma directa el contenido de la tabla. Están soportados los metodos para
     * eliminar un objeto (remove()), eliminar todo el contenido (clear) y la
     * creación de un Iterator (que incluye el método Iterator.remove()).
     */
    private class ValueCollection extends AbstractCollection<V>
    {
        @Override
        public Iterator<V> iterator()
        {
            return new TSBHashtableDA.ValueCollection.ValueCollectionIterator();
        }

        @Override
        public int size()
        {
            return TSBHashtableDA.this.count;
        }

        @Override
        public boolean contains(Object o)
        {
            return TSBHashtableDA.this.containsValue(o);
        }

        @Override
        public void clear()
        {
            TSBHashtableDA.this.clear();
        }

        private class ValueCollectionIterator implements Iterator<V>
        {

            // índice de la lista anterior (si se requiere en remove())...
            private int casilla_anterior;

            // índice del elemento actual en el iterador (el que fue retornado
            // la última vez por next() y será eliminado por remove())...
            private int casilla_actual;

            private int casillasProcesadas;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public ValueCollectionIterator()
            {
                casillasProcesadas = 0;
                casilla_actual = -1;
                casilla_anterior = -1;
                next_ok = false;
                expected_modCount = TSBHashtableDA.this.modCount;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext()
            {
                if (TSBHashtableDA.this.isEmpty()) return false;
                if (casillasProcesadas == count) return false;
                return true;

            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public V next()
            {
                // control: fail-fast iterator...
                if(TSBHashtableDA.this.modCount != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                Casilla<Map.Entry<K, V>> t[] = TSBHashtableDA.this.table;

                Map.Entry<K, V> objeto = null;

                for ( int i = casilla_actual ; i < t.length; i++) {
                    if (i == -1) continue;

                    if ( t[i].getEstado() == "cerrada")
                    {
                        objeto = t[i].getDato();
                        casilla_anterior = casilla_actual;
                        casilla_actual = i;
                        break;
                    }

                }

                // avisar que next() fue invocado con éxito...
                next_ok = true;
                casillasProcesadas ++;

                // y retornar la clave del elemento alcanzado...
                V valor = objeto.getValue();
                return valor;
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove()
            {
                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                // eliminar el objeto que retornó next() la última vez...
                TSBHashtableDA.this.removeIndice(casilla_actual);

                casilla_actual = casilla_anterior;

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                casillasProcesadas --;

                // la suma del modCount y la resta de Count, ya la hace el metodo remove()
                // fail_fast iterator: todo en orden...
                //TSBHashtableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }

    //*************************** metodos sin clasificacion hechos x mi
    private static final int siguientePrimo(int n) {
        n++;
        if (n % 2 == 0) n++;
        for (; !esPrimo(n); n += 2) ;
        return n;

    }

    private static boolean esPrimo(int n) {
        for (int i = 2; i < n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    private Map.Entry<K, V> buscarObjeto(int indiceInicio, K key) {
        int i = indiceInicio;
        int j = 1;

        while (true) {
            Casilla casilla = table[i];
            if (casilla.getEstado() != "abierta") {
                if (casilla.getEstado() != "tumba") {
                    Map.Entry<K, V> entry = (Map.Entry<K, V>) casilla.getDato();
                    if (key.equals(entry.getKey())) return entry;

                }
                i = (int) (i + Math.pow(j, 2));
                if (i >= table.length) {
                    i = i % table.length;
                }
                j++;
            } else {
                return null;
            }
        }
    }

    /*
        Este metodo busca el objeto antes de realizar el pul, si lo encuentra
        devuelve su indice en la tabla, y si no devuelve el indice de la primera
        tumba o casilla abierta encontrada
     */
    private int buscarObjeto(K key) {
        int i = this.h(key.hashCode());
        int j = 1;
        int iFinal = i;
        int iInicial = i;
        boolean tumba = false;

        while (true) {
            Casilla casilla = table[i];
            if (casilla.getEstado() != "abierta") {
                if (casilla.getEstado() != "tumba") {
                    Map.Entry<K, V> entry = (Map.Entry<K, V>) casilla.getDato();
                    if (key.equals(entry.getKey())) {
                        iFinal = i;
                        return iFinal;
                    }

                } else {
                    tumba = true;
                    iFinal = i;
                }
                i = (int) (iInicial + Math.pow(j, 2));
                if (i >= table.length) {
                    i = i % table.length;
                }
                j++;
            } else {
                if (!tumba) iFinal = i;
                return iFinal;
            }
        }
    }

    private boolean reducirTabla() {
        if (count > table.length / 2) return true;
        return false;
    }


    //********************* metodos de testeo propias

    public String mostrarVector()
    {
        String cadena = "";
        for (int i = 0; i < table.length; i++) {
            String cad1 = "indice" + i + " ";
            String cad2 = table[i].toString();
            String cad3 = "\n";
            cadena += cad1 + cad2 + cad3;
        }
        return cadena;
    }

}