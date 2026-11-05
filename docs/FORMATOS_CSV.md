# Formatos CSV de Nexo

Los archivos son UTF-8, pueden incluir BOM, usan coma y comillas dobles para campos entrecomillados. Admiten CRLF o LF. El máximo por carga es 5 MB y 10000 registros. Los ejemplos distribuidos son datos sintéticos.

## Operaciones internas

```csv
liquidacion,referencia,tipo,fecha,importe,moneda,referencia_original
LIQ-001,OP-001,COBRO,2026-11-01,1000.00,CRC,
LIQ-001,DEV-001,DEVOLUCION,2026-11-01,100.00,CRC,OP-001
```

La columna `liquidacion` indica a qué liquidación pertenecen las operaciones internas, incluso si el archivo del proveedor todavía no se ha importado. Este ajuste del modelo preliminar permite identificar operaciones internas sin correspondencia dentro del ámbito correcto; no se mezclan todas las operaciones del proveedor.

## Liquidaciones y movimientos del proveedor

```csv
liquidacion,fecha_liquidacion,total_reportado,referencia,tipo,fecha,importe,moneda,referencia_original
LIQ-001,2026-11-02,900.00,OP-001,COBRO,2026-11-01,1000.00,CRC,
LIQ-001,2026-11-02,900.00,DEV-001,DEVOLUCION,2026-11-01,100.00,CRC,OP-001
```

Cada fila contiene un movimiento. Las filas de una misma liquidación repiten su referencia, fecha y total reportado, que deben ser consistentes. Un archivo puede contener varias liquidaciones. Cada liquidación del proveedor debe importarse completa una sola vez.

## Reglas

- Los encabezados y su orden deben coincidir exactamente con el ejemplo seleccionado.
- Las referencias admiten letras, números y `_.:/-`, hasta 80 caracteres. Se convierten a mayúsculas y se recortan espacios exteriores.
- `tipo` admite COBRO, DEVOLUCION y COMISION. El importe del movimiento es positivo o cero: COBRO suma y los otros tipos restan.
- Las fechas usan AAAA-MM-DD. Los importes usan punto decimal, hasta 13 dígitos enteros y 2 decimales, sin separadores de miles ni notación científica. El total reportado admite signo negativo.
- La moneda debe coincidir con la fuente seleccionada. Las devoluciones requieren `referencia_original`.
- No se repite referencia y tipo para una misma fuente, ni dentro del archivo ni respecto de registros ya guardados.
- La fuente debe estar activa. Una liquidación con conciliación ya iniciada no admite más importaciones.
- Un archivo erróneo no incorpora filas parcialmente. Su lote registra ERROR, cero registros guardados y el motivo. Un archivo válido registra responsable, fecha, cantidad y huella SHA-256.

## Resultados de los ejemplos completos

En `interno.csv`, LIQ-001 tiene neto interno de 1470,00 CRC. En `proveedor.csv`, el neto y total reportado de LIQ-001 son 1580,00 CRC. Después de vincular OP-001, OP-002 y DEV-001 quedan cuatro diferencias: importe de OP-002, operación INT-001 sin movimiento y movimientos EXT-001 y COM-001 sin operación interna. La diferencia contra el total reportado es cero, aunque esas cuatro discrepancias necesitan resolución.

LIQ-002 contiene OP-OK por 250,00 CRC en ambos archivos. Después de vincularlo no quedan diferencias y un supervisor puede autorizar el cierre.
