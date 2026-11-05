# Traspaso de Nexo al siguiente integrante

Estado comprobado el 5 de noviembre de 2026. Este documento permite continuar la implementación existente y preparar la entrega del Avance 2. Se debe revisar y probar el proyecto; no comenzar nuevamente desde cero ni asumir que las pruebas sustituyen la defensa y colaboración del grupo.

## Obtener la versión correcta

Repositorio: https://github.com/Dennis11op05/DAWP_Grupo3_Proyecto

Rama de la aplicación: `feature/avance2-nexo`

Último commit de la aplicación: `1daaec66caee462525c62c308defdd825a8a7de4`

Pull request en borrador: https://github.com/Dennis11op05/DAWP_Grupo3_Proyecto/pull/1

CI comprobado con resultado Success: https://github.com/Dennis11op05/DAWP_Grupo3_Proyecto/actions/runs/42071849722

IMPORTANTE: main contiene únicamente el commit inicial de organización, con .gitignore y CONTRIBUTING.md. La aplicación está en la rama feature y todavía no está integrada a main.

```powershell
git clone --branch feature/avance2-nexo https://github.com/Dennis11op05/DAWP_Grupo3_Proyecto.git
cd DAWP_Grupo3_Proyecto
git switch -c feature/revision-avance2
```

Si ya existe un clon local, comprobar el estado de sus archivos antes de cambiar de rama. No sobrescribir cambios de otro integrante. Configurar la identidad Git propia del compañero, sin reutilizar la identidad del autor anterior ni cambiar los autores o fechas del historial.

Este archivo de traspaso se generó después del commit indicado. Se puede compartir por separado junto con el enlace a la rama; no forma parte de ese commit.

## Qué está implementado

Nexo es un sistema de conciliación de operaciones internas y liquidaciones de un proveedor de pagos. Hay implementación para HU-01 a HU-21, organizada con Java 21, Maven, Spring Boot 4.1.1, Hibernate/JPA, MySQL 8, Thymeleaf, Bootstrap 5.3.8, Jakarta Validation y Lombok.

- Inicio y cierre de sesión, contraseñas BCrypt y protección CSRF.
- Registro y edición de usuarios, varios roles por cuenta, activación y desactivación, protección del último administrador e invalidación de sesiones con permisos obsoletos.
- Roles ADMIN, ANALISTA y SUPERVISOR con permisos de servidor y navegación diferenciada. ADMIN no concede automáticamente SUPERVISOR.
- CRUD de fuentes de pago, código único, activación y desactivación. La moneda no se cambia después de existir importaciones y las fuentes inactivas no admiten cargas.
- Carga de CSV internos y del proveedor: UTF-8, encabezados, fechas, importes, moneda, comillas, duplicados y SHA-256. Una carga guarda todos sus registros o registra un lote de error sin importación parcial.
- Historial de importaciones con archivo, origen, usuario, fecha, cantidad y resultado.
- Liquidaciones con sus movimientos y totales.
- Inicio de una conciliación por liquidación y vínculos por referencia y tipo, con control de fuente, moneda y liquidación. Los registros no se pueden vincular repetidamente.
- Diferencias de importe, registros sin correspondencia, referencias originales distintas y total reportado inconsistente.
- Propuestas de resolución con explicación; aprobación o rechazo por otro usuario supervisor; historial conservado y prohibición de autoaprobarse.
- Cierre solo cuando no quedan diferencias pendientes o en revisión. El cierre guarda autorizante y fecha y protege los datos posteriores.
- Dashboard con datos reales, reportes de conciliaciones y diferencias, búsqueda por referencia, filtros de estado y consulta de conciliaciones cerradas.
- README, esquema SQL de doce tablas, CSV sintéticos, guion de demostración, explicación de módulos, backlog, plantilla de PR y workflow de CI.

Las entidades JPA y la tabla de unión usuario_rol representan el modelo preliminar; UsuarioRol se mapea mediante @ManyToMany y @JoinTable, sin una clase JPA adicional.

## Arquitectura y archivos de entrada

El paquete raíz es `com.mycompany.proyecto_desarrollo_app_web` dentro de `src/main/java`. Contiene `controller`, `service`, `repository` y `domain`, además de la clase principal, `SeguridadConfig` e `Inicializador`. Las vistas están en `src/main/resources/templates`; los estilos y JavaScript, en `static`.

Leer primero:

1. README.md
2. docs/MODULOS.md
3. docs/FORMATOS_CSV.md
4. docs/BACKLOG.md
5. docs/DEMOSTRACION.md
6. docs/VALIDACION.md
7. docs/PROTOTIPO.md y CONTRIBUTING.md

