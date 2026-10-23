-- ================================================================
--  Datos de EJEMPLO para ver el ranking funcionando (All Ten)
--
--  Crea 10 jugadores "demo_..." con una partida cada uno.
--  Ejecútalo en tu base de Clever Cloud (con la base seleccionada),
--  DESPUÉS de haber creado las tablas con scriptBD.sql.
--
--  Se puede ejecutar varias veces: primero borra los datos demo anteriores.
--  Para quitar los datos de ejemplo cuando ya no los necesites, ejecuta
--  solo los dos DELETE del principio.
-- ================================================================
SET SQL_SAFE_UPDATES = 0;

DELETE FROM Partida
WHERE Jugador_id_Jugador IN (SELECT id_Jugador FROM Jugador WHERE nickname LIKE 'demo!_%' ESCAPE '!');

DELETE FROM Jugador WHERE nickname LIKE 'demo!_%' ESCAPE '!';

INSERT INTO Jugador (nickname) VALUES
    ('demo_Valentina'), ('demo_Mateo'), ('demo_Sofia'), ('demo_Camilo'), ('demo_Daniela'),
    ('demo_Andres'), ('demo_Laura'), ('demo_Santiago'), ('demo_Juliana'), ('demo_Felipe');

INSERT INTO Partida (tiempoTotal, Jugador_id_Jugador, operacionesRealizadas, fechaFinalizacion) VALUES
    (42,  (SELECT id_Jugador FROM Jugador WHERE nickname = 'demo_Valentina'), 'demo', '2026-10-20 21:15:33'),
    (47,  (SELECT id_Jugador FROM Jugador WHERE nickname = 'demo_Mateo'),     'demo', '2026-10-21 18:02:10'),
    (55,  (SELECT id_Jugador FROM Jugador WHERE nickname = 'demo_Sofia'),     'demo', '2026-10-19 16:45:02'),
    (61,  (SELECT id_Jugador FROM Jugador WHERE nickname = 'demo_Camilo'),    'demo', '2026-10-22 09:30:48'),
    (68,  (SELECT id_Jugador FROM Jugador WHERE nickname = 'demo_Daniela'),   'demo', '2026-10-18 20:11:27'),
    (74,  (SELECT id_Jugador FROM Jugador WHERE nickname = 'demo_Andres'),    'demo', '2026-10-21 13:59:59'),
    (83,  (SELECT id_Jugador FROM Jugador WHERE nickname = 'demo_Laura'),     'demo', '2026-10-17 11:20:05'),
    (91,  (SELECT id_Jugador FROM Jugador WHERE nickname = 'demo_Santiago'),  'demo', '2026-10-22 19:08:41'),
    (105, (SELECT id_Jugador FROM Jugador WHERE nickname = 'demo_Juliana'),   'demo', '2026-10-16 15:33:17'),
    (132, (SELECT id_Jugador FROM Jugador WHERE nickname = 'demo_Felipe'),    'demo', '2026-10-20 08:47:36');

-- Comprobación: debe mostrar las 10 partidas del más rápido al más lento
SELECT j.nickname, p.tiempoTotal, p.fechaFinalizacion
FROM Partida p INNER JOIN Jugador j ON j.id_Jugador = p.Jugador_id_Jugador
WHERE p.tiempoTotal IS NOT NULL
ORDER BY p.tiempoTotal ASC, p.fechaFinalizacion ASC, p.id_Partida ASC
LIMIT 10;
