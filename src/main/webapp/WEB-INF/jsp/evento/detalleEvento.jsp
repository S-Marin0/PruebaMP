<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <c:choose>
        <c:when test="${not empty evento}">
            <title>Detalle: ${evento.nombre} - EventMaster</title>
        </c:when>
        <c:otherwise>
            <title>Detalle de Evento - EventMaster</title>
        </c:otherwise>
    </c:choose>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <jsp:include page="/jsp/common/navigation.jsp" />

    <div class="container">
        <c:if test="${not empty param.mensaje}">
            <p class="message success">${param.mensaje}</p>
        </c:if>
        <c:if test="${not empty param.errorCompra}">
            <p class="message error">Error en la compra: ${param.errorCompra}</p>
        </c:if>
        <c:if test="${not empty errorGeneral}">
            <p class="message error">${errorGeneral}</p>
        </c:if>

        <c:choose>
            <c:when test="${not empty evento}">
                <h2>${evento.nombre}</h2>
                <p><strong>Categoría:</strong> ${evento.categoria}</p>
               <p><strong>Fecha y Hora:</strong> ${fechaHoraFormateada}</p>
                <p><strong>Lugar:</strong> ${evento.lugar.nombre} - ${evento.lugar.direccion}</p>
                <p><strong>Organizador:</strong> ${evento.organizador.nombre}</p>
                <p><strong>Capacidad Total:</strong> ${evento.capacidadTotal} | <strong>Entradas Vendidas:</strong> ${evento.entradasVendidas} | <strong>Disponibles:</strong> ${evento.capacidadTotal - evento.entradasVendidas}</p>
                <p><strong>Estado:</strong> <span class="estado-${evento.estadoActual.nombreEstado.toLowerCase()}">${evento.estadoActual.nombreEstado}</span></p>

                <h3>Descripción</h3>
                <p>${evento.descripcion}</p>

                <c:if test="${not empty evento.urlsImagenes}">
                    <h3>Imágenes</h3>
                    <c:forEach var="imgUrl" items="${evento.urlsImagenes}">
                        <%-- En una app real, se usaría un proxy o un servlet para servir imágenes si no son públicas --%>
                        <img src="${imgUrl}" alt="Imagen del evento ${evento.nombre}" style="max-width:300px; margin:5px;">
                    </c:forEach>
                </c:if>

                <h3>Tipos de Entrada Disponibles</h3>
                <c:if test="${evento.estadoActual.nombreEstado == 'Publicado' or evento.estadoActual.nombreEstado == 'En Curso'}">
                    <form action="${pageContext.request.contextPath}/compra/iniciar" method="post">
                        <input type="hidden" name="eventoId" value="${evento.id}">
                        <table border="1">
                            <thead>
                                <tr>
                                    <th>Tipo</th>
                                    <th>Precio Base</th>
                                    <th>Disponibles</th>
                                    <th>Cantidad a Comprar</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="entry" items="${evento.tiposEntradaDisponibles}">
                                    <c:set var="tipoEntrada" value="${entry.value}" />
                                    <tr>
                                        <td>${tipoEntrada.nombreTipo}</td>
                                        <td><fmt:formatNumber value="${tipoEntrada.precioBase}" type="currency" currencySymbol="€" /></td>
                                        <td>${tipoEntrada.cantidadDisponible}</td>
                                        <td>
                                            <c:if test="${tipoEntrada.cantidadDisponible > 0}">
                                                <input type="number" name="cantidad_tipo_${tipoEntrada.nombreTipo}" value="0" min="0" max="${tipoEntrada.cantidadDisponible < tipoEntrada.limiteCompraPorUsuario ? tipoEntrada.cantidadDisponible : tipoEntrada.limiteCompraPorUsuario}">
                                                (Límite por compra: ${tipoEntrada.limiteCompraPorUsuario == 2147483647 ? 'Sin límite' : tipoEntrada.limiteCompraPorUsuario})
                                            </c:if>
                                             <c:if test="${tipoEntrada.cantidadDisponible <= 0}">
                                                Agotadas
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        <%-- Simplificación: el usuario selecciona un tipo y cantidad.
                             En una UI más rica, podría añadir varios tipos al carrito.
                             Aquí, asumimos que selecciona un tipo y cantidad, y pulsa comprar para ESE tipo.
                             Por tanto, necesitaremos un botón por cada tipo o una forma de identificar qué tipo se está comprando.
                             Vamos a simplificar: un solo select para el tipo y un input para la cantidad de ESE tipo.
                        --%>
                        <br/>
                        <label for="tipoEntradaNombre">Seleccionar Tipo de Entrada:</label>
                        <select name="tipoEntradaNombre" id="tipoEntradaNombre" onchange="actualizarOpcionesDecoradorYPrecio()">
                            <option value="">-- Seleccione un tipo de entrada --</option>
                            <c:forEach var="entry" items="${evento.tiposEntradaDisponibles}">
                                <c:if test="${entry.value.cantidadDisponible > 0}">
                                   <c:set var="te" value="${entry.value}" />
                                   <option value="${te.nombreTipo}"
                                           data-precio-base="${te.precioBase}"
                                           data-ofrece-mercancia="${te.ofreceMercanciaOpcional}"
                                           data-desc-mercancia="${te.descripcionMercancia}"
                                           data-precio-mercancia="${te.precioAdicionalMercancia}"
                                           data-ofrece-descuento="${te.ofreceDescuentoOpcional}"
                                           data-desc-descuento="${te.descripcionDescuento}"
                                           data-monto-descuento="${te.montoDescuentoFijo}">
                                       ${te.nombreTipo} (<fmt:formatNumber value="${te.precioBase}" type="currency" currencySymbol="€" />)
                                   </option>
                                </c:if>
                            </c:forEach>
                        </select>
                        <br/>

                        <%-- Contenedores para opciones de decorador (inicialmente ocultos) --%>
                        <div id="opcionMercanciaDiv" style="display:none; margin-top:10px;">
                            <input type="checkbox" id="checkMercancia" name="decorador_mercancia" value="true" onchange="actualizarOpcionesDecoradorYPrecio()">
                            <label for="checkMercancia" id="labelMercancia"></label>
                        </div>

                        <div id="opcionDescuentoDiv" style="display:none; margin-top:10px;">
                            <input type="checkbox" id="checkDescuento" name="decorador_descuento" value="true" onchange="actualizarOpcionesDecoradorYPrecio()">
                            <label for="checkDescuento" id="labelDescuento"></label>
                        </div>
                        <br/>

                        <label for="cantidad">Cantidad:</label>
                        <input type="number" name="cantidad" id="cantidad" value="1" min="1" required onchange="actualizarOpcionesDecoradorYPrecio()">
                        <br/>

                        <h4>Precio Total Estimado: <span id="precioTotalEstimado">€0.00</span></h4>
                        <br/>

                        <c:if test="${esAsistente}">
                             <button type="submit" id="botonComprar">Comprar Entradas Seleccionadas</button>
                        </c:if>
                         <c:if test="${empty sessionScope.usuarioLogueado}">
                             <p><a href="${pageContext.request.contextPath}/usuario/login?redirect=${pageContext.request.contextPath}/evento/detalle?id=${evento.id}">Inicie sesión para comprar</a></p>
                        </c:if>
                         <c:if test="${not esAsistente}">

                             <p>Solo los asistentes pueden comprar entradas.</p>
                        </c:if>
                    </form>
                </c:if>
                <c:if test="${evento.estadoActual.nombreEstado != 'Publicado' && evento.estadoActual.nombreEstado != 'En Curso'}">
                    <p>Las entradas para este evento no están actualmente a la venta (Estado: ${evento.estadoActual.nombreEstado}).</p>
                </c:if>

                <%-- Lógica para Organizador: Editar/Eliminar --%>
                <c:if test="${not empty sessionScope.usuarioLogueado && sessionScope.usuarioLogueado.id == evento.organizador.id}">
                    <hr>
                    <h4>Acciones de Organizador:</h4>
                    <a href="${pageContext.request.contextPath}/evento/editar?id=${evento.id}">Editar Evento</a>
                    <%-- Eliminar podría ser un POST a través de un formulario --%>
                    <form action="${pageContext.request.contextPath}/evento/eliminar?id=${evento.id}" method="post" style="display:inline;" onsubmit="return confirm('¿Está seguro de que desea eliminar este evento? Esta acción no se puede deshacer fácilmente.');">
                        <input type="hidden" name="eventoId" value="${evento.id}">
                        <button type="submit">Eliminar Evento</button>
                    </form>

                    <%-- Formularios para Cambiar Estado --%>
                    <c:set var="estadoActualNombre" value="${evento.estadoActual.nombreEstado.toLowerCase()}" />

                    <c:if test="${estadoActualNombre == 'borrador'}">
                        <form action="${pageContext.request.contextPath}/evento/cambiarEstado" method="post" style="display:inline;">
                            <input type="hidden" name="eventoId" value="${evento.id}">
                            <input type="hidden" name="accion" value="publicar">
                            <button type="submit">Publicar Evento</button>
                        </form>
                    </c:if>

                    <c:if test="${estadoActualNombre == 'publicado'}">
                        <form action="${pageContext.request.contextPath}/evento/cambiarEstado" method="post" style="display:inline;">
                            <input type="hidden" name="eventoId" value="${evento.id}">
                            <input type="hidden" name="accion" value="cancelar">
                            <button type="submit" class="button-danger">Cancelar Evento</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/evento/cambiarEstado" method="post" style="display:inline;">
                            <input type="hidden" name="eventoId" value="${evento.id}">
                            <input type="hidden" name="accion" value="iniciar">
                            <button type="submit">Iniciar Evento (Poner En Curso)</button>
                        </form>
                    </c:if>

                    <c:if test="${estadoActualNombre == 'en curso'}">
                         <form action="${pageContext.request.contextPath}/evento/cambiarEstado" method="post" style="display:inline;">
                            <input type="hidden" name="eventoId" value="${evento.id}">
                            <input type="hidden" name="accion" value="cancelar">
                            <button type="submit" class="button-danger">Cancelar Evento</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/evento/cambiarEstado" method="post" style="display:inline;">
                            <input type="hidden" name="eventoId" value="${evento.id}">
                            <input type="hidden" name="accion" value="finalizar">
                            <button type="submit">Finalizar Evento</button>
                        </form>
                    </c:if>
                     <%-- Para eventos Cancelados o Finalizados, usualmente no hay más acciones de estado manuales comunes --%>
                     <c:if test="${estadoActualNombre == 'cancelado' || estadoActualNombre == 'finalizado'}">
                        <p><em>Este evento está ${evento.estadoActual.nombreEstado} y no se pueden realizar más cambios de estado manuales.</em></p>
                    </c:if>

                </c:if>


            </c:when>
            <c:otherwise>
                <p>El evento solicitado no fue encontrado o no está disponible.</p>
            </c:otherwise>
        </c:choose>
        <p><a href="${pageContext.request.contextPath}/eventos">Volver a la lista de eventos</a></p>
    </div>

    <jsp:include page="/jsp/common/footer.jsp" />

    <script>
        function actualizarOpcionesDecoradorYPrecio() {
            const tipoEntradaSelect = document.getElementById('tipoEntradaNombre');
            const selectedOption = tipoEntradaSelect.options[tipoEntradaSelect.selectedIndex];
            const cantidadInput = document.getElementById('cantidad');
            const cantidad = parseInt(cantidadInput.value) || 0;

            const opcionMercanciaDiv = document.getElementById('opcionMercanciaDiv');
            const checkMercancia = document.getElementById('checkMercancia');
            const labelMercancia = document.getElementById('labelMercancia');

            const opcionDescuentoDiv = document.getElementById('opcionDescuentoDiv');
            const checkDescuento = document.getElementById('checkDescuento');
            const labelDescuento = document.getElementById('labelDescuento');

            const precioTotalEstimadoSpan = document.getElementById('precioTotalEstimado');
            const botonComprar = document.getElementById('botonComprar');

            if (!selectedOption || selectedOption.value === "") {
                opcionMercanciaDiv.style.display = 'none';
                checkMercancia.checked = false;
                opcionDescuentoDiv.style.display = 'none';
                checkDescuento.checked = false;
                precioTotalEstimadoSpan.textContent = '€0.00';
                if (botonComprar) botonComprar.disabled = true;
                return;
            }
            if (botonComprar) botonComprar.disabled = false;


            const precioBase = parseFloat(selectedOption.dataset.precioBase) || 0;
            const ofreceMercancia = selectedOption.dataset.ofreceMercancia === 'true';
            const descMercancia = selectedOption.dataset.descMercancia || 'Mercancía Adicional';
            const precioMercancia = parseFloat(selectedOption.dataset.precioMercancia) || 0;
            const ofreceDescuento = selectedOption.dataset.ofreceDescuento === 'true';
            const descDescuento = selectedOption.dataset.descDescuento || 'Descuento Aplicado';
            const montoDescuento = parseFloat(selectedOption.dataset.montoDescuento) || 0;

            let precioCalculado = precioBase;

            if (ofreceMercancia && precioMercancia > 0) {
                opcionMercanciaDiv.style.display = 'block';
                labelMercancia.textContent = `${descMercancia} (+€${precioMercancia.toFixed(2)})`;
                if (checkMercancia.checked) {
                    precioCalculado += precioMercancia;
                }
            } else {
                opcionMercanciaDiv.style.display = 'none';
                checkMercancia.checked = false;
            }

            if (ofreceDescuento && montoDescuento > 0) {
                opcionDescuentoDiv.style.display = 'block';
                labelDescuento.textContent = `${descDescuento} (-€${montoDescuento.toFixed(2)})`;
                if (checkDescuento.checked) {
                    precioCalculado -= montoDescuento;
                }
            } else {
                opcionDescuentoDiv.style.display = 'none';
                checkDescuento.checked = false;
            }

            if (precioCalculado < 0) { // Un descuento no debería hacer el precio negativo
                precioCalculado = 0;
            }

            const precioTotal = precioCalculado * cantidad;
            precioTotalEstimadoSpan.textContent = `€${precioTotal.toFixed(2)}`;
        }

        // Llamar una vez al cargar la página para inicializar (en caso de que haya algo preseleccionado o para deshabilitar botón)
        document.addEventListener('DOMContentLoaded', function() {
            actualizarOpcionesDecoradorYPrecio();
        });
    </script>
</body>
</html>
