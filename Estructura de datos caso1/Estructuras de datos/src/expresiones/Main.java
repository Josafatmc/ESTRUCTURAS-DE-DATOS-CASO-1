package expresiones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Programa de consola: análisis de expresiones aritméticas con una pila enlazada.
 * Contiene la rutina main() y el menú interactivo.
 */
public class Main {

    /** Con -Dpila.eco=true se repite en pantalla cada línea leída (útil al redirigir la entrada). */
    private static final boolean MODO_ECO = Boolean.getBoolean("pila.eco");

    private final Scanner entrada = new Scanner(System.in, "UTF-8");
    private final Analizador analizador = new Analizador();
    private final Map<String, Double> variables = new HashMap<>();
    private final Pila pilaManual = new Pila(10);   // pila de práctica (opción 7)

    private String expresion = null;       // expresión ingresada por el usuario
    private List<Token> postfija = null;   // última conversión exitosa

    /** Punto de entrada del programa. */
    public static void main(String[] args) {
        new Main().menu();
    }


    /** Muestra el menú en bucle hasta que el usuario elige salir (o se cierra la entrada). */
    public void menu() {
        System.out.println("==============================================================");
        System.out.println("  ANÁLISIS DE EXPRESIONES ARITMÉTICAS CON PILAS  (SOFT-10)");
        System.out.println("  Operadores: + - * / ^  Paréntesis: ( )  Variables: x, y1");
        System.out.println("==============================================================");

        boolean salir = false;
        while (!salir) {
            mostrarOpciones();
            String respuesta = leer("Elija una opción: ");
            if (respuesta == null) {   // fin de la entrada (Ctrl+D / archivo)
                break;
            }
            System.out.println();
            switch (respuesta.trim()) {
                case "1": opcionIngresar();   break;
                case "2": opcionValidar();    break;
                case "3": opcionPostfija();   break;
                case "4": opcionEvaluar();    break;
                case "5": opcionPasos();      break;
                case "6": opcionInfoPila();   break;
                case "7": opcionPracticar();  break;
                case "0": salir = true;       break;
                default:
                    System.out.println("Opción no válida. Escriba un número del 0 al 7.");
            }
        }
        System.out.println("\nPrograma finalizado. ¡Hasta pronto!");
    }

    private void mostrarOpciones() {
        System.out.println();
        System.out.println("-------------------------- MENÚ --------------------------");
        System.out.println("  Expresión actual: " + (expresion == null ? "(ninguna)" : expresion));
        System.out.println("  1. Ingresar una expresión aritmética");
        System.out.println("  2. Validar la expresión");
        System.out.println("  3. Convertir a notación postfija");
        System.out.println("  4. Evaluar la expresión");
        System.out.println("  5. Mostrar los pasos del procesamiento");
        System.out.println("  6. Ver las operaciones de la pila (demostración)");
        System.out.println("  7. Practicar con la pila (push / pop / peek)");
        System.out.println("  0. Salir");
        System.out.println("----------------------------------------------------------");
    }

    // OPCIONES

    /** Opción 1: guarda la expresión y la valida de inmediato. */
    private void opcionIngresar() {
        String texto = leer("Escriba la expresión (ej. 2 + 3 * (4 - 1)): ");
        if (texto == null) {
            return;
        }
        expresion = texto;
        postfija = null;
        System.out.println("Expresión guardada: " + expresion);
        validarYMostrar();
    }

    /** Opción 2: valida la expresión actual. */
    private void opcionValidar() {
        if (!hayExpresion()) {
            return;
        }
        validarYMostrar();
    }

    /** Opción 3: convierte a postfija. */
    private void opcionPostfija() {
        if (!hayExpresion() || !convertir()) {
            return;
        }
        System.out.println("Expresión infija : " + expresion.trim());
        System.out.println("Notación postfija: " + Analizador.textoPostfija(postfija));
        System.out.println("(Use la opción 5 para ver cómo cambió la pila en cada paso.)");
    }

    /** Opción 4: pide valores de variables y evalúa. */
    private void opcionEvaluar() {
        if (!hayExpresion() || !convertir()) {
            return;
        }
        for (String nombre : Analizador.variablesDe(postfija)) {
            if (!pedirValor(nombre)) {
                System.out.println("Evaluación cancelada.");
                return;
            }
        }
        try {
            double resultado = analizador.evaluar(postfija, variables);
            System.out.println("Expresión : " + expresion.trim());
            System.out.println("Postfija  : " + Analizador.textoPostfija(postfija));
            System.out.println("Resultado : " + Token.formatearNumero(resultado));
        } catch (ExpresionInvalidaException e) {
            System.out.println("Error al evaluar: " + e.getMessage());
        }
    }

