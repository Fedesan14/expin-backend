# Expin Backend - Agent Guide

Expin es una SuperApp modular para gestionar funcionalidades financieras y sociales dentro de un backend monolitico. El proyecto debe crecer como monolito modular usando MVC con vertical slicing por funcionalidad: cada modulo agrupa sus propios controllers, requests/responses, servicios y datos.

## Stack actual

- Java 25.
- Spring Boot 4.1.0.
- Maven Wrapper incluido en el repo.
- Spring Web MVC para exponer endpoints.
- Spring Data JPA para persistencia.
- H2 y MySQL disponibles como runtimes de base de datos por JDBC.
- Lombok disponible, con annotation processing configurado.

Comandos principales en Windows PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Regla de ejecucion para agentes: no compilar, levantar ni ejecutar tests del proyecto automaticamente. El usuario se encarga de hacerlo manualmente. Cuando un cambio lo amerite, indicar que conviene probar o que comando deberia ejecutar el usuario, pero no correrlo desde el agente.

## Estructura base

El paquete raiz actual es:

```text
com.fedesan14.expin_backend
```

Las nuevas funcionalidades deben ubicarse bajo ese paquete raiz, separadas por slice vertical:

```text
src/main/java/com/fedesan14/expin_backend/
  auth/
  personal_finances/
  events/
```

La estructura recomendada dentro de cada slice es MVC local al modulo:

```text
<slice>/
  controller/       # interfaces/implementaciones HTTP, requests y responses del slice
    interfaces/
    implementations/
    requests/
    responses/
  service/          # interfaces/implementaciones de casos de uso, reglas de aplicacion y transacciones
  data/
    model/          # entidades, value objects, enums y reglas propias del modelo
    repository/     # repositorios JPA y consultas de persistencia del slice
```

No crear carpetas globales como `controllers`, `services`, `repositories` o `dto` para nueva logica de negocio. MVC se aplica dentro de cada slice, no como capas globales de toda la aplicacion. Si una clase pertenece claramente a un modulo, vive dentro del modulo.

## Slices iniciales

### auth

Responsable de identidad, autenticacion, autorizacion y datos minimos de usuario.

Ejemplos de responsabilidades:

- Registro e inicio de sesion.
- Identidad del usuario autenticado.
- Roles, permisos o claims si aparecen.
- Integracion futura con tokens o sesiones.

Otros slices no deben implementar reglas de autenticacion propias. Cuando un endpoint necesite el usuario autenticado, obtenerlo en el controller con `@AuthenticationPrincipal User currentUser` y pasarlo al service como contexto explicito del caso de uso. Los slices no deben leer directamente `SecurityContextHolder` ni detalles del token Bearer.

Si otro slice necesita consultar usuarios por ID, debe depender de `auth/service/UserService` y no de `UserRepository`.

### personal_finances

Responsable del historial personal de gastos e ingresos monetarios.

Conceptos iniciales:

- Movimiento financiero: gasto o ingreso.
- Categoria y subcategoria.
- Cuenta o perfil financiero del usuario, si se necesita diferenciar del usuario auth.
- Filtros, totales, periodos y reportes del historial personal.

Regla de dominio importante: las categorias pueden formar jerarquias. Por ejemplo, `Carniceria` y `Verduleria` pueden ser subcategorias de `Comida`.

### events

Responsable del divisor de gastos por eventos compartidos.

Conceptos iniciales:

- Evento.
- Participante.
- Gasto registrado dentro del evento.
- Distribucion o liquidacion de deudas entre participantes.

Los gastos de eventos no deben mezclarse automaticamente con `personal_finances` salvo que exista un caso de uso explicito que sincronice o registre ese impacto.

La implementacion actual del slice `events` expone CRUD de eventos en `/events` y CRUD de gastos en `/events/{eventId}/expenses`, sin prefijo `/api`. Todos esos endpoints son privados y dependen de Bearer auth. El owner se toma desde `@AuthenticationPrincipal`, queda agregado automaticamente como participante y el `shareLink` se genera como path relativo con forma `/events/invite/<token>`.

Reglas vigentes de eventos:

- Crear/listar/obtener evento usa el usuario autenticado como contexto.
- La lista de participantes en create/update es opcional; el owner siempre queda incluido automaticamente y otros participantes pueden agregarse luego.
- Listar devuelve solo eventos donde el usuario autenticado es owner o participante.
- Obtener evento exige que el usuario autenticado sea owner o participante.
- `POST /events/invite/{token}` usa el token del `shareLink` generado para agregar al usuario autenticado como participante del evento y devuelve el detalle del evento. La operacion es idempotente si el usuario ya participa.
- Editar y eliminar evento exige ser owner.
- Editar evento no regenera `shareLink`.
- La generacion de `shareLink` vive en `EventShareLinkGenerator`; `EventService` no debe conocer los detalles del token aleatorio.
- No se puede remover de participantes a alguien que ya figura como pagador de un gasto existente.

