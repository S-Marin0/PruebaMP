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

    // Este método helper es para cuando se reenvía al FORMULARIO DE EDICIÓN debido a un error en procesarEditarEvento.
    // Necesita el evento original (antes de los cambios del form) para repoblar los atributos tipoEntrada_1, etc.
    // que la JSP usa como fallback si los param.* no existen.
    private void repopularEditarFormConDatosOriginales(HttpServletRequest request, Evento eventoOriginalCargado) {
        request.setAttribute("evento", eventoOriginalCargado);
        if (eventoOriginalCargado.getFechaHora() != null) {
            request.setAttribute("fechaHoraInput", eventoOriginalCargado.getFechaHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        }

        if (eventoOriginalCargado.getTiposEntradaDisponibles() != null) {
            List<com.eventmaster.model.pattern.factory.TipoEntrada> tiposEntradaList =
                new ArrayList<>(eventoOriginalCargado.getTiposEntradaDisponibles().values());
            // Opcional: Ordenar para consistencia si es importante para el usuario cómo se rellenan los slots 1,2,3
            // tiposEntradaList.sort(Comparator.comparing(com.eventmaster.model.pattern.factory.TipoEntrada::getNombreTipo));
            for (int i = 0; i < 3; i++) {
                if (i < tiposEntradaList.size()) {
                    request.setAttribute("tipoEntrada_" + (i + 1), tiposEntradaList.get(i));
                } else {
                    request.setAttribute("tipoEntrada_" + (i + 1), null);
                }
            }
        } else {
             for (int i = 1; i <= 3; i++) {
                 request.setAttribute("tipoEntrada_" + i, null);
            }
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
                case "/cambiarEstado": // Nueva acción
                    procesarCambioEstado(request, response);
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

            LocalDateTime fechaHora = null;
            try {
                // El input datetime-local envía "yyyy-MM-ddTHH:mm"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                fechaHora = LocalDateTime.parse(fechaHoraStr, formatter);
                System.out.println("EventoServlet#procesarCrearEvento: fechaHoraStr parseada a LocalDateTime: " + fechaHora);
            } catch (DateTimeParseException e) {
                System.err.println("EventoServlet#procesarCrearEvento: Error al parsear fechaHoraStr '" + fechaHoraStr + "': " + e.getMessage());
                request.setAttribute("errorCrearEvento", "Formato de Fecha/Hora inválido. Use el selector o formato YYYY-MM-DDTHH:MM.");
                mostrarFormularioCrearEvento(request, response);
                return;
            }

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

            // Procesar hasta 3 tipos de entrada
            for (int i = 1; i <= 3; i++) {
                String nombreTipo = request.getParameter("tipoEntradaNombre_" + i);
                String precioBaseStr = request.getParameter("tipoEntradaPrecioBase_" + i);
                String cantidadTotalStr = request.getParameter("tipoEntradaCantidadTotal_" + i);
                String limiteCompraStr = request.getParameter("tipoEntradaLimiteCompra_" + i);

                // El primer tipo de entrada es obligatorio, los otros son opcionales.
                // Si el nombre del tipo no está presente (especialmente para tipos 2 y 3), se ignora.
                if (nombreTipo != null && !nombreTipo.trim().isEmpty()) {
                    try {
                        double precioBase = Double.parseDouble(precioBaseStr);
                        int cantidadTotal = Integer.parseInt(cantidadTotalStr);
                        int limiteCompra = Integer.parseInt(limiteCompraStr);

                        if (precioBase >= 0 && cantidadTotal > 0 && limiteCompra > 0) {
                            com.eventmaster.model.pattern.factory.TipoEntrada nuevoTipoEntrada =
                                    new com.eventmaster.model.pattern.factory.TipoEntrada(
                                            nombreTipo,
                                            precioBase,
                                            cantidadTotal,
                                            limiteCompra
                                    );

                            // Leer y establecer opciones de decorador
                            boolean ofreceMerc = "true".equals(request.getParameter("ofreceMercanciaOpcional_" + i));
                            nuevoTipoEntrada.setOfreceMercanciaOpcional(ofreceMerc);
                            if (ofreceMerc) {
                                nuevoTipoEntrada.setDescripcionMercancia(request.getParameter("descripcionMercancia_" + i));
                                String precioMercStr = request.getParameter("precioAdicionalMercancia_" + i);
                                if (precioMercStr != null && !precioMercStr.trim().isEmpty()) {
                                    try {
                                        nuevoTipoEntrada.setPrecioAdicionalMercancia(Double.parseDouble(precioMercStr));
                                    } catch (NumberFormatException nfe) {
                                        if (i == 1) { // Campo obligatorio si el checkbox está marcado para el tipo 1
                                            request.setAttribute("errorCrearEvento", "Precio de mercancía inválido para Tipo de Entrada 1.");
                                            mostrarFormularioCrearEvento(request, response);
                                            return;
                                        } // Para opcionales, podría ignorarse o loguear
                                        System.err.println("Advertencia: Precio de mercancía inválido para tipo opcional " + i + ", se usará 0.0.");
                                        nuevoTipoEntrada.setPrecioAdicionalMercancia(0.0);
                                    }
                                } else if (i == 1) { // Campo obligatorio si el checkbox está marcado para el tipo 1
                                     request.setAttribute("errorCrearEvento", "Precio de mercancía requerido para Tipo de Entrada 1 si se ofrece mercancía.");
                                     mostrarFormularioCrearEvento(request, response);
                                     return;
                                } else {
                                    nuevoTipoEntrada.setPrecioAdicionalMercancia(0.0);
                                }
                            } else { // Si no ofrece mercancía, asegurar valores por defecto
                                nuevoTipoEntrada.setDescripcionMercancia(null);
                                nuevoTipoEntrada.setPrecioAdicionalMercancia(0.0);
                            }

                            boolean ofreceDesc = "true".equals(request.getParameter("ofreceDescuentoOpcional_" + i));
                            nuevoTipoEntrada.setOfreceDescuentoOpcional(ofreceDesc);
                            if (ofreceDesc) {
                                nuevoTipoEntrada.setDescripcionDescuento(request.getParameter("descripcionDescuento_" + i));
                                String montoDescStr = request.getParameter("montoDescuentoFijo_" + i);
                                if (montoDescStr != null && !montoDescStr.trim().isEmpty()) {
                                     try {
                                        nuevoTipoEntrada.setMontoDescuentoFijo(Double.parseDouble(montoDescStr));
                                    } catch (NumberFormatException nfe) {
                                        if (i == 1) { // Campo obligatorio si el checkbox está marcado para el tipo 1
                                            request.setAttribute("errorCrearEvento", "Monto de descuento inválido para Tipo de Entrada 1.");
                                            mostrarFormularioCrearEvento(request, response);
                                            return;
                                        }
                                        System.err.println("Advertencia: Monto de descuento inválido para tipo opcional " + i + ", se usará 0.0.");
                                        nuevoTipoEntrada.setMontoDescuentoFijo(0.0);
                                    }
                                } else if (i == 1) { // Campo obligatorio si el checkbox está marcado para el tipo 1
                                     request.setAttribute("errorCrearEvento", "Monto de descuento requerido para Tipo de Entrada 1 si se ofrece descuento.");
                                     mostrarFormularioCrearEvento(request, response);
                                     return;
                                } else {
                                     nuevoTipoEntrada.setMontoDescuentoFijo(0.0);
                                }
                            } else { // Si no ofrece descuento, asegurar valores por defecto
                                nuevoTipoEntrada.setDescripcionDescuento(null);
                                nuevoTipoEntrada.setMontoDescuentoFijo(0.0);
                            }

                            builder.addTipoEntrada(nombreTipo, nuevoTipoEntrada);
                            System.out.println("EventoServlet: Añadido tipo de entrada '" + nombreTipo + "' al evento '" + nombre + "'. Mercancia: " + ofreceMerc + ", Descuento: " + ofreceDesc);
                        } else if (i == 1) { // Si es el primer tipo (obligatorio) y los números base (precio, cantidad, limite) son inválidos
                            request.setAttribute("errorCrearEvento", "Valores numéricos base inválidos para el Tipo de Entrada 1.");
                            mostrarFormularioCrearEvento(request, response);
                            return;
                        } else {
                             System.out.println("EventoServlet: Valores numéricos inválidos o incompletos para el Tipo de Entrada opcional " + i + " para el evento '" + nombre + "'. Se omite.");
                        }
                    } catch (NumberFormatException e) {
                        // Si es el tipo obligatorio y hay error de formato, fallar.
                        // Si es opcional y el nombre estaba pero los números no, se podría ignorar o fallar.
                        // Aquí, si el nombre está pero los números no son parseables, es un error para el tipo 1.
                        // Para los opcionales, si el nombre está pero los números no, también es un error de datos.
                        if (i == 1 || (precioBaseStr != null && !precioBaseStr.isEmpty()) || (cantidadTotalStr != null && !cantidadTotalStr.isEmpty()) || (limiteCompraStr != null && !limiteCompraStr.isEmpty())) {
                           request.setAttribute("errorCrearEvento", "Error en el formato numérico para el Tipo de Entrada " + i + ".");
                           mostrarFormularioCrearEvento(request, response);
                           return;
                        }
                        // Si es un tipo opcional (i > 1) y todos los campos numéricos están vacíos/nulos junto con el nombre, está bien ignorarlo.
                        // La condición inicial (nombreTipo no vacío) ya maneja esto para los opcionales.
                    }
                } else if (i == 1) {
                    // El nombre del primer tipo de entrada es obligatorio
                    request.setAttribute("errorCrearEvento", "El nombre para el Tipo de Entrada 1 es obligatorio.");
                    mostrarFormularioCrearEvento(request, response);
                    return;
                }
            }

            Evento eventoParaGuardar = builder.build(); // Construir el evento primero
            // Verificar si se añadió al menos un tipo de entrada (el primero es obligatorio)
            if(eventoParaGuardar.getTiposEntradaDisponibles() == null || eventoParaGuardar.getTiposEntradaDisponibles().isEmpty()){
                 request.setAttribute("errorCrearEvento", "Debe definir al menos el Tipo de Entrada 1 correctamente.");
                 mostrarFormularioCrearEvento(request, response);
                 return;
            }
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

        // Preparar tipos de entrada para el formulario (hasta 3)
        if (evento.getTiposEntradaDisponibles() != null) {
            List<com.eventmaster.model.pattern.factory.TipoEntrada> tiposEntradaList =
                new ArrayList<>(evento.getTiposEntradaDisponibles().values());

            // Ordenar por nombre para consistencia, si se desea
            // tiposEntradaList.sort(Comparator.comparing(com.eventmaster.model.pattern.factory.TipoEntrada::getNombreTipo));

            for (int i = 0; i < 3; i++) {
                if (i < tiposEntradaList.size()) {
                    request.setAttribute("tipoEntrada_" + (i + 1), tiposEntradaList.get(i));
                } else {
                    // Para asegurar que los atributos no persistan de requests anteriores si no hay tipo
                    request.setAttribute("tipoEntrada_" + (i + 1), null);
                }
            }
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
            // El lugar y organizador no se suelen cambiar en una edición simple.

            // Procesar Tipos de Entrada
            // 'eventoAEditar' es el objeto cargado de la BD, representa el estado original.
            Map<String, com.eventmaster.model.pattern.factory.TipoEntrada> tiposEntradaOriginalesAlmacenados =
                (eventoAEditar.getTiposEntradaDisponibles() != null) ?
                new HashMap<>(eventoAEditar.getTiposEntradaDisponibles()) :
                new HashMap<>();

            // Limpiar el mapa en el objeto eventoAEditar para llenarlo con los datos del formulario.
            // El EventoService se encargará de la sincronización con el DAO.
            if (eventoAEditar.getTiposEntradaDisponibles() == null) {
                 eventoAEditar.setTiposEntradaDisponibles(new HashMap<>()); // Asumiendo setter, o modificar Evento para asegurar que nunca sea null
            }
            eventoAEditar.getTiposEntradaDisponibles().clear();

            boolean primerTipoEntradaValidoIngresado = false;

            for (int i = 1; i <= 3; i++) {
                String nombreTipo = request.getParameter("tipoEntradaNombre_" + i);
                String precioBaseStr = request.getParameter("tipoEntradaPrecioBase_" + i);
                String cantidadTotalStr = request.getParameter("tipoEntradaCantidadTotal_" + i);
                String limiteCompraStr = request.getParameter("tipoEntradaLimiteCompra_" + i);

                if (nombreTipo != null && !nombreTipo.trim().isEmpty()) {
                    try {
                        double precioBase = Double.parseDouble(precioBaseStr);
                        int cantidadTotalForm = Integer.parseInt(cantidadTotalStr);
                        int limiteCompra = Integer.parseInt(limiteCompraStr);

                        if (precioBase >= 0 && cantidadTotalForm >= 0 && limiteCompra > 0) { // cantidadTotalForm puede ser 0
                            com.eventmaster.model.pattern.factory.TipoEntrada tipoProcesado =
                                    new com.eventmaster.model.pattern.factory.TipoEntrada(
                                            nombreTipo,
                                            precioBase,
                                            cantidadTotalForm,
                                            limiteCompra
                                    );

                            com.eventmaster.model.pattern.factory.TipoEntrada originalTipoPersistido = tiposEntradaOriginalesAlmacenados.get(nombreTipo);
                            if (originalTipoPersistido != null) {
                                int vendidas = originalTipoPersistido.getCantidadTotal() - originalTipoPersistido.getCantidadDisponible();
                                if (cantidadTotalForm < vendidas) {
                                    request.setAttribute("errorEditarEvento", "Para el tipo '" + nombreTipo + "', la nueva cantidad total (" + cantidadTotalForm + ") no puede ser menor que las " + vendidas + " entradas ya vendidas.");
                                    repopularEditarFormConDatosOriginales(request, eventoAEditar); // Usa el método renombrado/correcto
                                    request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
                                    return;
                                }
                                tipoProcesado.reducirDisponibilidad(vendidas);
                            }

                            boolean ofreceMerc = "true".equals(request.getParameter("ofreceMercanciaOpcional_" + i));
                            tipoProcesado.setOfreceMercanciaOpcional(ofreceMerc);
                            if (ofreceMerc) {
                                tipoProcesado.setDescripcionMercancia(request.getParameter("descripcionMercancia_" + i));
                                String precioMercStr = request.getParameter("precioAdicionalMercancia_" + i);
                                if (precioMercStr != null && !precioMercStr.trim().isEmpty()) {
                                    try {
                                        tipoProcesado.setPrecioAdicionalMercancia(Double.parseDouble(precioMercStr));
                                    } catch (NumberFormatException nfe) {
                                        request.setAttribute("errorEditarEvento", "Precio de mercancía inválido para Tipo de Entrada " + i + ".");
                                        repopularEditarFormConDatosOriginales(request, eventoAEditar);
                                        request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
                                        return;
                                    }
                                } else {
                                    request.setAttribute("errorEditarEvento", "Precio de mercancía requerido para Tipo de Entrada " + i + " si se ofrece mercancía.");
                                    repopularEditarFormConDatosOriginales(request, eventoAEditar);
                                    request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
                                    return;
                                }
                            } else {
                                tipoProcesado.setDescripcionMercancia(null);
                                tipoProcesado.setPrecioAdicionalMercancia(0.0);
                            }

                            boolean ofreceDesc = "true".equals(request.getParameter("ofreceDescuentoOpcional_" + i));
                            tipoProcesado.setOfreceDescuentoOpcional(ofreceDesc);
                            if (ofreceDesc) {
                                tipoProcesado.setDescripcionDescuento(request.getParameter("descripcionDescuento_" + i));
                                String montoDescStr = request.getParameter("montoDescuentoFijo_" + i);
                                if (montoDescStr != null && !montoDescStr.trim().isEmpty()) {
                                    try {
                                        tipoProcesado.setMontoDescuentoFijo(Double.parseDouble(montoDescStr));
                                    } catch (NumberFormatException nfe) {
                                        request.setAttribute("errorEditarEvento", "Monto de descuento inválido para Tipo de Entrada " + i + ".");
                                        repopularEditarFormConDatosOriginales(request, eventoAEditar);
                                        request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
                                        return;
                                    }
                                } else {
                                     request.setAttribute("errorEditarEvento", "Monto de descuento requerido para Tipo de Entrada " + i + " si se ofrece descuento.");
                                     repopularEditarFormConDatosOriginales(request, eventoAEditar);
                                     request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
                                     return;
                                }
                            } else {
                                tipoProcesado.setDescripcionDescuento(null);
                                tipoProcesado.setMontoDescuentoFijo(0.0);
                            }

                            eventoAEditar.agregarTipoEntrada(nombreTipo, tipoProcesado);
                            if (i == 1) primerTipoEntradaValidoIngresado = true;
                            System.out.println("EventoServlet#procesarEditarEvento: Tipo de entrada '" + nombreTipo + "' procesado. Merc: "+ofreceMerc+", Desc: "+ofreceDesc);

                        } else if (i == 1) {
                            request.setAttribute("errorEditarEvento", "Valores numéricos base inválidos para el Tipo de Entrada 1 (precio, cantidad >= 0, límite > 0).");
                            repopularEditarFormConDatosOriginales(request, eventoAEditar);
                            request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
                            return;
                        } else {
                             System.out.println("EventoServlet#procesarEditarEvento: Tipo opcional " + i + " con nombre '" + nombreTipo + "' tiene datos numéricos base inválidos. Se omite.");
                        }
                    } catch (NumberFormatException e) {
                        request.setAttribute("errorEditarEvento", "Error en el formato numérico para los datos base del Tipo de Entrada " + i + ".");
                        repopularEditarFormConDatosOriginales(request, eventoAEditar);
                        request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
                        return;
                    }
                } else if (i == 1) {
                    request.setAttribute("errorEditarEvento", "El nombre para el Tipo de Entrada 1 es obligatorio.");
                    repopularEditarFormConDatosOriginales(request, eventoAEditar);
                    request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
                    return;
                }
            }

            if (!primerTipoEntradaValidoIngresado) {
                 request.setAttribute("errorEditarEvento", "Debe definir al menos el Tipo de Entrada 1 correctamente.");
                 repopularEditarFormConDatosOriginales(request, eventoAEditar);
                 request.getRequestDispatcher("/jsp/evento/editarEventoForm.jsp").forward(request, response);
                 return;
            }

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

    private void procesarCambioEstado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String eventoId = request.getParameter("eventoId");
        String accionEstado = request.getParameter("accion"); // ej. "publicar", "cancelar"

        HttpSession session = request.getSession(false);
        Usuario usuarioLogueado = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (!(usuarioLogueado instanceof Organizador)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. Debe ser organizador.");
            return;
        }
        Organizador organizador = (Organizador) usuarioLogueado;

        if (eventoId == null || eventoId.trim().isEmpty() || accionEstado == null || accionEstado.trim().isEmpty()) {
            request.setAttribute("errorGeneral", "ID de evento o acción de estado no proporcionados.");
            // Considerar redirigir a una página más específica si es posible, o a la lista de eventos.
            request.getRequestDispatcher("/jsp/error.jsp").forward(request, response);
            return;
        }

        Optional<Evento> eventoOpt = eventoService.findEventoById(eventoId);
        if (!eventoOpt.isPresent()) {
            response.sendRedirect(request.getContextPath() + "/eventos?error=Evento no encontrado con ID: " + eventoId);
            return;
        }

        Evento evento = eventoOpt.get();

        if (!evento.getOrganizador().getId().equals(organizador.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tiene permiso para cambiar el estado de este evento.");
            return;
        }

        String mensajeExito = "";
        String mensajeError = "";

        try {
            String estadoAnterior = evento.getEstadoActual().getNombreEstado();
            switch (accionEstado) {
                case "publicar":
                    evento.publicar();
                    mensajeExito = "Evento publicado exitosamente.";
                    break;
                case "cancelar":
                    evento.cancelar();
                    mensajeExito = "Evento cancelado exitosamente.";
                    break;
                case "iniciar":
                    evento.iniciar();
                     mensajeExito = "Evento iniciado exitosamente.";
                    break;
                case "finalizar":
                    evento.finalizar();
                    mensajeExito = "Evento finalizado exitosamente.";
                    break;
                default:
                    mensajeError = "Acción de estado desconocida: " + accionEstado;
                    break;
            }

            if (mensajeError.isEmpty()) {
                eventoService.actualizarEvento(evento); // Guardar el cambio de estado
                System.out.println("EventoServlet: Estado del evento ID " + eventoId + " cambiado de '" + estadoAnterior + "' a '" + evento.getEstadoActual().getNombreEstado() + "' por acción '" + accionEstado + "'.");
                response.sendRedirect(request.getContextPath() + "/evento/detalle?id=" + eventoId + "&mensaje=" + java.net.URLEncoder.encode(mensajeExito, "UTF-8"));
            } else {
                response.sendRedirect(request.getContextPath() + "/evento/detalle?id=" + eventoId + "&error=" + java.net.URLEncoder.encode(mensajeError, "UTF-8"));
            }

        } catch (IllegalStateException e) { // Captura errores de transiciones de estado inválidas
            System.err.println("EventoServlet: Transición de estado inválida para evento ID " + eventoId + ", acción " + accionEstado + " - " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/evento/detalle?id=" + eventoId + "&error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        } catch (Exception e) {
            System.err.println("EventoServlet: Error general al cambiar estado del evento ID " + eventoId + " - " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/evento/detalle?id=" + eventoId + "&error=" + java.net.URLEncoder.encode("Error inesperado al cambiar el estado.", "UTF-8"));
        }
    }
}