    /** Opción 5: muestra las trazas de la última validación, conversión y evaluación. */
    private void opcionPasos() {
        if (!hayExpresion()) {
            return;
        }
        if (!analizador.hayTrazaConversion()) {
            // Se genera la traza para que el usuario no tenga que ejecutar antes la opción 3.
            if (!convertir()) {
                return;
            }
        }
        System.out.println("PASO 1 - Balance de paréntesis (pila de paréntesis)");
        System.out.println(analizador.hayTrazaValidacion()
                ? analizador.getTrazaValidacion() : "(La expresión no contiene paréntesis.)\n");
        System.out.println("PASO 2 - Conversión infija -> postfija (pila de operadores)");
        System.out.println(analizador.getTrazaConversion());
        if (analizador.hayTrazaEvaluacion()) {
            System.out.println("PASO 3 - Evaluación de la postfija (pila de valores)");
            System.out.println(analizador.getTrazaEvaluacion());
        } else {
            System.out.println("(La evaluación aún no se ha ejecutado: use la opción 4 para ver el PASO 3.)");
        }
        System.out.println("Convención de las tablas: la pila se lista de la base a la cima (la cima es el último).");
    }

    /** Opción 6: demostración automática de push, peek, pop e isEmpty. */
    private void opcionInfoPila() {
        System.out.println("OPERACIONES DE LA PILA (todas O(1))");
        System.out.println("  push(t)  : inserta el token t en la cima");
        System.out.println("  pop()    : retira y devuelve el token de la cima");
        System.out.println("  peek()   : devuelve la cima sin retirarla");
        System.out.println("  isEmpty(): indica si la pila está vacía");
        System.out.println();

        Pila demo = new Pila();
        Token[] tokens = {Token.parentesisIzq(1), Token.operador("+", 2), Token.operador("*", 3)};
        System.out.println("Demostración con una pila nueva:");
        System.out.println("  isEmpty() -> " + demo.isEmpty());
        for (Token t : tokens) {
            demo.push(t);
            System.out.println("\n  push(" + t + ")  -> " + demo);
            System.out.println(demo.mostrarVertical());
        }
        System.out.println("\n  peek() -> " + demo.peek() + "   (la pila no cambia: " + demo + ")");
        while (!demo.isEmpty()) {
            Token t = demo.pop();
            System.out.println("  pop()  -> " + t + "   pila: " + demo);
        }
        System.out.println("  isEmpty() -> " + demo.isEmpty());
        try {
            demo.pop();
        } catch (PilaVaciaException e) {
            System.out.println("  pop() con la pila vacía -> " + e.getMessage());
        }
    }

    /** Opción 7: submenú para manipular manualmente una pila de capacidad 10. */
    private void opcionPracticar() {
        System.out.println("PRÁCTICA CON LA PILA (capacidad máxima: " + pilaManual.getCapacidadMaxima() + ")");
        boolean volver = false;
        while (!volver) {
            System.out.println();
            System.out.println("  a) push   b) pop   c) peek   d) isEmpty / tamaño   e) vaciar   f) mostrar   v) volver");
            String r = leer("  Acción: ");
            if (r == null) {
                return;
            }
            switch (r.trim().toLowerCase()) {
                case "a": practicarPush();  break;
                case "b": practicarPop();   break;
                case "c": practicarPeek();  break;
                case "d":
                    System.out.println("  isEmpty() -> " + pilaManual.isEmpty()
                            + "   size() -> " + pilaManual.size());
                    break;
                case "e":
                    pilaManual.vaciar();
                    System.out.println("  La pila fue vaciada.");
                    break;
                case "f":
                    System.out.println("  " + pilaManual);
                    System.out.println(pilaManual.mostrarVertical());
                    break;
                case "v": volver = true; break;
                default:
                    System.out.println("  Acción no válida.");
            }
        }
    }

