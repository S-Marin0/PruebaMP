<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title>Confirmar Compra - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <jsp:include page="/jsp/common/navigation.jsp" />

    <div class="container">
        <h2>Confirmar Compra</h2>

        <c:if test="${not empty errorCompra}">
            <p class="message error">${errorCompra}</p>
        </c:if>
        <c:if test="${not empty errorGeneral}">
            <p class="message error">${errorGeneral}</p>
        </c:if>

        <c:if test="${empty evento || empty tipoEntradaNombre || empty cantidad || empty precioUnitario}">
            <p class="message error">No hay información de compra para confirmar. Por favor, seleccione entradas desde la página del evento.</p>
            <p><a href="${pageContext.request.contextPath}/eventos">Ver eventos</a></p>
        </c:if>

        <c:if test="${not empty evento && not empty tipoEntradaNombre && not empty cantidad && not empty precioUnitario}">
            <h3>Resumen del Pedido:</h3>
            <p><strong>Evento:</strong> ${evento.nombre}</p>
            <p><strong>Fecha:</strong> <fmt:formatDate value="${evento.fechaHora}" type="BOTH" dateStyle="medium" timeStyle="short"/></p>
            <p><strong>Lugar:</strong> ${evento.lugar.nombre}</p>
            <hr/>
            <p><strong>Tipo de Entrada:</strong> ${tipoEntradaNombre}</p>
            <p><strong>Cantidad:</strong> ${cantidad}</p>
            <p><strong>Precio Unitario:</strong> <fmt:formatNumber value="${precioUnitario}" type="currency" currencySymbol="€"/></p>
            <p><strong>Total Provisional:</strong> <fmt:formatNumber value="${totalProvisional}" type="currency" currencySymbol="€"/></p>
            <hr/>

            <form action="${pageContext.request.contextPath}/compra/procesar-pago" method="post">
                <%-- Estos campos se recuperan en el Servlet desde la sesión, pero podrían pasarse como hidden si fuera necesario --%>
                <%--
                <input type="hidden" name="eventoId" value="${evento.id}">
                <input type="hidden" name="tipoEntradaNombre" value="${tipoEntradaNombre}">
                <input type="hidden" name="cantidad" value="${cantidad}">
                --%>

                <h4>Detalles de Pago (Simulado)</h4>
                <div>
                    <label for="nombreTitular">Nombre del Titular de la Tarjeta:</label>
                    <input type="text" id="nombreTitular" name="nombreTitular" required>
                </div>
                <div>
                    <label for="numeroTarjeta">Número de Tarjeta:</label>
                    <input type="text" id="numeroTarjeta" name="numeroTarjeta" placeholder="XXXX-XXXX-XXXX-XXXX" required>
                </div>
                <div>
                    <label for="fechaExpiracion">Fecha de Expiración (MM/AA):</label>
                    <input type="text" id="fechaExpiracion" name="fechaExpiracion" placeholder="MM/AA" required>
                </div>
                <div>
                    <label for="cvv">CVV:</label>
                    <input type="text" id="cvv" name="cvv" placeholder="XXX" required>
                </div>
                <br/>
                <div>
                    <label for="codigoDescuento">Código de Descuento (Opcional):</label>
                    <input type="text" id="codigoDescuento" name="codigoDescuento">
                </div>
                <br/>
                <button type="submit">Pagar y Confirmar Compra</button>
            </form>
        </c:if>
         <p><a href="${pageContext.request.contextPath}/evento/detalle?id=${evento.id}">Volver al detalle del evento</a></p>
    </div>

    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
