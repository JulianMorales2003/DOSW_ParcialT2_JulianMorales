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
| Servicio en Azure | https://foodhub-julian-2026.azurewebsites.net |
| Health | https://foodhub-julian-2026.azurewebsites.net/api/v1/health |
| Swagger | https://foodhub-julian-2026.azurewebsites.net/swagger-ui/index.html |

La API corre en **Azure App Service** (Linux) como un contenedor. La imagen se construye y se publica en Docker Hub (`julianmorales17/dosw-parcial-t2:latest`) desde el pipeline, y Azure la descarga de ahí. La base de datos es **MongoDB Atlas**.

| Recurso de Azure | Valor |
|---|---|
| Grupo de recursos | `rg-dosw-parcial` |
| Plan de App Service | `plan-dosw-parcial` (Linux, B1) |
| App Service | `foodhub-julian-2026` |
| Región | Spain Central |

Mi suscripción de estudiante restringe las regiones donde se pueden crear recursos. Por eso el plan se creó en Spain Central y no en East US.

### Configuración del App Service
Las credenciales no están en el código. Se configuran como variables de entorno del App Service:

| Variable | Descripción |
|---|---|
| `MONGODB_URI` | Cadena de conexión a MongoDB Atlas |
| `JWT_SECRET` | Secret de los tokens JWT, en Base64 |
| `WEBSITES_PORT` | `8080`, el puerto en que escucha el contenedor |

### Cómo se creó
```bash
az group create --name rg-dosw-parcial --location eastus2

az appservice plan create --name plan-dosw-parcial \
  --resource-group rg-dosw-parcial --is-linux --sku B1 --location spaincentral

az webapp create --name foodhub-julian-2026 \
  --resource-group rg-dosw-parcial --plan plan-dosw-parcial \
  --container-image-name julianmorales17/dosw-parcial-t2:latest

az webapp config appsettings set --name foodhub-julian-2026 \
  --resource-group rg-dosw-parcial \
  --settings MONGODB_URI="<cadena-atlas>" JWT_SECRET="<secret-base64>" WEBSITES_PORT=8080

az ad sp create-for-rbac --name dosw-parcial-sp --role contributor \
  --scopes /subscriptions/<subscription-id>/resourceGroups/rg-dosw-parcial --sdk-auth
```
El JSON que devuelve el último comando se guarda como secreto `AZURE_CREDENTIALS` en GitHub. Después, cada integración en `develop` despliega automáticamente con el job `deploy` de [`ci-cd.yml`](.github/workflows/ci-cd.yml).

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


Una cosa que se debe evitar es el cambio de datos, pues si algo falla no debe crear ningun peddido o seguir con el flujo y no modificar datos, y se guyarda en memoria un pedido al estar completo y correcto. 


## Punto 2 Preguntas para la revisión con Arquitectura: 

### Diagramas: 

#### a) Diagrama de componentes general:

![Diagrama de componentes general](docs/images/DComponentesGeneral.png)

#### b) Diagrama de componentes específico siguiendo el esquema de MVC:

![Diagrama de componentes específico MVC](docs/images/DComponentesEspecifico.png)

#### c) Diagrama de clases: modelos de dominio:

![diagrama-clases-dominio.png](docs/images/diagrama-clases-dominio.png)

### - Preguntas para la revisión con Arquitectura: 

##### ¿Qué problemas aparecen cuando no se separan correctamente las capas? Presenten un ejemplo del caso y expliquen cómo distribuyen sus responsabilidades.

Si las capas se mezclan, aparecen estos problemas:
 - Alto acoplamiento: un cambio en MongoDB o en el formato de JSON obliga a tocar la lógica del negocio.
 - Baja cohesión y duplicación: la misma regla se copia en varios endpoints.
 - Poca testeabilidad: no se puede probar una regla sin levantar HTTP y la base de datos.
 - Riesgos de seguridad: se expone la entidad de persistencia directamente.

