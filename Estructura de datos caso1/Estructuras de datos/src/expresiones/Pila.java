package expresiones;

public class Pila {

    /** Capacidad por defecto; evita que una entrada enorme consuma memoria sin control. */
    public static final int CAPACIDAD_POR_DEFECTO = 1000;

    private Nodo cima;                 // nodo superior; null si la pila está vacía
    private int tamano;                // cantidad de nodos
    private final int capacidadMaxima; // límite de elementos

    public Pila() {
        this(CAPACIDAD_POR_DEFECTO);
    }

    public Pila(int capacidadMaxima) {
        if (capacidadMaxima <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
        }
        this.cima = null;
        this.tamano = 0;
        this.capacidadMaxima = capacidadMaxima;
    }

    // ---------- Operaciones fundamentales ----------

    /** Inserta un token en la cima. O(1). */
    public void push(Token token) {
        if (token == null) {
            throw new IllegalArgumentException("No se puede apilar un token nulo.");
        }
        if (tamano >= capacidadMaxima) {
            throw new PilaLlenaException("Desbordamiento: la pila alcanzó su capacidad "
                    + "máxima (" + capacidadMaxima + ").");
        }
        // El nuevo nodo apunta a la cima anterior y pasa a ser la nueva cima.
        cima = new Nodo(token, cima);
        tamano++;
    }

    /** Retira y devuelve el token de la cima. O(1). */
    public Token pop() {
        if (isEmpty()) {
            throw new PilaVaciaException(
                    "Subdesbordamiento: no se puede hacer pop en una pila vacía.");
        }
        Nodo retirado = cima;
        cima = retirado.getSiguiente();   // la cima baja un nivel (sin perder referencias)
        retirado.setSiguiente(null);      // desconecta el nodo retirado
        tamano--;
        return retirado.getDato();
    }

    /** Devuelve el token de la cima sin retirarlo. O(1). */
    public Token peek() {
        if (isEmpty()) {
            throw new PilaVaciaException(
                    "Subdesbordamiento: no se puede consultar la cima de una pila vacía.");
        }
        return cima.getDato();
    }

    /** Indica si la pila no tiene elementos. O(1). */
    public boolean isEmpty() {
        return cima == null;
    }

    /** Cantidad de elementos almacenados. O(1). */
    public int size() {
        return tamano;
    }

    // ---------- Métodos adicionales ----------

    /** Elimina todos los elementos. */
    public void vaciar() {
        cima = null;
        tamano = 0;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    /** Representación en una línea, de la base a la cima: [ ( , + ] ← la cima es el último. */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "[ ]";
        }
        // Se recorre desde la cima hacia la base y se arma el texto en orden inverso.
        StringBuilder sb = new StringBuilder();
        for (Nodo actual = cima; actual != null; actual = actual.getSiguiente()) {
            sb.insert(0, actual.getDato().etiqueta());
            if (actual.getSiguiente() != null) {
                sb.insert(0, " , ");
            }
        }
        return "[ " + sb + " ]";
    }

    /** Dibujo vertical en texto, con la cima arriba. */
    public String mostrarVertical() {
        if (isEmpty()) {
            return "   (pila vacía: cima = null)";
        }
        int ancho = 3;
        for (Nodo n = cima; n != null; n = n.getSiguiente()) {
            ancho = Math.max(ancho, n.getDato().etiqueta().length());
        }
        String borde = "   +" + "-".repeat(ancho + 2) + "+";
        StringBuilder sb = new StringBuilder();
        for (Nodo n = cima; n != null; n = n.getSiguiente()) {
            String et = n.getDato().etiqueta();
            int izq = (ancho - et.length()) / 2;
            int der = ancho - et.length() - izq;
            sb.append(borde).append('\n');
            sb.append("   | ").append(" ".repeat(izq)).append(et).append(" ".repeat(der)).append(" |");
            if (n == cima) {
                sb.append("  <-- CIMA");
            }
            sb.append('\n');
        }
        sb.append(borde);
        return sb.toString();
    }
}
