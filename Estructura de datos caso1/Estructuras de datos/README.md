# Análisis de expresiones aritméticas con pilas (Java)

Estudio de caso 1 — **Estructuras de Datos (SOFT-10)**, Universidad CENFOTEC, sección SCV5, periodo C3-2026.
Docente facilitador: Romario Salas Cerdas.

Aplicación de consola que analiza expresiones aritméticas usando una **pila implementada con nodos enlazados**
(sin usar `java.util.Stack` ni otras colecciones para la pila).

## Funcionalidades (todas implementadas)

| Función | Descripción |
|---|---|
| Tokenización | Reconoce números (`3`, `4.25`), variables (`x`, `precio_1`), operadores `+ - * / ^`, signo unario (`-x`) y paréntesis. Rechaza cualquier otro carácter. |
| Validación | Detecta paréntesis desbalanceados (con una pila), operadores consecutivos o incompletos, operandos sin operador, entrada vacía, números mal formados y expresiones de más de 200 caracteres. Indica la posición del error. |
| Conversión a postfija | Algoritmo Shunting-Yard (Dijkstra) con pila de operadores; respeta precedencia y asociatividad. |
| Evaluación | Evalúa la postfija con una pila de valores; pide el valor de las variables y detecta división entre cero, desbordamiento y resultados no reales. |
| Pasos | Muestra tablas paso a paso con el estado de la pila (balance de paréntesis, conversión y evaluación). |
| Demostración y práctica | Opción 6: demostración automática de `push`, `peek`, `pop`, `isEmpty`. Opción 7: pila manual interactiva (capacidad 10). |

**Precedencia (mayor se aplica primero):** `^` (4, asociativo por la derecha) > signo unario (3) > `*` `/` (2) > `+` `-` (1).
**No incluido:** multiplicación implícita (`2(3+4)`), funciones (`sqrt`), notación científica (`1e5`) y asignación de variables en la expresión.

## Requisitos

* JDK 11 o superior (probado con OpenJDK 21).
* No requiere bibliotecas externas.

## Estructura

```
src/expresiones/
  Main.java                      main() y menú interactivo
  Nodo.java                      nodo enlazado (Token + referencia al siguiente)
  Pila.java                      pila con nodos enlazados (push, pop, peek, isEmpty, size, vaciar)
  Token.java / TipoToken.java    unidad léxica y sus categorías
  Analizador.java                tokenizar, validar, aPostfija, evaluar
  PilaVaciaException.java        pop/peek con la pila vacía (subdesbordamiento)
  PilaLlenaException.java        push con la pila llena (desbordamiento)
  ExpresionInvalidaException.java  errores de sintaxis o evaluación
  PruebasAutomaticas.java        65 casos de prueba sin JUnit
ejemplos/EjemploJVM.java         ejemplo del informe (bytecode de la JVM, se inspecciona con javap)
pruebas/                         resultados reales de las pruebas y sesiones de consola
```

## Compilar y ejecutar

Desde la carpeta raíz del proyecto:

```bash
# Compilar (genera la carpeta out/)
javac -encoding UTF-8 -d out src/expresiones/*.java

# Ejecutar el programa
java -cp out expresiones.Main

# Ejecutar las pruebas automáticas
java -cp out expresiones.PruebasAutomaticas
```

En Windows (PowerShell o CMD) los comandos son los mismos. Si las tildes se ven mal, ejecute antes
`chcp 65001` o agregue `-Dstdout.encoding=UTF-8` al comando `java`.

## Ejemplo de uso

```
Elija una opción: 1
Escriba la expresión (ej. 2 + 3 * (4 - 1)): 2 + 3 * (4 - 1)
La expresión es VÁLIDA.

Elija una opción: 4
Expresión : 2 + 3 * (4 - 1)
Postfija  : 2 3 4 1 - * +
Resultado : 11
```

Con variables, el programa solicita cada valor (Enter conserva el anterior):

```
Expresión: (a + b) * c - d / 2      con a=2, b=3, c=4, d=10   ->   Postfija: a b + c * d 2 / -   Resultado: 15
```

## Pruebas realizadas

`PruebasAutomaticas` ejecuta 65 casos: evaluación correcta (24), expresiones inválidas (21), errores de
evaluación (6) y comportamiento de la pila (14). Resultado de la última ejecución: **65 de 65 aprobadas**
(ver `pruebas/resultados_pruebas_automaticas.txt`). En `pruebas/sesiones/` hay transcripciones reales del
menú ejecutadas con entrada redirigida y `-Dpila.eco=true`, que repite en pantalla las líneas leídas:

```bash
printf '1\n2 + 3 * (4 - 1)\n4\n5\n0\n' | java -Dpila.eco=true -cp out expresiones.Main
```

Durante las pruebas se detectó un caso (`)3 + 4(`) en el que el mensaje no señalaba la causa más informativa
(cierre sin apertura); se corrigió el orden de las comprobaciones en `Analizador.validar` y se repitió la suite.

## Seguridad

El texto ingresado nunca se ejecuta como código: solo se aceptan los caracteres de una lista blanca, la longitud
está limitada (200 caracteres), la pila tiene capacidad máxima (1000) y todos los errores se informan sin cerrar el programa.