Ejemplo del caso:
 - Si OrderController calcula el subtotal, valida el mínimo de $3.500 y consulta MongoDB, las reglas de precio quedan atadas a HTTP. Al agregar otro punto de entrada, habria que duplicarlas y se podrian omitir.

  Distribucion de responsabilidades:
  - Controller: Recibe el request, dispara la validación de entrada @Valid y devuelve el ResponseEntity con su codigo HTTP.
  - Service: Orquesta el caso de uso y aplica las reglas de negocio como los productos activos, extras existentes, minimo, total.
  - Repository: Solo accede a MongoDB
  - Mapper: Convierte entre DTO y entidad.
  - Model/Entity: Representa el dominio. 

##### ¿Qué diferencia existe entre un validador, una utilidad y un servicio? Ubiquen un ejemplo de cada uno en su diseño e indiquen su responsabilidad.

###### Validador, utilidad y servicio

| Componente | Responsabilidad | Ejemplo |
|---|---|---|
| **Validador** | Verifica que un dato o regla se cumpla y rechaza si no. No orquesta ni persiste. | `DeliveryValidator`: ENTREGA_SALON exige bloque A o B y salón. |
| **Utilidad** | Función auxiliar, sin estado y sin lógica de negocio, reutilizable en cualquier capa. | `JwtUtil` (generar y leer el token) o `DateUtil`. |
| **Servicio** | Implementa el caso de uso: coordina validadores, repositorios y mappers, y aplica la lógica de negocio. | `OrderService.createOrder()`. |

##### ¿Qué diferencia existe entre el Mapper IN y el Mapper OUT? Indiquen dónde se ubican y qué representaciones transforman cada uno.

- Mapper IN (request): Convierte el DTO de entrada de entrada `CreateOrderRequest` a la entidad o modelo de dominio `Order`. Se ubica entre el Controller y el Service. Mantiene el contrato externo separado del modelo interno y datos como `userId`, `total` o `status` no vienen del cliente, sino que los asigna el servidor.
- Mapper OUT (response): convierte la entidad `Order` al DTO de salida `OrderResponse`. Se ubica entre el Service y el Controller. Controla qué se expone: arma el desglose `subtotal`, `delivery.cost`, `total` y evita filtrar campos internos.

##### Retomen las tres reglas clasificadas en el paso anterior: ¿en qué componente debe ejecutarse cada una y por qué? 

###### Componente que ejecuta cada regla

| Regla                                       | Tipo | Componente | Justificación |
|---------------------------------------------|---|---|---|
| Nombre del producto de 3 a 60 caracteres    | Entrada | **DTO + Controller** (`@Valid`, `@Size`) | Se evalúa solo con el request, sin consultar datos. Rechaza con 400 antes de llegar al Service. |
| Subtotal mínimo de $3.500                   | Negocio | **Service** (con `PriceCalculator`) | Depende de los precios reales de MongoDB, que el cliente no envía. Es una decisión del dominio y se rechaza con 422. |
| producto del pedido existan y esten activos | Negocio | **Service** (con `OrderValidator` y `ProductRepository`) | Para saber si un producto existe y está activo hay que consultar MongoDB, algo que el request no trae. Es una decisión del dominio, no de formato, así que no va en el DTO ni en el Controller. El Service consulta los productos y el validador comprueba la regla. Si falla, se rechaza con 404 antes de guardar nada. |



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
### Duplicación, crecimiento, ventaja y desventaja

**En cuanto a la duplicación de datos.** En cada producto del pedido copiamos el nombre y el precio que tenía al momento de la compra, y lo mismo con los extras. Es duplicación intencional puesto que un pedido funciona como una factura y debe recordar lo que realmente se cobró. Si mañana cambia el precio de un producto, los pedidos anteriores no se alteran.
Además, no hay riesgo de inconsistencias, porque un pedido nunca se modifica después de creado. Del usuario y del producto solo guardamos el id (`userId` y `productId`).

**En cuanto al crecimiento de los documentos.** Las reglas de negocio limitan el tamaño de un pedido: máximo 5 productos, extras de un catálogo fijo y una sola entrega. Por eso cada documento ocupa pocos kilobytes, muy por debajo del límite de 16 MB de MongoDB. Lo que crece es la cantidad de pedidos, y eso lo resuelven los índices `{userId: 1, createdAt: -1}` y `{createdAt: -1}`.

