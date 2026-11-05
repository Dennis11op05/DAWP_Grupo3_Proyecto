# Demostración del Avance 2

Esta guía recorre funcionalidades reales con los datos sintéticos incluidos. Debe ejecutarse en la base preparada por el grupo antes de grabar la defensa. No utiliza pantallas con cifras inventadas.

## Preparación

Crear las tablas, completar la configuración local e iniciar el sistema siguiendo el README. Iniciar sesión con el administrador inicial. Registrar una cuenta ANALISTA y otra SUPERVISOR, con correos y contraseñas propios de la demostración. No asignar ADMIN como sustituto de SUPERVISOR. Configurar una fuente activa con código DEMO y moneda CRC.

## Administración y permisos

Mostrar el registro y edición de usuarios, asignación de varios roles y estado activo. Iniciar sesión con una cuenta de prueba desactivada y comprobar el rechazo. Reactivarla para continuar. Mostrar que el menú y los accesos directos corresponden a los permisos. Cerrar sesión.

## Importaciones

Entrar como analista. Descargar `interno.csv`, `proveedor.csv` e `invalido.csv` desde Importaciones. Cargar el inválido y mostrar que el historial conserva el error y no guarda operaciones de ese archivo. Importar el interno y luego el del proveedor usando la fuente CRC. Repetir uno válido y mostrar el rechazo por archivo ya procesado. Consultar archivo, origen, fecha, responsable, cantidad y resultado de cada lote.

## Conciliación con diferencias

Abrir Liquidaciones y LIQ-001. Mostrar sus cinco movimientos y el total reportado de 1580,00 CRC. Iniciar la conciliación. Consultar y buscar las operaciones internas. Vincular OP-001, OP-002 y DEV-001 mediante sus referencias.

Después de esos vínculos deben quedar cuatro diferencias: importe de OP-002, operación interna INT-001 sin movimiento y movimientos EXT-001 y COM-001 sin operación. El neto interno es 1470,00 CRC; el neto del proveedor y el total reportado son 1580,00 CRC. La diferencia de total es cero, pero las discrepancias individuales siguen pendientes.

Mostrar las fechas y responsables de los vínculos. Intentar repetir una vinculación debe ser rechazado. Mostrar el Dashboard y el reporte de diferencias con acceso al detalle.

## Resolución y separación de funciones

Con todos los vínculos terminados, proponer una resolución para cada diferencia e ingresar una explicación del caso de demostración. Entrar como supervisor y comprobar que el cierre permanece bloqueado si quedan pendientes. Rechazar una propuesta indicando el motivo. Volver como analista, consultar el historial y enviar una nueva propuesta con una explicación corregida.

Entrar como supervisor, aprobar las propuestas y consultar el historial completo. Si una cuenta tiene ANALISTA y SUPERVISOR, no debe aprobar una propuesta creada por ella misma. Las decisiones no modifican los importes originales.

## Cierre e históricos

Cuando las cuatro diferencias estén resueltas, autorizar el cierre como supervisor. Mostrar autorizante, fecha y estado CERRADA. Consultar nuevamente el detalle y comprobar que no permite cambios. Buscar LIQ-001 en el reporte, filtrar por CERRADA y limpiar los filtros.

También se puede demostrar LIQ-002: contiene OP-OK por 250,00 CRC en ambos archivos. Vincularlo como analista y cerrarlo como supervisor sin resoluciones adicionales, porque no quedan diferencias.

## Persistencia y colaboración

Reiniciar la aplicación usando la misma base MySQL y mostrar que los datos, vínculos, decisiones y cierres se conservan. Presentar el backlog, los aportes reales de cada integrante y los enlaces a las ramas, pull requests y revisiones. Mostrar el resultado de GitHub Actions si ya se ejecutó. No declarar participación ni aceptación de cambios que todavía no se haya realizado.
