package expresiones;

/**
 * Se lanza al intentar insertar (push) en una pila que alcanzó su capacidad máxima.
 * Representa el "desbordamiento" (overflow) de la pila.
 */
public class PilaLlenaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PilaLlenaException(String mensaje) {
        super(mensaje);
    }
}
