<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
    <head>
        <title>Lista de Eventos - EventMaster</title>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    </head>
    <body>
        <jsp:include page="/jsp/common/header.jsp" />
        <jsp:include page="/jsp/common/navigation.jsp" />

        <div class="container">
            <h2>Eventos Disponibles</h2>

            <%-- Sección de Filtros (simple) --%>
            <form method="get" action="${pageContext.request.contextPath}/eventos">
                <label for="categoria">Filtrar por Categoría:</label>
                <input type="text" id="categoria" name="categoria" value="${param.categoria}">
                <input type="submit" value="Filtrar">
                <c:if test="${not empty param.categoria}">
                    <a href="${pageContext.request.contextPath}/eventos">Limpiar Filtro</a>
                </c:if>
            </form>
            <hr/>

            <c:if test="${not empty errorGeneral}">
                <p class="message error">${errorGeneral}</p>
            </c:if>

            <div class="event-list">
                <c:choose>
                    <c:when test="${not empty listaEventosConFecha}">
                        <c:forEach var="eventoMap" items="${listaEventosConFecha}">
                            <c:set var="evento" value="${eventoMap.evento}" />
                            <div class="event-item">
                                <h3><a href="${pageContext.request.contextPath}/evento/detalle?id=${evento.id}">${evento.nombre}</a></h3>
                                <p><strong>Categoría:</strong> ${evento.categoria}</p>
                                <p><strong>Fecha:</strong> ${eventoMap.fechaFormateada}</p>
                                <p><strong>Lugar:</strong> ${evento.lugar.nombre} (${evento.lugar.direccion})</p>
                                <p><strong>Organizador:</strong> ${evento.organizador.nombre}</p>
                                <p><strong>Estado:</strong> ${evento.estadoActual.nombreEstado}</p>
                                <p>
                                    ${evento.descripcion.length() > 100 ? evento.descripcion.substring(0, 100) : evento.descripcion}...
                                    <a href="${pageContext.request.contextPath}/evento/detalle?id=${evento.id}">Ver más</a>
                                </p>
                            </div>
                        </c:forEach>

                    </c:when>
                    <c:otherwise>
                        <p>No hay eventos que coincidan con los criterios de búsqueda o no hay eventos disponibles en este momento.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <jsp:include page="/jsp/common/footer.jsp" />
    </body>
</html>
