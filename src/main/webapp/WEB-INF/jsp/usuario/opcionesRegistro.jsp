<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Registrarse - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <jsp:include page="/jsp/common/navigation.jsp" />

    <div class="container">
        <h2>Registrarse en EventMaster</h2>
        <p>Selecciona el tipo de cuenta que deseas crear:</p>
        <ul>
            <li><a href="${pageContext.request.contextPath}/usuario/registro/asistente" class="button">Registrarme como Asistente</a> (Para comprar entradas y asistir a eventos)</li>
            <li><a href="${pageContext.request.contextPath}/usuario/registro/organizador" class="button">Registrarme como Organizador</a> (Para crear y gestionar eventos)</li>
        </ul>
        <p>¿Ya tienes una cuenta? <a href="${pageContext.request.contextPath}/usuario/login">Inicia sesión aquí</a>.</p>
    </div>

    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