    private void practicarPush() {
        String texto = leer("  Token a apilar (número, variable, operador o paréntesis): ");
        if (texto == null) {
            return;
        }
        try {
            List<Token> lista = analizador.tokenizar(texto);
            if (lista.size() != 1) {
                System.out.println("  Escriba exactamente un token.");
                return;
            }
            pilaManual.push(lista.get(0));
            System.out.println("  push(" + lista.get(0) + ") -> " + pilaManual);
        } catch (ExpresionInvalidaException e) {
            System.out.println("  Token no válido: " + e.getMessage());
        } catch (PilaLlenaException e) {
            System.out.println("  " + e.getMessage());
        }
    }

    private void practicarPop() {
        try {
            Token t = pilaManual.pop();
            System.out.println("  pop() -> " + t + "   pila: " + pilaManual);
        } catch (PilaVaciaException e) {
            System.out.println("  " + e.getMessage());
        }
    }

    private void practicarPeek() {
        try {
            System.out.println("  peek() -> " + pilaManual.peek() + "   pila: " + pilaManual);
        } catch (PilaVaciaException e) {
            System.out.println("  " + e.getMessage());
        }
    }

    private boolean hayExpresion() {
        if (expresion == null) {
            System.out.println("Primero ingrese una expresión con la opción 1.");
            return false;
        }
        return true;
    }

    /** Tokeniza y valida; muestra el resultado y, si es correcta, la lista de tokens. */
    private void validarYMostrar() {
        try {
            List<Token> tokens = analizador.tokenizar(expresion);
            analizador.validar(tokens);
            System.out.println("La expresión es VÁLIDA.");
            System.out.println("Tokens (" + tokens.size() + "): " + descripcion(tokens));
            if (!Analizador.variablesDe(tokens).isEmpty()) {
                System.out.println("Variables detectadas: " + Analizador.variablesDe(tokens)
                        + " (se pedirán sus valores al evaluar)");
            }
        } catch (ExpresionInvalidaException e) {
            System.out.println("La expresión NO es válida: " + e.getMessage());
            senalar(e.getPosicion());
        }
    }

    /** Tokeniza, valida y convierte a postfija. Devuelve false si hubo error (ya informado). */
    private boolean convertir() {
        try {
            postfija = analizador.aPostfija(analizador.tokenizar(expresion));
            return true;
        } catch (ExpresionInvalidaException e) {
            postfija = null;
            System.out.println("No se puede continuar. La expresión no es válida: " + e.getMessage());
            senalar(e.getPosicion());
            return false;
        }
    }

    /** Dibuja un acento circunflejo bajo el carácter erróneo. */
    private void senalar(int posicion) {
        if (posicion > 0 && expresion != null && posicion <= expresion.length()) {
            System.out.println("  " + expresion);
            System.out.println("  " + " ".repeat(posicion - 1) + "^");
        }
    }

    /** Pide el valor de una variable; admite Enter para conservar el valor anterior. */
    private boolean pedirValor(String nombre) {
        for (int intento = 1; intento <= 3; intento++) {
            Double anterior = variables.get(nombre);
            String sugerencia = anterior == null ? "" : " [Enter = " + Token.formatearNumero(anterior) + "]";
            String texto = leer("Valor de " + nombre + sugerencia + ": ");
            if (texto == null) {
                return false;
            }
            texto = texto.trim();
            if (texto.isEmpty() && anterior != null) {
                return true;
            }
            if (texto.matches("[+-]?[0-9]+(\\.[0-9]+)?") && texto.length() <= 30) {
                variables.put(nombre, Double.parseDouble(texto));
                return true;
            }
            System.out.println("  Valor no válido. Use un número como 4, -2 o 3.5 (intento " + intento + " de 3).");
        }
        return false;
    }

    private String descripcion(List<Token> tokens) {
        List<String> partes = new ArrayList<>();
        for (Token t : tokens) {
            String clase;
            switch (t.getTipo()) {
                case NUMERO:          clase = "num";  break;
                case VARIABLE:        clase = "var";  break;
                case OPERADOR:        clase = "op";   break;
                case OPERADOR_UNARIO: clase = "unario"; break;
                default:              clase = "par";  break;
            }
            partes.add(t.getTexto() + ":" + clase);
        }
        return String.join("  ", partes);
    }

    /** Lee una línea mostrando un mensaje; devuelve null si ya no hay más entrada. */
    private String leer(String mensaje) {
        System.out.print(mensaje);
        System.out.flush();
        if (!entrada.hasNextLine()) {
            System.out.println();
            return null;
        }
        String linea = entrada.nextLine();
        if (MODO_ECO) {
            System.out.println(linea);   // permite documentar sesiones con entrada redirigida
        }
        return linea;
    }
}
