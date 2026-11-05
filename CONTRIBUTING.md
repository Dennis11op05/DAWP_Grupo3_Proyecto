# Trabajo colaborativo del Grupo 03

El repositorio del grupo es https://github.com/Dennis11op05/DAWP_Grupo3_Proyecto. Este proyecto contiene una implementación de trabajo asistida; cada integrante debe comprender el código, comprobarlo y realizar aportes propios verificables. No se deben repartir commits ya escritos cambiando sus autores ni simular revisiones.

## Flujo de integración

1. Obtener la versión actual de la rama principal y crear una rama para una mejora concreta, por ejemplo `feature/validacion-csv` o `fix/permisos-supervisor`.
2. Hacer cambios relacionados con una historia, explicarlos y ejecutar las pruebas pertinentes.
3. Crear commits con la identidad del integrante que realmente hizo el aporte.
4. Abrir un pull request hacia la rama principal, indicando historias, comportamiento y pruebas.
5. Otro integrante revisa el diff, ejecuta o comprueba la evidencia y solicita correcciones cuando corresponda.
6. Integrar cuando se complete la revisión y pasen los checks. La protección de la rama se configura en GitHub por una persona con permisos sobre el repositorio.

Los responsables pueden distribuir mejoras de seguridad y usuarios, validaciones e importación, conciliación y cálculos, e interfaz y pruebas. La asignación debe acordarse en el equipo y reflejar trabajo real.

No subir `application-local.properties`, `.env`, `.cache/`, `target/`, claves de Firebase, contraseñas ni archivos financieros reales. Los CSV incluidos son datos sintéticos para demostración.

Antes de la defensa, conservar enlaces a los pull requests y revisiones, comprobar que todos pueden explicar sus aportes y grabar la demostración del sistema ejecutándose. No afirmar que CI pasó sin revisar su resultado en GitHub Actions.
