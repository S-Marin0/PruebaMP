<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title>Perfil de ${usuario.nombre} - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <jsp:include page="/jsp/common/navigation.jsp" />

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
                <form action="${pageContext.request.contextPath}/usuario/actualizarNotificaciones" method="POST">
                    <fieldset>
                        <legend>Preferencias de Notificación</legend>

                        <p>
                            <input type="checkbox" id="recibirNuevosEventos" name="recibirNotificacionesNuevosEventosImportantes" value="true" ${usuario.configuracionNotificaciones.recibirNotificacionesNuevosEventosImportantes ? 'checked' : ''}>
                            <label for="recibirNuevosEventos">Recibir Notificaciones de Nuevos Eventos Importantes</label>
                        </p>

                        <p>
                            <input type="checkbox" id="recibirRecordatorios" name="recibirRecordatoriosEventosComprados" value="true" ${usuario.configuracionNotificaciones.recibirRecordatoriosEventosComprados ? 'checked' : ''}>
                            <label for="recibirRecordatorios">Recibir Recordatorios de Eventos Comprados</label>
                        </p>

                        <p>
                            <input type="checkbox" id="recibirCambiosEvento" name="recibirNotificacionesCambiosEventoComprado" value="true" ${usuario.configuracionNotificaciones.recibirNotificacionesCambiosEventoComprado ? 'checked' : ''}>
                            <label for="recibirCambiosEvento">Recibir Notificaciones de Cambios en Eventos Comprados</label>
                        </p>

                        <p>
                            <input type="checkbox" id="recibirRecomendaciones" name="recibirRecomendacionesPersonalizadas" value="true" ${usuario.configuracionNotificaciones.recibirRecomendacionesPersonalizadas ? 'checked' : ''}>
                            <label for="recibirRecomendaciones">Recibir Recomendaciones Personalizadas</label>
                        </p>

                        <p>
                            <label for="metodoPreferido">Método Preferido:</label>
                            <%-- Opciones para metodoPreferido pueden ser un dropdown si hay varias opciones. Por ahora, un campo de texto o mostrar el valor actual --%>
                            <%-- <input type="text" id="metodoPreferido" name="metodoPreferido" value="${usuario.configuracionNotificaciones.metodoPreferido}"> --%>
                            <span>${usuario.configuracionNotificaciones.metodoPreferido}</span>
                            <%-- Si se quiere hacer editable, se necesitará saber las opciones válidas. Ejemplo con dropdown:
                            <select id="metodoPreferido" name="metodoPreferido">
                                <option value="Email" ${usuario.configuracionNotificaciones.metodoPreferido == 'Email' ? 'selected' : ''}>Email</option>
                                <option value="SMS" ${usuario.configuracionNotificaciones.metodoPreferido == 'SMS' ? 'selected' : ''}>SMS (si implementado)</option>
                            </select>
                            --%>
                        </p>

                        <p><input type="submit" value="Guardar Preferencias de Notificación"></p>
                    </fieldset>
                </form>
            </c:if>
            <c:if test="${empty usuario.configuracionNotificaciones}">
                <p>No se pudo cargar la configuración de notificaciones.</p>
            </c:if>

            <%--
            <h3>Historial de Compras</h3>
            <jsp:include page="/jsp/compra/misEntradas.jsp">
                <jsp:param name="showHeader" value="false"/>
            </jsp:include>
            --%>

        </c:if>
        <c:if test="${empty usuario}">
             <p class="message error">No se pudo cargar la información del perfil.</p>
        </c:if>


    </div>

    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
