# All Ten

Aplicación JavaFX del juego All Ten. El juego utiliza los modelos `Jugador`,
`Partida`, `Expresion` y `Cronometro` para mantener el estado de cada partida.

## Persistencia

La capa DAO (`IPartidaDAO` y `PartidaDAOImpl`) conecta el juego con MySQL y la
base de datos `all_ten_db`:

1. Copia `.env.example` a `.env` en la raíz del proyecto y configura
   `ALL_TEN_DB_USER` y `ALL_TEN_DB_PASSWORD`. No agregues `.env` al repositorio;
   comparte solo el archivo de ejemplo.
2. Inicia MySQL y crea la base de datos y las tablas `Jugador` y `Partida`
   esperadas por el DAO.
3. Incluye MySQL Connector/J y las dependencias de JavaFX en el classpath de
   ejecución.

Al iniciar una partida, se busca el nickname y se registra el jugador si aún no
existe. Cuando completa los diez objetivos, se guarda la duración (redondeada a
segundos), la fecha de finalización y las operaciones. Los botones **Historial**
y **Probar conexión con la base de datos** permiten consultar los registros y
comprobar la conexión; los errores de persistencia se muestran en pantalla.
