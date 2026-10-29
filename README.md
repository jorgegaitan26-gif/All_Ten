# All Ten

Juego de cálculo mental hecho en JavaFX para la asignatura **Práctica aplicada a Sistemas**
(Politécnico Grancolombiano).

El jugador recibe 4 números del 1 al 9 y debe formar, usando cada número una sola vez,
expresiones cuyo resultado sea cada uno de los objetivos del **1 al 10**. Gana cuando
completa los diez; el cronómetro mide cuánto tardó.

## Modos de juego

- **Reto diario:** todos los jugadores reciben los mismos 4 números ese día (la fecha de
  Bogotá es la semilla). Se puede repetir para mejorar el tiempo y entra al ranking.
- **Partida libre:** cada partida tiene números aleatorios nuevos. No entra al ranking.

Durante la partida hay un **panel de ayuda** con una solución para cada objetivo y se
muestra el **resultado parcial** de la expresión que se va armando. Al ganar, la partida
se guarda en MySQL y se abre una **tarjeta de resultados** (tiempo, categoría de velocidad y
operaciones) que se puede guardar como PNG o copiar para compartirla. El botón **Ranking** muestra los 10 mejores tiempos del reto de hoy
(una entrada por jugador, con su mejor tiempo).

## Requisitos

- JDK 26
- Apache Maven 3.9 o superior (descarga JavaFX, el conector de MySQL y JUnit)
- MySQL 8

## Configuración de la base de datos

1. Ejecuta [`database/create_tables.sql`](database/create_tables.sql) en MySQL Workbench o
   con el cliente de MySQL. Crea la base `all_ten_db` con las tablas `Jugador` y `Partida`.
   - Si ya tenías la tabla `Partida` de antes del reto diario, ejecuta también
     [`migrate_daily_challenge.sql`](database/migrate_daily_challenge.sql) y luego
     [`migrate_free_play.sql`](database/migrate_free_play.sql).
   - Si aparece `Data too long for column 'operacionesRealizadas'`, ejecuta
     [`fix_operaciones_partida.sql`](database/fix_operaciones_partida.sql).
2. Copia [`.env.example`](.env.example) como `.env` en la raíz del proyecto y pon tus datos:

   ```
   DB_HOST=localhost
   DB_PORT=3306
   DB_NAME=all_ten_db
   DB_USER=root
   DB_PASSWORD=tu_contrasena
   ```

   Si tu base tiene otro nombre, cámbialo en `DB_NAME`. El archivo `.env` no se sube al
   repositorio.

## Ejecutar

Desde la raíz del proyecto:

```bash
mvn clean javafx:run
```

Para ver solo la tarjeta de resultados con datos de ejemplo (sin jugar ni usar la base de
datos):

```bash
mvn javafx:run -Djavafx.mainClass=co.edu.poli.Allten.App.DemoResultado
```

## Pruebas

Las pruebas JUnit de los modelos están en `test/`:

```bash
mvn test
```

## Estructura

```
src/co/edu/poli/Allten/
├── App/           Punto de entrada (App.java)
├── Controlador/   Controladores de cada pantalla (menú, juego, ranking, resultado, instrucciones)
├── DAO/           Acceso a MySQL: CRUD, ConexionBD, DAOJugador, DAOPartida, EntradaRanking
└── Modelo/        Jugador, Partida, Expresion, Cronometro
src/vista/         Pantallas FXML, estilos CSS e imágenes
test/              Pruebas JUnit de los modelos
database/          Script de creación de tablas y migraciones
docs/              Diagrama de clases, modelo relacional y diagramas de flujo
```

El diagrama de clases actualizado está en [`docs/diagrama_clases.mmd`](docs/diagrama_clases.mmd)
(GitHub lo muestra como imagen).

## Equipo

- Jorge Gaitan
- Angie Tobacia
- Julian Larrarte
