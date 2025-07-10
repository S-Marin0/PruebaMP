<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title>Perfil de ${usuario.nombre} - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/header.jsp" />
    <jsp:include page="/WEB-INF/jsp/common/navigation.jsp" />

    <div class="container">
        <h2>Perfil de Asistente: ${usuario.nombre}</h2>

        <c:if test="${not empty param.mensaje}">
            <p class="message success">${param.mensaje}</p>
        </c:if>
        <c:if test="${not empty errorGeneral}">
            <p class="message error">${errorGeneral}</p>
        </c:if>

        <c:if test="${not empty usuario}">
            <p><strong>ID:</strong> ${usuario.id}</p>
            <p><strong>Email:</strong> ${usuario.email}</p>

            <h3>Preferencias</h3>
            <c:choose>
                <c:when test="${not empty usuario.preferenciasDetalladas}">
                    <ul>
                        <c:forEach var="pref" items="${usuario.preferenciasDetalladas}">
                            <li>${pref.nombre} (${pref.tipo})</li>
                        </c:forEach>
                    </ul>
                </c:when>
                <c:otherwise>
                    <p>No has configurado tus preferencias detalladas aún.</p>
                </c:otherwise>
            </c:choose>
            <%-- Aquí iría un formulario para editar preferencias --%>

            <h3>Configuración de Notificaciones</h3>
            <c:if test="${not empty usuario.configuracionNotificaciones}">
                <p>Recibir Notificaciones de Nuevos Eventos Importantes: ${usuario.configuracionNotificaciones.recibirNotificacionesNuevosEventosImportantes ? 'Sí' : 'No'}</p>
                <p>Recibir Recordatorios de Eventos Comprados: ${usuario.configuracionNotificaciones.recibirRecordatoriosEventosComprados ? 'Sí' : 'No'}</p>
                <p>Recibir Notificaciones de Cambios en Eventos Comprados: ${usuario.configuracionNotificaciones.recibirNotificacionesCambiosEventoComprado ? 'Sí' : 'No'}</p>
                <p>Recibir Recomendaciones Personalizadas: ${usuario.configuracionNotificaciones.recibirRecomendacionesPersonalizadas ? 'Sí' : 'No'}</p>
                <p>Método Preferido: ${usuario.configuracionNotificaciones.metodoPreferido}</p>
            </c:if>
            <%-- Aquí iría un formulario para editar configuración de notificaciones --%>

            <h3>Historial de Compras</h3>
            <jsp:include page="/WEB-INF/jsp/compra/misEntradas.jsp">
                 <jsp:param name="showHeader" value="false"/> <%-- No mostrar título si se incluye --%>
            </jsp:include>


        </c:if>
        <c:if test="${empty usuario}">
             <p class="message error">No se pudo cargar la información del perfil.</p>
        </c:if>


    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
