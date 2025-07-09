<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<nav>
    <div class="container">
        <ul>
            <li><a href="${pageContext.request.contextPath}/">Inicio</a></li>
            <li><a href="${pageContext.request.contextPath}/eventos">Eventos</a></li>
            <c:choose>
                <c:when test="${not empty sessionScope.usuarioLogueado}">
                    <li><a href="${pageContext.request.contextPath}/usuario/perfil">Mi Perfil (${sessionScope.usuarioLogueado.nombre})</a></li>
                    <c:if test="${sessionScope.usuarioLogueado.class.simpleName == 'Asistente'}">
                         <li><a href="${pageContext.request.contextPath}/compra/mis-entradas">Mis Entradas</a></li>
                    </c:if>
                    <c:if test="${sessionScope.usuarioLogueado.class.simpleName == 'Organizador'}">
                        <li><a href="${pageContext.request.contextPath}/evento/crear">Crear Evento</a></li>
                        <%-- Otros enlaces para organizador --%>
                    </c:if>
                    <li><a href="${pageContext.request.contextPath}/usuario/logout">Cerrar Sesión</a></li>
                </c:when>
                <c:otherwise>
                    <li><a href="${pageContext.request.contextPath}/usuario/login">Iniciar Sesión</a></li>
                    <li><a href="${pageContext.request.contextPath}/usuario/registro">Registrarse</a></li>
                </c:otherwise>
            </c:choose>
        </ul>
    </div>
</nav>
