CREATE DATABASE IF NOT EXISTS all_ten_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE all_ten_db;

CREATE TABLE IF NOT EXISTS Jugador (
    id_Jugador INT NOT NULL AUTO_INCREMENT,
    nickname VARCHAR(45) NOT NULL,
    PRIMARY KEY (id_Jugador),
    UNIQUE KEY uq_jugador_nickname (nickname)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Partida (
    id_Partida INT NOT NULL AUTO_INCREMENT,
    tiempoTotal BIGINT NOT NULL,
    Jugador_id_Jugador INT NOT NULL,
    operacionesRealizadas TEXT NOT NULL,
    fechaFinalizacion DATETIME NOT NULL,
    fechaReto DATE NULL,
    nivelDificultad VARCHAR(10) NOT NULL DEFAULT 'FACIL',
    PRIMARY KEY (id_Partida),
    KEY idx_partida_jugador (Jugador_id_Jugador),
    KEY idx_partida_fecha_finalizacion (fechaFinalizacion),
    KEY idx_partida_reto_tiempo (fechaReto, tiempoTotal),
    CONSTRAINT fk_partida_jugador
        FOREIGN KEY (Jugador_id_Jugador)
        REFERENCES Jugador (id_Jugador)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
