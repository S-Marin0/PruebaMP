package com.eventmaster.controller;

import com.eventmaster.model.entity.Evento; // Asegurar que esté presente
import com.eventmaster.model.entity.Organizador; // Para creación/edición
import com.eventmaster.model.entity.Usuario;
// ELIMINAR: import com.eventmaster.model.pattern.builder.EventoBuilder;
import com.eventmaster.service.EventoService;
// ELIMINAR: import com.eventmaster.service.LugarService;
import com.eventmaster.service.UsuarioService; // Para obtener organizador de sesión

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
// Importar DAOs o servicios necesarios para crear/editar eventos si se hace aquí
// import com.eventmaster.model.entity.Lugar;


// Un Servlet para manejar múltiples acciones relacionadas con Eventos
// Se podría dividir en Servlets más pequeños si crece mucho.
@WebServlet(name = "EventoServlet", urlPatterns = {"/eventos", "/evento/*"})
public class EventoServlet extends HttpServlet {

    private EventoService eventoService;
    // ELIMINAR: private LugarService lugarService;
    private UsuarioService usuarioService;


    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext context = getServletContext();
        eventoService = (EventoService) context.getAttribute("eventoService");
        // ELIMINAR: lugarService = (LugarService) context.getAttribute("lugarService");
        usuarioService = (UsuarioService) context.getAttribute("usuarioService");

        if (eventoService == null) {
            throw new ServletException("EventoService no disponible.");
        }
         if (usuarioService == null) {
            throw new ServletException("UsuarioService no disponible.");
        }
        // lugarService puede ser opcional dependiendo de las acciones implementadas
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo(); // Obtiene la parte de la URL después de /evento
        if (action == null) { // Si la URL es solo /eventos (sin /evento/*)
            if ("/eventos".equals(request.getServletPath())) {
                listarEventos(request, response);
                return;
            }
            action = "/lista"; // Acción por defecto si es /evento/
        }

        System.out.println("EventoServlet: doGet, PathInfo: " + action);

        try {
            switch (action) {
                case "/detalle":
                    verDetalleEvento(request, response);
                    break;
                case "/crear":
                    mostrarFormularioCrearEvento(request, response);
                    break;
                case "/editar":
                    mostrarFormularioEditarEvento(request, response);
                    break;
                // case "/eliminar": // GET para eliminar no es ideal, mejor POST o un GET de confirmación
                //     confirmarEliminarEvento(request, response);
                //     break;
                case "/lista":
                default: // Si es /eventos o /evento/ o /evento/lista
                    listarEventos(request, response);
                    break;
            }
        } catch (Exception e) {
            System.err.println("EventoServlet: Error en doGet - " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorGeneral", "Error procesando la solicitud de evento: " + e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
            dispatcher.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        System.out.println("EventoServlet: doPost, PathInfo: " + action);

        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no especificada para POST.");
            return;
        }

        try {
            switch (action) {
                case "/crear":
                    procesarCrearEvento(request, response);
                    break;
                case "/editar":
                    procesarEditarEvento(request, response);
                    break;
                case "/eliminar":
                    procesarEliminarEvento(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción POST desconocida: " + action);
                    break;
            }
        } catch (Exception e) {
            System.err.println("EventoServlet: Error en doPost - " + e.getMessage());
            e.printStackTrace();
            // Guardar el error y reenviar al formulario correspondiente o a una página de error
            request.setAttribute("errorGeneral", "Error al procesar la acción del evento: " + e.getMessage());
             // Determinar a dónde redirigir en caso de error POST puede ser complejo,
            // a veces es mejor mostrar el error en la misma página del formulario si es posible.
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/error.jsp"); // Página genérica
            dispatcher.forward(request, response);
        }
    }

    private void listarEventos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("EventoServlet: Accediendo a listarEventos");
        String categoria = request.getParameter("categoria");
        // Otros filtros: fecha, precio, popularidad, etc.

        List<Evento> listaEventos;
        if (categoria != null && !categoria.trim().isEmpty()) {
            listaEventos = eventoService.findEventosPorCategoria(categoria);
            request.setAttribute("filtroCategoria", categoria);
        } else {
            listaEventos = eventoService.getAllEventos(); // O findEventosPublicadosYFuturos()
        }

        request.setAttribute("listaEventos", listaEventos);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/evento/listaEventos.jsp");
        dispatcher.forward(request, response);
    }

