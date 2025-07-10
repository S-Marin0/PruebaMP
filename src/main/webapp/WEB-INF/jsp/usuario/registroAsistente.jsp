<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Registro de Asistente - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <jsp:include page="/jsp/common/navigation.jsp" />

    <div class="container">
        <h2>Registro de Nuevo Asistente</h2>

        <c:if test="${not empty errorRegistro}">
            <p class="message error">${errorRegistro}</p>
        </c:if>

        <form action="${pageContext.request.contextPath}/usuario/registro/asistente" method="post">
            <div>
                <label for="nombre">Nombre Completo:</label>
                <input type="text" id="nombre" name="nombre" value="${param.nombre}" required>
            </div>
            <div>
                <label for="email">Email:</label>
                <input type="email" id="email" name="email" value="${param.email}" required>
            </div>
            <div>
                <label for="password">Contraseña:</label>
                <input type="password" id="password" name="password" required>
            </div>
            <div>
                <label for="confirmarPassword">Confirmar Contraseña:</label>
                <input type="password" id="confirmarPassword" name="confirmarPassword" required>
            </div>
            <div>
                <button type="submit">Registrarme como Asistente</button>
            </div>
        </form>
        <p>¿Ya tienes una cuenta? <a href="${pageContext.request.contextPath}/usuario/login">Inicia sesión</a>.</p>
        <p>¿Quieres registrarte como Organizador? <a href="${pageContext.request.contextPath}/usuario/registro/organizador">Regístrate aquí</a>.</p>
    </div>

    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
