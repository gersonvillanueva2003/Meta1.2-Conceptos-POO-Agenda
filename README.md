# Meta1.1_-Programar-un-CRUD
# Sistema de Agenda CRUD - Meta 1.2 (JavaFX y MariaDB)

Aplicación de escritorio desarrollada en JavaFX para gestionar personas, sus teléfonos y direcciones asociadas mediante una relación N:M (Muchos a Muchos).

## Tecnologías Utilizadas

* Lenguaje: Java 23
* Interfaz Gráfica: JavaFX
* Base de Datos: MariaDB / MySQL (MariaDB Connector/J)
* Gestor de Proyecto: Maven

## Esquema de la Base de Datos

El script SQL se encuentra en src/main/resources/schema.sql:

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

## Instalación y Ejecución

1. Clonar el repositorio:
   git clone https://github.com/gersonvillanueva2003/Meta1.2-Conceptos-POO-Agenda.git
   cd Meta1.2-Conceptos-POO-Agenda

2. Cargar la base de datos:
   Ejecutar el script src/main/resources/schema.sql en el cliente MariaDB/MySQL.

3. Ejecutar la aplicación:
   mvn clean compile
   mvn javafx:run

4. Ejecutar pruebas unitarias:
   mvn test
