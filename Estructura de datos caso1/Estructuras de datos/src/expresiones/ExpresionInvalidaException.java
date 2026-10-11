package expresiones;


public class ExpresionInvalidaException extends Exception {

    private static final long serialVersionUID = 1L;
    private final int posicion;

    /** Error sin posición asociada (posicion = 0). */
    public ExpresionInvalidaException(String mensaje) {
        this(mensaje, 0);
    }

    /** Error asociado a una posición de la expresión original. */
    public ExpresionInvalidaException(String mensaje, int posicion) {
        super(posicion > 0 ? mensaje + " (posición " + posicion + ")" : mensaje);
        this.posicion = posicion;
    }

    public int getPosicion() {
        return posicion;
    }
}
