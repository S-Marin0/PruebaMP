package com.eventmaster.controller;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Asistente;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.pattern.factory.TipoEntrada;
import com.eventmaster.model.facade.ProcesoCompraFacade;
import com.eventmaster.service.EventoService;
import com.eventmaster.model.command.ComprarEntradaCommand; // Podríamos usar el comando aquí
import com.eventmaster.model.command.CommandInvoker; // Si el Asistente usa su propio invoker

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "CompraServlet", urlPatterns = {"/compra/*"})
public class CompraServlet extends HttpServlet {

    private EventoService eventoService;
    private ProcesoCompraFacade procesoCompraFacade;
    // private CommandInvoker commandInvoker; // O se obtiene del Asistente

    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext context = getServletContext();
        eventoService = (EventoService) context.getAttribute("eventoService");
        procesoCompraFacade = (ProcesoCompraFacade) context.getAttribute("procesoCompraFacade");
        // commandInvoker = (CommandInvoker) context.getAttribute("globalCommandInvoker"); // Si hay uno global

        if (eventoService == null) {
            throw new ServletException("EventoService no disponible.");
        }
        if (procesoCompraFacade == null) {
            throw new ServletException("ProcesoCompraFacade no disponible.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        System.out.println("CompraServlet: doGet, PathInfo: " + action);

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect(request.getContextPath() + "/usuario/login?mensaje=Debe iniciar sesión para realizar compras.");
            return;
        }
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (!(usuarioLogueado instanceof Asistente)) {
             response.sendRedirect(request.getContextPath() + "/index?mensaje=Solo los asistentes pueden realizar compras.");
            return;
        }

        try {
            if (action != null) {
                switch (action) {
                    case "/confirmar": // Muestra la página de confirmación con el resumen
                        mostrarPaginaConfirmacion(request, response, session);
                        break;
                    case "/exitosa":
                        mostrarPaginaCompraExitosa(request, response);
                        break;
                    case "/mis-entradas":
                        mostrarMisEntradas(request, response, (Asistente)usuarioLogueado);
                        break;
                    default:
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Ruta de compra no encontrada.");
                        break;
                }
            } else {
                 response.sendError(HttpServletResponse.SC_NOT_FOUND, "Acción de compra no especificada.");
            }
        } catch (Exception e) {
            System.err.println("CompraServlet: Error en doGet - " + e.getMessage());
            request.setAttribute("errorGeneral", "Error procesando la solicitud de compra: " + e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/error.jsp");
            dispatcher.forward(request, response);
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        System.out.println("CompraServlet: doPost, PathInfo: " + action);

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect(request.getContextPath() + "/usuario/login?mensaje=Debe iniciar sesión para realizar compras.");
            return;
        }
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
         if (!(usuarioLogueado instanceof Asistente)) {
             response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo los asistentes pueden realizar compras.");
            return;
        }
        Asistente asistente = (Asistente) usuarioLogueado;


        try {
            if (action != null) {
                switch (action) {
                    case "/iniciar": // Viene del detalle del evento, al seleccionar entradas
                        procesarInicioCompra(request, response, session, asistente);
                        break;
                    case "/procesar-pago": // Viene del formulario de confirmación y pago
                        procesarPagoCompra(request, response, session, asistente);
                        break;
                    default:
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción POST de compra desconocida.");
                        break;
                }
            } else {
                 response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción POST de compra no especificada.");
            }
        } catch (Exception e) {
            System.err.println("CompraServlet: Error en doPost - " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorCompra", "Error al procesar la compra: " + e.getMessage());
            // Reenviar a una página relevante, ej. la de confirmar o detalle evento
             String eventoId = (String) session.getAttribute("compraEnProgresoEventoId");
             if(eventoId != null) {
                response.sendRedirect(request.getContextPath() + "/evento/detalle?id=" + eventoId + "&errorCompra=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
             } else {
                RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/error.jsp");
                dispatcher.forward(request, response);
             }
        }
    }

    private void procesarInicioCompra(HttpServletRequest request, HttpServletResponse response, HttpSession session, Asistente asistente)
            throws ServletException, IOException {
        String eventoId = request.getParameter("eventoId");
        String tipoEntradaNombre = request.getParameter("tipoEntradaNombre");
        int cantidad = 0;
        try {
            cantidad = Integer.parseInt(request.getParameter("cantidad"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/evento/detalle?id=" + eventoId + "&error=Cantidad inválida");
            return;
        }

        if (eventoId == null || tipoEntradaNombre == null || cantidad <= 0) {
            response.sendRedirect(request.getContextPath() + "/evento/detalle?id=" + eventoId + "&error=Datos de compra incompletos");
            return;
        }

        Optional<Evento> eventoOpt = eventoService.findEventoById(eventoId);
        if (!eventoOpt.isPresent()) {
            response.sendRedirect(request.getContextPath() + "/eventos?error=Evento no encontrado");
            return;
        }
        Evento evento = eventoOpt.get();
        TipoEntrada tipoEntradaDef = evento.getTipoEntrada(tipoEntradaNombre);

        if (tipoEntradaDef == null) {
            response.sendRedirect(request.getContextPath() + "/evento/detalle?id=" + eventoId + "&error=Tipo de entrada no válido");
            return;
        }

        // Guardar datos preliminares de la compra en sesión para el siguiente paso
        session.setAttribute("compraEnProgresoEventoId", eventoId);
        session.setAttribute("compraEnProgresoTipoEntradaNombre", tipoEntradaNombre);
        session.setAttribute("compraEnProgresoCantidad", cantidad);
        session.setAttribute("compraEnProgresoPrecioUnitario", tipoEntradaDef.getPrecioBase()); // Precio base del TipoEntrada seleccionado

        // Leer y guardar estado de los decoradores
        boolean decoradorMercanciaSeleccionado = "true".equalsIgnoreCase(request.getParameter("decorador_mercancia"));
        boolean decoradorDescuentoSeleccionado = "true".equalsIgnoreCase(request.getParameter("decorador_descuento"));

        session.setAttribute("compraEnProgresoDecoradorMercancia", decoradorMercanciaSeleccionado);
        session.setAttribute("compraEnProgresoDecoradorDescuento", decoradorDescuentoSeleccionado);

        System.out.println("CompraServlet: Inicio de compra procesado. EventoID: " + eventoId +
                           ", TipoEntrada: " + tipoEntradaNombre +
                           ", Cantidad: " + cantidad +
                           ", Mercancia: " + decoradorMercanciaSeleccionado +
                           ", Descuento: " + decoradorDescuentoSeleccionado +
                           ". Redirigiendo a confirmación.");
        response.sendRedirect(request.getContextPath() + "/compra/confirmar");
    }

    private void mostrarPaginaConfirmacion(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException {
        String eventoId = (String) session.getAttribute("compraEnProgresoEventoId");
        String tipoEntradaNombre = (String) session.getAttribute("compraEnProgresoTipoEntradaNombre");
        Integer cantidad = (Integer) session.getAttribute("compraEnProgresoCantidad");
        Double precioUnitario = (Double) session.getAttribute("compraEnProgresoPrecioUnitario");

        if (eventoId == null || tipoEntradaNombre == null || cantidad == null || precioUnitario == null) {
            request.setAttribute("errorGeneral", "No hay una compra en progreso o los datos están incompletos.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/error.jsp");
            dispatcher.forward(request, response);
            return;
        }

        Optional<Evento> eventoOpt = eventoService.findEventoById(eventoId);
        if(!eventoOpt.isPresent()){
            request.setAttribute("errorGeneral", "El evento para la compra ya no está disponible.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/error.jsp");
            dispatcher.forward(request, response);
            return;
        }

        request.setAttribute("evento", eventoOpt.get());
        request.setAttribute("tipoEntradaNombre", tipoEntradaNombre);
        request.setAttribute("cantidad", cantidad);
        request.setAttribute("precioUnitario", precioUnitario);
        request.setAttribute("totalProvisional", precioUnitario * cantidad);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/compra/confirmarCompra.jsp");
        dispatcher.forward(request, response);
    }

    private void procesarPagoCompra(HttpServletRequest request, HttpServletResponse response, HttpSession session, Asistente asistente)
            throws ServletException, IOException {
        String eventoId = (String) session.getAttribute("compraEnProgresoEventoId");
        String tipoEntradaNombre = (String) session.getAttribute("compraEnProgresoTipoEntradaNombre");
        Integer cantidad = (Integer) session.getAttribute("compraEnProgresoCantidad");
        Boolean decoradorMercancia = (Boolean) session.getAttribute("compraEnProgresoDecoradorMercancia");
        Boolean decoradorDescuento = (Boolean) session.getAttribute("compraEnProgresoDecoradorDescuento");

        // Double precioUnitario = (Double) session.getAttribute("compraEnProgresoPrecioUnitario"); // No se usa aquí directamente para el facade

        if (eventoId == null || tipoEntradaNombre == null || cantidad == null || decoradorMercancia == null || decoradorDescuento == null) {
            response.sendRedirect(request.getContextPath() + "/eventos?error=Datos de compra en sesión inválidos o incompletos. Intente de nuevo.");
            return;
        }

        // Simulación de detalles de pago
        Map<String, Object> detallesPago = new HashMap<>();
        detallesPago.put("numeroTarjeta", request.getParameter("numeroTarjeta")); // Simplificado, ¡NO HACER ESTO EN PRODUCCIÓN!
        detallesPago.put("nombreTitular", request.getParameter("nombreTitular"));
        detallesPago.put("fechaExpiracion", request.getParameter("fechaExpiracion"));
        detallesPago.put("cvv", request.getParameter("cvv"));
        String codigoDescuento = request.getParameter("codigoDescuento");

        System.out.println("CompraServlet: Procesando pago para evento " + eventoId + ", Asistente: " + asistente.getEmail());

        // Usar el Facade para el proceso completo
        Compra compraRealizada = procesoCompraFacade.ejecutarProcesoCompra(
            asistente,
            eventoId,
            tipoEntradaNombre,
            cantidad,
            detallesPago,
            codigoDescuento,
            decoradorMercancia, // Nuevo parámetro
            decoradorDescuento  // Nuevo parámetro
        );

        // Limpiar atributos de sesión de compra en progreso
        session.removeAttribute("compraEnProgresoEventoId");
        session.removeAttribute("compraEnProgresoTipoEntradaNombre");
        session.removeAttribute("compraEnProgresoCantidad");
        session.removeAttribute("compraEnProgresoPrecioUnitario");
        session.removeAttribute("compraEnProgresoDecoradorMercancia"); // Limpiar nuevos atributos
        session.removeAttribute("compraEnProgresoDecoradorDescuento"); // Limpiar nuevos atributos


        if (compraRealizada != null && "COMPLETADA".equals(compraRealizada.getEstadoCompra())) {
            // Opcional: guardar la compra en sesión si se va a mostrar inmediatamente después,
            // o mejor pasar solo el ID y recuperarla.
            // session.setAttribute("ultimaCompraRealizada", compraRealizada);
            response.sendRedirect(request.getContextPath() + "/compra/exitosa?compraId=" + compraRealizada.getId());
        } else {
            String errorMsg = (compraRealizada != null && compraRealizada.getEstadoCompra() != null) ?
                              "Estado de compra: " + compraRealizada.getEstadoCompra() :
                              "La compra no pudo ser completada.";
            System.err.println("CompraServlet: Fallo en procesoCompraFacade. " + errorMsg);
            response.sendRedirect(request.getContextPath() + "/evento/detalle?id=" + eventoId + "&errorCompra=" + java.net.URLEncoder.encode(errorMsg, "UTF-8"));
        }
    }

    private void mostrarPaginaCompraExitosa(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String compraId = request.getParameter("compraId");
        // Aquí se debería recuperar la Compra desde un CompraService usando el compraId
        // y pasarla a la JSP.
        // Optional<Compra> compraOpt = compraService.findById(compraId);
        // if(compraOpt.isPresent()){ request.setAttribute("compra", compraOpt.get()); }
        // else { request.setAttribute("errorGeneral", "No se encontró el detalle de la compra."); }

        request.setAttribute("compraId", compraId); // Simplificado: solo pasamos el ID
        request.setAttribute("mensajeExito", "¡Tu compra ha sido realizada con éxito!");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/compra/compraExitosa.jsp");
        dispatcher.forward(request, response);
    }

    private void mostrarMisEntradas(HttpServletRequest request, HttpServletResponse response, Asistente asistente)
            throws ServletException, IOException {
        // El UsuarioService ya debería cargar el historial de compras al obtener el Asistente.
        // Si no, se cargaría aquí:
        // List<Compra> misCompras = compraService.findByUsuarioId(asistente.getId());
        // request.setAttribute("listaMisCompras", misCompras);
        // El Asistente ya tiene su historial de compras cargado por UsuarioService
        request.setAttribute("listaMisCompras", asistente.getHistorialCompras());
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/compra/misEntradas.jsp");
        dispatcher.forward(request, response);
    }

}
