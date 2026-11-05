# Referencia del prototipo de Nexo

La referencia visual es el [archivo de Figma del grupo](https://www.figma.com/design/Mulmt4pEdzP9IwgdhhC1TG/Sin-t%C3%ADtulo?node-id=0-1), organizado en acceso y sesión, administrador, analista y supervisor. La [pantalla Fuente de pago](https://www.figma.com/design/Mulmt4pEdzP9IwgdhhC1TG/Sin-t%C3%ADtulo?node-id=5-21153) se inspeccionó para ajustar el primer módulo.

## Criterios aplicados

Se conserva el menú lateral azul oscuro, encabezado blanco, fondo gris claro, tarjetas blancas y botones azules. Las vistas de usuarios, importación, liquidaciones, conciliaciones, diferencias y resoluciones reutilizan ese diseño. Bootstrap mantiene la distribución, formularios, tablas, alertas, confirmaciones y adaptación a pantallas pequeñas; `static/css/estilos.css` aporta los ajustes visuales.

Fuente de pago presenta su tarjeta y el formulario en columnas. Los errores de campos, códigos duplicados y cambios de estado aparecen al producirse la situación real. Las variaciones del prototipo no se copian como paneles estáticos simultáneos.

La navegación se adapta a los roles. El encabezado presenta la identidad real de la sesión y el cierre de sesión funcional. El Dashboard muestra cantidades de la base, y las vistas enlazan a los registros correspondientes. En pantallas pequeñas, el menú utiliza un offcanvas de Bootstrap y las columnas se apilan.

## Ajustes funcionales respecto de la propuesta inicial

Se agregó referencia de liquidación al CSV interno para identificar su ámbito con precisión. Las vinculaciones quedan protegidas tras la primera propuesta de resolución y las conciliaciones cerradas no admiten cambios. Se mantiene eliminación de fuentes sin relaciones como acción secundaria para completar el CRUD; la desactivación es la acción principal para conservar datos históricos.

El diseño de las vistas nuevas adapta el lenguaje visual compartido del prototipo; no se afirma equivalencia píxel a píxel de cada marco de Figma. Los estados y datos se generan por el comportamiento real de la aplicación.

## Repositorio

El repositorio es [DAWP Grupo 3 Proyecto](https://github.com/Dennis11op05/DAWP_Grupo3_Proyecto). La participación de los integrantes y las revisiones deberán verificarse en su historial real. La implementación no crea contribuciones con identidades de otras personas.
