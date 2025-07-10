<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Iniciar Sesión - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <jsp:include page="/jsp/common/navigation.jsp" />

    <div class="container">
        <h2>Iniciar Sesión</h2>

        <c:if test="${not empty param.mensaje}">
            <p class="message success">${param.mensaje}</p>
        </c:if>
        <c:if test="${not empty errorLogin}">
            <p class="message error">${errorLogin}</p>
        </c:if>
         <c:if test="${not empty errorGeneral}">
            <p class="message error">${errorGeneral}</p>
        </c:if>

        <form action="${pageContext.request.contextPath}/usuario/login" method="post">
            <div>
                <label for="email">Email:</label>
                <input type="email" id="email" name="email" value="${param.email}" required>
            </div>
            <div>
                <label for="password">Contraseña:</label>
                <input type="password" id="password" name="password" required>
            </div>
            <div>
                <button type="submit">Iniciar Sesión</button>
            </div>
        </form>
        <p>¿No tienes cuenta? <a href="${pageContext.request.contextPath}/usuario/registro">Regístrate aquí</a>.</p>
    </div>

    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
