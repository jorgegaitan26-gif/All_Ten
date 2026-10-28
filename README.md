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

1. Inicia MySQL y ejecuta el script
   [`database/create_tables.sql`](database/create_tables.sql) en MySQL Workbench
   o con el cliente de MySQL. El script crea la base `all_ten_db` y las tablas
   `Jugador` y `Partida` que utiliza el DAO.
   Si la tabla `Partida` ya existía antes del reto diario, ejecuta también
   [`database/migrate_daily_challenge.sql`](database/migrate_daily_challenge.sql).
   Para habilitar partidas libres en una base existente, ejecuta después
   [`database/migrate_free_play.sql`](database/migrate_free_play.sql), que permite
   guardar partidas sin fecha de reto diario.
   Si MySQL muestra `Data too long for column 'operacionesRealizadas'`, ejecuta
   [`database/fix_operaciones_partida.sql`](database/fix_operaciones_partida.sql)
   para ampliar la columna de operaciones sin perder su contenido.
2. Ejecuta la aplicación con `mvn javafx:run` para que Maven incluya las
   dependencias necesarias.

Al iniciar una partida, se busca el nickname y se registra el jugador si aún no
existe. Se puede elegir entre dos modos:

- **Reto diario**: se genera de forma determinista a partir de la fecha en la
  zona horaria `America/Bogota`. Todos reciben los mismos cuatro números del día
  y se puede repetir el reto para mejorar el tiempo.
- **Partida libre**: cada partida genera una combinación aleatoria nueva,
  independiente de la fecha. Al reiniciarla se genera otra combinación.

Al completar una partida se guarda su duración, fecha de finalización,
operaciones y, solo para el reto diario, la fecha del reto. El botón
**Ranking** muestra los diez mejores tiempos del reto actual, con una sola
entrada por jugador y su mejor partida de ese día; las partidas libres no se
incluyen en ese ranking.
