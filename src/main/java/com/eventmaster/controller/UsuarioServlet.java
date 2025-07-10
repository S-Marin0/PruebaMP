package com.eventmaster.controller;

import com.eventmaster.model.entity.Asistente;
import com.eventmaster.model.entity.Organizador;
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.service.UsuarioService;
import com.eventmaster.service.NotificacionService; // Para notificar registro exitoso

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "UsuarioServlet", urlPatterns = {"/usuario/*"})
public class UsuarioServlet extends HttpServlet {

    private UsuarioService usuarioService;
    private NotificacionService notificacionService; // Opcional

    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext context = getServletContext();
        usuarioService = (UsuarioService) context.getAttribute("usuarioService");
        notificacionService = (NotificacionService) context.getAttribute("notificacionService");

        if (usuarioService == null) {
            throw new ServletException("UsuarioService no disponible.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        if (action == null) {
            action = "/login"; // Acción por defecto
        }
        System.out.println("UsuarioServlet: doGet, PathInfo: " + action);

        try {
            switch (action) {
                case "/login":
                    mostrarFormularioLogin(request, response);
                    break;
                case "/registro": // Un formulario genérico o una página que enlace a ambos
                    mostrarPaginaDeOpcionesRegistro(request, response);
                    break;
                case "/registro/asistente":
                    mostrarFormularioRegistroAsistente(request, response);
                    break;
                case "/registro/organizador":
                    mostrarFormularioRegistroOrganizador(request, response);
                    break;
                case "/logout":
                    procesarLogout(request, response);
                    break;
                case "/perfil":
                    mostrarPerfilUsuario(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Ruta de usuario no encontrada.");
                    break;
            }
        } catch (Exception e) {
            System.err.println("UsuarioServlet: Error en doGet - " + e.getMessage());
            request.setAttribute("errorGeneral", "Error procesando la solicitud de usuario: " + e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/error.jsp");
            dispatcher.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        System.out.println("UsuarioServlet: doPost, PathInfo: " + action);

        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no especificada para POST de usuario.");
            return;
        }

        try {
            switch (action) {
                case "/login":
                    procesarLogin(request, response);
                    break;
                case "/registro/asistente":
                    procesarRegistroAsistente(request, response);
                    break;
                case "/registro/organizador":
                    procesarRegistroOrganizador(request, response);
                    break;
                // case "/perfil/editar": // Ejemplo
                //     procesarEditarPerfil(request, response);
                //     break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción POST de usuario desconocida: " + action);
                    break;
            }
        } catch (Exception e) {
            System.err.println("UsuarioServlet: Error en doPost - " + e.getMessage());
            request.setAttribute("errorGeneral", "Error al procesar la acción de usuario: " + e.getMessage());
            // Reenviar al formulario relevante con el mensaje de error
            // Esto es simplificado, idealmente se redirige al form específico
            String formOrigen = "/jsp/usuario/login.jsp"; // Default
            if ("/registro/asistente".equals(action)) formOrigen = "/jsp/usuario/registroAsistente.jsp";
            if ("/registro/organizador".equals(action)) formOrigen = "/jsp/usuario/registroOrganizador.jsp";
            RequestDispatcher dispatcher = request.getRequestDispatcher(formOrigen);
            dispatcher.forward(request, response);
        }
    }

    private void mostrarFormularioLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/usuario/login.jsp");
        dispatcher.forward(request, response);
    }

    private void mostrarPaginaDeOpcionesRegistro(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Una JSP simple que ofrece link a registro de asistente u organizador
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/usuario/opcionesRegistro.jsp");
        dispatcher.forward(request, response);
    }


    private void mostrarFormularioRegistroAsistente(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/usuario/registroAsistente.jsp");
        dispatcher.forward(request, response);
    }

    private void mostrarFormularioRegistroOrganizador(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/usuario/registroOrganizador.jsp");
        dispatcher.forward(request, response);
    }

    private void procesarLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("errorLogin", "Email y contraseña son obligatorios.");
            mostrarFormularioLogin(request, response);
            return;
        }

        Usuario usuario = usuarioService.autenticarUsuario(email, password);

