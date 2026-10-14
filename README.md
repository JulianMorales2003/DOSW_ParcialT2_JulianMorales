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

Azure no me permitió crear el App Service con la suscripción disponible por problemas de la cuenta estudiantil, por lo que las URLs anteriores quedan como referencia (`<nombre-app>` es el nombre del App Service). La configuración de despliegue está completa en el repositorio:

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


## Solución parcial naranja 

## Punto 1 Asignación del equipo de Producto - Definición del contrato de la API

### link de excel con la tabla en la carpeta de docs con el nombre : [ExcelPrimerPunto.xlsx](docs%2FExcelPrimerPunto.xlsx) 

### Preguntas para la revisión con Producto:

#### a) ¿Cuál es la diferencia entre una validación de entrada y una validación de negocio? Seleccionen tres reglas del caso, incluyendo al menos una de cada tipo, y justifiquen su clasificación.
LA diferencia es es que la validacion de entrada verifica que la peiticion esta bien hecha o bien formada, donde se revisa que los campos estan bien y existen, tengan el tipo correcto con la el formato, longitud y demas parametros correctos
es importante aclarar que la revisión y su respectiva validación se hace basandose unicamente en la petición, por lo que no se consulta la bsae de datos en ningun momento, por lo que al hacer la revision de la solicitud, el sistema responde 
si encuentra algo mal. Por otro lado la validacion de negocio, verifica que una solicitud o peticion bien hecha o fromada ahora cumpla con las reglas del dominio y hace una consulta de la base de datos para verificar y devolver un erro en 
caso de haberlo. 

selección de las reglas:
1. Cantidad de productos inválida, un pedido puede tener máximo 5 productos ( E06 ), se hace un conteo de la cantidad de datos de la entrada y no hay necesidad de consultar la base de datos, puesto que es algo vereificable desde la entrada, siendo
este un limite estructural para las solicitudes que se realizan. Por esta razón esta se clasifica en una validación de entrada.
2. Bajo el minimo, donde el subtotal del pedido debe ser de minimo 3500, donde se verifica por medio de que el sistema consulta la base de datos y busca los productos del pedido para sumarlos y evaluar si la regla del minimom se esta cumpliendo, por esta 
razon esto corresponde a una validacion de negocio.
3. Producto inexistente o inactivo, donde se revisa que el producto o productos del pedido que realizóm el cliente existan y esten activos, por lo que el sistema debe verificar la base de datos directamente pues aca se encuentra la informaciuon y por esta razon
al sistema no le basta con revisar la solicitud, de acuerdo con lo anterior esto corresponde a una validación de negocio.

#### b) En Pedir un turno, ¿cómo debe responder la API si fallan varias condiciones? Expliquen qué error se informa y qué cambios deben evitarse.
Si fallan varias condiciones dentro del contexto de pedir un turno, la respuesta de la API debe ser que al revisar cada una de las etapas verificando una poprm una y se detiene al momento de encontrar una que falle y devuelve el error para el caso, y luego revisa los otros si es necesario
esto se hace uno a uno, es decir solo entrega un mensaje de error , y va en orden, primero las de entrada, luego revision sonbre existencia de productos y por ultimo las reglas de negocio, se hace así en orden y por etapas porque algunas reglas de negocio y entrada dependen unas de otras entonces
por esta razón las respuestas deben ser en orden e individuales para cada uno de las condciones o errores que se hayan. 

Una cosa que se debe evitar es el cambio de datos, pues si algo falla no debe crear ningun peddido o seguir con el flujo y no modificar datos, y se guarda en memoria un pedido al estar completo y correcto. 

## 5. Asignación del equipo de Calidad

Los escenarios son del caso de uso **Hacer un pedido** (`POST /api/v1/orders`) y se construyen con TDD (Red, Green, Refactor).

### 5.1 Orden de las validaciones

Si un pedido falla por varias razones a la vez, la API informa **solo el primer error** según este orden:

1. Token y rol (401 / 403)
2. Validación de entrada: entre 1 y 5 productos, `delivery.type` válido, y bloque y salón presentes si es ENTREGA_SALON (400)
3. Productos y extras existentes, y productos activos (404)
4. Bloque A o B en ENTREGA_SALON (422)
5. Subtotal mínimo de $3.500 (422)
6. Cálculo del total y guardado del pedido como CONFIRMADO (201)

