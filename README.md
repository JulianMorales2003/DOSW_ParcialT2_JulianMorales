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


## Punto 3 Asignación del Equipo de Base de datos - Diseño de la persistencia:

### Decisión 

Para guardar la información de University Food Hub usamos MongoDB con tres colecciones: `users`, `products` y `orders`. Decidimos un **modelo híbrido**, es decir, combinamos dos formas de relacionar datos:

- **Embebido:** guardar un dato *dentro* de otro documento. Aquí, `delivery`, `items` y `extras` viven dentro del pedido (`orders`).
- **Referenciado:** guardar solo el *id* de otro documento que vive en su propia colección. Aquí, `orders.userId` apunta a `users._id` y `items[].productId` apunta a `products._id`.



En el metamodelo, los recuadros dentro de `orders` son lo embebido y las flechas punteadas son las referencias.

### Razón de la decisión

A partir de los puntos anteriores y de los diagramas realizados deicmos que:
Lo que no existe sin el pedido se embebe. La entrega, los productos del pedido y sus extras nacen con el pedido y siempre se consultan con él. En el diagrama de clases esto es una *composición* (rombo relleno). 
Como un pedido se muestra siempre completo, tenerlo todo junto permite leerlo con **una sola consulta**, sin uniones, y guardarlo de una sola vez.

Por otro lado lo que tiene vida propia se referencia.Un usuario o un producto existen por sí solos: el producto se crea con `POST /api/v1/products` y el usuario inicia sesión con `POST /api/v1/auth/login`. 
En el diagrama de clases esto es una asociación. Por eso viven en su propia colección y el pedido solo guarda su id.

Se decide hacer hibirdo y no solo embebido porque copiaríamos usuarios y productos completos en cada pedido, y cualquier cambio obligaría a corregir muchos documentos. y no todo referenciado puesto que 
para mostrar un solo pedido haría falta consultar varias colecciones, algo innecesario cuando el pedido es pequeño y siempre se lee completo. por esta razón se decide hacer en el modelo hibrido descrito.

### Forma como se consultan y actualizan los datos

| Operación del contrato | Qué hace el modelo |
|---|---|
| `POST /api/v1/auth/login` | Busca el usuario por `email` (índice único). |
| `POST /api/v1/products` | Inserta en `products`. El índice único en `name`, sin distinguir mayúsculas, evita duplicados (409). |
| `POST /api/v1/orders` | Lee cada producto por `_id` para validar que exista y esté activo. Luego guarda el pedido completo en **una sola operación**. |
| `GET /api/v1/orders/me` | Busca por `userId` (tomado del token) y ordena por fecha descendente. Usa el índice `{userId: 1, createdAt: -1}`. |
| `GET /api/v1/orders` | Lista todos los pedidos ordenados por fecha descendente. Usa el índice `{createdAt: -1}`. |

Un pedido se crea una vez y no se modifica: nace en estado CONFIRMADO. Esto mantiene el modelo simple, y como MongoDB guarda un documento de forma atómica, 
si alguna validación falla no queda nada guardado a medias.

### Considere cómo se consultan y actualizan los datos, su posible duplicación y el crecimiento de los documentos. Expliquen una ventaja y una desventaja de la alternativa elegida.
### 3.4 Duplicación, crecimiento, ventaja y desventaja

**En cuanto a la duplicación de datos.** En cada producto del pedido copiamos el nombre y el precio que tenía al momento de la compra, y lo mismo con los extras. Es duplicación intencional puesto que un pedido funciona como una factura y debe recordar lo que realmente se cobró. Si mañana cambia el precio de un producto, los pedidos anteriores no se alteran.
Además, no hay riesgo de inconsistencias, porque un pedido nunca se modifica después de creado. Del usuario y del producto solo guardamos el id (`userId` y `productId`).

**En cuanto al crecimiento de los documentos.** Las reglas de negocio limitan el tamaño de un pedido: máximo 5 productos, extras de un catálogo fijo y una sola entrega. Por eso cada documento ocupa pocos kilobytes, muy por debajo del límite de 16 MB de MongoDB. Lo que crece es la cantidad de pedidos, y eso lo resuelven los índices `{userId: 1, createdAt: -1}` y `{createdAt: -1}`.

**En cuantoo a las Ventajas y desventajas.** La ventaja es que un pedido se lee y se guarda completo en una sola operación, sin unir colecciones, y conserva los precios del momento de la compra. La desventaja es que hay datos copiados, por lo que se usa algo más de almacenamiento y los pedidos antiguos no reflejan cambios posteriores del producto. Para conocer su estado actual hay que consultar `products` por id.

Diagrama se puede ver en la carpeta de imagenes, se adjunta la imagen acaa:
![Metamodelo conceptual NoSQL](docs/images/metamodelo-nosql-.png)

## 6. Asignación del equipo de DevOps

