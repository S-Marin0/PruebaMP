<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Compra Exitosa - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <jsp:include page="/jsp/common/navigation.jsp" />

    <div class="container">
        <h2>¡Compra Realizada con Éxito!</h2>

        <c:if test="${not empty mensajeExito}">
            <p class="message success">${mensajeExito}</p>
        </c:if>
        <c:if test="${not empty compraId}">
            <p>Gracias por tu compra. Tu ID de compra es: <strong>${compraId}</strong>.</p>
            <p>Recibirás un email de confirmación con los detalles y tus entradas digitales.</p>
            <%-- En una app real, aquí se mostrarían los detalles de la compra recuperada y las entradas --%>
        </c:if>
        <c:if test="${empty compraId && empty mensajeExito}">
            <p class="message warning">No hay detalles de compra para mostrar, pero la operación pudo haber sido exitosa.</p>
        </c:if>


        <p><a href="${pageContext.request.contextPath}/compra/mis-entradas">Ver Mis Entradas</a></p>
        <p><a href="${pageContext.request.contextPath}/eventos">Seguir Explorando Eventos</a></p>
    </div>

    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
