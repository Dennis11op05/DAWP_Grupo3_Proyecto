# Validación del Avance 2

Verificado el 5 de noviembre de 2026 con Java 21.0.12, Spring Boot 4.1.1 y MySQL 8.0.46.

## Pruebas automatizadas

- H2: 14 pruebas, cero fallos, cero errores.
- MySQL 8.0.46: las mismas 14 pruebas, cero fallos, cero errores.
- La ejecución MySQL utilizó `creaTablas.sql` y `spring.jpa.hibernate.ddl-auto=validate`.
- El empaquetado Maven terminó correctamente y produjo el JAR ejecutable.

Se verificaron acceso y logout, cuentas desactivadas, permisos de servidor y CSRF, revocación de sesiones al cambiar roles, registro y contraseña hash, CRUD de fuentes, importación atómica, duplicados, moneda, fuente inactiva, cálculos, vínculos únicos, separación de fuentes y liquidaciones, último administrador activo, decisiones con historial, bloqueo de autoaprobación y protección de cierres. Las pruebas también renderizan las vistas con los roles correspondientes.

## Recorrido de navegador

Se inició la aplicación con el JAR normal y una conexión MySQL, sin H2 en su ejecución. Se verificó el login, navegación por roles, registro de fuente, carga real de los CSV interno y proveedor y creación de una conciliación. El historial registró cinco operaciones internas y seis movimientos del proveedor. Para LIQ-001, tras vincular OP-001, OP-002 y DEV-001, se obtuvieron las cuatro discrepancias esperadas, con total interno 1470,00 CRC, neto del proveedor 1580,00 CRC y diferencia contra total reportado de cero.

La prueba MySQL se realizó en una instancia temporal ligada a 127.0.0.1, sin modificar las bases existentes del equipo. Los datos utilizados son sintéticos y no corresponden a operaciones financieras reales.

## Alcance y pendientes de la entrega

La configuración de la base definitiva del grupo requiere ejecutar el esquema y completar sus propias credenciales. Los resultados locales no sustituyen la ejecución de GitHub Actions ni las revisiones por los integrantes. El equipo debe conservar sus evidencias auténticas de colaboración y preparar la grabación y defensa usando el guion de demostración.

Durante la verificación se utilizó una salida de compilación aislada (`-Dnexo.build.directory=.cache/verified-build`) para evitar interferencias con el directorio target del IDE. La ejecución normal mantiene target como destino.