## Prototipo y estilo del curso

Figma: https://www.figma.com/design/Mulmt4pEdzP9IwgdhhC1TG/Sin-t%C3%ADtulo?node-id=0-1

Se tomó como referencia el menú lateral azul oscuro, encabezado claro, fondo gris, tarjetas, formularios y botones Bootstrap. Fuente de pago se contrastó con su marco específico. Las demás vistas reutilizan ese lenguaje visual; todavía conviene compararlas una por una con el prototipo. No se afirma equivalencia píxel a píxel.

La estructura sigue los ejemplos de categorías y productos del curso: Controllers MVC, Services transaccionales, JpaRepository, entidades JPA y fragmentos Thymeleaf. En el equipo de Kenlly, los ejemplos están en la carpeta de la asignatura, principalmente Clases/Clase 1/tienda_vm y el caso de estudio de libros. El compañero deberá usar sus propios materiales o solicitar esos ejemplos para contrastar el estilo.

## Validación realizada

- 14 pruebas locales con H2: cero fallos y errores.
- Las mismas 14 con MySQL 8.0.46: cero fallos y errores, usando creaTablas.sql y ddl-auto=validate.
- GitHub Actions del commit indicado terminó Success. Su workflow ejecuta pruebas con H2 y MySQL.
- Recorrido real del navegador con MySQL: login y logout, fuente, carga de 5 registros internos y 6 movimientos del proveedor, conciliación y vínculos de LIQ-001, cuatro diferencias esperadas y una propuesta aprobada desde otra cuenta supervisora.
- Maven generó correctamente el JAR ejecutable.

La instancia MySQL utilizada para comprobar el sistema es temporal y está aislada de las bases existentes del equipo. Esa instancia y sus cuentas sintéticas no son la configuración definitiva del grupo ni algo que deba copiarse a otro equipo.

## Qué falta y qué debe revisar el compañero

### Prioridad 1 Configurar y reproducir el entorno

Preparar MySQL en su equipo, ejecutar todo `src/main/resources/creaTablas.sql` y crear una cuenta para la aplicación. Copiar `application-local.properties.example` a `application-local.properties` en la raíz y completar conexión, correo y contraseña del primer administrador. El archivo privado está ignorado por Git.

La cuenta inicial se crea solo si la base no contiene usuarios. No hay una contraseña predeterminada de uso normal. Después, el administrador registra las cuentas de analista y supervisor.

Usar Java 21. Ejecutar `scripts/iniciar.ps1`, el comando Maven del README con `-Dspring-boot.run.profiles=local` o el JAR con `--spring.profiles.active=local`.

AJUSTE IDENTIFICADO: `nbactions.xml` todavía ejecuta spring-boot:run sin activar local. Si se desea usar el botón Ejecutar de NetBeans con application-local.properties, agregar el perfil local a esa acción o configurar el IDE para activarlo. El script y los comandos del README ya lo activan.

### Prioridad 2 Revisar aceptación y diseño

Repetir la demostración completa en la base definitiva y comprobar que los datos se conservan tras reiniciar. Auditar los criterios de aceptación de cada historia contra el Avance 1, incluyendo entradas inválidas, permisos, duplicados, resoluciones y protección de cierres. Que una historia tenga código no certifica por sí solo todos sus criterios ni la evaluación del docente.

Comparar las vistas restantes con los marcos de Figma y ajustar las diferencias justificadamente, manteniendo Bootstrap y el estilo del curso. No reemplazar el proyecto por otro framework o una interfaz estática.

### Prioridad 3 Revisiones y GitHub

El PR #1 continúa como borrador, sin revisiones al comprobar este traspaso. Los otros integrantes deben realizar aportes propios, revisar los cambios y registrar evidencia real antes de integrar a main. No hacer una integración automática sin la revisión del equipo.

CI pasó, pero muestra avisos por actions/checkout@v4, actions/setup-java@v4 y actions/upload-artifact@v4, que utilizan Node 20 y están siendo ejecutadas con Node 24. Actualizar a versiones oficialmente compatibles y verificar nuevamente el workflow. No es un fallo actual de las pruebas.

### Prioridad 4 Entrega

Preparar la grabación funcional, defensa, consultas técnicas propias y enlaces a commits, ramas, PR y revisiones. La rúbrica pide aportes trazables del 100% de los integrantes. Si realmente se trabajó en una sola computadora, explicarlo con honestidad y consultar al docente qué evidencia admite; un solo autor en GitHub no demuestra por sí mismo las contribuciones de todos.

## Reglas que no deben perderse

