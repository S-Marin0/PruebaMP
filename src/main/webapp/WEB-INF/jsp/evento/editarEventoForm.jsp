<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title>Editar Evento: ${evento.nombre} - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/header.jsp" />
    <jsp:include page="/WEB-INF/jsp/common/navigation.jsp" />

    <div class="container">
        <h2>Editar Evento: <c:out value="${evento.nombre}" /></h2>

        <c:if test="${not empty errorEditarEvento}">
            <p class="message error">${errorEditarEvento}</p>
        </c:if>
        <c:if test="${not empty errorGeneral}">
            <p class="message error">${errorGeneral}</p>
        </c:if>

        <c:choose>
            <c:when test="${not empty evento && not empty sessionScope.usuarioLogueado && sessionScope.usuarioLogueado.id == evento.organizador.id}">
                <form action="${pageContext.request.contextPath}/evento/editar" method="post">
                    <input type="hidden" name="eventoId" value="${evento.id}">

                    <div>
                        <label for="nombre">Nombre del Evento:</label>
                        <input type="text" id="nombre" name="nombre" value="<c:out value="${not empty param.nombre ? param.nombre : evento.nombre}" />" required>
                    </div>
                    <div>
                        <label for="descripcion">Descripción:</label>
                        <textarea id="descripcion" name="descripcion" rows="5" required><c:out value="${not empty param.descripcion ? param.descripcion : evento.descripcion}" /></textarea>
                    </div>
                    <div>
                        <label for="categoria">Categoría:</label>
                        <input type="text" id="categoria" name="categoria" value="<c:out value="${not empty param.categoria ? param.categoria : evento.categoria}" />" required>
                        (Ej: Concierto, Conferencia, Deporte, Teatro)
                    </div>
                    <div>
                        <label for="fechaHora">Fecha y Hora:</label>
                        <%-- El valor para datetime-local debe ser yyyy-MM-ddTHH:mm --%>
                        <input type="datetime-local" id="fechaHora" name="fechaHora" value="${not empty param.fechaHora ? param.fechaHora : fechaHoraInput}" required>
                    </div>
                    <div>
                        <label for="lugarNombre">Nombre del Lugar:</label>
                        <input type="text" id="lugarNombre" name="lugarNombre" value="<c:out value="${not empty param.lugarNombre ? param.lugarNombre : evento.lugar.nombre}" />" readonly><br/>
                        <small>La edición del lugar no está implementada en este formulario.</small><br/>
                        <label for="lugarDireccion">Dirección del Lugar:</label>
                        <input type="text" id="lugarDireccion" name="lugarDireccion" value="<c:out value="${not empty param.lugarDireccion ? param.lugarDireccion : evento.lugar.direccion}" />" readonly>
                    </div>
                    <div>
                        <label for="capacidad">Capacidad Total Estimada:</label>
                        <input type="number" id="capacidad" name="capacidad" value="${not empty param.capacidad ? param.capacidad : evento.capacidadTotal}" min="1" required>
                    </div>

                    <div>
                        <button type="submit">Guardar Cambios</button>
                        <a href="${pageContext.request.contextPath}/evento/detalle?id=${evento.id}" class="button secondary">Cancelar</a>
                    </div>
                </form>
            </c:when>
            <c:when test="${empty evento}">
                 <p class="message error">El evento que intenta editar no fue encontrado.</p>
            </c:when>
            <c:otherwise>
                <p class="message error">No tiene permisos para editar este evento o no ha iniciado sesión como organizador.</p>
                <p><a href="${pageContext.request.contextPath}/usuario/login?redirect=${pageContext.request.contextPath}/evento/editar?id=${evento.id}">Iniciar Sesión</a></p>
            </c:otherwise>
        </c:choose>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
