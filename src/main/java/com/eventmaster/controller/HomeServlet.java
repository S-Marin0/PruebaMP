package com.eventmaster.controller;

import com.eventmaster.model.entity.Evento;
import com.eventmaster.service.EventoService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "HomeServlet", urlPatterns = {"", "/index"}) // Mapea a la raíz y /index
public class HomeServlet extends HttpServlet {

    private EventoService eventoService;

    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext context = getServletContext();
        eventoService = (EventoService) context.getAttribute("eventoService");
        if (eventoService == null) {
            throw new ServletException("EventoService no disponible en el contexto de la aplicación.");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("HomeServlet: doGet - Sirviendo página de inicio.");
        try {
            // Obtener algunos eventos para mostrar (ej. los próximos 5 publicados)
            List<Evento> eventosDestacados = eventoService.findEventosPublicadosYFuturos();

            // Limitar la cantidad si es necesario para la home page
            eventosDestacados = eventosDestacados.stream().limit(5).collect(Collectors.toList());

            request.setAttribute("eventosDestacados", eventosDestacados);
            System.out.println("HomeServlet: " + eventosDestacados.size() + " eventos destacados cargados para la vista.");

        } catch (Exception e) {
            System.err.println("HomeServlet: Error al obtener eventos destacados: " + e.getMessage());
            request.setAttribute("errorGeneral", "No se pudieron cargar los eventos destacados: " + e.getMessage());
            // e.printStackTrace(); // Para depuración
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp"); // Asumiendo que tienes un index.jsp en la raíz de webapp
        // O si está en WEB-INF/jsp: request.getRequestDispatcher("/WEB-INF/jsp/home.jsp");
        dispatcher.forward(request, response);
    }
}
