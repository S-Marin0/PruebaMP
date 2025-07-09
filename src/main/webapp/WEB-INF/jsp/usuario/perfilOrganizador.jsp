<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title>Perfil de Organizador: ${usuario.nombre} - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/header.jsp" />
    <jsp:include page="/WEB-INF/jsp/common/navigation.jsp" />

    <div class="container">
        <h2>Perfil de Organizador: ${usuario.nombre}</h2>

        <c:if test="${not empty param.mensaje}">
            <p class="message success">${param.mensaje}</p>
        </c:if>
         <c:if test="${not empty errorGeneral}">
            <p class="message error">${errorGeneral}</p>
        </c:if>

        <c:if test="${not empty usuario}">
            <p><strong>ID de Organizador:</strong> ${usuario.id}</p>
            <p><strong>Email de Contacto:</strong> ${usuario.email}</p>
            <p><strong>Información de Contacto Adicional:</strong> ${usuario.infoContacto}</p> <%-- Asumiendo que Organizador tiene getInfoContacto --%>

            <h3>Mis Eventos Creados</h3>
            <c:choose>
                <c:when test="${not empty usuario.eventosCreados}">
                    <table border="1">
                        <thead>
                            <tr>
                                <th>Nombre del Evento</th>
                                <th>Fecha</th>
                                <th>Estado</th>
                                <th>Entradas Vendidas</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="evento" items="${usuario.eventosCreados}">
                                <tr>
                                    <td><a href="${pageContext.request.contextPath}/evento/detalle?id=${evento.id}">${evento.nombre}</a></td>
                                    <td><fmt:formatDate value="${evento.fechaHora}" type="BOTH" dateStyle="medium" timeStyle="short"/></td>
                                    <td>${evento.estadoActual.nombreEstado}</td>
                                    <td>${evento.entradasVendidas} / ${evento.capacidadTotal}</td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/evento/editar?id=${evento.id}">Editar</a>
                                        <%-- Eliminar podría ser un form POST --%>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <p>Aún no has creado ningún evento.</p>
                </c:otherwise>
            </c:choose>
            <p><a href="${pageContext.request.contextPath}/evento/crear" class="button">Crear Nuevo Evento</a></p>

            <%-- Aquí se podrían mostrar estadísticas generales del organizador --%>

        </c:if>
        <c:if test="${empty usuario}">
             <p class="message error">No se pudo cargar la información del perfil del organizador.</p>
        </c:if>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
