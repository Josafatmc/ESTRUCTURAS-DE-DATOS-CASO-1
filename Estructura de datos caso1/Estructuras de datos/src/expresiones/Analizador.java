package expresiones;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Analizador {

    /** Longitud máxima aceptada para una expresión (limita el consumo de recursos). */
    public static final int LONGITUD_MAXIMA = 200;

    private static final String OPERADORES = "+-*/^";

    // Trazas de la última ejecución (cada fila = un paso del algoritmo)
    private final List<String[]> filasValidacion = new ArrayList<>();
    private final List<String[]> filasConversion = new ArrayList<>();
    private final List<String[]> filasEvaluacion = new ArrayList<>();

    // 1. ANÁLISIS LÉXICO
 

    /** Convierte el texto en tokens. Lanza ExpresionInvalidaException si hay símbolos no permitidos. */
    public List<Token> tokenizar(String expresion) throws ExpresionInvalidaException {
        if (expresion == null || expresion.trim().isEmpty()) {
            throw new ExpresionInvalidaException("La expresión está vacía.");
        }
        if (expresion.length() > LONGITUD_MAXIMA) {
            throw new ExpresionInvalidaException("La expresión supera el máximo de "
                    + LONGITUD_MAXIMA + " caracteres.");
        }

        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int n = expresion.length();

        while (i < n) {
            char c = expresion.charAt(i);
            int posicion = i + 1;

            if (Character.isWhitespace(c)) {
                i++;
            } else if (esDigito(c)) {
                int inicio = i;
                while (i < n && esDigito(expresion.charAt(i))) {
                    i++;
                }
                if (i < n && expresion.charAt(i) == '.') {
                    i++;
                    int inicioDecimales = i;
                    while (i < n && esDigito(expresion.charAt(i))) {
                        i++;
                    }
                    if (i == inicioDecimales) {
                        throw new ExpresionInvalidaException(
                                "Número mal formado: faltan dígitos después del punto decimal", posicion);
                    }
                }
                if (i < n && expresion.charAt(i) == '.') {
                    throw new ExpresionInvalidaException(
                            "Número mal formado: tiene más de un punto decimal", posicion);
                }
                String texto = expresion.substring(inicio, i);
                if (Double.isInfinite(Double.parseDouble(texto))) {
                    throw new ExpresionInvalidaException("Número fuera del rango representable", posicion);
                }
                tokens.add(Token.numero(texto, posicion));
            } else if (esInicioIdentificador(c)) {
                int inicio = i;
                while (i < n && (esInicioIdentificador(expresion.charAt(i)) || esDigito(expresion.charAt(i)))) {
                    i++;
                }
                tokens.add(Token.variable(expresion.substring(inicio, i), posicion));
            } else if (OPERADORES.indexOf(c) >= 0) {
                // + y - son unarios si aparecen donde se espera un operando
                // (al inicio, después de otro operador o después de '(').
                boolean enPosicionDeOperando = tokens.isEmpty() || tokens.get(tokens.size() - 1).esOperador()
                        || tokens.get(tokens.size() - 1).getTipo() == TipoToken.PARENTESIS_IZQ;
                if ((c == '+' || c == '-') && enPosicionDeOperando) {
                    tokens.add(Token.unario(String.valueOf(c), posicion));
                } else {
                    tokens.add(Token.operador(String.valueOf(c), posicion));
                }
                i++;
            } else if (c == '(') {
                tokens.add(Token.parentesisIzq(posicion));
                i++;
            } else if (c == ')') {
                tokens.add(Token.parentesisDer(posicion));
                i++;
            } else if (c == '.') {
                throw new ExpresionInvalidaException(
                        "Número mal formado: el punto decimal debe ir después de un dígito", posicion);
            } else {
                throw new ExpresionInvalidaException("Carácter no permitido '" + c + "'", posicion);
            }
        }
        return tokens;
    }

    private static boolean esDigito(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean esInicioIdentificador(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    // 2. VALIDACIÓN SINTÁCTICA (pila para los paréntesis)
   

    /**
     * Verifica que la secuencia de tokens forme una expresión bien construida.
     * Usa un indicador "esperaOperando" para detectar operadores consecutivos o faltantes
     * y una Pila para comprobar que los paréntesis estén balanceados.
     */
    public void validar(List<Token> tokens) throws ExpresionInvalidaException {
        filasValidacion.clear();
        if (tokens == null || tokens.isEmpty()) {
            throw new ExpresionInvalidaException("La expresión está vacía.");
        }

        Pila parentesis = new Pila();
        boolean esperaOperando = true;   // al inicio se espera un operando o '('
        Token previo = null;

        for (Token t : tokens) {
            switch (t.getTipo()) {
                case NUMERO:
                case VARIABLE:
                    if (!esperaOperando) {
                        throw new ExpresionInvalidaException("Falta un operador entre '"
                                + previo.getTexto() + "' y '" + t.getTexto() + "'", t.getPosicion());
                    }
                    esperaOperando = false;
                    break;

                case OPERADOR_UNARIO:
                    // El analizador léxico solo lo genera en posición de operando.
                    esperaOperando = true;
                    break;

                case OPERADOR:
                    if (esperaOperando) {
                        if (previo == null || previo.getTipo() == TipoToken.PARENTESIS_IZQ) {
                            throw new ExpresionInvalidaException("El operador '" + t.getTexto()
                                    + "' no tiene operando a su izquierda", t.getPosicion());
                        }
                        throw new ExpresionInvalidaException("Operadores consecutivos: '"
                                + previo.getTexto() + "' seguido de '" + t.getTexto() + "'", t.getPosicion());
                    }
                    esperaOperando = true;
                    break;

                case PARENTESIS_IZQ:
                    if (!esperaOperando) {
                        throw new ExpresionInvalidaException(
                                "Falta un operador antes del paréntesis de apertura", t.getPosicion());
                    }
                    parentesis.push(t);
                    registrar(filasValidacion, "(", "push: se abre un paréntesis", parentesis.toString());
                    break;

                case PARENTESIS_DER:
                    if (parentesis.isEmpty()) {
                        // Aquí la pila evita un error: sin ella no se detectaría el cierre sobrante.
                        // Se revisa primero porque es la causa más informativa del error.
                        throw new ExpresionInvalidaException("Paréntesis de cierre sin apertura correspondiente",
                                t.getPosicion());
                    }
                    if (esperaOperando) {
                        if (previo != null && previo.getTipo() == TipoToken.PARENTESIS_IZQ) {
                            throw new ExpresionInvalidaException("Paréntesis vacíos: '()'", t.getPosicion());
                        }
                        throw new ExpresionInvalidaException("Falta un operando antes del paréntesis de cierre",
                                t.getPosicion());
                    }
                    parentesis.pop();
                    registrar(filasValidacion, ")", "pop: se cierra el paréntesis más reciente", parentesis.toString());
                    break;

                default:
                    break;
            }
            previo = t;
        }

        if (esperaOperando) {
            throw new ExpresionInvalidaException("La expresión está incompleta: termina con '"
                    + previo.getTexto() + "'", previo.getPosicion());
        }
        if (!parentesis.isEmpty()) {
            throw new ExpresionInvalidaException("Paréntesis de apertura sin cerrar",
                    parentesis.peek().getPosicion());
        }
    }
    // 3. CONVERSIÓN INFIJA -> POSTFIJA (Shunting-Yard)
   
    public List<Token> aPostfija(List<Token> tokens) throws ExpresionInvalidaException {
        validar(tokens);            // solo se convierte lo que es sintácticamente correcto
        filasConversion.clear();

        Pila operadores = new Pila();
        List<Token> salida = new ArrayList<>();

        for (Token t : tokens) {
            String accion;
            switch (t.getTipo()) {
                case NUMERO:
                case VARIABLE:
                    salida.add(t);
                    accion = "Operando: pasa directo a la salida";
                    break;

                case OPERADOR_UNARIO:
                    operadores.push(t);   // prefijo: no retira nada, espera a su operando
                    accion = "Signo unario: push";
                    break;

                case OPERADOR:
                    StringBuilder retirados = new StringBuilder();
                    while (!operadores.isEmpty() && debeRetirar(operadores.peek(), t)) {
                        Token superior = operadores.pop();
                        salida.add(superior);
                        retirados.append(superior.etiqueta()).append(' ');
                    }
                    operadores.push(t);
                    accion = retirados.length() == 0
                            ? "Operador: push"
                            : "Operador: pop de " + retirados.toString().trim()
                                    + " a la salida; push";
                    break;

                case PARENTESIS_IZQ:
                    operadores.push(t);
                    accion = "Apertura: push";
                    break;

                case PARENTESIS_DER:
                    StringBuilder sacados = new StringBuilder();
                    while (operadores.peek().getTipo() != TipoToken.PARENTESIS_IZQ) {
                        Token superior = operadores.pop();
                        salida.add(superior);
                        sacados.append(superior.etiqueta()).append(' ');
                    }
                    operadores.pop();     // descarta el '(' correspondiente
                    accion = sacados.length() == 0
                            ? "Cierre: pop de '(' (se descarta)"
                            : "Cierre: pop de " + sacados.toString().trim()
                                    + " a la salida y de '('";
                    break;

                default:
                    accion = "";
                    break;
            }
            registrar(filasConversion, t.etiqueta(), accion, operadores.toString(),
                    textoPostfija(salida));
        }

        // Al terminar, los operadores restantes pasan a la salida (de la cima hacia la base).
        StringBuilder restantes = new StringBuilder();
        while (!operadores.isEmpty()) {
            Token superior = operadores.pop();
            salida.add(superior);
            restantes.append(superior.etiqueta()).append(' ');
        }
        registrar(filasConversion, "(fin)",
                restantes.length() == 0 ? "Fin: la pila ya estaba vacía"
                        : "Fin: pop de " + restantes.toString().trim() + " a la salida",
                operadores.toString(), textoPostfija(salida));
        return salida;
    }

    private boolean debeRetirar(Token cima, Token actual) {
        if (cima.getTipo() == TipoToken.PARENTESIS_IZQ) {
            return false;
        }
        int pCima = cima.precedencia();
        int pActual = actual.precedencia();
        return pCima > pActual || (pCima == pActual && !actual.esAsociativoDerecha());
    }

    public double evaluar(List<Token> postfija, Map<String, Double> variables)
            throws ExpresionInvalidaException {
        filasEvaluacion.clear();
        Pila valores = new Pila();
        Token actual = null;

        try {
            for (Token t : postfija) {
                actual = t;
                String accion;
                switch (t.getTipo()) {
                    case NUMERO:
                        valores.push(t);
                        accion = "Número: push " + t.getTexto();
                        break;

                    case VARIABLE:
                        Double valor = variables == null ? null : variables.get(t.getTexto());
                        if (valor == null) {
                            throw new ExpresionInvalidaException(
                                    "La variable '" + t.getTexto() + "' no tiene un valor asignado");
                        }
                        valores.push(Token.numero(valor));
                        accion = "Variable " + t.getTexto() + " = " + Token.formatearNumero(valor)
                                + ": push " + Token.formatearNumero(valor);
                        break;

                    case OPERADOR_UNARIO:
                        double unico = valores.pop().getValor();
                        double rUnario = t.getTexto().equals("-") ? -unico : unico;
                        valores.push(Token.numero(rUnario));
                        accion = "Signo " + t.getTexto() + ": pop " + Token.formatearNumero(unico)
                                + ", push " + Token.formatearNumero(rUnario);
                        break;

                    case OPERADOR:
                        double b = valores.pop().getValor();   // el segundo operando sale primero
                        double a = valores.pop().getValor();
                        double r = aplicar(t, a, b);
                        valores.push(Token.numero(r));
                        accion = "Operador " + t.getTexto() + ": pop " + Token.formatearNumero(b)
                                + " y " + Token.formatearNumero(a) + ", push " + Token.formatearNumero(r);
                        break;

                    default:
                        throw new ExpresionInvalidaException("Token no válido en notación postfija: " + t.getTexto());
                }
                registrar(filasEvaluacion, t.etiqueta(), accion, valores.toString());
            }
        } catch (PilaVaciaException e) {
            // Un operador no encontró sus operandos: la expresión postfija estaba incompleta.
            throw new ExpresionInvalidaException("Expresión postfija incompleta: faltan operandos para '"
                    + (actual == null ? "?" : actual.getTexto()) + "' (la pila estaba vacía)");
        }

        if (valores.size() != 1) {
            throw new ExpresionInvalidaException("Expresión postfija inválida: sobran operandos ("
                    + valores.size() + " valores en la pila al terminar)");
        }
        return valores.pop().getValor();
    }

    private double aplicar(Token operador, double a, double b) throws ExpresionInvalidaException {
        double resultado;
        switch (operador.getTexto()) {
            case "+": resultado = a + b; break;
            case "-": resultado = a - b; break;
            case "*": resultado = a * b; break;
            case "/":
                if (b == 0) {
                    throw new ExpresionInvalidaException("División entre cero: "
                            + Token.formatearNumero(a) + " / 0");
                }
                resultado = a / b;
                break;
            case "^":
                if (a == 0 && b < 0) {
                    throw new ExpresionInvalidaException("División entre cero: 0 elevado a un exponente negativo");
                }
                resultado = Math.pow(a, b);
                break;
            default:
                throw new ExpresionInvalidaException("Operador desconocido: " + operador.getTexto());
        }
        if (Double.isNaN(resultado)) {
            throw new ExpresionInvalidaException("El resultado no es un número real (p. ej. raíz de un negativo)");
        }
        if (Double.isInfinite(resultado)) {
            throw new ExpresionInvalidaException("El resultado excede el rango numérico representable");
        }
        return resultado;
    }

    // Utilidades


    /** Nombres distintos de variable que aparecen en la lista, en orden de aparición. */
    public static Set<String> variablesDe(List<Token> tokens) {
        Set<String> nombres = new LinkedHashSet<>();
        for (Token t : tokens) {
            if (t.getTipo() == TipoToken.VARIABLE) {
                nombres.add(t.getTexto());
            }
        }
        return nombres;
    }

    /** Texto de una lista de tokens separados por espacio, p. ej. "2 3 4 1 - * +". */
    public static String textoPostfija(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Token t : tokens) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(t.etiqueta());
        }
        return sb.toString();
    }

    private void registrar(List<String[]> filas, String... columnas) {
        filas.add(columnas);
    }

    public boolean hayTrazaValidacion() { return !filasValidacion.isEmpty(); }
    public boolean hayTrazaConversion() { return !filasConversion.isEmpty(); }
    public boolean hayTrazaEvaluacion() { return !filasEvaluacion.isEmpty(); }

    public String getTrazaValidacion() {
        return tabla(new String[] {"Token", "Acción", "Pila de paréntesis"}, filasValidacion);
    }

    public String getTrazaConversion() {
        return tabla(new String[] {"Token", "Acción", "Pila de operadores", "Salida"}, filasConversion);
    }

    public String getTrazaEvaluacion() {
        return tabla(new String[] {"Token", "Acción", "Pila de valores"}, filasEvaluacion);
    }

    /** Arma una tabla de texto con columnas alineadas. */
    private static String tabla(String[] encabezado, List<String[]> filas) {
        int[] ancho = new int[encabezado.length];
        for (int c = 0; c < encabezado.length; c++) {
            ancho[c] = encabezado[c].length();
            for (String[] fila : filas) {
                ancho[c] = Math.max(ancho[c], fila[c].length());
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(linea(encabezado, ancho)).append('\n');
        StringBuilder sep = new StringBuilder();
        for (int c = 0; c < ancho.length; c++) {
            sep.append(c == 0 ? "" : "-+-").append("-".repeat(ancho[c]));
        }
        sb.append(sep).append('\n');
        for (String[] fila : filas) {
            sb.append(linea(fila, ancho)).append('\n');
        }
        return sb.toString();
    }

    private static String linea(String[] celdas, int[] ancho) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < celdas.length; c++) {
            if (c > 0) {
                sb.append(" | ");
            }
            sb.append(celdas[c]);
            if (c < celdas.length - 1) {
                sb.append(" ".repeat(ancho[c] - celdas[c].length()));
            }
        }
        return sb.toString();
    }
}