**En cuantoo a las Ventajas y desventajas.** La ventaja es que un pedido se lee y se guarda completo en una sola operación, sin unir colecciones, y conserva los precios del momento de la compra. La desventaja es que hay datos copiados, por lo que se usa algo más de almacenamiento y los pedidos antiguos no reflejan cambios posteriores del producto. Para conocer su estado actual hay que consultar `products` por id.

Diagrama se puede ver en la carpeta de imagenes, se adjunta la imagen acaa:
![Metamodelo conceptual NoSQL](docs/images/metamodelo-nosql-punto3.png)


## Punto 4 Asignación del Equipo de Seguridad - Proteger el acceso y la información

### ¿Cuál es la diferencia entre autenticación, autorización e integridad? Expliquen dónde y cómo aplicarán cada concepto en la API, con ejemplos del caso.

Presentamos la repuesta en una tabla comparativaa: 
- **Autenticación responde a la pregunta de ¿quién eres?** Se comprueba la identidad. En `POST /api/v1/auth/login` el usuario envía correo y contraseña; el sistema compara la contraseña con el hash guardado (BCrypt) y, si coincide, entrega un token JWT. Con credenciales inválidas o sin token responde **401**.
- **Autorización responde a la pregunta de ¿qué puedes hacer?** Se verifica el permiso según el rol del token. Solo el ADMIN crea productos (`POST /api/v1/products`) y lista todos los pedidos (`GET /api/v1/orders`); solo el CLIENTE hace pedidos y ve los suyos. Un cliente que intenta crear un producto recibe **403**.
- **Integridad: responde a la preguntaa de ¿los datos son confiables y no fueron alterados?** El token va firmado, así que si alguien cambia su contenido (por ejemplo, el rol) se rechaza. El total del pedido lo calcula el servidor, el `userId` sale del token y no del cuerpo de la petición, y la comunicación debe ir por HTTPS.

### Seleccionen dos riesgos del OWASP Top 10 que se pueden encontrar en su API. Para cada uno, describan cómo se vería ese escenario aplicable a la fundacion, su mitigación y cómo verificarían que el control funciona.

Se seleccionaron:

**A03 Injection (inyección).**
- Escenario: inyección NoSQL en el login, por ejemplo enviando `{"email": {"$ne": null}, "password": {"$ne": null}}` para entrar sin credenciales válidas.
- Mitigación: para validar tipo y formato de cada dato de entrada con Bean Validation en los DTO (el correo debe ser texto con formato de email, el nombre de 3 a 60 caracteres, el precio numérico), rechazando con **400** antes de tocar la base de datos. Además, usar solo los repositorios de Spring Data y nunca armar consultas concatenando texto del usuario..
  - Verificación: las ruebas que envíen objetos o caracteres especiales en el login y en la creación de productos, y comprueben que la respuesta es **400** y que no se devuelve ni se guarda ningún dato.

**A01 Broken Access Control (control de acceso roto).**
- Escenario: un cliente intenta ver los pedidos de otra persona, llamar al endpoint de administrador, o enviar el `userId` de otro para hacer un pedido a su nombre.
- Mitigación: la autorización por rol en cada endpoint, y tomar siempre la identidad del token. `GET /api/v1/orders/me` filtra por el `userId` del token y no recibe ningún id del cliente.
- Verificación: pruebas que comprueben que un token de CLIENTE en `GET /api/v1/orders` o `POST /api/v1/products` devuelve **403**, que sin token devuelve **401**, y que `/orders/me` nunca incluye pedidos de otro cliente.



### ¿Cuál de los tipos de autenticación es el más adecuado para este MVP? Justifique su respuesta  

Para este caso elegimos **JWT** 
Descartamos **Basic Auth** porque envía la contraseña en cada petición, las **sesiones con cookies** porque exigen guardar estado en el servidor, y **OAuth2** 
porque es demasiado complejo para un MVP con usuarios predefinidos. Las contraseñas se guardan con BCrypt, el token expira pronto y la clave de firma viene de 
una variable de entorno, no del código.

