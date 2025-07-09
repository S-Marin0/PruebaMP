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
    <jsp:include page="/WEB-INF/jsp/common/header.jsp" />
    <jsp:include page="/WEB-INF/jsp/common/navigation.jsp" />

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
                <p><strong>Fecha y Hora:</strong> <fmt:formatDate value="${evento.fechaHora}" type="BOTH" dateStyle="full" timeStyle="medium" /></p>
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
                        <select name="tipoEntradaNombre" id="tipoEntradaNombre">
                            <c:forEach var="entry" items="${evento.tiposEntradaDisponibles}">
                                <c:if test="${entry.value.cantidadDisponible > 0}">
                                   <option value="${entry.value.nombreTipo}">${entry.value.nombreTipo} (<fmt:formatNumber value="${entry.value.precioBase}" type="currency" currencySymbol="€" />)</option>
                                </c:if>
                            </c:forEach>
                        </select>
                        <br/>
                        <label for="cantidad">Cantidad:</label>
                        <input type="number" name="cantidad" value="1" min="1" required>
                        <br/>
                        <c:if test="${not empty sessionScope.usuarioLogueado && sessionScope.usuarioLogueado.class.simpleName == 'Asistente'}">
                             <button type="submit">Comprar Entradas Seleccionadas</button>
                        </c:if>
                         <c:if test="${empty sessionScope.usuarioLogueado}">
                             <p><a href="${pageContext.request.contextPath}/usuario/login?redirect=${pageContext.request.contextPath}/evento/detalle?id=${evento.id}">Inicie sesión para comprar</a></p>
                        </c:if>
                         <c:if test="${not empty sessionScope.usuarioLogueado && sessionScope.usuarioLogueado.class.simpleName != 'Asistente'}">
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
                </c:if>


            </c:when>
            <c:otherwise>
                <p>El evento solicitado no fue encontrado o no está disponible.</p>
            </c:otherwise>
        </c:choose>
        <p><a href="${pageContext.request.contextPath}/eventos">Volver a la lista de eventos</a></p>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
