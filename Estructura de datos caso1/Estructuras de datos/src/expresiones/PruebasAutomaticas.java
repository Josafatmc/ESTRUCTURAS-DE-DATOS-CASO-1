package expresiones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PruebasAutomaticas {

    private static final Analizador ANALIZADOR = new Analizador();
    private static int total = 0;
    private static int aprobadas = 0;
    private static final List<String> fallidas = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("PRUEBAS AUTOMÁTICAS - Análisis de expresiones aritméticas con pila");
        System.out.println("===================================================================");

        System.out.println("\n--- A. Evaluación correcta (postfija y resultado) ---");
        evaluacion("A01", "Suma y resta simples", "3 + 4", null, "3 4 +", "7");
        evaluacion("A02", "Resta asociativa por la izquierda", "10 - 4 - 3", null, "10 4 - 3 -", "3");
        evaluacion("A03", "Multiplicación y división", "20 / 4 * 3", null, "20 4 / 3 *", "15");
        evaluacion("A04", "División asociativa por la izquierda", "20 / 4 / 5", null, "20 4 / 5 /", "1");
        evaluacion("A05", "Precedencia: * antes que +", "2 + 3 * 4", null, "2 3 4 * +", "14");
        evaluacion("A06", "Precedencia: * antes que + (orden inverso)", "2 * 3 + 4", null, "2 3 * 4 +", "10");
        evaluacion("A07", "Paréntesis cambian la precedencia", "(2 + 3) * 4", null, "2 3 + 4 *", "20");
        evaluacion("A08", "Paréntesis dentro de la expresión", "2 + 3 * (4 - 1)", null, "2 3 4 1 - * +", "11");
        evaluacion("A09", "Paréntesis anidados", "((2 + 3) * (4 - 1)) / 5", null, "2 3 + 4 1 - * 5 /", "3");
        evaluacion("A10", "Paréntesis muy anidados", "(((1 + 2) * 3) - 4) ^ 2", null, "1 2 + 3 * 4 - 2 ^", "25");
        evaluacion("A11", "Potencia asociativa por la derecha", "2 ^ 3 ^ 2", null, "2 3 2 ^ ^", "512");
        evaluacion("A12", "Potencia con mayor precedencia que *", "3 * 2 ^ 3", null, "3 2 3 ^ *", "24");
        evaluacion("A13", "Espacios irregulares", "  7+   8 *2  ", null, "7 8 2 * +", "23");
        evaluacion("A14", "Sin espacios", "7+8*2-(1+1)", null, "7 8 2 * + 1 1 + -", "21");
        evaluacion("A15", "Números decimales", "3.5 + 2.25 * 2", null, "3.5 2.25 2 * +", "8");
        evaluacion("A16", "Redondeo de decimales", "0.1 + 0.2", null, "0.1 0.2 +", "0.3");
        evaluacion("A17", "Signo unario al inicio", "-5 + 2", null, "5 u- 2 +", "-3");
        evaluacion("A18", "Signo unario tras operador", "10 - -3", null, "10 3 u- -", "13");
        evaluacion("A19", "Unario menor que potencia: -2^2", "-2 ^ 2", null, "2 2 ^ u-", "-4");
        evaluacion("A20", "Unario aplicado a paréntesis", "-(3 + 4)", null, "3 4 + u-", "-7");
        evaluacion("A21", "Unario después de * ", "2 * -3", null, "2 3 u- *", "-6");
        evaluacion("A22", "Variables numéricas", "(a + b) * c - d / 2", vars("a", 2, "b", 3, "c", 4, "d", 10),
                "a b + c * d 2 / -", "15");
        evaluacion("A23", "Variable con guion bajo y potencia", "precio_1 ^ 2 + y", vars("precio_1", 3, "y", 1),
                "precio_1 2 ^ y +", "10");
        evaluacion("A24", "Variable con valor decimal", "x * 2", vars("x", 1.25), "x 2 *", "2.5");

        System.out.println("\n--- B. Expresiones inválidas (deben rechazarse con un mensaje claro) ---");
        error("B01", "Entrada vacía", "", null, "vacía");
        error("B02", "Solo espacios", "     ", null, "vacía");
        error("B03", "Paréntesis sin cerrar", "(3 + 4", null, "sin cerrar");
        error("B04", "Paréntesis de cierre sobrante", "3 + 4)", null, "sin apertura");
        error("B05", "Paréntesis desbalanceados anidados", "((3 + 4) * 2", null, "sin cerrar");
        error("B06", "Cierre antes de apertura", ")3 + 4(", null, "sin apertura");
        error("B07", "Paréntesis vacíos", "3 + ()", null, "vacíos");
        error("B08", "Operadores consecutivos", "3 + * 4", null, "consecutivos");
        error("B09", "Operador al final", "3 +", null, "incompleta");
        error("B10", "Operador al inicio", "* 3", null, "izquierda");
        error("B11", "Dos operandos sin operador", "3 4", null, "Falta un operador");
        error("B12", "Multiplicación implícita no admitida", "2(3 + 4)", null, "Falta un operador");
        error("B13", "Paréntesis seguidos sin operador", "(1 + 2)(3 + 4)", null, "Falta un operador");
        error("B14", "Carácter no permitido", "2 $ 3", null, "no permitido");
        error("B15", "Número con dos puntos", "1.2.3", null, "más de un punto");
        error("B16", "Punto sin decimales", "3. + 1", null, "faltan dígitos");
        error("B17", "Punto sin parte entera", ".5 + 1", null, "después de un dígito");
        error("B18", "Intento de inyección de código", "Math.exit(0)", null, "después de un dígito");
        error("B19", "Expresión demasiado larga", "1+".repeat(150) + "1", null, "máximo");
        error("B20", "Operador antes de cierre", "(3 + )", null, "Falta un operando");
        error("B21", "Operador unario sin operando", "5 + -", null, "incompleta");

        System.out.println("\n--- C. Errores durante la evaluación ---");
        error("C01", "División entre cero", "3 / 0", null, "División entre cero");
        error("C02", "División entre cero calculada", "5 / (2 - 2)", null, "División entre cero");
        error("C03", "Cero elevado a exponente negativo", "0 ^ -1", null, "División entre cero");
        error("C04", "Variable sin valor", "a + 1", null, "no tiene un valor");
        error("C05", "Desbordamiento numérico", "10 ^ 200 * 10 ^ 200", null, "rango");
        error("C06", "Resultado no real", "(-8) ^ 0.5", null, "no es un número real");

        System.out.println("\n--- D. Pila: operaciones, LIFO y casos límite ---");
        pila("D01", "Pila nueva está vacía", () -> {
            Pila p = new Pila();
            return p.isEmpty() && p.size() == 0 ? "vacía,size=0" : "ERROR";
        }, "vacía,size=0");
        pila("D02", "push y peek no retiran", () -> {
            Pila p = new Pila();
            p.push(Token.numero("5", 1));
            return p.peek().getTexto() + ",size=" + p.size();
        }, "5,size=1");
        pila("D03", "Orden LIFO (push 1,2,3 -> pop 3,2,1)", () -> {
            Pila p = new Pila();
            for (String s : new String[] {"1", "2", "3"}) {
                p.push(Token.numero(s, 0));
            }
            return p.pop().getTexto() + p.pop().getTexto() + p.pop().getTexto();
        }, "321");
        pila("D04", "pop en pila vacía lanza PilaVaciaException", () -> {
            try {
                new Pila().pop();
                return "no lanzó excepción";
            } catch (PilaVaciaException e) {
                return "PilaVaciaException";
            }
        }, "PilaVaciaException");
        pila("D05", "peek en pila vacía lanza PilaVaciaException", () -> {
            try {
                new Pila().peek();
                return "no lanzó excepción";
            } catch (PilaVaciaException e) {
                return "PilaVaciaException";
            }
        }, "PilaVaciaException");
        pila("D06", "push sobre pila llena lanza PilaLlenaException", () -> {
            Pila p = new Pila(2);
            p.push(Token.numero("1", 0));
            p.push(Token.numero("2", 0));
            try {
                p.push(Token.numero("3", 0));
                return "no lanzó excepción";
            } catch (PilaLlenaException e) {
                return "PilaLlenaException,size=" + p.size();
            }
        }, "PilaLlenaException,size=2");
        pila("D07", "vaciar deja la pila vacía", () -> {
            Pila p = new Pila();
            p.push(Token.parentesisIzq(1));
            p.push(Token.operador("+", 2));
            p.vaciar();
            return p.isEmpty() + ",size=" + p.size();
        }, "true,size=0");
        pila("D08", "pop tras vaciar por completo lanza excepción", () -> {
            Pila p = new Pila();
            p.push(Token.operador("*", 1));
            p.pop();
            try {
                p.pop();
                return "no lanzó excepción";
            } catch (PilaVaciaException e) {
                return "PilaVaciaException";
            }
        }, "PilaVaciaException");
        pila("D09", "push de null es rechazado", () -> {
            try {
                new Pila().push(null);
                return "no lanzó excepción";
            } catch (IllegalArgumentException e) {
                return "IllegalArgumentException";
            }
        }, "IllegalArgumentException");
        pila("D10", "Evaluar postfija incompleta ('+' sola): operar con pila vacía", () -> {
            List<Token> mala = new ArrayList<>();
            mala.add(Token.operador("+", 1));
            try {
                ANALIZADOR.evaluar(mala, new HashMap<String, Double>());
                return "no lanzó excepción";
            } catch (ExpresionInvalidaException e) {
                return e.getMessage().contains("pila estaba vacía") ? "rechazada (pila vacía)" : e.getMessage();
            }
        }, "rechazada (pila vacía)");
        pila("D11", "Evaluar postfija con operandos sobrantes ('1 2')", () -> {
            List<Token> mala = new ArrayList<>();
            mala.add(Token.numero("1", 1));
            mala.add(Token.numero("2", 3));
            try {
                ANALIZADOR.evaluar(mala, new HashMap<String, Double>());
                return "no lanzó excepción";
            } catch (ExpresionInvalidaException e) {
                return e.getMessage().contains("sobran operandos") ? "rechazada (sobran operandos)" : e.getMessage();
            }
        }, "rechazada (sobran operandos)");
        pila("D12", "Un solo operando binario falta ('3 +')", () -> {
            List<Token> mala = new ArrayList<>();
            mala.add(Token.numero("3", 1));
            mala.add(Token.operador("+", 3));
            try {
                ANALIZADOR.evaluar(mala, new HashMap<String, Double>());
                return "no lanzó excepción";
            } catch (ExpresionInvalidaException e) {
                return e.getMessage().contains("pila estaba vacía") ? "rechazada (pila vacía)" : e.getMessage();
            }
        }, "rechazada (pila vacía)");
        pila("D13", "toString muestra base -> cima", () -> {
            Pila p = new Pila();
            p.push(Token.parentesisIzq(1));
            p.push(Token.operador("+", 2));
            return p.toString();
        }, "[ ( , + ]");
        pila("D14", "Pila grande (1000 elementos) y vaciado completo", () -> {
            Pila p = new Pila();
            for (int i = 0; i < Pila.CAPACIDAD_POR_DEFECTO; i++) {
                p.push(Token.numero(i));
            }
            int cont = 0;
            while (!p.isEmpty()) {
                p.pop();
                cont++;
            }
            return "retirados=" + cont;
        }, "retirados=1000");

        System.out.println("\n===================================================================");
        System.out.println("RESUMEN: " + aprobadas + " de " + total + " pruebas aprobadas.");
        if (!fallidas.isEmpty()) {
            System.out.println("FALLARON: " + fallidas);
            System.exit(1);
        }
    }

    // Auxiliares
    

    /** Prueba que una expresión válida produzca la postfija y el resultado esperados. */
    private static void evaluacion(String id, String descripcion, String entrada, Map<String, Double> vars,
            String postfijaEsperada, String resultadoEsperado) {
        String esperado = postfijaEsperada + "  =>  " + resultadoEsperado;
        String obtenido;
        try {
            List<Token> postfija = ANALIZADOR.aPostfija(ANALIZADOR.tokenizar(entrada));
            double r = ANALIZADOR.evaluar(postfija, vars == null ? new HashMap<String, Double>() : vars);
            obtenido = Analizador.textoPostfija(postfija) + "  =>  " + Token.formatearNumero(r);
        } catch (ExpresionInvalidaException e) {
            obtenido = "ERROR: " + e.getMessage();
        }
        registrar(id, descripcion, "\"" + entrada + "\"" + (vars == null ? "" : " " + vars), esperado, obtenido,
                esperado.equals(obtenido));
    }

    /** Prueba que una entrada inválida sea rechazada con un mensaje que contenga cierto fragmento. */
    private static void error(String id, String descripcion, String entrada, Map<String, Double> vars,
            String fragmento) {
        String esperado = "Error que contiene \"" + fragmento + "\"";
        String obtenido;
        boolean ok = false;
        try {
            List<Token> postfija = ANALIZADOR.aPostfija(ANALIZADOR.tokenizar(entrada));
            double r = ANALIZADOR.evaluar(postfija, vars == null ? new HashMap<String, Double>() : vars);
            obtenido = "Sin error (resultado " + Token.formatearNumero(r) + ")";
        } catch (ExpresionInvalidaException e) {
            obtenido = "Error: " + e.getMessage();
            ok = e.getMessage().contains(fragmento);
        }
        String mostrada = entrada.length() > 30 ? "\"" + entrada.substring(0, 12) + "...\" (" + entrada.length()
                + " caracteres)" : "\"" + entrada + "\"";
        registrar(id, descripcion, mostrada, esperado, obtenido, ok);
    }

    private static void pila(String id, String descripcion, java.util.function.Supplier<String> accion,
            String esperado) {
        String obtenido = accion.get();
        registrar(id, descripcion, "(operación sobre Pila)", esperado, obtenido, esperado.equals(obtenido));
    }

    private static void registrar(String id, String descripcion, String entrada, String esperado,
            String obtenido, boolean ok) {
        total++;
        if (ok) {
            aprobadas++;
        } else {
            fallidas.add(id);
        }
        System.out.println("[" + id + "] " + descripcion);
        System.out.println("      Entrada  : " + entrada);
        System.out.println("      Esperado : " + esperado);
        System.out.println("      Obtenido : " + obtenido);
        System.out.println("      Estado   : " + (ok ? "APROBADA" : "FALLÓ"));
    }

    private static Map<String, Double> vars(Object... pares) {
        Map<String, Double> mapa = new HashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            mapa.put((String) pares[i], ((Number) pares[i + 1]).doubleValue());
        }
        return mapa;
    }
}