    private void verDetalleEvento(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idEvento = request.getParameter("id");
        if (idEvento == null || idEvento.trim().isEmpty()) {
            request.setAttribute("errorGeneral", "ID de evento no proporcionado.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
            dispatcher.forward(request, response);
            return;
        }
        System.out.println("EventoServlet: Accediendo a verDetalleEvento para ID: " + idEvento);

        Optional<Evento> eventoOpt = eventoService.findEventoById(idEvento);
        if (eventoOpt.isPresent()) {
            request.setAttribute("evento", eventoOpt.get());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/evento/detalleEvento.jsp");
            dispatcher.forward(request, response);
        } else {
            request.setAttribute("errorGeneral", "Evento no encontrado con ID: " + idEvento);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
            dispatcher.forward(request, response);
        }
    }

    private void mostrarFormularioCrearEvento(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario usuarioLogueado = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (!(usuarioLogueado instanceof Organizador)) {
            response.sendRedirect(request.getContextPath() + "/login?mensaje=Acceso denegado. Debe ser organizador.");
            return;
        }
        // ELIMINAR USO:
        // // Aquí se podrían cargar datos necesarios para el formulario, como lista de lugares disponibles
        // // List<Lugar> lugares = lugarService.findAll();
        // // request.setAttribute("lugares", lugares);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/evento/crearEventoForm.jsp");
        dispatcher.forward(request, response);
    }

    private void procesarCrearEvento(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario usuarioLogueado = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (!(usuarioLogueado instanceof Organizador)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado.");
            return;
        }
        Organizador organizador = (Organizador) usuarioLogueado;

        try {
            String nombre = request.getParameter("nombre");
            String descripcion = request.getParameter("descripcion");
            String categoria = request.getParameter("categoria");
            String fechaHoraStr = request.getParameter("fechaHora"); // Formato esperado: YYYY-MM-DDTHH:MM
            // String lugarId = request.getParameter("lugarId"); // Asumiendo que se selecciona un lugar
            int capacidad = Integer.parseInt(request.getParameter("capacidad")); // Podría venir del lugar

            // Validaciones básicas
            if (nombre == null || nombre.trim().isEmpty() || fechaHoraStr == null || fechaHoraStr.trim().isEmpty()) {
                request.setAttribute("errorCrearEvento", "Nombre y Fecha/Hora son obligatorios.");
                mostrarFormularioCrearEvento(request, response); // Reenviar al formulario con error
                return;
            }
            LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr);

            // Simulación de Lugar (en una app real, se obtendría de la BBDD o se crearía)
            // Optional<Lugar> lugarOpt = lugarService.findById(lugarId);
            // if(!lugarOpt.isPresent()){ /* error */ }
            // Lugar lugar = lugarOpt.get();
            com.eventmaster.model.entity.Lugar lugarSimulado = new com.eventmaster.model.entity.Lugar("lugarTemp123", "Lugar de Prueba", "Dirección de Prueba");
            lugarSimulado.agregar(new com.eventmaster.model.entity.Asiento("A1")); // Para que tenga capacidad > 0

            // CAMBIO: Usar Evento.EventoBuilder
            Evento.EventoBuilder builder = new Evento.EventoBuilder(nombre, organizador, lugarSimulado, fechaHora)
                .setDescripcion(descripcion)
                .setCategoria(categoria)
                .setCapacidadTotal(capacidad > 0 ? capacidad : lugarSimulado.obtenerCapacidadTotal());

            // Aquí se podrían añadir Tipos de Entrada desde el formulario
            // Ejemplo: builder.addTipoEntrada("General", new com.eventmaster.model.pattern.factory.TipoEntrada("General", 25.00, 100, 5));

            Evento eventoParaGuardar = builder.build(); // Construir el evento primero
            Evento eventoGuardado = eventoService.registrarNuevoEvento(eventoParaGuardar); // Luego pasarlo al servicio


            System.out.println("EventoServlet: Evento creado con ID: " + eventoGuardado.getId());
            response.sendRedirect(request.getContextPath() + "/evento/detalle?id=" + eventoGuardado.getId() + "&mensaje=Evento creado exitosamente");

        } catch (DateTimeParseException e) {
            request.setAttribute("errorCrearEvento", "Formato de Fecha/Hora inválido. Use YYYY-MM-DDTHH:MM");
            mostrarFormularioCrearEvento(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("errorCrearEvento", "Capacidad debe ser un número.");
            mostrarFormularioCrearEvento(request, response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("errorCrearEvento", "Error al crear evento: " + e.getMessage());
            mostrarFormularioCrearEvento(request, response);
        }
    }

    private void mostrarFormularioEditarEvento(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Similar a mostrarFormularioCrearEvento, pero carga los datos del evento existente
        // y verifica permisos.
        // ... (implementación omitida por brevedad, pero seguiría el patrón)
        request.setAttribute("errorGeneral", "Funcionalidad Editar Evento (GET) no implementada completamente.");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
        dispatcher.forward(request, response);
    }

    private void procesarEditarEvento(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Similar a procesarCrearEvento, pero actualiza un evento existente.
        // ... (implementación omitida por brevedad)
        request.setAttribute("errorGeneral", "Funcionalidad Editar Evento (POST) no implementada completamente.");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
        dispatcher.forward(request, response);
    }

    private void procesarEliminarEvento(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Obtener ID del evento, verificar permisos, llamar a EventoService.eliminarEventoRegistrado
        // ... (implementación omitida por brevedad)
        request.setAttribute("errorGeneral", "Funcionalidad Eliminar Evento (POST) no implementada completamente.");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
        dispatcher.forward(request, response);
    }

}
