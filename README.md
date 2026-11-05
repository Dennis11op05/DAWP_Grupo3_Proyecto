# Nexo

Aplicación web del Grupo 03 para conciliar operaciones internas con liquidaciones de un proveedor de pagos. Implementa Java 21, Spring Boot 4.1.1, Hibernate/JPA, MySQL 8, Thymeleaf y Bootstrap 5.3.8, siguiendo la organización del proyecto tienda suministrado en clase.

## Estado del Avance 2

Están codificadas las 21 historias del Avance 1: acceso, usuarios y roles, fuentes de pago, importación CSV, liquidaciones, conciliación, diferencias, resoluciones, dashboard, reportes e históricos. Las pruebas automatizadas recorren el negocio y los permisos; la matriz de historias y comprobaciones está en [docs/BACKLOG.md](docs/BACKLOG.md).

El sistema se verificó con H2 y con una instancia aislada de MySQL 8.0.46, usando el SQL del proyecto y `ddl-auto=validate`. La base definitiva del grupo todavía debe configurarse con sus propias credenciales. La participación de los cuatro integrantes, los pull requests, sus revisiones y la defensa son evidencias que el equipo debe producir con trabajo real; no se simulan mediante commits de otras personas.

## Preparación de MySQL

1. Iniciar MySQL 8 y abrir Workbench con una cuenta autorizada para crear tablas.
2. Ejecutar **todo** `src/main/resources/creaTablas.sql`. Crea las doce tablas del modelo sin eliminar tablas ni datos existentes. Si ya preparó el primer módulo, vuelva a ejecutarlo para agregar las tablas restantes.
3. Preparar una cuenta de aplicación. Ejemplo, reemplazando la contraseña antes de ejecutar:

```sql
CREATE USER IF NOT EXISTS 'nexo'@'localhost' IDENTIFIED BY 'SU_CONTRASENA_PROPIA';
GRANT SELECT, INSERT, UPDATE, DELETE ON nexo.* TO 'nexo'@'localhost';
```

Si la cuenta ya existe, el comando no cambia su contraseña. Use su contraseña vigente. Las tablas deben crearse con la cuenta administrativa; la cuenta normal de la aplicación solo necesita acceso a los datos.

## Configuración local y primer administrador

Copiar `application-local.properties.example` a `application-local.properties` en la raíz. Completar la conexión, el correo del administrador inicial y una contraseña propia de 10 a 64 caracteres. El archivo local está ignorado por Git.

Al arrancar con una base vacía, Nexo crea los roles ADMIN, ANALISTA y SUPERVISOR y el primer administrador configurado. No crea cuentas con contraseñas predeterminadas. En los siguientes arranques conserva las cuentas existentes; cambiar esas propiedades no cambia sus contraseñas. Desde Usuarios, el administrador registra al analista y al supervisor.

También se pueden usar variables de entorno: `NEXO_DB_URL`, `NEXO_DB_USER`, `NEXO_DB_PASSWORD`, `NEXO_ADMIN_EMAIL` y `NEXO_ADMIN_PASSWORD`. No comparta contraseñas por el repositorio.

## Ejecutar en Windows

Seleccionar JDK 21 en NetBeans. Desde PowerShell, en la carpeta del proyecto:

```powershell
.\scripts\iniciar.ps1
```

