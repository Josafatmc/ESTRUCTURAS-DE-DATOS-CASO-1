package expresiones;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Token {

    private final TipoToken tipo;
    private final String texto;   // texto tal como aparece en la expresión
    private final double valor;   // solo significativo para tokens NUMERO
    private final int posicion;   // posición (base 1) en la expresión original; 0 si no aplica

    private Token(TipoToken tipo, String texto, double valor, int posicion) {
        this.tipo = tipo;
        this.texto = texto;
        this.valor = valor;
        this.posicion = posicion;
    }

    // ---------- Fábricas ----------

    /** Número literal; el texto ya fue validado por el analizador léxico. */
    public static Token numero(String texto, int posicion) {
        return new Token(TipoToken.NUMERO, texto, Double.parseDouble(texto), posicion);
    }

    /** Número calculado (resultado intermedio durante la evaluación). */
    public static Token numero(double valor) {
        return new Token(TipoToken.NUMERO, formatearNumero(valor), valor, 0);
    }

    public static Token variable(String nombre, int posicion) {
        return new Token(TipoToken.VARIABLE, nombre, 0, posicion);
    }

    public static Token operador(String simbolo, int posicion) {
        return new Token(TipoToken.OPERADOR, simbolo, 0, posicion);
    }

    public static Token unario(String simbolo, int posicion) {
        return new Token(TipoToken.OPERADOR_UNARIO, simbolo, 0, posicion);
    }

    public static Token parentesisIzq(int posicion) {
        return new Token(TipoToken.PARENTESIS_IZQ, "(", 0, posicion);
    }

    public static Token parentesisDer(int posicion) {
        return new Token(TipoToken.PARENTESIS_DER, ")", 0, posicion);
    }

    // ---------- Consultas ----------

    public TipoToken getTipo() { return tipo; }
    public String getTexto() { return texto; }
    public double getValor() { return valor; }
    public int getPosicion() { return posicion; }

    /** Indica si es un operador (binario o unario). */
    public boolean esOperador() {
        return tipo == TipoToken.OPERADOR || tipo == TipoToken.OPERADOR_UNARIO;
    }

    public int precedencia() {
        if (tipo == TipoToken.OPERADOR_UNARIO) {
            return 3;
        }
        switch (texto) {
            case "+": case "-": return 1;
            case "*": case "/": return 2;
            case "^":           return 4;
            default:            return 0; // no es operador
        }
    }

    /** Asociatividad derecha: la potencia (2^3^2 = 2^9) y los signos unarios. */
    public boolean esAsociativoDerecha() {
        return tipo == TipoToken.OPERADOR_UNARIO || "^".equals(texto);
    }

    /** Texto para mostrar en trazas; distingue el signo unario con el prefijo "u". */
    public String etiqueta() {
        return tipo == TipoToken.OPERADOR_UNARIO ? "u" + texto : texto;
    }

    @Override
    public String toString() {
        return etiqueta();
    }


    /** Formatea un número sin notación científica y sin ceros finales (máx. 10 decimales). */
    public static String formatearNumero(double v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e15) {
            return Long.toString((long) v);
        }
        return new BigDecimal(v).setScale(10, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
    }
}
