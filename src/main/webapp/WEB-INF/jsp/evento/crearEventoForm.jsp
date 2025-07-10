<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Crear Nuevo Evento - EventMaster</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <jsp:include page="/jsp/common/navigation.jsp" />

    <div class="container">
        <h2>Crear Nuevo Evento</h2>

        <c:if test="${not empty errorCrearEvento}">
            <p class="message error">${errorCrearEvento}</p>
        </c:if>

        <c:choose>
            <c:when test="${esOrganizador}">
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
                        <label for="lugarNombre">Nombre del Lugar:</label>
                        <input type="text" id="lugarNombre" name="lugarNombre" value="Lugar de Prueba Form" required><br/>
                        <label for="lugarDireccion">Dirección del Lugar:</label>
                        <input type="text" id="lugarDireccion" name="lugarDireccion" value="Calle Falsa 123" required>
                    </div>
                    <div>
                        <label for="capacidad">Capacidad Total Estimada:</label>
                        <input type="number" id="capacidad" name="capacidad" value="${param.capacidad != null ? param.capacidad : 100}" min="1" required>
                    </div>

                    <hr/>
                    <h4>Definir Tipos de Entrada (hasta 3)</h4>

                    <%-- Tipo de Entrada 1 --%>
                    <h5>Tipo de Entrada 1 (Obligatorio)</h5>
                    <div>
                        <label for="tipoEntradaNombre_1">Nombre del Tipo de Entrada 1:</label>
                        <input type="text" id="tipoEntradaNombre_1" name="tipoEntradaNombre_1" value="${not empty param.tipoEntradaNombre_1 ? param.tipoEntradaNombre_1 : 'General'}" required>
                    </div>
                    <div>
                        <label for="tipoEntradaPrecioBase_1">Precio Base 1 (€):</label>
                        <input type="number" id="tipoEntradaPrecioBase_1" name="tipoEntradaPrecioBase_1" value="${not empty param.tipoEntradaPrecioBase_1 ? param.tipoEntradaPrecioBase_1 : '25.00'}" step="0.01" min="0" required>
                    </div>
                    <div>
                        <label for="tipoEntradaCantidadTotal_1">Cantidad Disponible Tipo 1:</label>
                        <input type="number" id="tipoEntradaCantidadTotal_1" name="tipoEntradaCantidadTotal_1" value="${not empty param.tipoEntradaCantidadTotal_1 ? param.tipoEntradaCantidadTotal_1 : '100'}" min="1" required>
                    </div>
                     <div>
                        <label for="tipoEntradaLimiteCompra_1">Límite Compra Usuario Tipo 1:</label>
                        <input type="number" id="tipoEntradaLimiteCompra_1" name="tipoEntradaLimiteCompra_1" value="${not empty param.tipoEntradaLimiteCompra_1 ? param.tipoEntradaLimiteCompra_1 : '10'}" min="1" required>
                    </div>
                    <div>
                        <input type="checkbox" id="ofreceMercanciaOpcional_1" name="ofreceMercanciaOpcional_1" value="true" ${param.ofreceMercanciaOpcional_1 == 'true' ? 'checked' : ''}>
                        <label for="ofreceMercanciaOpcional_1">Ofrecer Mercancía Opcional</label><br/>
                        <label for="descripcionMercancia_1">Descripción Mercancía Tipo 1:</label>
                        <input type="text" id="descripcionMercancia_1" name="descripcionMercancia_1" value="${param.descripcionMercancia_1}"><br/>
                        <label for="precioAdicionalMercancia_1">Precio Adicional Mercancía Tipo 1 (€):</label>
                        <input type="number" id="precioAdicionalMercancia_1" name="precioAdicionalMercancia_1" value="${param.precioAdicionalMercancia_1}" step="0.01" min="0">
                    </div>
                    <div>
                        <input type="checkbox" id="ofreceDescuentoOpcional_1" name="ofreceDescuentoOpcional_1" value="true" ${param.ofreceDescuentoOpcional_1 == 'true' ? 'checked' : ''}>
                        <label for="ofreceDescuentoOpcional_1">Ofrecer Descuento Opcional</label><br/>
                        <label for="descripcionDescuento_1">Descripción Descuento Tipo 1:</label>
                        <input type="text" id="descripcionDescuento_1" name="descripcionDescuento_1" value="${param.descripcionDescuento_1}"><br/>
                        <label for="montoDescuentoFijo_1">Monto Descuento Fijo Tipo 1 (€):</label>
                        <input type="number" id="montoDescuentoFijo_1" name="montoDescuentoFijo_1" value="${param.montoDescuentoFijo_1}" step="0.01" min="0">
                    </div>
                    <br/>

                    <%-- Tipo de Entrada 2 (Opcional) --%>
                    <h5>Tipo de Entrada 2 (Opcional)</h5>
                    <div>
                        <label for="tipoEntradaNombre_2">Nombre del Tipo de Entrada 2:</label>
                        <input type="text" id="tipoEntradaNombre_2" name="tipoEntradaNombre_2" value="${param.tipoEntradaNombre_2}">
                    </div>
                    <div>
                        <label for="tipoEntradaPrecioBase_2">Precio Base 2 (€):</label>
                        <input type="number" id="tipoEntradaPrecioBase_2" name="tipoEntradaPrecioBase_2" value="${param.tipoEntradaPrecioBase_2}" step="0.01" min="0">
                    </div>
                    <div>
                        <label for="tipoEntradaCantidadTotal_2">Cantidad Disponible Tipo 2:</label>
                        <input type="number" id="tipoEntradaCantidadTotal_2" name="tipoEntradaCantidadTotal_2" value="${param.tipoEntradaCantidadTotal_2}" min="0">
                    </div>
                     <div>
                        <label for="tipoEntradaLimiteCompra_2">Límite Compra Usuario Tipo 2:</label>
                        <input type="number" id="tipoEntradaLimiteCompra_2" name="tipoEntradaLimiteCompra_2" value="${param.tipoEntradaLimiteCompra_2}" min="0">
                    </div>
                    <div>
                        <input type="checkbox" id="ofreceMercanciaOpcional_2" name="ofreceMercanciaOpcional_2" value="true" ${param.ofreceMercanciaOpcional_2 == 'true' ? 'checked' : ''}>
                        <label for="ofreceMercanciaOpcional_2">Ofrecer Mercancía Opcional</label><br/>
                        <label for="descripcionMercancia_2">Descripción Mercancía Tipo 2:</label>
                        <input type="text" id="descripcionMercancia_2" name="descripcionMercancia_2" value="${param.descripcionMercancia_2}"><br/>
                        <label for="precioAdicionalMercancia_2">Precio Adicional Mercancía Tipo 2 (€):</label>
                        <input type="number" id="precioAdicionalMercancia_2" name="precioAdicionalMercancia_2" value="${param.precioAdicionalMercancia_2}" step="0.01" min="0">
                    </div>
                    <div>
                        <input type="checkbox" id="ofreceDescuentoOpcional_2" name="ofreceDescuentoOpcional_2" value="true" ${param.ofreceDescuentoOpcional_2 == 'true' ? 'checked' : ''}>
                        <label for="ofreceDescuentoOpcional_2">Ofrecer Descuento Opcional</label><br/>
                        <label for="descripcionDescuento_2">Descripción Descuento Tipo 2:</label>
                        <input type="text" id="descripcionDescuento_2" name="descripcionDescuento_2" value="${param.descripcionDescuento_2}"><br/>
                        <label for="montoDescuentoFijo_2">Monto Descuento Fijo Tipo 2 (€):</label>
                        <input type="number" id="montoDescuentoFijo_2" name="montoDescuentoFijo_2" value="${param.montoDescuentoFijo_2}" step="0.01" min="0">
                    </div>
                    <br/>

                    <%-- Tipo de Entrada 3 (Opcional) --%>
                    <h5>Tipo de Entrada 3 (Opcional)</h5>
                    <div>
                        <label for="tipoEntradaNombre_3">Nombre del Tipo de Entrada 3:</label>
                        <input type="text" id="tipoEntradaNombre_3" name="tipoEntradaNombre_3" value="${param.tipoEntradaNombre_3}">
                    </div>
                    <div>
                        <label for="tipoEntradaPrecioBase_3">Precio Base 3 (€):</label>
                        <input type="number" id="tipoEntradaPrecioBase_3" name="tipoEntradaPrecioBase_3" value="${param.tipoEntradaPrecioBase_3}" step="0.01" min="0">
                    </div>
                    <div>
                        <label for="tipoEntradaCantidadTotal_3">Cantidad Disponible Tipo 3:</label>
                        <input type="number" id="tipoEntradaCantidadTotal_3" name="tipoEntradaCantidadTotal_3" value="${param.tipoEntradaCantidadTotal_3}" min="0">
                    </div>
                     <div>
                        <label for="tipoEntradaLimiteCompra_3">Límite Compra Usuario Tipo 3:</label>
                        <input type="number" id="tipoEntradaLimiteCompra_3" name="tipoEntradaLimiteCompra_3" value="${param.tipoEntradaLimiteCompra_3}" min="0">
                    </div>
                    <div>
                        <input type="checkbox" id="ofreceMercanciaOpcional_3" name="ofreceMercanciaOpcional_3" value="true" ${param.ofreceMercanciaOpcional_3 == 'true' ? 'checked' : ''}>
                        <label for="ofreceMercanciaOpcional_3">Ofrecer Mercancía Opcional</label><br/>
                        <label for="descripcionMercancia_3">Descripción Mercancía Tipo 3:</label>
                        <input type="text" id="descripcionMercancia_3" name="descripcionMercancia_3" value="${param.descripcionMercancia_3}"><br/>
                        <label for="precioAdicionalMercancia_3">Precio Adicional Mercancía Tipo 3 (€):</label>
                        <input type="number" id="precioAdicionalMercancia_3" name="precioAdicionalMercancia_3" value="${param.precioAdicionalMercancia_3}" step="0.01" min="0">
                    </div>
                    <div>
                        <input type="checkbox" id="ofreceDescuentoOpcional_3" name="ofreceDescuentoOpcional_3" value="true" ${param.ofreceDescuentoOpcional_3 == 'true' ? 'checked' : ''}>
                        <label for="ofreceDescuentoOpcional_3">Ofrecer Descuento Opcional</label><br/>
                        <label for="descripcionDescuento_3">Descripción Descuento Tipo 3:</label>
                        <input type="text" id="descripcionDescuento_3" name="descripcionDescuento_3" value="${param.descripcionDescuento_3}"><br/>
                        <label for="montoDescuentoFijo_3">Monto Descuento Fijo Tipo 3 (€):</label>
                        <input type="number" id="montoDescuentoFijo_3" name="montoDescuentoFijo_3" value="${param.montoDescuentoFijo_3}" step="0.01" min="0">
                    </div>
                    <hr/>

                    <div>
                        <button type="submit">Crear Evento</button>
                    </div>
                </form>
            </c:when>
            <c:otherwise>
                <p class="message error">Acceso denegado. Debe ser un organizador para crear eventos.</p>
            </c:otherwise>
        </c:choose>
    </div>

    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
