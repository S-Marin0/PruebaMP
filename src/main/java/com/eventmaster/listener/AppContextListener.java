package com.eventmaster.listener;

import com.eventmaster.dao.*;
import com.eventmaster.dao.impl.mysql.*; // Changed from com.eventmaster.dao.impl.memoria.*
import com.eventmaster.service.*;
import com.eventmaster.util.DatabaseManager; // Added DatabaseManager import
import com.eventmaster.model.facade.ProcesoCompraFacade;
import com.eventmaster.model.pattern.chain_of_responsibility.*;
import com.eventmaster.model.pattern.factory.TipoEntradaFactory;
import com.eventmaster.model.pattern.factory.EntradaGeneralFactory;
import com.eventmaster.model.pattern.factory.EntradaVIPFactory;
import com.eventmaster.model.pattern.factory.EntradaEarlyAccessFactory;
import com.eventmaster.model.pattern.strategy.GestorRecomendacionesStrategy;
import com.eventmaster.model.singleton.SistemaNotificaciones;
// ELIMINAR: import com.eventmaster.model.pattern.mediator.MediadorConcreto;
// ELIMINAR: import com.eventmaster.model.pattern.mediator.MediadorCompras;


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

        // Initialize DatabaseManager
        // No need to store DatabaseManager in servlet context as it's a static utility class.
        // However, you might want to perform an initial connection test here if desired.
        try {
            DatabaseManager.getConnection().close(); // Test connection
            System.out.println("AppContextListener: Database connection test successful.");
        } catch (Exception e) {
            System.err.println("AppContextListener: Failed to connect to the database during initialization.");
            // Depending on the application's requirements, you might want to throw a RuntimeException
            // to prevent the application from starting if the DB is not available.
            // throw new RuntimeException("Failed to initialize database connection", e);
        }


        // 1. Inicializar DAOs (MySQL)
        // These DAOs use DatabaseManager.getConnection() internally and don't require it in constructor.
        UsuarioDAO usuarioDAO = new UsuarioDAOImplMySQL();
        LugarDAO lugarDAO = new LugarDAOImplMySQL();
        // TipoEntradaDAO tipoEntradaDAO = new TipoEntradaDAOImplMySQL(); // To be created
        // EntradaDAO entradaDAO = new EntradaDAOImplMySQL(); // To be created
        // CompraDAO compraDAO = new CompraDAOImplMySQL(); // To be created
        // PromocionDAO promocionDAO = new PromocionDAOImplMySQL(); // To be created
        // CodigoDescuentoDAO codigoDescuentoDAO = new CodigoDescuentoDAOImplMySQL(); // To be created

        // EventoDAOImplMySQL requires LugarDAO and UsuarioDAO for fetching related entities
        EventoDAOImplMySQL eventoDAOImplMySQL = new EventoDAOImplMySQL();
        eventoDAOImplMySQL.setLugarDAO(lugarDAO); // Inject LugarDAO
        eventoDAOImplMySQL.setUsuarioDAO(usuarioDAO); // Inject UsuarioDAO
        EventoDAO eventoDAO = eventoDAOImplMySQL;


        // Initialize other DAOs (placeholder for now, will be MySQL implementations)
        // These will be replaced with actual MySQL implementations as they are created.
        TipoEntradaDAO tipoEntradaDAO = new TipoEntradaDAOImplMySQL(); // Changed from Memoria

        EntradaDAOImplMySQL entradaDAOImplMySQL = new EntradaDAOImplMySQL();
        entradaDAOImplMySQL.setEventoDAO(eventoDAO); // Inject EventoDAO into EntradaDAO
        EntradaDAO entradaDAO = entradaDAOImplMySQL; // Changed from Memoria

        CompraDAOImplMySQL compraDAOImplMySQL = new CompraDAOImplMySQL();
        compraDAOImplMySQL.setUsuarioDAO(usuarioDAO); // Inject UsuarioDAO
        compraDAOImplMySQL.setEventoDAO(eventoDAO);   // Inject EventoDAO
        // If CompraDAO needed EntradaDAO to populate List<Entrada>, it would be injected here too.
        CompraDAO compraDAO = compraDAOImplMySQL; // Changed from Memoria

        PromocionDAO promocionDAO = new PromocionDAOImplMySQL(); // Changed from Memoria

        CodigoDescuentoDAOImplMySQL codigoDescuentoDAOImplMySQL = new CodigoDescuentoDAOImplMySQL();
        codigoDescuentoDAOImplMySQL.setPromocionDAO(promocionDAO); // Inject PromocionDAO
        CodigoDescuentoDAO codigoDescuentoDAO = codigoDescuentoDAOImplMySQL; // Changed from Memoria


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
        // Instanciar PagoServiceImpl y referenciarlo mediante la interfaz ProcesadorPago
        ProcesadorPago procesadorPago = new PagoServiceImpl();
        NotificacionService notificacionServiceStub = new NotificacionService(); // Stub
        ControlAccesoService controlAccesoService = new ControlAccesoService(); // Stub/Simple
        // Asegurarse que ProcesadorReembolso espera un ProcesadorPago
        ProcesadorReembolso procesadorReembolso = new ProcesadorReembolso(procesadorPago, eventoService, notificacionServiceStub);

        ctx.setAttribute("eventoService", eventoService);
        ctx.setAttribute("usuarioService", usuarioService);
        ctx.setAttribute("procesadorPago", procesadorPago); // Guardar bajo la interfaz
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
            procesadorPago, // Usar la instancia de ProcesadorPago
            notificacionServiceStub, // Usando el stub
            entradaFactories,
            compraDAO // Pasar la instancia de CompraDAO
        );
        ctx.setAttribute("procesoCompraFacade", procesoCompraFacade);

        // SistemaNotificaciones (Singleton)
        SistemaNotificaciones sistemaNotificaciones = SistemaNotificaciones.getInstance();
        ctx.setAttribute("sistemaNotificacionesSingleton", sistemaNotificaciones);

        // GestorRecomendacionesStrategy
        GestorRecomendacionesStrategy gestorRecomendaciones = new GestorRecomendacionesStrategy(eventoService);
        ctx.setAttribute("gestorRecomendacionesStrategy", gestorRecomendaciones);

        // ELIMINAR BLOQUE DEL MEDIADOR:
        // // MediadorConcreto
        // MediadorCompras mediador = new MediadorConcreto();
        // mediador.registrarProcesoCompraFacade(procesoCompraFacade);
        // mediador.registrarGestorRecomendaciones(gestorRecomendaciones);
        // mediador.registrarSistemaNotificaciones(sistemaNotificaciones);
        // // El mediador también podría necesitar EventoService y UsuarioService para algunas operaciones
        // ((MediadorConcreto)mediador).registrarEventoService(eventoService);
        // ((MediadorConcreto)mediador).registrarUsuarioService(usuarioService);
        // // Los usuarios se registrarían dinámicamente en el mediador al iniciar sesión o al ser cargados.
        // ctx.setAttribute("mediadorCompras", mediador);

        System.out.println("AppContextListener: Componentes de patrones de diseño inicializados (Mediador excluido).");
        System.out.println("AppContextListener: Contexto de la aplicación inicializado correctamente.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Limpiar recursos si es necesario
        System.out.println("AppContextListener: Contexto de la aplicación destruido.");
    }
}
