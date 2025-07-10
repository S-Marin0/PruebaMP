<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title>Mis Entradas - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <c:if test="${param.showHeader == null || param.showHeader == true}">
        <jsp:include page="/WEB-INF/jsp/common/header.jsp" />
        <jsp:include page="/WEB-INF/jsp/common/navigation.jsp" />
    </c:if>

    <div class="container">
        <c:if test="${param.showHeader == null || param.showHeader == true}">
            <h2>Mis Entradas Compradas</h2>
        </c:if>

        <c:if test="${not empty sessionScope.usuarioLogueado && sessionScope.usuarioLogueado.class.simpleName == 'Asistente'}">
            <c:choose>
                <c:when test="${not empty listaMisCompras}">
                    <c:forEach var="compra" items="${listaMisCompras}">
                        <div class="compra-item">
                            <h4>Compra ID: ${compra.id} (Estado: ${compra.estadoCompra})</h4>
                            <p><strong>Evento:</strong> <a href="${pageContext.request.contextPath}/evento/detalle?id=${compra.evento.id}">${compra.evento.nombre}</a></p>
                            <p><strong>Fecha del Evento:</strong> <fmt:formatDate value="${compra.evento.fechaHora}" type="BOTH" dateStyle="medium" timeStyle="short"/></p>
                            <p><strong>Fecha de Compra:</strong> <fmt:formatDate value="${compra.fechaCompra}" type="BOTH" dateStyle="medium" timeStyle="short"/></p>
                            <p><strong>Total Pagado:</strong> <fmt:formatNumber value="${compra.totalPagado}" type="currency" currencySymbol="€"/></p>

                            <h5>Entradas Adquiridas en esta Compra:</h5>
                            <c:if test="${not empty compra.entradasCompradas}">
                                <ul>
                                    <c:forEach var="entrada" items="${compra.entradasCompradas}">
                                        <li>
                                            ID Entrada: ${entrada.id} <br/>
                                            Tipo: ${entrada.tipo} <br/>
                                            Descripción: ${entrada.descripcion} <br/>
                                            Precio Pagado por esta entrada: <fmt:formatNumber value="${entrada.precio}" type="currency" currencySymbol="€"/>
                                            <%-- Aquí podría ir un enlace a un "e-ticket" o QR --%>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:if>
                            <c:if test="${empty compra.entradasCompradas}">
                                <p>No hay detalles de entradas para esta compra (podría ser un error o estar pendiente).</p>
                            </c:if>
                            <hr/>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <p>Aún no has comprado ninguna entrada.</p>
                </c:otherwise>
            </c:choose>
        </c:if>
        <c:if test="${empty sessionScope.usuarioLogueado || sessionScope.usuarioLogueado.class.simpleName != 'Asistente'}">
            <p class="message error">Debes ser un asistente e iniciar sesión para ver tus entradas.</p>
        </c:if>

    </div>
    <c:if test="${param.showHeader == null || param.showHeader == true}">
      <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
    </c:if>
</body>
</html>
