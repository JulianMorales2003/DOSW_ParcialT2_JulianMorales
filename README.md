# DOSW_ParcialT2_JulianMorales

## Índice
1. [Datos del estudiante](#1-datos-del-estudiante)
2. [Descripción del proyecto](#2-descripción-del-proyecto)
3. [Despliegue](#3-despliegue)
4. [Estructura del proyecto](#4-estructura-del-proyecto)
5. [Tecnologías y dependencias](#5-tecnologías-y-dependencias)
6. [Configuración y variables de entorno](#6-configuración-y-variables-de-entorno)
7. [Cómo ejecutar](#7-cómo-ejecutar)
8. [Pruebas](#8-pruebas)
9. [Docker](#9-docker)
10. [CI/CD](#10-cicd)
11. [Herramientas de modelado y diseño (evidencias)](#11-herramientas-de-modelado-y-diseño-evidencias)

## 1. Datos del estudiante
- **Nombre completo:** Julian Felipe Morales Zambrano
- **Grupo:** _1_
- **Profesor agregado al repositorio:** _(completar: marcar ✔ cuando esté agregado como colaborador)_

## 2. Descripción del proyecto
Servicio backend base (Spring Boot + MongoDB) para el parcial práctico del segundo tercio de DOSW. Por ahora expone un endpoint de salud y la documentación Swagger; la lógica de negocio, la autenticación con JWT y los roles se implementan durante el laboratorio, sobre esta estructura MVC por capas. La base de JWT ya está en `core/services/JwtService` (generación y validación de tokens con el secret y la expiración de la configuración), con sus pruebas unitarias.

## 3. Despliegue
| Recurso | URL |
|---|---|
| Servicio en Azure | `https://<nombre-app>.azurewebsites.net` |
| Health | `https://<nombre-app>.azurewebsites.net/api/v1/health` |
| Swagger | `https://<nombre-app>.azurewebsites.net/swagger-ui/index.html` |

Azure no permitió crear el App Service con la suscripción disponible, por lo que las URLs anteriores quedan como referencia (`<nombre-app>` es el nombre del App Service). La configuración de despliegue está completa en el repositorio:

- La imagen Docker se construye y se publica en Docker Hub desde el pipeline.
- El job `deploy` de [`ci-cd.yml`](.github/workflows/ci-cd.yml) despliega la imagen en Azure App Service.
- Los comandos para crear los recursos en Azure están en [`docs/azure-despliegue.md`](docs/azure-despliegue.md).

El servicio se verifica en local (ver [Cómo ejecutar](#7-cómo-ejecutar)).

## 4. Estructura del proyecto
```
DOSW_ParcialT2_JulianMorales/
├── docs/
│   ├── uml/                      # diagramas fuente
│   └── images/                   # exportes y evidencias del README
├── src/
│   ├── main/
│   │   ├── java/edu/dosw/parcial/
│   │   │   ├── controller/
│   │   │   │   ├── dtos/{request,response}/
│   │   │   │   ├── mappers/
│   │   │   │   └── handlers/
│   │   │   ├── core/{services,models,validators,utils}/
│   │   │   ├── persistence/{entities,repositories,mappers}/
│   │   │   ├── config/
│   │   │   └── main/
│   │   └── resources/application.yaml
│   └── test/java/edu/dosw/parcial/{controller,core,persistence}/
├── Dockerfile
├── .dockerignore
├── .github/workflows/ci-cd.yml
├── pom.xml
├── .gitignore
└── README.md
```

| Paquete | Responsabilidad |
|---|---|
| `controller` | Endpoints REST; recibe y devuelve DTOs |
| `controller/dtos` | Objetos de request y response |
| `controller/mappers` | Mapper IN: DTO ↔ Dominio |
| `controller/handlers` | Manejo global de excepciones |
| `core/services` | Lógica de negocio |
| `core/models` | Objetos de dominio |
| `core/validators`, `core/utils` | Validaciones y utilidades |
| `persistence/entities` | Clases `@Document` de MongoDB |
| `persistence/repositories` | `MongoRepository` |
| `persistence/mappers` | Mapper OUT: Dominio ↔ Document |
| `config` | Configuración (seguridad, Swagger) |
| `main` | Clase con `@SpringBootApplication` |

## 5. Tecnologías y dependencias
| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje |
| Spring Boot | 3.3.5 | Framework base (Web, Validation, Security) |
| Spring Data MongoDB | (BOM de Spring Boot) | Persistencia |
| jjwt | 0.12.3 | Tokens JWT |
| springdoc-openapi | 2.5.0 | Swagger UI |
| Lombok | (BOM de Spring Boot) | Reducción de boilerplate y logs `@Slf4j` |
| MapStruct | 1.5.5.Final | Mapeo entre capas |
| JUnit 5 + Mockito | (spring-boot-starter-test) | Pruebas |
| Jacoco | 0.8.12 | Cobertura |
| sonar-maven-plugin | 3.10.0.2594 | Análisis de calidad |

## 6. Configuración y variables de entorno
| Variable | Descripción | Ejemplo |
|---|---|---|
| `MONGODB_URI` | Cadena de conexión a MongoDB (por defecto `mongodb://localhost:27017/dosw_parcial`) | `mongodb://usuario:clave@host:27017/db` |
| `JWT_SECRET` | Secret JWT en Base64, mínimo 44 caracteres | `<secret_base64>` |
| `PORT` | Puerto del servicio (por defecto 8080) | `8080` |

Ninguna credencial se sube al repositorio (`.env` y `*.p12` están en `.gitignore`).

## 7. Cómo ejecutar
**Mongo local con Docker**
```bash
docker run -d -p 27017:27017 --name mongo-parcial mongo:7
```

**Local con Maven**
```bash
export JWT_SECRET=<secret_base64>
mvn spring-boot:run
```

**Local con Docker**
```bash
docker build -t dosw-parcial:local .
docker run -p 8080:8080 -e JWT_SECRET=<secret_base64> dosw-parcial:local
```

Verificar: `http://localhost:8080/api/v1/health` y `http://localhost:8080/swagger-ui/index.html`.

## 8. Pruebas
```bash
mvn test
mvn verify   # genera el reporte Jacoco en target/site/jacoco/index.html
```
Evidencias:

![Pruebas en verde](docs/images/pruebas-verde.png)

![Reporte Jacoco](docs/images/jacoco.png)

## 9. Docker
Comandos de build y run en la sección 7. Evidencia:

![Contenedor corriendo](docs/images/docker-corriendo.png)

## 10. CI/CD
Pipeline en `.github/workflows/ci-cd.yml`, se ejecuta con cada push a `main` y `develop`:
1. **test:** `mvn verify` (pruebas + Jacoco).
2. **docker:** construye la imagen y la publica en Docker Hub.
3. **deploy:** despliega la imagen en Azure App Service.

Secrets usados (solo nombres): `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `JWT_SECRET`, `AZURE_CREDENTIALS`, `AZURE_WEBAPP_NAME`.

Evidencia:

![Workflow](docs/images/workflow-verde.png)

## 11. Herramientas de modelado y diseño (evidencias)
- Herramienta de modelado (Lucidchart / Draw.io / Miro):

![Cuenta de modelado](docs/images/cuenta-modelado.png)
- Herramienta de diseño (Figma):

![Cuenta de Figma](docs/images/cuenta-figma.png)
