<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title>EventMaster - Bienvenido</title>
    <%-- Aquí iría un enlace a un CSS común --%>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <%-- Incluir cabecera común --%>
    <jsp:include page="/WEB-INF/jsp/common/header.jsp" />
    <jsp:include page="/WEB-INF/jsp/common/navigation.jsp" />

    <div class="container">
        <h1>Bienvenido a EventMaster</h1>
        <p>Tu portal para descubrir y gestionar eventos increíbles.</p>

        <c:if test="${not empty param.mensaje}">
            <p class="message success">${param.mensaje}</p>
        </c:if>
        <c:if test="${not empty errorGeneral}">
            <p class="message error">${errorGeneral}</p>
        </c:if>

        <h2>Eventos Destacados</h2>
        <c:choose>
            <c:when test="${not empty eventosDestacados}">
                <div class="event-list">
                    <c:forEach var="evento" items="${eventosDestacados}">
                        <div class="event-item-simple">
                            <h3><a href="${pageContext.request.contextPath}/evento/detalle?id=${evento.id}">${evento.nombre}</a></h3>
                            <p>Categoría: ${evento.categoria}</p>
                            <p>Fecha: <fmt:formatDate value="${evento.fechaHora}" type="BOTH" dateStyle="medium" timeStyle="short" /></p>
                            <p>Lugar: ${evento.lugar.nombre}</p>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p>No hay eventos destacados en este momento. ¡Vuelve pronto!</p>
            </c:otherwise>
        </c:choose>

        <p><a href="${pageContext.request.contextPath}/eventos">Ver todos los eventos</a></p>
    </div>

    <%-- Incluir pie de página común --%>
    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
