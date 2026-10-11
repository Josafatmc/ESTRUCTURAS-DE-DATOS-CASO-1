package expresiones;

/**
 * Categorías de token que reconoce el analizador de expresiones aritméticas.
 */
public enum TipoToken {
    NUMERO,            // literal numérico, p. ej. 3 o 4.25
    VARIABLE,          // identificador, p. ej. x o precio_1
    OPERADOR,          // operador binario: + - * / ^
    OPERADOR_UNARIO,   // signo prefijo: -x o +x
    PARENTESIS_IZQ,    // (
    PARENTESIS_DER     // )
}