        if (usuario != null) {
            HttpSession session = request.getSession(true); // Crear sesión si no existe
            session.setAttribute("usuarioLogueado", usuario);
            System.out.println("UsuarioServlet: Usuario " + usuario.getEmail() + " logueado. Rol: " + usuario.getClass().getSimpleName());

            // Registrar en el mediador si aplica
            // MediadorCompras mediador = (MediadorCompras) getServletContext().getAttribute("mediadorCompras");
            // if (mediador != null) { mediador.registrarUsuario(usuario); }

            response.sendRedirect(request.getContextPath() + "/index?mensaje=Login exitoso");
        } else {
            request.setAttribute("errorLogin", "Email o contraseña incorrectos.");
            mostrarFormularioLogin(request, response);
        }
    }

    private void procesarRegistroAsistente(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombre = request.getParameter("nombre");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmarPassword = request.getParameter("confirmarPassword");

        if (nombre == null || nombre.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty()) {
            request.setAttribute("errorRegistro", "Todos los campos son obligatorios.");
            mostrarFormularioRegistroAsistente(request, response);
            return;
        }
        if (!password.equals(confirmarPassword)) {
            request.setAttribute("errorRegistro", "Las contraseñas no coinciden.");
            mostrarFormularioRegistroAsistente(request, response);
            return;
        }

        try {
            // ID se generará en el DAO o servicio si es nulo
            Asistente nuevoAsistente = new Asistente(null, nombre, email, password);
            usuarioService.registrarUsuario(nuevoAsistente);

            // Opcional: Notificar registro exitoso
            // if (notificacionService != null) { notificacionService.enviarBienvenida(nuevoAsistente); }

            System.out.println("UsuarioServlet: Asistente registrado: " + email);
            response.sendRedirect(request.getContextPath() + "/usuario/login?mensaje=Registro de asistente exitoso. Por favor, inicie sesión.");
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorRegistro", e.getMessage()); // Ej. email ya existe
            mostrarFormularioRegistroAsistente(request, response);
        }
    }

    private void procesarRegistroOrganizador(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombre = request.getParameter("nombre");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmarPassword = request.getParameter("confirmarPassword");
        String infoContacto = request.getParameter("infoContacto");


        if (nombre == null || nombre.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty() ||
            infoContacto == null || infoContacto.trim().isEmpty()) {
            request.setAttribute("errorRegistro", "Todos los campos son obligatorios.");
            mostrarFormularioRegistroOrganizador(request, response);
            return;
        }
        if (!password.equals(confirmarPassword)) {
            request.setAttribute("errorRegistro", "Las contraseñas no coinciden.");
            mostrarFormularioRegistroOrganizador(request, response);
            return;
        }

        try {
            Organizador nuevoOrganizador = new Organizador(null, nombre, email, password, infoContacto);
            usuarioService.registrarUsuario(nuevoOrganizador);

            System.out.println("UsuarioServlet: Organizador registrado: " + email);
            response.sendRedirect(request.getContextPath() + "/usuario/login?mensaje=Registro de organizador exitoso. Por favor, inicie sesión.");
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorRegistro", e.getMessage());
            mostrarFormularioRegistroOrganizador(request, response);
        }
    }

    private void procesarLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
            if(usuario != null) System.out.println("UsuarioServlet: Cerrando sesión para " + usuario.getEmail());
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/usuario/login?mensaje=Sesión cerrada exitosamente.");
    }

    private void mostrarPerfilUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect(request.getContextPath() + "/usuario/login?mensaje=Debe iniciar sesión para ver su perfil.");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        // Recargar usuario desde el servicio para obtener datos actualizados (ej. historial de compras)
        Optional<Usuario> usuarioActualizadoOpt = usuarioService.findUsuarioById(usuario.getId());

        if(!usuarioActualizadoOpt.isPresent()){
             session.invalidate(); // Algo raro pasó, el usuario de sesión no existe en BBDD
             response.sendRedirect(request.getContextPath() + "/usuario/login?mensaje=Error al cargar perfil, inicie sesión de nuevo.");
             return;
        }
        Usuario usuarioActualizado = usuarioActualizadoOpt.get();
        session.setAttribute("usuarioLogueado", usuarioActualizado); // Actualizar sesión
        request.setAttribute("usuario", usuarioActualizado);


        if (usuarioActualizado instanceof Asistente) {
            // Asistente asistente = (Asistente) usuarioActualizado;
            // El historial de compras ya se carga en findUsuarioById dentro de UsuarioService
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/usuario/perfilAsistente.jsp");
            dispatcher.forward(request, response);
        } else if (usuarioActualizado instanceof Organizador) {
            // Organizador organizador = (Organizador) usuarioActualizado;
            // Cargar eventos creados por el organizador
            // List<Evento> eventosCreados = eventoService.findEventosPorOrganizador(organizador);
            // request.setAttribute("eventosCreados", eventosCreados);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/usuario/perfilOrganizador.jsp");
            dispatcher.forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tipo de usuario desconocido para perfil.");
        }
    }
}
