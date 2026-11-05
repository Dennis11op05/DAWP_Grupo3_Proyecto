# Backlog del Avance 2

La implementación contiene las 21 historias del Avance 1. Esta tabla marca el estado del código y las comprobaciones disponibles; la entrega también requiere configuración del entorno del grupo, demostración, aportes propios y revisión de pull requests. No representa una calificación ni prueba de participación de todos los miembros.

| Historia | Estado del código | Evidencia funcional |
| --- | --- | --- |
| HU-01 Iniciar sesión | Implementada | Correo y contraseña BCrypt; errores de acceso y cuenta desactivada. |
| HU-02 Cerrar sesión | Implementada | POST de logout con CSRF; acceso protegido exige autenticación. |
| HU-03 Registrar usuarios | Implementada | Formulario validado, correo único y contraseña almacenada como hash. |
| HU-04 Asignar roles | Implementada | Asignación de varios roles y permisos aplicados en servidor e interfaz. |
| HU-05 Activar o desactivar usuarios | Implementada | Edición del estado sin eliminar registros; invalidación de sesiones afectadas. |
| HU-06 Gestionar fuente | Implementada | CRUD, código único, moneda y rechazo de importación desde fuente inactiva. |
| HU-07 Importar CSV | Implementada | Archivos internos y del proveedor; lote, responsable, fecha y huella. |
| HU-08 Validar importación | Implementada | Encabezados, filas, fechas, importes, moneda y duplicados; rechazo completo y lote de error. |
| HU-09 Consultar liquidaciones | Implementada | Listado, movimientos, total reportado y estado de revisión. |
| HU-10 Iniciar conciliación | Implementada | Una por liquidación, creador, estado y totales persistidos. |
| HU-11 Vincular operaciones | Implementada | Referencia y tipo iguales, ámbito por fuente y liquidación, unicidad y responsable. |
| HU-12 Identificar diferencias | Implementada | Registros sin correspondencia, importes distintos y total reportado inconsistente. |
| HU-13 Proponer resolución | Implementada | Clasificación y explicación obligatorias; responsable y estado de revisión. |
| HU-14 Panel general | Implementada | Cantidades reales por estado, pendientes y acceso al detalle según rol. |
| HU-15 Reporte de conciliaciones | Implementada | Fechas, estados, totales y responsables; detalle consultable. |
| HU-16 Reporte de diferencias | Implementada | Tipo, descripción, estado, importe, conciliación y detalle. |
| HU-17 Historial de resoluciones | Implementada | Proponente, evaluador, resultado y motivo de rechazo; propuestas anteriores conservadas. |
| HU-18 Historial de importaciones | Implementada | Archivo, origen, usuario, fecha, registros guardados y resultado. |
| HU-19 Configuración del proveedor | Implementada | Mantenimiento exclusivo del administrador; código único y cambios de estado. |
| HU-20 Consulta de cerradas | Implementada | Datos, vínculos, diferencias, decisiones, fecha y autorizante; modificaciones rechazadas. |
| HU-21 Búsqueda y filtrado | Implementada | Referencia, estado, limpieza de filtros y consulta sin modificar registros. |

## Validación y defensa

`NexoIntegrationTest` verifica acceso, logout, cuentas inactivas, roles, CSRF, revocación de sesiones, usuarios, persistencia, importación atómica, duplicados, moneda, fuentes inactivas, cálculos, vínculos únicos, diferencias, autoaprobación prohibida, rechazo con historial y cierre protegido. También solicita las vistas con distintos roles para comprobar su renderizado Thymeleaf.

Se ejecutó la suite con H2 y MySQL 8.0.46. La comprobación MySQL utiliza las tablas de `creaTablas.sql` y validación del esquema, no generación automática. Antes de entregar, repetir la demostración en la base definitiva del equipo y conservar la evidencia del resultado de CI.

## Evidencias que requiere el equipo

- Que los cuatro integrantes expliquen sus aportes y elaboren sus consultas técnicas.
- Commits propios y significativos, ramas funcionales, pull requests y revisiones auténticas.
- Aceptación controlada hacia la rama principal y estado visible de las comprobaciones.
- Grabación del flujo real y defensa ante el docente.

La meta inicial de 13 historias equivale al 61,9% de las 21. El código se extendió al flujo de resolución y cierre para permitir una demostración completa, pero no sustituye las evidencias anteriores.