segun la eleccion, enviaddo en el encabezado `Authorization: Bearer <token>`:

- **Sin estado:** el servidor no guarda sesiones; usuario y rol viajan dentro del token firmado. Encaja con una API REST desplegada en Azure.
- **Resuelve nuestras reglas:** el token trae el `userId` y el rol, justo lo que necesitamos para autorizar y para asociar cada pedido a su dueño.
- **Simple:** un login y un filtro que valide el token, sin servidores externos. Swagger lo soporta con el botón Authorize.


## 5. Asignación del equipo de Calidad

Los escenarios de prueba son del caso de uso **Hacer un pedido** (`POST /api/v1/orders`) y los construimos siguiendo TDD (Red, Green, Refactor).

### 5.1 Orden de las validaciones

Cuando un pedido falla por varias razones a la vez, la API solo informa el primer error, siguiendo este orden:

1. Token y rol (401 / 403).
2. Validación de entrada: entre 1 y 5 productos, tipo de entrega válido, y bloque y salón presentes si es ENTREGA_SALON (400).
3. Que los productos existan y estén activos, y que los extras existan (404).
4. Que el bloque sea A o B cuando la entrega es a salón (422).
5. Que el subtotal sea de mínimo $3.500 (422).
6. Cálculo del total y guardado del pedido como CONFIRMADO (201).

El pedido se guarda siempre de último. Así, si algo falla antes, no queda nada nuevo en la base de datos.

### 5.2 Escenarios de prueba

| ID | Escenario | Datos de entrada | Resultado esperado |
|---|---|---|---|
| T01 | Pedido válido | Una bandeja universitaria con porción extra de proteína y queso, para llevar | 201. Subtotal $11.300, entrega $300, total $11.600, estado CONFIRMADO |
| T02 | Pedido válido con entrega a salón | Un sándwich con pan integral, entregado en el salón 101 del bloque A | 201. Subtotal $5.500, entrega $1.000, total $6.500 |
| T03 | Subtotal justo en el mínimo | Una sopa del día ($3.500), para consumir en la cafetería | 201, porque el mínimo es inclusivo |
| T04 | Bajo el mínimo | Solo un jugo natural ($2.500), sin extras | 422. No se guarda el pedido |
| T05 | Bloque inválido  | Entrega a salón en el bloque C | 422. No se guarda el pedido |
| T06 | Seis productos | Un pedido con 6 productos | 400. No se guarda el pedido |
| T07 | Producto inexistente o inactivo  | Un producto que no está en el catálogo o que está desactivado | 404. No se guarda el pedido |
| T08 | Extra inexistente | Un extra con un código inventado | 404. No se guarda el pedido |
| T09 | Usuario y total desde el servidor | El cliente manda el `userId` de otra persona y un `total` modificado | Se guarda con el `userId` del token y el total calculado por el servidor |
| T10 | Sin token o rol ADMIN | Pedido sin token / pedido con token de administrador | 401 / 403 |
| T11 | Varias fallas a la vez | 6 productos, bloque C y subtotal bajo en el mismo pedido | Solo se informa el error de entrada (400), que es el primero del orden |

### 5.3 Cómo aplicamos TDD

Con cada escenario hacemos tres pasos:

1. **Red:** escribimos la prueba antes que el código (por ejemplo, T04) y comprobamos que falla.
2. **Green:** escribimos lo mínimo para que pase, por ejemplo, lanzar una excepción si el subtotal es menor a $3.500.
3. **Refactor:** con las pruebas en verde, ordenamos el código (por ejemplo, mover el cálculo a `PriceCalculator`) y revisamos que todo siga pasando.

Empezamos por el pedido válido (T01) y después pasamos a los rechazos, del más simple al más complejo.

### 5.4 Cómo comprobamos el orden y que una falla no deja cambios

