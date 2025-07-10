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
                    <h4>Definir un Tipo de Entrada Principal</h4>
                    <div>
                        <label for="tipoEntradaNombre">Nombre del Tipo de Entrada:</label>
                        <input type="text" id="tipoEntradaNombre" name="tipoEntradaNombre" value="${not empty param.tipoEntradaNombre ? param.tipoEntradaNombre : 'General'}" required>
                        (Ej: General, VIP, Preventa)
                    </div>
                    <div>
                        <label for="tipoEntradaPrecioBase">Precio Base (€):</label>
                        <input type="number" id="tipoEntradaPrecioBase" name="tipoEntradaPrecioBase" value="${not empty param.tipoEntradaPrecioBase ? param.tipoEntradaPrecioBase : '25.00'}" step="0.01" min="0" required>
                    </div>
                    <div>
                        <label for="tipoEntradaCantidadTotal">Cantidad Disponible para este Tipo:</label>
                        <input type="number" id="tipoEntradaCantidadTotal" name="tipoEntradaCantidadTotal" value="${not empty param.tipoEntradaCantidadTotal ? param.tipoEntradaCantidadTotal : '100'}" min="1" required>
                    </div>
                     <div>
                        <label for="tipoEntradaLimiteCompra">Límite de Compra por Usuario (para este tipo):</label>
                        <input type="number" id="tipoEntradaLimiteCompra" name="tipoEntradaLimiteCompra" value="${not empty param.tipoEntradaLimiteCompra ? param.tipoEntradaLimiteCompra : '10'}" min="1" required>
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
