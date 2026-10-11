package expresiones;

/**
 * Se lanza al intentar retirar (pop) o consultar (peek) en una pila vacía.
 * Representa el "subdesbordamiento" (underflow) de la pila.
 */
public class PilaVaciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PilaVaciaException(String mensaje) {
        super(mensaje);
    }
}
