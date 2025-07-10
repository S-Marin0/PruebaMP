<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.eventmaster.model.entity.Asistente, com.eventmaster.model.entity.Organizador" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<nav>
    <div class="container">
        <ul>
            <li><a href="${pageContext.request.contextPath}/">Inicio</a></li>
            <li><a href="${pageContext.request.contextPath}/eventos">Eventos</a></li>
            <c:choose>
                <c:when test="${not empty sessionScope.usuarioLogueado}">
                    <li>
                        <a href="${pageContext.request.contextPath}/usuario/perfil">
                            Mi Perfil (${sessionScope.usuarioLogueado.nombre})
                        </a>
                    </li>

                    <!-- Solo para Asistente -->
                    <c:if test="${fn:contains(sessionScope.usuarioLogueado['class']['name'], 'Asistente')}">
                        <li>
                            <a href="${pageContext.request.contextPath}/compra/mis-entradas">
                                Mis Entradas
                            </a>
                        </li>
                    </c:if>

                    <!-- Solo para Organizador -->
                    <c:if test="${fn:contains(sessionScope.usuarioLogueado['class']['name'], 'Organizador')}">
                        <li>
                            <a href="${pageContext.request.contextPath}/evento/crear">
                                Crear Evento
                            </a>
                        </li>
                    </c:if>

                    <li>
                        <a href="${pageContext.request.contextPath}/usuario/logout">Cerrar Sesión</a>
                    </li>
                </c:when>
                <c:otherwise>
                    <li>
                        <a href="${pageContext.request.contextPath}/usuario/login">Iniciar Sesión</a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/usuario/registro">Registrarse</a>
                    </li>
                </c:otherwise>
            </c:choose>
        </ul>
    </div>
</nav>