- La operación interna incluye referencia de liquidación para no mezclar ámbitos al detectar faltantes.
- COBRO suma; DEVOLUCION y COMISION restan. Se usa BigDecimal, no Double.
- La diferencia de total es neto del proveedor menos total reportado. Puede ser cero y aun así existir diferencias individuales.
- Completar los vínculos antes de enviar la primera resolución: después quedan protegidos para conservar el historial.
- Las resoluciones no cambian silenciosamente los datos de origen y el rechazo no elimina propuestas anteriores.
- Un supervisor no evalúa su propia propuesta y el cierre exige que no queden pendientes.
- No subir application-local.properties, .env, .cache, target, claves ni datos privados. Para compartir el proyecto, preferir el clon de GitHub; copiar toda la carpeta local incluiría archivos temporales que no se necesitan.
- Las pruebas pueden limpiar sus datos de trabajo: las variables NEXO_TEST_DB_* deben apuntar exclusivamente a una base desechable de pruebas, nunca a la base definitiva.

## Prompt listo para el Codex del compañero

Eres el asistente que continuará Nexo, proyecto académico de Java y Spring Boot del Grupo 03. Continúa la implementación existente; no la reconstruyas desde cero.

Repositorio: https://github.com/Dennis11op05/DAWP_Grupo3_Proyecto
Rama con la aplicación: feature/avance2-nexo
Commit de referencia: 1daaec66caee462525c62c308defdd825a8a7de4
PR en borrador: https://github.com/Dennis11op05/DAWP_Grupo3_Proyecto/pull/1
Prototipo: https://www.figma.com/design/Mulmt4pEdzP9IwgdhhC1TG/Sin-t%C3%ADtulo?node-id=0-1
CI de referencia exitoso: https://github.com/Dennis11op05/DAWP_Grupo3_Proyecto/actions/runs/42071849722

Primero comprueba la rama y el estado del repositorio. main aún no contiene la aplicación. Trabaja en una rama propia basada en feature/avance2-nexo y conserva los cambios del compañero.

Lee README.md, docs/MODULOS.md, docs/FORMATOS_CSV.md, docs/BACKLOG.md, docs/DEMOSTRACION.md, docs/VALIDACION.md, docs/PROTOTIPO.md y CONTRIBUTING.md. Revisa AGENTS.md si existe. Identifica los ejemplos del curso que estén disponibles en este equipo antes de cambiar convenciones.

La aplicación implementa usuarios y varios roles, login y logout, fuente de pago, importación CSV atómica, liquidaciones y movimientos, conciliación por referencias, diferencias, propuestas y revisión de resoluciones, cierre protegido, dashboard, reportes e históricos. Existe código para HU-01 a HU-21. Está organizada en Controller, Service, Repository y Domain, con Java 21, Spring Boot 4.1.1, JPA/Hibernate, MySQL 8, Thymeleaf y Bootstrap 5.3.8.

Pasaron 14 pruebas con H2, las mismas con MySQL 8.0.46 y el workflow de GitHub. No des por certificadas las historias sin auditar sus criterios ni afirmes que todos los integrantes ya participaron.

Mis prioridades son:
1. Preparar y ejecutar el proyecto en este equipo con una base MySQL propia y configuración privada, siguiendo el README. No reutilices credenciales temporales del equipo anterior ni pidas que publique contraseñas en el chat.
2. Revisar nbactions.xml: si se ejecuta desde NetBeans con application-local.properties, la acción debe activar local, como ya hacen el script y los comandos del README.
3. Ejecutar las pruebas y recorrer docs/DEMOSTRACION.md. Comprobar persistencia después de reiniciar, permisos, importaciones inválidas, vínculos únicos, diferencias y cierre. No apuntar las pruebas que limpian datos a la base definitiva.
4. Auditar los criterios de HU-01 a HU-21 y comparar las pantallas con Figma. Corregir problemas reales, conservando Bootstrap y las convenciones vistas en clase.
5. Actualizar las acciones de CI obsoletas a versiones oficialmente compatibles y comprobar el resultado remoto.
6. Explicar los cambios realizados y preparar un PR con mi identidad Git. Mantener la integración a main pendiente de revisión real del equipo; no inventar commits, autores, fechas ni aprobaciones de otros integrantes.

Conserva BigDecimal, la separación de registros por fuente y liquidación, el rechazo completo de CSV inválidos, los historiales, la prohibición de autoaprobarse y el bloqueo de cambios después del cierre. Los datos del prototipo no deben sustituir datos reales guardados por la aplicación.

Avanza por módulos, explica cada cambio en español y entrega al final qué corregiste, qué verificaste y qué sigue pendiente para la entrega académica.
