USE all_ten_db;

ALTER TABLE Partida
    ADD COLUMN fechaReto DATE NULL AFTER fechaFinalizacion,
    ADD KEY idx_partida_reto_tiempo (fechaReto, tiempoTotal);
