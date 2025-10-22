# 🦖 DinoChomp - Backend

DinoChomp es un videojuego multijugador en tiempo real donde los usuarios 
controlan dinosaurios que compiten recolectando comida en el mapa, usando 
poderes especiales y atacando a sus rivales con mordiscos.

Este repositorio corresponde al **backend del videojuego**. Su objetivo es 
proveer los servicios y lógica de negocio necesarios para el videojuego.

## 🚀 Requisitos previos

Antes de ejecutar el proyecto, asegúrate de tener instaladas las siguientes herramientas:

- [Java JDK 21 o superior](https://adoptium.net/)
- [Apache Maven 3.8+](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/)
- (Opcional) [IntelliJ IDEA](https://www.jetbrains.com/idea/)

Verifica las versiones con:

```bash
java -version
mvn -version
git --version
````

## 📦 Clonar el repositorio

Abre una terminal en el directorio donde quieras guardar el proyecto y ejecuta:

```bash
git clone https://github.com/Dinoprogramadores/Backend-DinoChomp.git
cd Backend-DinoChomp
```

## 🧰 Compilar y ejecutar

1. Para compilar el proyecto:
```bash
mvn clean package
```
2. Para ejecutar la aplicación en local:
```bash
mvn spring-boot:run
```
3. Este proyecto usa contenedores docker, para contruirlos y ejecutarlos en segundo plano:
```bash
docker compose up --build -d
```
4. Ingresa a: 
```
http://localhost:8080/
```

Cuando realizes cambios (NO a `docker-compose.yml` ni la configuración 
de la base de datos) ejecuta nuevamente el paso 1 y 3.
## 🧪 Pruebas

Ejecuta las pruebas unitarias con:
```bash
mvn test
```

## 🧤 Contribuciones

1. Crea una nueva rama:
```bash
git checkout -b feat/nueva-funcionalidad
```

2. Realiza tus cambios y súbelos:
```bash
git commit -m "Agrega nueva funcionalidad"
git push origin feat/nueva-funcionalidad
```

3. Abre un Pull Request y espera la revisión de otro miembro del equipo.