### 6.1 Diagrama de despliegue

![Diagrama de despliegue](docs/images/DDespliegue.png)

*Figura X. Diagrama de despliegue de University Food Hub.*

**Nodos y dispositivos**

| Elemento | Tipo | Qué contiene |
|---|---|---|
| Computador o celular del usuario | Dispositivo | Navegador web o Swagger UI que consume la API |
| Azure App Service | Nodo (entorno de ejecución) | JRE 21 y el artefacto `food-hub.jar` (Spring Boot) |
| MongoDB | Nodo (base de datos) | Colecciones `users`, `products` y `orders` |
| GitHub / GitHub Actions | Nodo (integración) | Repositorio y pipeline que construye y despliega |

**Comunicación entre nodos**

| Origen | Destino | Mecanismo |
|---|---|---|
| Dispositivo del usuario | Azure App Service | HTTPS (REST con JSON, puerto 443) |
| Azure App Service | MongoDB | Protocolo de MongoDB (puerto 27017, conexión cifrada) |
| GitHub Actions | Azure App Service | Despliegue del `.jar` autenticado con el perfil de publicación |

**Especificación del despliegue:** las credenciales (cadena de conexión a MongoDB, clave del JWT) no están en el código sino en variables de entorno configuradas en Azure App Service. El perfil de publicación se guarda como secreto en GitHub.

**Justificación:** separar el dispositivo, la aplicación y la base de datos permite cambiar o escalar cada parte sin afectar a las otras. Además, la API nunca expone la base de datos directamente al usuario.

### 6.2 Preguntas de revisión

**¿Cuáles son las etapas principales de un pipeline CI/CD y qué propósito tiene cada una?**

1. **Checkout:** descarga el código de la rama `develop`.
2. **Build (compilación):** compila el proyecto con Maven y verifica que no haya errores de sintaxis ni dependencias faltantes.
3. **Pruebas:** ejecuta las pruebas unitarias con JUnit y Mockito para comprobar que las reglas de negocio siguen funcionando.
4. **Cobertura y análisis estático:** JaCoCo mide que la cobertura sea superior al 80 % y el análisis estático busca malas prácticas o vulnerabilidades.
5. **Empaquetado:** genera el `.jar` listo para ejecutar.
6. **Despliegue (CD):** publica el `.jar` en Azure App Service.

CI (integración continua) abarca las etapas 1 a 5 y verifica cada cambio de forma automática. CD (despliegue continuo) es la etapa 6.

**¿Qué sucede si una prueba falla? ¿Debe permitirse el despliegue?**

El pipeline se detiene en esa etapa y las siguientes no se ejecutan. **No debe permitirse el despliegue**, porque una prueba fallida indica que una regla de negocio dejó de cumplirse (por ejemplo, que se acepten pedidos bajo el mínimo de $3.500). Si se desplegara, el error llegaría a los usuarios reales. Lo correcto es corregir el código, volver a ejecutar el pipeline y desplegar solo cuando todo esté en verde.

**¿Para qué sirven los logs y qué registraríamos para investigar un pedido fallido o un error de autenticación?**

Los logs son el registro de lo que hace la aplicación mientras funciona. Sirven para saber qué pasó, cuándo y por qué falló algo, sin necesidad de reproducir el error.

- **Pedido fallido:** fecha y hora, identificador del usuario (`userId`), la regla que falló (por ejemplo, "subtotal $2.500 menor al mínimo"), el código HTTP devuelto y los códigos de los productos involucrados.
- **Error de autenticación:** fecha y hora, el correo que intentó entrar, el motivo interno (usuario no existe o contraseña incorrecta), la IP de origen y si el token estaba vencido o mal formado.

Se usan los niveles según la gravedad: `INFO` para operaciones normales (pedido creado), `WARN` para rechazos esperados (regla de negocio incumplida) y `ERROR` para fallas inesperadas (base de datos caída).

**Ejemplos de información útil y de datos que no deben registrarse**

| Útil para rastrear | No debe registrarse |
|---|---|
| Id del pedido y del usuario | Contraseñas (ni en texto ni en hash) |
| Código HTTP y regla que falló | El token JWT completo |
| Fecha, hora y nivel del log | Cadena de conexión a MongoDB ni claves secretas |
| IP de origen en errores de login | Datos personales innecesarios |

**¿Qué diferencia debe existir entre el log interno y el mensaje que recibe el cliente?**

El log interno tiene todo el detalle técnico para que el equipo encuentre el problema: la excepción completa, la clase donde ocurrió y el motivo exacto. El mensaje al cliente debe ser corto, claro y sin información sensible. Por ejemplo, en un login fallido el log dice "usuario no existe" o "contraseña incorrecta", pero el cliente solo recibe "Credenciales inválidas", para no revelar qué dato falló. Igual en un error inesperado: el cliente ve "Error interno del servidor" y el detalle queda solo en el log.
