# All Ten

Aplicación JavaFX del juego All Ten. El juego utiliza los modelos `Jugador`,
`Partida`, `Expresion` y `Cronometro` para mantener el estado de cada partida.

## Maven

El proyecto usa Apache Maven para descargar y administrar JavaFX y MySQL
Connector/J. Se requiere JDK 26 y Maven 3.9 o superior.

Desde la raíz del proyecto, ejecuta:

```bash
mvn clean package
mvn javafx:run
```

Maven conserva las fuentes y recursos en `src`, sin necesidad de agregar los
JAR manualmente al classpath. Las dependencias se definen en `pom.xml`.

## Persistencia

La capa DAO (`DAOJugador` y `DAOPartida`) conecta el juego con MySQL y la
base de datos `all_ten_db`. Ambas implementan el contrato genérico `CRUD` para
crear, consultar, actualizar y eliminar registros. `DAOJugador` también busca
por nickname; `DAOPartida` permite consultar partidas por jugador y obtener un
ranking diario:

1. Copia `.env.example` a `.env` en la raíz del proyecto y configura
   `ALL_TEN_DB_USER` y `ALL_TEN_DB_PASSWORD`. No agregues `.env` al repositorio;
   comparte solo el archivo de ejemplo.
2. Inicia MySQL y ejecuta el script
   [`database/create_tables.sql`](database/create_tables.sql) en MySQL Workbench
   o con el cliente de MySQL. El script crea la base `all_ten_db` y las tablas
   `Jugador` y `Partida` que utiliza el DAO.
   Si la tabla `Partida` ya existía antes del reto diario, ejecuta también
   [`database/migrate_daily_challenge.sql`](database/migrate_daily_challenge.sql).
   Si MySQL muestra `Data too long for column 'operacionesRealizadas'`, ejecuta
   [`database/fix_operaciones_partida.sql`](database/fix_operaciones_partida.sql)
   para ampliar la columna de operaciones sin perder su contenido.
3. Ejecuta la aplicación con `mvn javafx:run` para que Maven incluya las
   dependencias necesarias.

Al iniciar una partida, se busca el nickname y se registra el jugador si aún no
existe. El reto se genera de forma determinista a partir de la fecha en la zona
horaria `America/Bogota`: todos los jugadores reciben los mismos cuatro números
ese día, y la combinación conserva la posibilidad de completar los diez
objetivos. Al completar el reto, se guardan la duración, la fecha de
finalización, la fecha del reto y las operaciones. El botón **Ranking** muestra
los diez mejores tiempos del reto actual, con una sola entrada por jugador y su
mejor partida de ese día. Las partidas anteriores a la migración no aparecen en
el ranking diario porque no tienen registrada la fecha de su reto.
**Probar conexión con la base de datos** permite comprobar la conexión; los
errores de persistencia se muestran en pantalla.
