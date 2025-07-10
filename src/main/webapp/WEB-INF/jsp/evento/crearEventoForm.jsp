<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Crear Nuevo Evento - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/header.jsp" />
    <jsp:include page="/WEB-INF/jsp/common/navigation.jsp" />

    <div class="container">
        <h2>Crear Nuevo Evento</h2>

        <c:if test="${not empty errorCrearEvento}">
            <p class="message error">${errorCrearEvento}</p>
        </c:if>
        <c:if test="${empty sessionScope.usuarioLogueado || sessionScope.usuarioLogueado.class.simpleName != 'Organizador'}">
            <p class="message error">Acceso denegado. Debe ser un organizador para crear eventos.</p>
            <%-- Podría redirigir o simplemente no mostrar el formulario --%>
        </c:if>

        <c:if test="${not empty sessionScope.usuarioLogueado && sessionScope.usuarioLogueado.class.simpleName == 'Organizador'}">
            <form action="${pageContext.request.contextPath}/evento/crear" method="post">
                <div>
                    <label for="nombre">Nombre del Evento:</label>
                    <input type="text" id="nombre" name="nombre" value="${param.nombre}" required>
                </div>
                <div>
                    <label for="descripcion">Descripción:</label>
                    <textarea id="descripcion" name="descripcion" rows="5" required>${param.descripcion}</textarea>
                </div>
                <div>
                    <label for="categoria">Categoría:</label>
                    <input type="text" id="categoria" name="categoria" value="${param.categoria}" required>
                    (Ej: Concierto, Conferencia, Deporte, Teatro)
                </div>
                <div>
                    <label for="fechaHora">Fecha y Hora:</label>
                    <input type="datetime-local" id="fechaHora" name="fechaHora" value="${param.fechaHora}" required>
                </div>
                <div>
                    <%-- En una app real, esto sería un selector de Lugares existentes o opción de crear nuevo --%>
                    <label for="lugarInfo">Información del Lugar (Simulado):</label>
                    <input type="text" id="lugarNombre" name="lugarNombre" placeholder="Nombre del Lugar (ej. Estadio Principal)" value="Lugar de Prueba Form" required><br/>
                    <input type="text" id="lugarDireccion" name="lugarDireccion" placeholder="Dirección del Lugar" value="Calle Falsa 123" required><br/>
                    <%-- <input type="hidden" name="lugarId" value="lugarSimulado123"> --%>
                </div>
                 <div>
                    <label for="capacidad">Capacidad Total Estimada:</label>
                    <input type="number" id="capacidad" name="capacidad" value="${param.capacidad != null ? param.capacidad : 100}" min="1" required>
                    (Si el lugar tiene capacidad definida, esta podría ser un override o ignorada)
                </div>

                <%-- Aquí iría la lógica para añadir Tipos de Entrada dinámicamente --%>
                <%-- Por ahora, se crearán tipos por defecto en el servlet o se añadirán después --%>
                <p><em>Los tipos de entrada (General, VIP, etc.) se podrán configurar después de crear el evento base, o se crearán unos por defecto.</em></p>

                <div>
                    <button type="submit">Crear Evento</button>
                </div>
            </form>
        </c:if>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
