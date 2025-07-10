<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title>Mis Entradas - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <jsp:include page="/jsp/common/navigation.jsp" />

    <div class="container">
        <c:if test="${param.showHeader == null || param.showHeader == 'true'}">
            <h2>Mis Entradas Compradas</h2>
        </c:if>

        <c:if test="${not empty errorGeneral}">
            <p class="message error">${errorGeneral}</p>
        </c:if>

        <c:choose>
            <c:when test="${not empty listaMisCompras}">
                <c:forEach var="compra" items="${listaMisCompras}">
                    <div class="compra-item" style="border: 1px solid #ccc; margin-bottom: 20px; padding: 15px;">
                        <h3>Evento: <c:out value="${compra.evento.nombre}"/></h3>
                        <p>
                            <strong>ID Compra:</strong> <c:out value="${compra.id}"/> <br/>
                            <strong>Fecha de Compra:</strong>
                            <c:if test="${not empty compra.fechaCompra}">
                                <fmt:formatDate value="${compra.fechaCompra}" type="BOTH" dateStyle="long" timeStyle="medium"/>
                            </c:if>
                            <c:if test="${empty compra.fechaCompra}">
                                N/A
                            </c:if>
                            <br/>
                            <strong>Estado Compra:</strong> <c:out value="${compra.estadoCompra}"/> <br/>
                            <strong>Total Pagado:</strong> <fmt:formatNumber value="${compra.totalPagado}" type="currency" currencySymbol="€"/>
                        </p>
                        <h4>Entradas Adquiridas:</h4>
                        <c:if test="${not empty compra.entradasCompradas}">
                            <ul>
                                <c:forEach var="entrada" items="${compra.entradasCompradas}">
                                    <li>
                                        <strong>ID Entrada:</strong> <c:out value="${entrada.id}"/> <br/>
                                        <strong>Tipo:</strong> <c:out value="${entrada.tipo}"/> <br/>
                                        <strong>Precio Pagado:</strong> <fmt:formatNumber value="${entrada.precio}" type="currency" currencySymbol="€"/> <br/>
                                        <em><c:out value="${entrada.descripcion}"/></em>
                                        <%-- Aquí se podría añadir un enlace o botón para "Ver QR" o "Descargar PDF" si existiera esa funcionalidad --%>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:if>
                        <c:if test="${empty compra.entradasCompradas}">
                            <p>No hay detalles de entradas para esta compra.</p>
                        </c:if>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <p>No has realizado ninguna compra todavía o no se pudieron cargar tus entradas.</p>
            </c:otherwise>
        </c:choose>
        <p><a href="${pageContext.request.contextPath}/eventos">Ver más eventos</a></p>
    </div>

    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