- **No se crea el pedido:** en las pruebas con Mockito, en cada rechazo (T04 a T08) verificamos que `orderRepository.save()` nunca se llama. El repositorio se simula, así que estas pruebas no dependen de MongoDB; la conexión real con la base de datos la comprobamos aparte, con las pruebas funcionales en Swagger.
- **No se modifica el catálogo:** este caso de uso solo lee productos, así que verificamos que `productRepository.save()` tampoco se llama.
- **Se respeta el orden:** en T11 mandamos varios errores juntos y comprobamos que solo sale el primero de la lista de la sección 5.1.

### 5.5 Preguntas de revisión

**¿Cómo aportan las pruebas evidencia del cumplimiento de las reglas de negocio? ¿Qué se verifica con pruebas unitarias?**

Cada regla de negocio tiene al menos una prueba con datos conocidos y un resultado esperado que sale del contrato de la API. Si alguien cambia el código y rompe una regla, la prueba falla y nos damos cuenta. Así, cada escenario del caso (E02, E03, E4, E5, E06 y E14) queda cubierto, incluyendo el caso límite del subtotal de $3.500 (T03).

Con las pruebas unitarias (JUnit y Mockito, con repositorios simulados) revisamos cada clase por separado:

- Que el subtotal, la entrega y el total se calculen bien.
- Que el Service aplique sus reglas: producto activo, extra existente, bloque A o B y subtotal mínimo.
- Que ante una falla se lance la excepción correcta y no se guarde nada.
- Que el usuario y el total salgan del servidor y no de lo que mande el cliente.

Los códigos HTTP, el token y la conexión real con MongoDB se comprueban aparte, con pruebas de integración y funcionales.

**¿Garantizan las pruebas por sí solas la ausencia de errores?**

No. Una prueba solo demuestra que el sistema funciona en los casos que se nos ocurrió probar, y pueden existir otros que no imaginamos. Además, tener 80 % de cobertura solo dice cuántas líneas se ejecutaron, no que las pruebas revisen lo correcto. Una prueba incluso puede pasar y estar validando una regla mal entendida. Por eso las pruebas se complementan con revisión de código, análisis estático y validación con el equipo de Producto.

**¿Garantizan las pruebas por sí solas la ausencia de errores?**

No. Una prueba solo demuestra que el sistema funciona en los casos que se nos ocurrió probar, pero pueden existir casos que no imaginamos. Además, tener 80 % de cobertura solo indica cuántas líneas se ejecutaron, no que las pruebas verifiquen lo correcto. Una prueba también puede pasar y estar validando una regla que se puedo entender de la manera incorrecta. Por eso las pruebas se complementan con revisión de código y análisis estático.

## 6. Asignación del equipo de DevOps

### 6.1 Diagrama de despliegue

![Diagrama de despliegue](docs/images/DDespliegue.png)

*Figura 6. Diagrama de despliegue de University Food Hub.*

Este diagrama muestra dónde va a funcionar la aplicación una vez esté en línea y cómo se comunican sus partes. Lo organizamos en cuatro nodos:

| Elemento | Qué es | Qué tiene |
|---|---|---|
| Computador o celular del usuario | Dispositivo | El navegador o Swagger UI, desde donde se hacen las peticiones |
| Azure App Service | Servidor donde corre la API | Java 21 y el archivo `food-hub.jar` (nuestra aplicación Spring Boot) |
| MongoDB Atlas | Base de datos | Las colecciones `users`, `products` y `orders` |
| GitHub Actions | Servidor de integración | El pipeline que compila, prueba y despliega |

Las conexiones entre ellos son estas:

| De | A | Cómo se comunican |
|---|---|---|
| Usuario | Azure App Service | HTTPS, con peticiones REST en formato JSON |
| Azure App Service | MongoDB Atlas | Protocolo de MongoDB, por el puerto 27017 y con conexión cifrada |
| GitHub Actions | Azure App Service | Despliegue del `.jar` usando el perfil de publicación |

Los datos sensibles, como la cadena de conexión a MongoDB y la clave del JWT, no van escritos en el código. Los configuramos como variables de entorno en Azure, y el perfil de publicación lo guardamos como secreto en GitHub.

Decidimos separar el dispositivo, la aplicación y la base de datos para que cada parte se pueda cambiar o ampliar sin tocar las demás. Además, el usuario nunca habla directamente con la base de datos: todo pasa por la API, que es la que aplica las reglas y revisa los permisos.

