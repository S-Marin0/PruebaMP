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
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/error.jsp");
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
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/error.jsp"); // Página genérica
            dispatcher.forward(request, response);
        }
    }

    private void listarEventos(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    String categoria = request.getParameter("categoria");
    List<Evento> listaEventos;

    if (categoria != null && !categoria.trim().isEmpty()) {
        listaEventos = eventoService.findEventosPorCategoria(categoria);
        request.setAttribute("filtroCategoria", categoria);
    } else {
        listaEventos = eventoService.getAllEventos();
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withLocale(new Locale("es", "ES"));

    // Crear una lista de mapas con el evento y su fecha formateada
    List<Map<String, Object>> eventosConFecha = new ArrayList<>();
    for (Evento evento : listaEventos) {
        Map<String, Object> map = new HashMap<>();
        map.put("evento", evento);
        map.put("fechaFormateada", evento.getFechaHora().format(formatter));
        eventosConFecha.add(map);
    }

    request.setAttribute("listaEventosConFecha", eventosConFecha);
    System.out.println("EventoServlet.listarEventos: Número de eventos recuperados para listar: " + (listaEventos != null ? listaEventos.size() : "null")); // Log
    if (listaEventos != null && !listaEventos.isEmpty()) {
        for (Evento ev : listaEventos) {
            System.out.println("EventoServlet.listarEventos: Evento en lista: ID=" + ev.getId() + ", Nombre=" + ev.getNombre()); // Log
        }
    }

    RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/evento/listaEventos.jsp");
    dispatcher.forward(request, response);
}



private void verDetalleEvento(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    String idEvento = request.getParameter("id");
    if (idEvento == null || idEvento.trim().isEmpty()) {
        request.setAttribute("errorGeneral", "ID de evento no proporcionado.");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/error.jsp");
        dispatcher.forward(request, response);
        return;
    }

    Optional<Evento> eventoOpt = eventoService.findEventoById(idEvento);
    System.out.println("EventoServlet.verDetalleEvento: Buscando evento con ID: " + idEvento + ". Encontrado: " + eventoOpt.isPresent()); // Log
    if (eventoOpt.isPresent()) {
        Evento evento = eventoOpt.get();
        System.out.println("EventoServlet.verDetalleEvento: Mostrando detalle para evento: " + evento.getNombre()); // Log
        request.setAttribute("evento", evento);

        // ✅ Formatear la fecha a texto (ejemplo: "miércoles, 10 de julio de 2025 21:30")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy HH:mm")
                .withLocale(new java.util.Locale("es", "ES"));
        String fechaHoraFormateada = evento.getFechaHora().format(formatter);
        request.setAttribute("fechaHoraFormateada", fechaHoraFormateada);

        HttpSession session = request.getSession(false);
        Usuario usuarioLogueado = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;
        request.setAttribute("esAsistente", usuarioLogueado instanceof com.eventmaster.model.entity.Asistente);
        request.setAttribute("esOrganizador", usuarioLogueado instanceof com.eventmaster.model.entity.Organizador);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/evento/detalleEvento.jsp");
        dispatcher.forward(request, response);
    } else {
        request.setAttribute("errorGeneral", "Evento no encontrado con ID: " + idEvento);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/error.jsp");
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

    // ✅ Esto es lo nuevo
    request.setAttribute("esOrganizador", true);

    RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/evento/crearEventoForm.jsp");
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

            System.out.println("EventoServlet.procesarCrearEvento: Evento procesado. ID del evento guardado: " + eventoGuardado.getId() + ". Nombre: " + eventoGuardado.getNombre()); // Log
            System.out.println("EventoServlet.procesarCrearEvento: Redirigiendo a /evento/detalle?id=" + eventoGuardado.getId()); // Log

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
        HttpSession session = request.getSession(false);
        Usuario usuarioLogueado = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (!(usuarioLogueado instanceof Organizador)) {
            response.sendRedirect(request.getContextPath() + "/usuario/login?mensaje=Acceso denegado. Debe ser organizador.");
            return;
        }

        String eventoId = request.getParameter("id");
        if (eventoId == null || eventoId.trim().isEmpty()) {
            request.setAttribute("errorGeneral", "ID de evento no proporcionado para editar.");
            request.getRequestDispatcher("/jsp/error.jsp").forward(request, response);
            return;
        }

        Optional<Evento> eventoOpt = eventoService.findEventoById(eventoId);
        if (!eventoOpt.isPresent()) {
            request.setAttribute("errorGeneral", "Evento no encontrado para editar con ID: " + eventoId);
            request.getRequestDispatcher("/jsp/error.jsp").forward(request, response);
            return;
        }

        Evento evento = eventoOpt.get();
        // Verificar si el usuario logueado es el organizador del evento
        if (!evento.getOrganizador().getId().equals(usuarioLogueado.getId())) {
            request.setAttribute("errorGeneral", "No tiene permiso para editar este evento.");
            request.getRequestDispatcher("/jsp/error.jsp").forward(request, response);
            return;
        }

        request.setAttribute("evento", evento);
        // Para el formato de fecha en el input datetime-local
        if (evento.getFechaHora() != null) {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            request.setAttribute("fechaHoraInput", evento.getFechaHora().format(inputFormatter));
        }

        request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
    }

    private void procesarEditarEvento(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario usuarioLogueado = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (!(usuarioLogueado instanceof Organizador)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado.");
            return;
        }

        String eventoId = request.getParameter("eventoId"); // ID del evento a editar

        if (eventoId == null || eventoId.trim().isEmpty()) {
            request.setAttribute("errorEditarEvento", "ID de evento no proporcionado.");
            // Reenviar al formulario de alguna manera o a una página de error.
            // Como no tenemos los datos del evento aquí, es mejor ir a error general.
            request.setAttribute("errorGeneral", "ID de evento no fue enviado para la actualización.");
            request.getRequestDispatcher("/jsp/error.jsp").forward(request, response);
            return;
        }

        // Es crucial recargar el evento para asegurarse de que el usuario tiene permiso
        // y para tener el objeto original antes de aplicar cambios.
        Optional<Evento> eventoOpt = eventoService.findEventoById(eventoId);
        if (!eventoOpt.isPresent()) {
            request.setAttribute("errorGeneral", "Evento a editar no encontrado con ID: " + eventoId);
            request.getRequestDispatcher("/jsp/error.jsp").forward(request, response);
            return;
        }

        Evento eventoAEditar = eventoOpt.get();

        if (!eventoAEditar.getOrganizador().getId().equals(usuarioLogueado.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tiene permiso para editar este evento.");
            return;
        }

        try {
            String nombre = request.getParameter("nombre");
            String descripcion = request.getParameter("descripcion");
            String categoria = request.getParameter("categoria");
            String fechaHoraStr = request.getParameter("fechaHora"); // Formato esperado: YYYY-MM-DDTHH:MM
            int capacidad = Integer.parseInt(request.getParameter("capacidad"));

            // Validaciones básicas
            if (nombre == null || nombre.trim().isEmpty() || fechaHoraStr == null || fechaHoraStr.trim().isEmpty()) {
                request.setAttribute("errorEditarEvento", "Nombre y Fecha/Hora son obligatorios.");
                request.setAttribute("evento", eventoAEditar); // Reenviar datos originales al form
                 if (eventoAEditar.getFechaHora() != null) {
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                    request.setAttribute("fechaHoraInput", eventoAEditar.getFechaHora().format(inputFormatter));
                }
                request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
                return;
            }
            LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr);

            // Actualizar los campos del evento existente
            eventoAEditar.setNombre(nombre);
            eventoAEditar.setDescripcion(descripcion);
            eventoAEditar.setCategoria(categoria);
            eventoAEditar.setFechaHora(fechaHora);
            eventoAEditar.setCapacidadTotal(capacidad);
            // El lugar y organizador no se suelen cambiar en una edición simple,
            // pero si fuera necesario, se añadirían aquí.
            // Nota: El Evento.Lugar es simulado en la creación, aquí no se toca.

            eventoService.actualizarEvento(eventoAEditar);

            System.out.println("EventoServlet: Evento actualizado con ID: " + eventoAEditar.getId());
            response.sendRedirect(request.getContextPath() + "/evento/detalle?id=" + eventoAEditar.getId() + "&mensaje=Evento actualizado exitosamente");

        } catch (DateTimeParseException e) {
            request.setAttribute("errorEditarEvento", "Formato de Fecha/Hora inválido. Use YYYY-MM-DDTHH:MM");
            request.setAttribute("evento", eventoAEditar); // Reenviar datos originales
            if (eventoAEditar.getFechaHora() != null) {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                request.setAttribute("fechaHoraInput", eventoAEditar.getFechaHora().format(inputFormatter));
            }
            request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("errorEditarEvento", "Capacidad debe ser un número.");
            request.setAttribute("evento", eventoAEditar); // Reenviar datos originales
             if (eventoAEditar.getFechaHora() != null) {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                request.setAttribute("fechaHoraInput", eventoAEditar.getFechaHora().format(inputFormatter));
            }
            request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("errorEditarEvento", "Error al actualizar evento: " + e.getMessage());
            request.setAttribute("evento", eventoAEditar); // Reenviar datos originales
             if (eventoAEditar.getFechaHora() != null) {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                request.setAttribute("fechaHoraInput", eventoAEditar.getFechaHora().format(inputFormatter));
            }
            request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
        }
    }

    private void procesarEliminarEvento(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario usuarioLogueado = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (!(usuarioLogueado instanceof Organizador)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. Debe ser organizador.");
            return;
        }

        String eventoId = request.getParameter("eventoId"); // Obtenido del campo oculto del formulario

        if (eventoId == null || eventoId.trim().isEmpty()) {
            request.setAttribute("errorGeneral", "ID de evento no proporcionado para eliminar.");
            request.getRequestDispatcher("/jsp/error.jsp").forward(request, response);
            return;
        }

        Optional<Evento> eventoOpt = eventoService.findEventoById(eventoId);
        if (!eventoOpt.isPresent()) {
            request.setAttribute("errorGeneral", "Evento no encontrado para eliminar con ID: " + eventoId);
            // Podríamos redirigir a la lista de eventos si el evento ya no existe.
            response.sendRedirect(request.getContextPath() + "/eventos?mensaje=Evento no encontrado o ya eliminado.");
            return;
        }

        Evento eventoAEliminar = eventoOpt.get();

        // Verificar si el usuario logueado es el organizador del evento
        if (!eventoAEliminar.getOrganizador().getId().equals(usuarioLogueado.getId())) {
            request.setAttribute("errorGeneral", "No tiene permiso para eliminar este evento.");
            // Es mejor no dar demasiada información, simplemente redirigir o mostrar error.
             response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tiene permiso para eliminar este evento.");
            return;
        }

        try {
            eventoService.eliminarEventoRegistrado(eventoAEliminar);
            System.out.println("EventoServlet: Evento eliminado con ID: " + eventoId);
            response.sendRedirect(request.getContextPath() + "/eventos?mensaje=Evento '" + eventoAEliminar.getNombre() + "' eliminado exitosamente.");
        } catch (Exception e) {
            System.err.println("EventoServlet: Error al eliminar evento ID " + eventoId + " - " + e.getMessage());
            e.printStackTrace();
            // Si hay un error, podría ser útil redirigir a la página de detalles del evento
            // o a la lista con un mensaje de error específico.
            response.sendRedirect(request.getContextPath() + "/evento/detalle?id=" + eventoId + "&errorEliminar=Error al intentar eliminar el evento: " + e.getMessage());
        }
    }

}
