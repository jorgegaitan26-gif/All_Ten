-- ================================================================
--  Script MySQL — All Ten
--
--  Tablas usadas por DAOJugador y DAOPartida.
-- 
-- ================================================================

-- CREATE DATABASE IF NOT EXISTS all_ten_db
--     CHARACTER SET utf8mb4
--     COLLATE utf8mb4_unicode_ci;
-- USE all_ten_db;

CREATE TABLE IF NOT EXISTS Jugador (
    id_Jugador  INT          NOT NULL AUTO_INCREMENT,
    nickname    VARCHAR(45)  NULL,

    PRIMARY KEY (id_Jugador)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS Partida (
    id_Partida             INT          NOT NULL AUTO_INCREMENT,
    tiempoTotal            INT          NULL,
    Jugador_id_Jugador     INT          NULL,
    operacionesRealizadas  VARCHAR(45)  NULL,
    fechaFinalizacion      DATETIME(1)  NULL,

    PRIMARY KEY (id_Partida),

    CONSTRAINT fk_Partida_Jugador
        FOREIGN KEY (Jugador_id_Jugador)
        REFERENCES Jugador (id_Jugador)
) ENGINE = InnoDB;

-- Comprobación
SHOW TABLES;
