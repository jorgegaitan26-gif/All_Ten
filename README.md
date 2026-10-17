# All Ten

Aplicación JavaFX del juego All Ten.

## Persistencia

La capa DAO (`IPartidaDAO` y `PartidaDAOImpl`) registra jugadores, guarda partidas
y consulta el historial en una base de datos MySQL llamada `all_ten_db`. Para
configurar la conexión:

1. Copia `.env.example` a `.env` en la raíz del proyecto y configura allí
   `ALL_TEN_DB_USER` y `ALL_TEN_DB_PASSWORD`. El archivo `.env` está excluido
   de Git para no publicar credenciales.
2. Asegúrate de que MySQL esté iniciado y que la base de datos y las tablas esperadas por
   `PartidaDAOImpl` existan.
3. Incluye MySQL Connector/J en el classpath de ejecución junto con las
   dependencias de JavaFX.

El botón **Probar conexión con la base de datos** del menú abre una conexión y
ejecuta `SELECT 1`, mostrando en pantalla si tuvo éxito o el error recibido.
El botón **Historial** consulta y muestra las partidas almacenadas.
`GameController.guardarPartida(jugador, partida)` registra al jugador si todavía
no tiene ID y guarda su partida en una misma transacción. El flujo de juego
actual aún no implementa la finalización de una partida; ese evento deberá
invocar este método cuando esté disponible.

Ejemplo de `.env` (reemplaza los valores con tus credenciales; no subas este
archivo al repositorio):

```dotenv
ALL_TEN_DB_USER=root
ALL_TEN_DB_PASSWORD=tu_contraseña
```