El script usa Maven de PATH o el Maven instalado con NetBeans. Si Windows bloquea scripts, se puede ejecutar el objetivo directamente:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.12'
& 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' '-Dmaven.repo.local=.cache/maven' spring-boot:run '-Dspring-boot.run.profiles=local'
```

Abrir <http://localhost:8080>. La base debe estar preparada antes de iniciar: Hibernate valida el esquema y no altera automáticamente sus tablas.

Para ejecutar el JAR después de compilar:

```powershell
& 'C:\Program Files\Java\jdk-21.0.12\bin\java.exe' -jar target/nexo.jar --spring.profiles.active=local
```

## Roles y navegación

| Rol | Funciones |
| --- | --- |
| Administrador | Usuarios, roles, activación de cuentas, fuente de pago e historial de importaciones. |
| Analista | Importar y validar archivos, consultar liquidaciones, iniciar conciliaciones, vincular registros, proponer resoluciones y consultar reportes. |
| Supervisor | Consultar conciliaciones y reportes, aprobar o rechazar propuestas y autorizar cierres. |

Una persona puede tener varios roles. ADMIN no concede automáticamente permisos de supervisor. Una persona no puede evaluar su propia propuesta, aunque posea ambos roles. Los cambios de roles o desactivación invalidan las sesiones afectadas. Los POST conservan la protección CSRF de Spring Security.

## CSV y reglas de conciliación

Descargar los ejemplos desde Importaciones o usar `src/main/resources/ejemplos/`. Los ejemplos usan CRC. El formato y los resultados esperados se explican en [docs/FORMATOS_CSV.md](docs/FORMATOS_CSV.md).

Se trabaja con una fuente y una moneda por conciliación. La referencia de liquidación en el CSV interno delimita sus operaciones y evita comparar registros de liquidaciones distintas. Las referencias se normalizan a mayúsculas. Se vinculan referencias y tipos iguales; los importes distintos generan diferencias. COBRO suma; DEVOLUCION y COMISION restan, con cálculos BigDecimal.

Los archivos se validan completos y se guardan en una sola transacción. Los rechazados conservan un lote de error, sin filas parcialmente importadas. Una huella SHA-256 detecta archivos válidos ya procesados. No se permite importar desde fuentes inactivas, cambiar la moneda de una fuente con lotes ni agregar datos a una liquidación que ya tiene conciliación.

La diferencia total es el neto de movimientos del proveedor menos el importe reportado. Las diferencias entre registros y los registros sin correspondencia se conservan por separado. Un total cero no garantiza que no existan discrepancias.

Se deben completar los vínculos antes de enviar la primera propuesta de resolución. Desde ese momento quedan protegidos para preservar la trazabilidad. Las resoluciones no modifican los datos de origen y el rechazo conserva el historial. El supervisor solo puede cerrar cuando no quedan diferencias pendientes o en revisión. Una conciliación cerrada es de solo consulta.

## Pruebas

```powershell
mvn test
mvn package
```

Si Maven no está en PATH, usar la ruta de NetBeans y `-Dmaven.repo.local=.cache/maven` como en el comando de ejecución. Las pruebas usan H2 de forma predeterminada y no modifican la base normal.

Para verificar contra **una base MySQL exclusiva de pruebas**, crear el esquema y definir `NEXO_TEST_DB_URL`, `NEXO_TEST_DB_USER`, `NEXO_TEST_DB_PASSWORD`, `NEXO_TEST_DB_DRIVER=com.mysql.cj.jdbc.Driver`, `NEXO_TEST_DB_DIALECT=org.hibernate.dialect.MySQLDialect` y `NEXO_TEST_DB_DDL=validate`. Las pruebas eliminan y recrean sus registros de trabajo; nunca deben apuntar a la base del grupo ni a una base con datos que deseen conservar.

El workflow de GitHub ejecuta la suite con H2 y con un servicio temporal de MySQL. Se ejecutará al subir cambios; su estado remoto debe comprobarse en Actions.

## Explicación por módulos y demostración

- [Guía técnica de los módulos](docs/MODULOS.md).
- [Guion de demostración con resultados esperados](docs/DEMOSTRACION.md).
- [Backlog y evidencias](docs/BACKLOG.md).
- [Diseño del prototipo](docs/PROTOTIPO.md).
- [Flujo de colaboración](CONTRIBUTING.md).

El prototipo de Figma define la referencia visual: menú lateral oscuro, encabezado claro, tarjetas, formularios Bootstrap y navegación por roles. Las pantallas presentan datos persistidos; los errores y estados de validación aparecen cuando corresponden, sin simular operaciones.