Reglas vigentes de gastos de eventos:

- Crear, obtener, editar o eliminar gastos exige que el usuario autenticado sea participante del evento.
- `paidByParticipantId` debe pertenecer al mismo evento del path.
- `amount` debe ser mayor a cero.
- Todavia no se implementa calculo de deuda/liquidacion entre participantes.

## Reglas de arquitectura

- Mantener el acoplamiento entre slices bajo y explicito.
- Preferir dependencias hacia contratos pequenos antes que usar entidades internas de otro slice.
- Evitar que un slice lea o escriba tablas de otro slice directamente.
- Las reglas de negocio deben estar en `data/model` o `service`, no en controllers.
- Los controllers solo traducen HTTP hacia servicios y devuelven respuestas.
- Los DTOs de controller no deben filtrarse como modelo de dominio.
- Las entidades JPA viven en `data/model` salvo que un slice necesite una separacion explicita entre modelo de dominio y persistencia.
- Para operaciones de escritura, definir limites transaccionales en `service`.
- Los IDs de entidades de dominio deben ser `UUID` generados aleatoriamente salvo decision explicita contraria.
- Mantener nombres ubicuos del negocio en ingles o espanol de forma consistente dentro del slice. Si se empieza un slice con ingles, seguir en ingles.

## Interfaces e implementaciones

Controllers y services deben definirse siempre como una interfaz mas una implementacion.

Convencion de nombres:

```text
controller/
  ExpenseController.java
  ExpenseControllerImpl.java
  CreateExpenseRequest.java
  ExpenseResponse.java

service/
  ExpenseService.java
  ExpenseServiceImpl.java

data/
  model/
    Expense.java
  repository/
    ExpenseRepository.java
```

Reglas para controllers:

- La interfaz del controller es el contrato publico del endpoint.
- Toda la documentacion de Swagger/OpenAPI debe estar en la interfaz del controller.
- La implementacion del controller debe implementar esa interfaz y contener solo adaptacion HTTP minima, validacion de borde y delegacion al service.
- Para respuestas con status distinto de 200, preferir `@ResponseStatus` en el contrato del controller antes que `ResponseEntity`, salvo que sea necesario controlar headers u otro detalle HTTP.
- Evitar duplicar anotaciones de documentacion Swagger entre interfaz e implementacion.
- Las anotaciones de mapping HTTP pueden vivir en la interfaz si Spring las resuelve correctamente en el proyecto; si se detecta un problema practico, mantener la documentacion Swagger en la interfaz y ubicar el mapping minimo necesario en la implementacion.
- Para slices con mas de unas pocas clases de controller, separar en subpaquetes `interfaces`, `implementations`, `requests` y `responses`.

Reglas para services:

- La interfaz del service define los casos de uso disponibles para el slice.
- La implementacion del service contiene la orquestacion, transacciones y reglas de aplicacion.
- Controllers deben depender de interfaces de service, no de implementaciones concretas.
- Otros slices solo pueden depender de una interfaz de service de otro slice si el caso de uso esta explicitamente permitido y documentado.

## Convenciones de implementacion

- Usar paquetes y clases pequenas con responsabilidad clara.
- Validar input en el borde HTTP y reforzar invariantes importantes en dominio.
- Crear request/response DTOs especificos por caso de uso cuando la forma publica no sea igual al modelo interno. Ubicarlos dentro de `controller`, preferentemente como clases directas del paquete mientras el slice sea chico.
- Si los requests/responses crecen demasiado, se puede crear un subpaquete dentro de `controller`, por ejemplo `controller/request` y `controller/response`; no moverlos fuera del controller.
- Preferir IDs y ownership explicitos en los casos de uso: un usuario solo debe operar sobre sus propios recursos salvo regla contraria.
- No introducir abstracciones compartidas hasta que al menos dos slices realmente las necesiten.
- Si aparece codigo comun tecnico, ubicarlo en un paquete `shared` o `common` solo cuando sea transversal y estable.
- Los manejadores de excepciones transversales deben vivir en el slice `common`.
- No crear dependencias circulares entre slices.

## Testing

Cada cambio funcional debe incluir o ajustar tests proporcionales al riesgo:

