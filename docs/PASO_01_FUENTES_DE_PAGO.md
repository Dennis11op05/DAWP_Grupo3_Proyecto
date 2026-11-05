# Paso 1 Base web y fuentes de pago

Este paso transforma el proyecto Maven inicial en una aplicación Spring Boot y desarrolla el primer CRUD de Nexo. Se toma como referencia el mantenimiento de categorías de `Clases/Clase 1/tienda_vm` y la separación en capas del caso de estudio de libros. Conviene comprender y ejecutar este módulo antes de agregar usuarios y autenticación.

## Relación con lo visto en clase

| Ejemplo del curso | Aplicación en Nexo |
| --- | --- |
| `Categoria` y `Producto` con `@Entity` | `FuentePago` representa una fila de `fuente_pago`. |
| `CategoriaRepository extends JpaRepository` | `FuentePagoRepository` proporciona consultas y persistencia. |
| `CategoriaService` con `@Transactional` | `FuentePagoService` valida la unicidad y guarda o elimina dentro de transacciones. |
| `CategoriaController` con listado y guardar | `FuentePagoController` atiende las rutas del módulo. |
| `templates/general/fragmentos.html` | Nexo reutiliza encabezado, recursos Bootstrap, mensajes y pie. |
| `templates/categoria/listado.html` y fragmentos | El listado y los modales de fuentes de pago están separados en vistas y fragmentos. |
| `creaTablas.sql` y MySQL | Un script manual prepara el esquema de Nexo. |
| `@NotBlank`, `@Size`, `@Valid` | El servidor valida el formulario aunque se omita la validación del navegador. |
| Lombok `@Getter`, `@Setter`, `@RequiredArgsConstructor` | Se mantiene el patrón del proyecto tienda. |

Se conservó el paquete original del proyecto para que la clase principal pueda detectar sus subpaquetes. Se utilizaron Java 21, Spring Boot 4.1.1, Lombok 1.18.48 y Bootstrap 5.3.8, declarados en el proyecto tienda suministrado. Firebase queda fuera de este módulo porque no se requieren imágenes ni almacenamiento externo.

## Cómo recorre el sistema una solicitud

Al abrir `/fuente-pago/listado`, el controlador solicita los proveedores al servicio. El servicio llama al repositorio, que utiliza JPA para consultar MySQL. El controlador agrega los registros al modelo y Thymeleaf genera las tarjetas y el formulario del navegador. Bootstrap aporta el diseño, la adaptación al ancho de pantalla y el comportamiento de los modales de confirmación. El formulario ocupa una columna junto a la fuente configurada, siguiendo el prototipo de Figma.

Al presionar Guardar, el formulario envía un POST a `/fuente-pago/guardar`. `@Valid` comprueba los campos. El servicio comprueba si el código ya pertenece a otra fuente. Si el identificador está vacío, crea el registro; si existe, carga y actualiza esa fuente. El repositorio guarda la entidad y el navegador vuelve al listado con un mensaje. Cuando hay errores, se conservan los valores y se muestran los mensajes dentro del formulario.

## Qué hace cada archivo Java

- `Proyecto_Desarrollo_App_Web.java`: inicia Spring Boot en lugar del antiguo Hello World.
- `domain/FuentePago.java`: define identificador, nombre, código, moneda y estado. Normaliza el código a mayúsculas y elimina espacios al inicio y al final.
- `repository/FuentePagoRepository.java`: busca fuentes ordenadas por nombre, fuentes activas o una fuente por código.
- `service/FuentePagoService.java`: coordina la persistencia, evita códigos duplicados y rechaza identificadores inexistentes.
- `controller/FuentePagoController.java`: recibe datos, presenta errores y elige la vista o redirección.
- `controller/IndexController.java`: presenta la página de inicio.

## Decisiones que deben poder explicar

La restricción única del código existe tanto en la lógica como en la tabla. El servicio ofrece un mensaje comprensible y la base de datos impide duplicados incluso si llegan solicitudes simultáneas.

La desactivación conserva el registro. La eliminación es una operación distinta y se confirma con un modal. En próximos módulos, las claves foráneas protegerán las fuentes que tengan importaciones asociadas.

La validación del navegador ayuda a completar campos, pero el servidor también valida. Los mensajes se muestran junto a los campos, y los errores generales se muestran dentro del formulario.

El esquema de MySQL se crea mediante SQL y JPA lo valida al iniciar. Las pruebas usan H2 en un perfil separado para no modificar datos del grupo. Aprobar las pruebas con H2 no demuestra por sí solo que MySQL esté configurado correctamente.

## Evolución del primer paso

El mantenimiento inicial se integró con usuarios, autenticación e importaciones. Ahora el servidor restringe sus modificaciones al administrador e impide importar desde fuentes inactivas. El estado de todas las historias se encuentra en `BACKLOG.md` y la explicación del flujo completo en `MODULOS.md`.

La interfaz utiliza Bootstrap y se adaptó a la pantalla Fuente de pago del prototipo de Figma. La comparación y los límites de esta adaptación se documentan en `PROTOTIPO.md`.

## Ejercicio de comprobación

Preparar MySQL y ejecutar siguiendo el README. Crear una fuente, editarla, desactivarla y comprobar que permanece tras reiniciar la aplicación. Intentar guardar un código repetido y dejar un nombre vacío. Explicar qué parte comprueba el navegador, qué valida Java y qué garantiza la base de datos. Después eliminar únicamente el registro de prueba.

Continúe con `MODULOS.md` para estudiar usuarios y roles y después el flujo de importación, conciliación, resolución y cierre.
