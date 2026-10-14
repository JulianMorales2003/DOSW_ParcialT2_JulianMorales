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


Una cosa que se debe evitar es el cambio de datos, pues si algo falla no debe crear ningun peddido o seguir con el flujo y no modificar datos, y se guyarda en memoria un pedido al estar completo y correcto. 


## Punto 2 Preguntas para la revisión con Arquitectura: 

### Diagramas: 

#### a) Diagrama de componentes general:

![Diagrama de componentes general](docs/images/DComponentesGeneral.png)

#### b) Diagrama de componentes específico siguiendo el esquema de MVC:

![Diagrama de componentes específico MVC](docs/images/DComponentesEspecifico.png)

#### c) Diagrama de clases: modelos de dominio:


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

Los escenarios son del caso de uso **Hacer un pedido** (`POST /api/v1/orders`) y se construyen con TDD (Red, Green, Refactor).

### 5.1 Orden de las validaciones

Si un pedido falla por varias razones a la vez, la API informa **solo el primer error** según este orden:

1. Token y rol (401 / 403)
2. Validación de entrada: entre 1 y 5 productos, `delivery.type` válido, y bloque y salón presentes si es ENTREGA_SALON (400)
3. Productos y extras existentes, y productos activos (404)
4. Bloque A o B en ENTREGA_SALON (422)
5. Subtotal mínimo de $3.500 (422)
6. Cálculo del total y guardado del pedido como CONFIRMADO (201)

### 5.2 Escenarios de prueba

| ID | Escenario | Datos de entrada | Resultado esperado |
|---|---|---|---|
| T01 | Pedido válido  | Una bandeja universitaria con porción extra de proteína y queso, para llevar | 201. Subtotal $11.300, entrega $300, total $11.600, estado CONFIRMADO |
| T02 | Pedido válido con entrega a salón | Un sándwich con pan integral, entregado en el salón 101 del bloque A | 201. Subtotal $5.500, entrega $1.000, total $6.500 |
| T03 | Subtotal justo en el mínimo | Una sopa del día ($3.500), para consumir en la cafetería | 201, porque el mínimo es inclusivo |
| T04 | Bajo el mínimo  | Solo un jugo natural ($2.500), sin extras | 422. No se guarda el pedido |
| T05 | Bloque inválido  | Entrega a salón en el bloque C | 422. No se guarda el pedido |
| T06 | Seis productos | Un pedido con 6 productos | 400. No se guarda el pedido |
| T07 | Producto inexistente o inactivo  | Un producto que no existe en el catálogo o que está desactivado | 404. No se guarda el pedido |
| T08 | Extra inexistente | Un extra que no existe (por ejemplo, un código inventado) | 404. No se guarda el pedido |
| T09 | Usuario y total desde el servidor | Un pedido donde el cliente manda el `userId` de otra persona y un `total` modificado | Se guarda con el `userId` del token y el total calculado por el servidor |
| T10 | Sin token o rol ADMIN | Pedido sin token / pedido con token de administrador | 401 / 403 |
| T11 | Varias fallas a la vez | 6 productos, bloque C y subtotal bajo en el mismo pedido | Solo se informa el error de entrada (400), que es el primero del orden |

### 5.3 TDD (Red, Green, Refactor)

Para cada escenario seguimos tres pasos:

1. **Red:** escribimos la prueba antes que el código por ejemmplo T04 y comprobamos que falla.
2. **Green:** escribimos lo mínimo para que pase (`if (subtotal < 3500) lanzar excepción`).
3. **Refactor:** con las pruebas en verde, ordenamos el código (por ejemplo, mover el cálculo a `PriceCalculator`) y verificamos que todas sigan pasando.

Empezamos por el pedido válido T01 y luego pasamos a los rechazos, del más simple al más complejo.

### 5.4 Cómo comprobamos el orden y que una falla no deja cambios

- **No se crea el pedido:** en las pruebas con Mockito, en cada rechazo (T04 a T08) verificamos que `orderRepository.save()` nunca se llama (`verify(..., never())`).
- **No se modifica el catálogo:** este caso de uso solo lee productos, así que verificamos que `productRepository.save()` tampoco se llama.
- **Se respeta el orden:** T11 manda varios errores juntos y comprobamos que solo sale el primero del orden de la sección 5.1.

### 5.5 Preguntas de revisión

**¿Cómo aportan las pruebas evidencia del cumplimiento de las reglas de negocio? ¿Qué se verifica con pruebas unitarias?**

Cada regla de negocio tiene al menos una prueba con datos conocidos y un resultado esperado que sale del contrato. Si alguien cambia el código y rompe la regla, la prueba falla. Así cada escenario del caso (E02, E03, E4, E5, E06, E14) queda cubierto por una prueba, incluyendo el caso límite del subtotal de $3.500 (T03).

Con **pruebas unitarias** JUnit verificamos cada clase por separado:
- El cálculo del subtotal, la entrega y el total.
- Las reglas del Service: producto activo, extra existente, bloque A o B y subtotal mínimo.
- Que ante una falla se lance la excepción correcta y no se guarde nada.
- Que el usuario y el total vengan del servidor y no del body.

Los códigos HTTP, el token y la conexión real con MongoDB se prueban aparte, con pruebas de integración.

**¿Garantizan las pruebas por sí solas la ausencia de errores?**

No. Una prueba solo demuestra que el sistema funciona en los casos que se nos ocurrió probar, pero pueden existir casos que no imaginamos. Además, tener 80 % de cobertura solo indica cuántas líneas se ejecutaron, no que las pruebas verifiquen lo correcto. Una prueba también puede pasar y estar validando una regla que se puedo entender de la manera incorrecta. Por eso las pruebas se complementan con revisión de código y análisis estático.