### 6.2 Preguntas de revisión

**¿Cuáles son las etapas principales de un pipeline CI/CD y para qué sirve cada una?**

Un pipeline es una serie de pasos automáticos que se ejecutan cada vez que se integra código. Los nuestros son:

1. **Descargar el código:** trae la última versión de la rama `develop`.
2. **Compilar:** Maven construye el proyecto y avisa si hay errores o dependencias que faltan.
3. **Ejecutar las pruebas:** corren las pruebas unitarias para confirmar que las reglas de negocio siguen funcionando.
4. **Cobertura y análisis estático:** JaCoCo revisa que las pruebas cubran más del 80 % del código, y el análisis estático busca malas prácticas y posibles fallas de seguridad.
5. **Empaquetar:** se genera el `.jar`, que es el archivo listo para ejecutar.
6. **Desplegar:** se publica el `.jar` en Azure App Service.

Los pasos 1 al 5 son la integración continua (CI), que revisa que cada cambio esté bien. El paso 6 es el despliegue continuo (CD), que lo deja funcionando para los usuarios.

**¿Qué pasa si una prueba falla? ¿Se debe permitir el despliegue?**

Si una prueba falla, el pipeline se detiene ahí y los pasos siguientes no se ejecutan. No se debe desplegar, porque una prueba que falla indica que algo dejó de cumplirse, por ejemplo que ahora se aceptan pedidos por debajo del mínimo de $3.500. Si lo desplegáramos, ese error llegaría a los usuarios reales. Lo correcto es arreglar el código, volver a correr el pipeline y desplegar solo cuando todo salga en verde.

**¿Para qué sirven los logs y qué registraríamos para investigar un pedido fallido o un error de autenticación?**

Los logs son el diario de la aplicación: van guardando lo que pasa mientras funciona. Sirven para descubrir qué falló, cuándo y por qué, sin tener que adivinar ni reproducir el error.

- **Pedido fallido:** fecha y hora, el `userId`, la regla que falló (por ejemplo, "subtotal de $2.500 menor al mínimo") y el código HTTP que se devolvió.
- **Error de autenticación:** fecha y hora, el correo con el que intentaron entrar, el motivo real (el usuario no existe, la contraseña no coincide o el token venció) y la IP de origen.

También usamos niveles según la gravedad: `INFO` para lo normal (un pedido creado), `WARN` para rechazos esperados (una regla de negocio incumplida) y `ERROR` para fallas inesperadas (por ejemplo, que se caiga la base de datos).

**¿Qué información es útil registrar y cuál no se debe registrar nunca?**

| Sí registrar | No registrar |
|---|---|
| Id del pedido y del usuario | Contraseñas, ni en texto ni encriptadas |
| Código HTTP y regla que falló | El token JWT completo |
| Fecha, hora y nivel del log | La cadena de conexión a MongoDB o claves secretas |
| IP de origen en errores de login | Datos personales que no hagan falta |

**¿En qué se diferencia el log interno del mensaje que recibe el cliente?**

El log interno es para el equipo, así que lleva todo el detalle técnico: la excepción completa, dónde ocurrió y el motivo exacto. El mensaje al cliente tiene que ser corto, claro y sin datos sensibles. Por ejemplo, en un login fallido el log dice "la contraseña no coincide", pero el cliente solo recibe "Credenciales inválidas", para no darle pistas a quien intente adivinar cuentas. Con los errores inesperados pasa igual: el cliente ve "Error interno del servidor" y el detalle queda solo en el log.


## Implementación y evidencias

### Cómo ejecutar

Variables de entorno necesarias (sin sus valores):

| Variable | Para qué sirve |
|---|---|
| `MONGODB_URI` | Cadena de conexión a MongoDB Atlas |
| `JWT_SECRET` | Clave para firmar los tokens, en Base64 y de mínimo 44 caracteres |

Para correr la aplicación desde la raíz del proyecto, en PowerShell:

