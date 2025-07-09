package com.eventmaster.listener;

import com.eventmaster.dao.*;
import com.eventmaster.dao.impl.memoria.*;
import com.eventmaster.service.*;
import com.eventmaster.model.facade.ProcesoCompraFacade;
import com.eventmaster.model.pattern.chain_of_responsibility.*;
import com.eventmaster.model.pattern.factory.TipoEntradaFactory;
import com.eventmaster.model.pattern.factory.EntradaGeneralFactory;
import com.eventmaster.model.pattern.factory.EntradaVIPFactory;
import com.eventmaster.model.pattern.factory.EntradaEarlyAccessFactory;
import com.eventmaster.model.pattern.strategy.GestorRecomendacionesStrategy;
import com.eventmaster.model.singleton.SistemaNotificaciones;
import com.eventmaster.model.pattern.mediator.MediadorConcreto;
import com.eventmaster.model.pattern.mediator.MediadorCompras;


import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.HashMap;
import java.util.Map;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        System.out.println("AppContextListener: Inicializando contexto de la aplicación...");

        // 1. Inicializar DAOs (En Memoria)
        UsuarioDAO usuarioDAO = new UsuarioDAOImplMemoria();
        EventoDAO eventoDAO = new EventoDAOImplMemoria();
        LugarDAO lugarDAO = new LugarDAOImplMemoria();
        TipoEntradaDAO tipoEntradaDAO = new TipoEntradaDAOImplMemoria();
        EntradaDAO entradaDAO = new EntradaDAOImplMemoria(); // No usado directamente por servicios refactorizados, pero podría ser útil
        CompraDAO compraDAO = new CompraDAOImplMemoria();
        PromocionDAO promocionDAO = new PromocionDAOImplMemoria(); // No usado directamente aún, pero listo
        CodigoDescuentoDAO codigoDescuentoDAO = new CodigoDescuentoDAOImplMemoria(); // Idem

        ctx.setAttribute("usuarioDAO", usuarioDAO);
        ctx.setAttribute("eventoDAO", eventoDAO);
        ctx.setAttribute("lugarDAO", lugarDAO);
        ctx.setAttribute("tipoEntradaDAO", tipoEntradaDAO);
        ctx.setAttribute("entradaDAO", entradaDAO);
        ctx.setAttribute("compraDAO", compraDAO);
        System.out.println("AppContextListener: DAOs en memoria inicializados y almacenados.");

        // 2. Inicializar Servicios (con sus dependencias DAO)
        EventoService eventoService = new EventoService(eventoDAO, tipoEntradaDAO);
        UsuarioService usuarioService = new UsuarioService(usuarioDAO, compraDAO);
        // PagoService es una clase concreta por ahora, podría ser interfaz y tener impl.
        PagoServiceImpl pagoService = new PagoServiceImpl(); // O ProcesadorPago pagoService = new PagoServiceImpl();
        NotificacionService notificacionServiceStub = new NotificacionService(); // Stub
        ControlAccesoService controlAccesoService = new ControlAccesoService(); // Stub/Simple
        ProcesadorReembolso procesadorReembolso = new ProcesadorReembolso(pagoService, eventoService, notificacionServiceStub);


        ctx.setAttribute("eventoService", eventoService);
        ctx.setAttribute("usuarioService", usuarioService);
        ctx.setAttribute("pagoService", pagoService);
        ctx.setAttribute("notificacionService", notificacionServiceStub);
        ctx.setAttribute("controlAccesoService", controlAccesoService);
        ctx.setAttribute("procesadorReembolso", procesadorReembolso);
        System.out.println("AppContextListener: Servicios inicializados y almacenados.");

        // 3. Inicializar Componentes de Patrones de Diseño que necesitan configuración global

        // Fábricas de Entradas
        Map<String, TipoEntradaFactory> entradaFactories = new HashMap<>();
        entradaFactories.put("General", new EntradaGeneralFactory());
        entradaFactories.put("VIP", new EntradaVIPFactory());
        entradaFactories.put("EarlyAccess", new EntradaEarlyAccessFactory()); // Asumiendo que "EarlyAccess" es un nombreTipo
        ctx.setAttribute("entradaFactories", entradaFactories);

        // Cadena de Responsabilidad para validación de compra
        ValidacionHandler validarDisponibilidad = new ValidarDisponibilidadHandler();
        ValidacionHandler validarLimite = new ValidarLimiteUsuarioHandler();
        ValidacionHandler validarPromocion = new ValidarPromocionHandler(); // Podría necesitar PromocionService/DAO

        validarDisponibilidad.setNext(validarLimite);
        validarLimite.setNext(validarPromocion); // La cadena termina aquí o en el último handler
        ctx.setAttribute("cadenaValidacionCompra", validarDisponibilidad);

        // ProcesoCompraFacade
        ProcesoCompraFacade procesoCompraFacade = new ProcesoCompraFacade(
            validarDisponibilidad, // inicio de la cadena
            eventoService,
            usuarioService,
            pagoService, // Usando la instancia de PagoServiceImpl como ProcesadorPago
            notificacionServiceStub, // Usando el stub
            entradaFactories
        );
        ctx.setAttribute("procesoCompraFacade", procesoCompraFacade);

        // SistemaNotificaciones (Singleton)
        SistemaNotificaciones sistemaNotificaciones = SistemaNotificaciones.getInstance();
        ctx.setAttribute("sistemaNotificacionesSingleton", sistemaNotificaciones);

        // GestorRecomendacionesStrategy
        GestorRecomendacionesStrategy gestorRecomendaciones = new GestorRecomendacionesStrategy(eventoService);
        ctx.setAttribute("gestorRecomendacionesStrategy", gestorRecomendaciones);

        // MediadorConcreto
        MediadorCompras mediador = new MediadorConcreto();
        mediador.registrarProcesoCompraFacade(procesoCompraFacade);
        mediador.registrarGestorRecomendaciones(gestorRecomendaciones);
        mediador.registrarSistemaNotificaciones(sistemaNotificaciones);
        // El mediador también podría necesitar EventoService y UsuarioService para algunas operaciones
        ((MediadorConcreto)mediador).registrarEventoService(eventoService);
        ((MediadorConcreto)mediador).registrarUsuarioService(usuarioService);
        // Los usuarios se registrarían dinámicamente en el mediador al iniciar sesión o al ser cargados.
        ctx.setAttribute("mediadorCompras", mediador);

        System.out.println("AppContextListener: Componentes de patrones de diseño inicializados.");
        System.out.println("AppContextListener: Contexto de la aplicación inicializado correctamente.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Limpiar recursos si es necesario
        System.out.println("AppContextListener: Contexto de la aplicación destruido.");
    }
}
