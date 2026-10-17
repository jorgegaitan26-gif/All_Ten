-- Base de datos de All Ten (la que usan ConexionBD y PartidaDAOImpl).
-- Ejecutar en MySQL:  mysql -u root -p < sql/all_ten_db.sql

CREATE DATABASE IF NOT EXISTS all_ten_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE all_ten_db;

CREATE TABLE IF NOT EXISTS Jugador (
    id_Jugador INT NOT NULL AUTO_INCREMENT,
    nickname   VARCHAR(45) NOT NULL,
    PRIMARY KEY (id_Jugador),
    UNIQUE KEY uk_jugador_nickname (nickname)
);

CREATE TABLE IF NOT EXISTS Partida (
    id_Partida            INT NOT NULL AUTO_INCREMENT,
    tiempoTotal           INT NOT NULL,
    Jugador_id_Jugador    INT NOT NULL,
    operacionesRealizadas TEXT,
    fechaFinalizacion     DATETIME NOT NULL,
    PRIMARY KEY (id_Partida),
    CONSTRAINT fk_partida_jugador
        FOREIGN KEY (Jugador_id_Jugador) REFERENCES Jugador (id_Jugador)
);
