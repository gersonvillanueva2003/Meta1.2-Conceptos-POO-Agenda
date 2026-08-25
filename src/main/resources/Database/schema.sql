CREATE DATABASE IF NOT EXISTS agenda;
USE agenda;

DROP TABLE IF EXISTS PersonaDireccion;
DROP TABLE IF EXISTS Direcciones;
DROP TABLE IF EXISTS Telefonos;
DROP TABLE IF EXISTS Personas;

CREATE TABLE Personas (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          nombre VARCHAR(100) NOT NULL
);

CREATE TABLE Telefonos (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           personaId INT NOT NULL,
                           telefono VARCHAR(30) NOT NULL,
                           FOREIGN KEY (personaId) REFERENCES Personas(id) ON DELETE CASCADE
);

CREATE TABLE Direcciones (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             direccion VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE PersonaDireccion (
                                  personaId INT NOT NULL,
                                  direccionId INT NOT NULL,
                                  PRIMARY KEY (personaId, direccionId),
                                  FOREIGN KEY (personaId) REFERENCES Personas(id) ON DELETE CASCADE,
                                  FOREIGN KEY (direccionId) REFERENCES Direcciones(id) ON DELETE CASCADE
);

INSERT INTO Personas (id, nombre) VALUES (1, 'Gerson Villanueva'), (2, 'Maria Lopez');
INSERT INTO Telefonos (personaId, telefono) VALUES (1, '686-111-2233'), (2, '686-777-8899');
INSERT INTO Direcciones (id, direccion) VALUES (1, 'Av. Reforma #123'), (2, 'Calle Segunda #456');
INSERT INTO PersonaDireccion (personaId, direccionId) VALUES (1, 1), (1, 2), (2, 1);