- Preferir tests de integracion para flujos funcionales completos.
- Usar tests unitarios solo para logica especifica, pequena y aislada que convenga probar sin levantar el contexto.
- Controller/security/service: tests de integracion HTTP cuando cambie el contrato expuesto o el flujo de autenticacion/autorizacion.
- Data repository: tests de integracion JPA cuando el mapping o query tenga logica relevante.
- Usar tests parametrizados cuando varios casos prueban el mismo comportamiento y solo cambian los parametros.
- Centralizar configuracion y utilidades comunes de tests de integracion en una clase abstracta reutilizable.
- Crear clases `DataMock` por slice, por ejemplo `AuthDataMock`, para alojar casos de prueba repetibles, datos base, generacion de datos unicos y fuentes de parametros. En tests HTTP, los `DataMock` deben crear los DTOs reales que se envian en la request cuando existan. Los tests deben leer de forma declarativa y no construir sufijos, emails o usernames a mano.

Antes de cerrar una tarea, indicar si el usuario deberia correr:

```powershell
.\mvnw.cmd test
```

No ejecutar el comando desde el agente.

## Configuracion

`src/main/resources/application.yaml` contiene la configuracion base compartida. No duplicar archivos `application-<environment>.yaml` si la configuracion es igual y puede resolverse por variables de entorno.

El perfil activo de Spring debe levantarse desde variable de entorno, con `local` como default:

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
```

Los ambientes principales son `local`, `development` y `production`. Todos usan MySQL.

El perfil `local` tiene su override en `src/main/resources/application-local.yaml`. En local, solo `DATABASE_USERNAME`, `DATABASE_PASSWORD` y opcionalmente `JWT_SECRET` se leen por variable de entorno; host, puerto y nombre de base quedan fijos en la URL local:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/expin?createDatabaseIfNotExist=true
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
```

Para ambientes no locales, las credenciales y ubicacion de la base no deben escribirse en el repo; se leen desde variables de entorno:

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DATABASE_HOST}:${DATABASE_PORT}/expin?createDatabaseIfNotExist=true
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
```

Variables para ejecutar con MySQL fuera de local:

- `DATABASE_HOST`
- `DATABASE_PORT`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS`, lista separada por comas con los origins HTTP permitidos para navegadores, por ejemplo `https://app.expin.com`.
- `DDL_AUTO`, opcional; si no se define, Spring usa `validate`.

Variables para ejecutar con MySQL local:

- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`, opcional; si no se define, Spring usa un secreto default solo apto para desarrollo local.
- `CORS_ALLOWED_ORIGINS`, opcional; si no se define, Spring permite `http://localhost:5173` y `http://127.0.0.1:5173`.
- `DDL_AUTO`, opcional; si no se define, Spring usa `validate`.

Los tests usan H2 por JDBC desde `src/test/resources/application.yaml`, con modo de compatibilidad MySQL, y JPA crea el esquema con `ddl-auto: create-drop`. No hacer que los tests dependan de una base MySQL externa salvo que sea un test de integracion explicitamente separado.

## Seguridad

El slice `auth` expone endpoints publicos para identidad:

- `POST /auth/signup`: registra usuario.
- `POST /auth/login`: inicia sesion con `Authorization: Basic ...`.

El login acepta como identificador `username` o `email` junto con password. La respuesta incluye un JWT de sesion de 15 minutos y un refresh token de 30 minutos. Los endpoints no publicos deben autenticarse con `Authorization: Bearer <sessionToken>` usando el `BearerAuthenticationProvider`.

La configuracion CORS vive en `auth/security/config/SecurityConfig`. El bean `CorsConfigurationSource` debe estar activado en el `SecurityFilterChain` con `cors(...)`, y las preflight `OPTIONS /**` deben quedar permitidas antes de exigir autenticacion Bearer.

El modelo inicial de identidad usa:

- `User`, implementando `UserDetails`, con `username` y `password`.
- `Profile`, asociado al usuario, con `email`.
- `username` y `email` no pueden duplicarse.
- `username` tiene un maximo de 30 caracteres.

Las clases de soporte de seguridad del slice `auth` se separan por responsabilidad dentro de `auth/security`: `basic` para credenciales Basic, `bearer` para autenticacion Bearer, `jwt` para emision/validacion de tokens y `config` para la configuracion de Spring Security. Los errores transversales de autenticacion/autorizacion deben manejarse desde `common` si no son especificos de un endpoint del slice.

Cuando se agregue configuracion por ambiente, documentar:

- Nombre de la variable o propiedad.
- Valor esperado.
- Perfil donde aplica.
- Si tiene default seguro para desarrollo local.

## Cuando modificar esta guia

Actualizar este archivo cuando se tome una decision durable sobre:

- Estructura de paquetes.
- Nombre o responsabilidad de un slice.
- Convenciones de DTOs, errores, seguridad o persistencia.
- Comandos obligatorios de build/test.
- Integraciones externas.

La guia debe reflejar el codigo real del repo, no aspiraciones que no se implementaron.
