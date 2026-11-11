USE all_ten_db;

-- Nivel de dificultad de la partida: FACIL o DIFICIL (las partidas anteriores quedan como FACIL).
ALTER TABLE Partida
    ADD COLUMN nivelDificultad VARCHAR(10) NOT NULL DEFAULT 'FACIL' AFTER fechaReto;
