<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Error - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <jsp:include page="/jsp/common/navigation.jsp" />

    <div class="container">
        <h2>Oops! Ha ocurrido un error.</h2>

        <p class="message error">
            <c:choose>
                <c:when test="${not empty requestScope['jakarta.servlet.error.message']}">
                    Mensaje del Servlet: ${requestScope['jakarta.servlet.error.message']}
                </c:when>
                <c:when test="${not empty errorGeneral}">
                    ${errorGeneral}
                </c:when>
                <c:when test="${not empty exception}">
                    Detalle de la Excepción: ${exception.message}
                </c:when>
                <c:otherwise>
                    Se ha producido un error inesperado. Por favor, inténtelo de nuevo más tarde.
                </c:otherwise>
            </c:choose>
        </p>

        <c:if test="${not empty requestScope['jakarta.servlet.error.status_code']}">
            <p>Código de Estado: ${requestScope['jakarta.servlet.error.status_code']}</p>
        </c:if>
        <c:if test="${not empty requestScope['jakarta.servlet.error.request_uri']}">
            <p>URI Solicitada: ${requestScope['jakarta.servlet.error.request_uri']}</p>
        </c:if>

        <%-- En desarrollo, podría ser útil mostrar más detalles de la excepción --%>
        <%--
        <c:if test="${not empty exception}">
            <pre>
            <c:forEach var="trace" items="${exception.stackTrace}">
                ${trace}
            </c:forEach>
            </pre>
        </c:if>
        --%>

        <p><a href="${pageContext.request.contextPath}/">Volver a la Página de Inicio</a></p>
    </div>

    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
