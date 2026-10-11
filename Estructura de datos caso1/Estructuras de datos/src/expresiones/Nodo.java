package expresiones;


public class Nodo {

    private Token dato;
    private Nodo siguiente;

    /** Crea un nodo aislado (su siguiente es null). */
    public Nodo(Token dato) {
        this(dato, null);
    }

    /** Crea un nodo que apunta al nodo indicado. */
    public Nodo(Token dato, Nodo siguiente) {
        this.dato = dato;
        this.siguiente = siguiente;
    }

    public Token getDato() { return dato; }
    public void setDato(Token dato) { this.dato = dato; }

    public Nodo getSiguiente() { return siguiente; }
    public void setSiguiente(Nodo siguiente) { this.siguiente = siguiente; }

    @Override
    public String toString() {
        return "Nodo[" + dato + "]";
    }
}
