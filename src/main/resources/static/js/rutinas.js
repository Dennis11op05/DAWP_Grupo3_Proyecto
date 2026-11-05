document.addEventListener('DOMContentLoaded', function () {
    const eliminacion = document.getElementById('confirmarEliminar');
    if (eliminacion) {
        eliminacion.addEventListener('show.bs.modal', function (evento) {
            const boton = evento.relatedTarget;
            document.getElementById('eliminarId').value = boton.dataset.id;
            document.getElementById('eliminarNombre').textContent = boton.dataset.nombre;
        });
    }

    const estado = document.getElementById('confirmarEstado');
    if (estado) {
        estado.addEventListener('show.bs.modal', function (evento) {
            const boton = evento.relatedTarget;
            document.getElementById('estadoId').value = boton.dataset.id;
            document.getElementById('estadoNombre').textContent = boton.dataset.nombre;
        });
    }
});