```powershell
$env:MONGODB_URI = "<cadena-de-conexion>"
$env:JWT_SECRET = "<secret-base64>"
mvn spring-boot:run
```

Para correr las pruebas y generar el reporte de JaCoCo:

```bash
mvn clean verify
```

| Recurso | URL |
|---|---|
| Swagger en producción | https://foodhub-julian-2026.azurewebsites.net/swagger-ui/index.html |
| Swagger en local | http://localhost:8080/swagger-ui/index.html |

### Estado de los casos de uso

| # | Caso de uso | Estado |
|---|---|---|
| 1 | Crear productos | Implementado |
| 2 | Hacer un pedido | Pendiente |
| 3 | Listar mis pedidos | Pendiente |
| 4 | Listar todos los pedidos | Pendiente |
| 5 | Iniciar sesión | Pendiente |

### 1. Crear productos

`POST /api/v1/products`, solo para el rol **ADMIN**. Se desarrolló en la rama `feature/crear-productos`.

Ejemplo de entrada:

```json
{
  "name": "Sopa del día",
  "price": 3500
}
```

Ejemplo de salida (201):

```json
{
  "id": "6710a1f2c3d4e5f607182930",
  "name": "Sopa del día",
  "price": 3500,
  "active": true
}
```

**Validaciones**

| Tipo | Regla | Dónde se hace |
|---|---|---|
| Entrada | El nombre es obligatorio y tiene entre 3 y 60 caracteres | `CreateProductRequest` con `@Valid` |
| Entrada | El precio es obligatorio y mayor a 0 | `CreateProductRequest` con `@Valid` |
| Negocio | No puede existir otro producto con el mismo nombre, sin importar mayúsculas | `ProductService` |
| Negocio | El producto nace con `active = true` | `ProductService` |

**Códigos de respuesta**

| Código | Cuándo | Quién lo genera |
|---|---|---|
| 201 | Producto creado | `ProductController` |
| 400 | Nombre o precio inválidos, o JSON mal formado | `GlobalExceptionHandler` |
| 401 | No hay token o es inválido | `SecurityConfig` |
| 403 | El token es de un CLIENTE | `SecurityConfig` |
| 409 | Ya existe un producto con ese nombre | `GlobalExceptionHandler` |


#### Pruebas funcionales

**201 – Producto creado** (token ADMIN):

![Crear producto 201](docs/images/swagger-crear-201.png)

**401 – Sin token:**

![Crear producto 401](docs/images/swagger-crear-401.png)

**409 – Nombre repetido** (mismo body del 201):

![Crear producto 409](docs/images/swagger-crear-409.png)

**400 – Datos inválidos** (`{"name":"ab","price":0}`):

![Crear producto 400](docs/images/swagger-crear-400.png)

**403 – Token de rol CLIENTE:**

![Crear producto 403](docs/images/swagger-crear-403.png)

**Cómo está organizado**

| Capa | Clases |
|---|---|
| Controller | `ProductController`, `CreateProductRequest`, `ProductResponse`, `ErrorResponse`, `ProductMapper`, `GlobalExceptionHandler` |
| Core | `ProductService`, `Product`, `ConflictException` |
| Persistence | `ProductRepository`, `ProductEntity`, `ProductEntityMapper` |
| Config | `SecurityConfig`, `JwtAuthenticationFilter`, `OpenApiConfig`, `MongoConfig` |

El controlador recibe el DTO y lo pasa al servicio, que valida el nombre repetido y guarda con el repositorio. Los mappers convierten entre el DTO, el modelo y el documento de MongoDB. La seguridad la hace un filtro que lee el token, saca el rol y deja que Spring Security decida si puede entrar.

**Logs**

- `INFO`: cuando se recibe la solicitud y cuando se crea el producto, con su id.
- `WARN`: cuando se rechaza por validación o por nombre repetido.

La excepción completa no se le muestra al cliente; solo recibe un mensaje corto.

**Evidencias**

- [ ] Pruebas unitarias en verde
- [ ] Reporte de JaCoCo
- [ ] Prueba funcional en Swagger (201, 400, 401, 403 y 409)
- [ ] Logs de la prueba funcional