El pedido se guarda **siempre al final**. Por eso, si algo falla antes, no se crea nada en la base de datos.

### 5.2 Escenarios de prueba

| ID | Escenario | Datos de entrada | Resultado esperado |
|---|---|---|---|
| T01 | Pedido válido (E14) | PROD-001 + EXT-001 + EXT-004, PARA_LLEVAR | 201. Subtotal $11.300, entrega $300, total $11.600, estado CONFIRMADO |
| T02 | Pedido válido con entrega a salón | PROD-002 + EXT-003, ENTREGA_SALON bloque A, salón 101 | 201. Subtotal $5.500, entrega $1.000, total $6.500 |
| T03 | Subtotal justo en el mínimo | PROD-004 ($3.500), CONSUMO_LOCAL | 201, porque el mínimo es inclusivo |
| T04 | Bajo el mínimo (E4) | PROD-005 ($2.500) sin extras | 422. No se guarda el pedido |
| T05 | Bloque inválido (E5) | ENTREGA_SALON bloque C | 422. No se guarda el pedido |
| T06 | Seis productos (E06) | 6 productos en el pedido | 400. No se guarda el pedido |
| T07 | Producto inexistente o inactivo (E02) | `productId` que no existe o con `active = false` | 404. No se guarda el pedido |
| T08 | Extra inexistente (E03) | Extra `EXT-999` | 404. No se guarda el pedido |
| T09 | Usuario y total desde el servidor | Body con `userId` ajeno y `total` manipulado | Se guarda con el `userId` del token y el total calculado por el servidor |
| T10 | Sin token o rol ADMIN | Petición sin token / con token de ADMIN | 401 / 403 |
| T11 | Varias fallas a la vez | 6 productos, bloque C y subtotal bajo | Solo se informa el error de entrada (400), que es el primero del orden |

### 5.3 TDD (Red, Green, Refactor)

Para cada escenario seguimos tres pasos:

1. **Red:** escribimos la prueba antes que el código (por ejemplo T04) y comprobamos que falla.
2. **Green:** escribimos lo mínimo para que pase (`if (subtotal < 3500) lanzar excepción`).
3. **Refactor:** con las pruebas en verde, ordenamos el código (por ejemplo, mover el cálculo a `PriceCalculator`) y verificamos que todas sigan pasando.

Empezamos por el pedido válido (T01) y luego pasamos a los rechazos, del más simple al más complejo.

### 5.4 Cómo comprobamos el orden y que una falla no deja cambios

- **No se crea el pedido:** en las pruebas con Mockito, en cada rechazo (T04 a T08) verificamos que `orderRepository.save()` nunca se llama (`verify(..., never())`).
- **No se modifica el catálogo:** este caso de uso solo lee productos, así que verificamos que `productRepository.save()` tampoco se llama.
- **Se respeta el orden:** T11 manda varios errores juntos y comprobamos que solo sale el primero del orden de la sección 5.1.

### 5.5 Preguntas de revisión

**¿Cómo aportan las pruebas evidencia del cumplimiento de las reglas de negocio? ¿Qué se verifica con pruebas unitarias?**

Cada regla de negocio tiene al menos una prueba con datos conocidos y un resultado esperado que sale del contrato. Si alguien cambia el código y rompe la regla, la prueba falla. Así cada escenario del caso (E02, E03, E4, E5, E06, E14) queda cubierto por una prueba, incluyendo el caso límite del subtotal de $3.500 (T03).

Con **pruebas unitarias** (JUnit y Mockito, con repositorios simulados) verificamos cada clase por separado:
- El cálculo del subtotal, la entrega y el total.
- Las reglas del Service: producto activo, extra existente, bloque A o B y subtotal mínimo.
- Que ante una falla se lance la excepción correcta y no se guarde nada.
- Que el usuario y el total vengan del servidor y no del body.

Los códigos HTTP, el token y la conexión real con MongoDB se prueban aparte, con pruebas de integración.

**¿Garantizan las pruebas por sí solas la ausencia de errores?**

No. Una prueba solo demuestra que el sistema funciona en los casos que se nos ocurrió probar, pero pueden existir casos que no imaginamos. Además, tener 80 % de cobertura solo indica cuántas líneas se ejecutaron, no que las pruebas verifiquen lo correcto. Una prueba también puede pasar y estar validando una regla mal entendida. Por eso las pruebas se complementan con revisión de código, análisis estático y validación con el equipo de Producto.