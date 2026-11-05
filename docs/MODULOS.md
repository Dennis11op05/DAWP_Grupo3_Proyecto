# Guía técnica de Nexo por módulos

## Estructura del curso

Cada solicitud sigue Controller, Service, Repository y Domain. Thymeleaf genera HTML usando datos del modelo y Bootstrap proporciona formularios, tablas, tarjetas, alertas, modales y menú adaptable. Las entidades usan JPA y Lombok, como los ejemplos de categorías y productos. MySQL se prepara mediante SQL y Hibernate valida las tablas.

## Usuarios y acceso

`Usuario`, `Rol` y la tabla de unión `usuario_rol` permiten varios roles por persona. `UsuarioService` guarda usuarios, valida correo y roles y protege al último administrador activo. Las contraseñas se almacenan con BCrypt; el formulario de edición no devuelve el hash ni la contraseña anterior.

`SeguridadConfig` protege rutas y configura el formulario de login, logout y CSRF. Las operaciones de escritura también tienen `@PreAuthorize` en los servicios. `UsuarioDetailsService` obtiene la cuenta y sus roles de la base. `CuentaVigenteFilter` comprueba que la cuenta siga activa y que los roles de la sesión sigan vigentes. La interfaz obtiene nombre y roles actuales desde `ModeloGlobal`.

Esta configuración completa la autenticación que faltaba en los archivos del curso entregados. Usa la configuración estándar de [Spring Security](https://docs.spring.io/spring-security/reference/servlet/configuration/java.html) y su [formulario de acceso](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html).

## Fuentes de pago

`FuentePagoService` implementa el mantenimiento del proveedor, código único y estado. Solo ADMIN puede modificar. Cuando existen lotes, la moneda queda protegida para evitar que los registros históricos se interpreten con otra moneda. Eliminar un proveedor con datos relacionados respeta las claves foráneas; desactivar conserva el historial.

## Importación y persistencia

`ArchivoCsv` analiza UTF-8, delimitadores y comillas. `ImportacionService` prepara la identidad, archivo y huella. `ImportacionPersistenciaService` valida todos los registros antes de guardar dentro de una transacción. La separación permite que, si una carga falla y se revierte, el servicio exterior registre un lote de error en una transacción nueva del repositorio.

Las filas internas incorporan `referenciaLiquidacion` para delimitar su ámbito. Las entidades conservan su lote y fuente. `Liquidacion` agrupa los `MovimientoProveedor`. Los importes son BigDecimal con escala decimal; las filas originales no se editan silenciosamente para cuadrar resultados.

## Conciliación

`ConciliacionService.iniciar` crea una conciliación por liquidación y calcula sus discrepancias. `vincular` comprueba fuente, liquidación, referencia, tipo, moneda y unicidad. El registro del vínculo conserva usuario, criterio y fecha. Las restricciones únicas de la base impiden reutilizar una operación o movimiento.

El servicio identifica operaciones sin movimiento, movimientos sin operación, importes distintos, referencias originales distintas y netos que no coinciden con el total reportado. La diferencia de total y las discrepancias individuales se muestran separadas para no confundir un neto correcto con registros completamente conciliados.

Los cambios de una conciliación bloquean su fila durante la transacción. Después de enviar la primera propuesta no se recalculan vínculos ni se borran diferencias: se protege el historial que ya existe.

## Resoluciones y cierre

Una propuesta contiene clasificación, explicación y proponente. SUPERVISOR puede aprobar o rechazar, pero no su propia propuesta. Al rechazar se exige un motivo y se permite una nueva propuesta sin sobrescribir la anterior. Aprobar cambia el estado de la diferencia; no altera importes de origen.

El cierre requiere que todas las diferencias estén resueltas. Guarda autorizante y fecha. Los servicios rechazan intentos de modificar una conciliación cerrada. La vista histórica conserva las operaciones, movimientos, vínculos, diferencias y decisiones.

## Consultas y pruebas

El Dashboard resume datos de la base según los permisos. Las consultas de conciliaciones filtran por referencia y estado y pueden limpiar sus filtros. Los reportes de conciliaciones y diferencias permiten abrir sus detalles. El historial de importaciones está disponible a ADMIN y ANALISTA.

`NexoIntegrationTest` verifica escenarios de negocio con H2 o MySQL, autenticación y formularios mediante MockMvc. No modifica la base normal: solo utiliza el perfil y las variables de pruebas. El workflow permite repetir las comprobaciones en GitHub.

## Para preparar la defensa

Cada integrante debe recorrer el módulo que trabaje, seguir una solicitud desde la vista hasta la base y explicar una validación concreta. Conviene discutir por qué no se usa Double para dinero, por qué las validaciones del navegador no sustituyen las del servidor, qué garantiza una transacción y cómo se preserva el historial al rechazar una propuesta. Las respuestas y los aportes deben ser propios del equipo.
