package com.eventmaster.model.pattern.template_method;

import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.pattern.factory.Entrada;
import com.eventmaster.model.pattern.factory.TipoEntrada;
import com.eventmaster.model.pattern.factory.TipoEntradaFactory; // Necesitaremos las fábricas
import com.eventmaster.model.pattern.decorator.EntradaConDescuento; // Ejemplo de decorador
import com.eventmaster.model.CodigoDescuento; // Para manejar códigos de descuento
import com.eventmaster.service.*; // Todos los servicios

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Implementación concreta del Template Method
public class ProcesoCompraEstandar extends ProcesoCompraTemplate {

    private Map<String, TipoEntradaFactory> entradaFactories;
    // private PromocionService promocionService; // Para validar códigos de descuento

    // CAMBIO en el tipo del primer parámetro del constructor
    public ProcesoCompraEstandar(ProcesadorPago procesadorPago, NotificacionService notificacionService,
                                 EventoService eventoService, UsuarioService usuarioService,
                                 Map<String, TipoEntradaFactory> entradaFactories
                                 /*, PromocionService promocionService */) {
        super(procesadorPago, notificacionService, eventoService, usuarioService); // CAMBIO: pasar procesadorPago
        this.entradaFactories = entradaFactories;
        // this.promocionService = promocionService;
    }

    @Override
    protected void aplicarPromocionesSiCorresponde(Compra compra, String codigoDescuentoIngresado, TipoEntrada tipoEntrada) {
        System.out.println("ProcesoCompraEstandar: Aplicando promociones/descuentos...");
        double precioActual = compra.getTotalPagado(); // Total calculado con precio base
        double descuentoAplicado = 0.0;
        String motivoDescuentoTotal = "";

        // Lógica para validar y aplicar código de descuento (simplificada)
        if (codigoDescuentoIngresado != null && !codigoDescuentoIngresado.isEmpty()) {
            // Aquí se buscaría el CodigoDescuento en un servicio o repositorio
            // CodigoDescuento codigo = promocionService.validarYObtenerCodigo(codigoDescuentoIngresado);
            // Simulamos un código válido:
            if ("DESCUENTO10".equals(codigoDescuentoIngresado)) {
                CodigoDescuento codigo = new CodigoDescuento("DESCUENTO10", 0.10, java.time.LocalDateTime.now().plusDays(1), 100);
                if (codigo.esValido()) {
                    descuentoAplicado = tipoEntrada.getPrecioBase() * codigo.getPorcentajeDescuento(); // Descuento por entrada
                    precioActual -= (descuentoAplicado * compra.getEntradasCompradas().size()); // Asumiendo que se aplica a todas
                    motivoDescuentoTotal += "Código DESCUENTO10 aplicado. ";
                    // codigo.incrementarUso(); // Marcar uso
                    System.out.println("ProcesoCompraEstandar: Descuento del 10% aplicado por código DESCUENTO10.");
                } else {
                     System.out.println("ProcesoCompraEstandar: Código de descuento " + codigoDescuentoIngresado + " no válido o expirado.");
                }
            } else {
                System.out.println("ProcesoCompraEstandar: Código de descuento " + codigoDescuentoIngresado + " no encontrado.");
            }
        }

        // Lógica para promociones automáticas del evento/tipo de entrada (ejemplo)
        // if (evento.tienePromocionAutomaticaVigentePara(tipoEntrada)) {
        //     Promocion promo = evento.getPromocionAutomatica();
        //     double descuentoPromo = tipoEntrada.getPrecioBase() * promo.getPorcentajeDescuento();
        //     precioActual -= (descuentoPromo * compra.getEntradasCompradas().size());
        //     motivoDescuentoTotal += promo.getDescripcion() + " aplicada. ";
        // }

        if (descuentoAplicado > 0) {
            compra.setTotalPagado(precioActual); // Actualizar el total en la compra
            // Podríamos añadir una nota sobre el descuento en la compra
            // compra.setNotaDescuento(motivoDescuentoTotal);
        }
        System.out.println("ProcesoCompraEstandar: Total después de promociones: " + compra.getTotalPagado());
    }

    @Override
    protected List<Entrada> generarEntradas(Compra compra, TipoEntrada tipoEntradaDef, int cantidad) {
        System.out.println("ProcesoCompraEstandar: Generando " + cantidad + " entradas de tipo '" + tipoEntradaDef.getNombreTipo() + "'.");
        List<Entrada> entradasGeneradas = new ArrayList<>();

        TipoEntradaFactory factory = entradaFactories.get(tipoEntradaDef.getNombreTipo());
        if (factory == null) {
            System.err.println("ProcesoCompraEstandar: No se encontró fábrica para tipo '" + tipoEntradaDef.getNombreTipo() + "'. Usando General por defecto.");
            factory = entradaFactories.get("General"); // Fallback o error
            if (factory == null) {
                 System.err.println("ProcesoCompraEstandar: No hay fábrica General disponible. No se pueden generar entradas.");
                 return null;
            }
        }

        for (int i = 0; i < cantidad; i++) {
            String idUnicoEntrada = UUID.randomUUID().toString();
            // El precio final por entrada ya debería estar calculado si hubo descuentos
            // y reflejado en compra.getTotalPagado() / cantidad.
            // O, si el descuento se aplica a la entrada individualmente:
            double precioEntradaIndividual = compra.getTotalPagado() / cantidad; // Simplificación

            Map<String, Object> extrasParaEntrada = new HashMap<>();
             if (tipoEntradaDef.getNombreTipo().toLowerCase().contains("early")) {
                // Asumimos que los minutos de anticipación están en los beneficios o un campo específico de TipoEntrada
                // Por ahora, un valor fijo como ejemplo.
                extrasParaEntrada.put("minutosAnticipacion", tipoEntradaDef.getBeneficiosExtra().contains("Acceso Anticipado 60min") ? 60 : 30);
            }

            Entrada entradaBase = factory.crearEntrada(idUnicoEntrada, tipoEntradaDef, compra.getEvento(), precioEntradaIndividual, extrasParaEntrada);

            // Ejemplo de cómo se podría aplicar un decorador basado en una condición
            // (ej. si se aplicó un código de descuento específico que añade un beneficio)
            // if (compra.getNotaDescuento() != null && compra.getNotaDescuento().contains("DESCUENTO10")) {
            //    entradaBase = new EntradaConDescuento(entradaBase, 0.10, "Promoción Web");
            //    // ¡Ojo! Esto aplicaría el descuento dos veces si ya se reflejó en precioEntradaIndividual.
            //    // Sería mejor que el decorador solo añada la descripción si el precio ya está ajustado.
            // }

            entradasGeneradas.add(entradaBase);
        }
        return entradasGeneradas;
    }

    @Override
    protected void finalizarCompraHook(Compra compra) {
        super.finalizarCompraHook(compra); // Llama al hook base (que no hace nada)
        System.out.println("ProcesoCompraEstandar Hook: Compra estándar finalizada. ID: " + compra.getId());
        // Podría registrar logs adicionales, etc.
        compra.generarComprobante(); // Mostrar el comprobante al final
    }
}
