package com.eventmaster.model.pattern.factory;

// Esta clase representa la definición de un tipo de entrada para un evento,
// no una instancia de entrada vendida.
public class TipoEntrada {
    private String nombreTipo; // "General", "VIP", "Early Bird"
    private double precioBase;
    private int cantidadTotal;
    private int cantidadDisponible;
    private int limiteCompraPorUsuario;
    // Otros atributos específicos del tipo de entrada, como beneficios para VIP.
    private java.util.List<String> beneficiosExtra;

    // Campos para decoradores configurables (Simplificación Inicial)
    private boolean ofreceMercanciaOpcional;
    private String descripcionMercancia;
    private double precioAdicionalMercancia;

    private boolean ofreceDescuentoOpcional; // Podría ser un tipo de descuento específico
    private String descripcionDescuento;
    private double montoDescuentoFijo; // Usaremos monto fijo por ahora. Porcentaje sería otra variable.
    // private int limiteUsosDescuento; // Para descuentos limitados
    // private int usosActualesDescuento;


    public TipoEntrada(String nombreTipo, double precioBase, int cantidadTotal, int limiteCompraPorUsuario) {
        this.nombreTipo = nombreTipo;
        this.precioBase = precioBase;
        this.cantidadTotal = cantidadTotal;
        this.cantidadDisponible = cantidadTotal; // Inicialmente todas disponibles
        this.limiteCompraPorUsuario = limiteCompraPorUsuario > 0 ? limiteCompraPorUsuario : Integer.MAX_VALUE; // 0 o negativo significa sin límite
        this.beneficiosExtra = new java.util.ArrayList<>();

        // Inicializar valores por defecto para opciones de decorador
        this.ofreceMercanciaOpcional = false;
        this.precioAdicionalMercancia = 0.0;
        this.ofreceDescuentoOpcional = false;
        this.montoDescuentoFijo = 0.0;
    }

    // Getters y Setters para los nuevos campos de decoradores
    public boolean isOfreceMercanciaOpcional() {
        return ofreceMercanciaOpcional;
    }

    public void setOfreceMercanciaOpcional(boolean ofreceMercanciaOpcional) {
        this.ofreceMercanciaOpcional = ofreceMercanciaOpcional;
    }

    public String getDescripcionMercancia() {
        return descripcionMercancia;
    }

    public void setDescripcionMercancia(String descripcionMercancia) {
        this.descripcionMercancia = descripcionMercancia;
    }

    public double getPrecioAdicionalMercancia() {
        return precioAdicionalMercancia;
    }

    public void setPrecioAdicionalMercancia(double precioAdicionalMercancia) {
        this.precioAdicionalMercancia = precioAdicionalMercancia;
    }

    public boolean isOfreceDescuentoOpcional() {
        return ofreceDescuentoOpcional;
    }

    public void setOfreceDescuentoOpcional(boolean ofreceDescuentoOpcional) {
        this.ofreceDescuentoOpcional = ofreceDescuentoOpcional;
    }

    public String getDescripcionDescuento() {
        return descripcionDescuento;
    }

    public void setDescripcionDescuento(String descripcionDescuento) {
        this.descripcionDescuento = descripcionDescuento;
    }

    public double getMontoDescuentoFijo() {
        return montoDescuentoFijo;
    }

    public void setMontoDescuentoFijo(double montoDescuentoFijo) {
        this.montoDescuentoFijo = montoDescuentoFijo;
    }

    // --- Fin Getters y Setters para decoradores ---


    // Getters Originales
    public String getNombreTipo() {
        return nombreTipo;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    public int getCantidadTotal() {
        return cantidadTotal;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    public int getLimiteCompraPorUsuario() {
        return limiteCompraPorUsuario;
    }

    public java.util.List<String> getBeneficiosExtra() {
        return beneficiosExtra;
    }

    // Setters
    public void setNombreTipo(String nombreTipo) {
        this.nombreTipo = nombreTipo;
    }

    public void setPrecioBase(double precioBase) {
        this.precioBase = precioBase;
    }

    public void setCantidadTotal(int cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
        // Aquí se podría ajustar cantidadDisponible si es necesario, aunque usualmente se define al crear.
    }

    public void setLimiteCompraPorUsuario(int limite) {
        this.limiteCompraPorUsuario = limite > 0 ? limite : Integer.MAX_VALUE;
    }

    public void addBeneficioExtra(String beneficio) {
        this.beneficiosExtra.add(beneficio);
    }


    // Métodos de negocio
    public boolean revisarDisponibilidad(int cantidadRequerida) {
        return cantidadDisponible >= cantidadRequerida;
    }

    public void reducirDisponibilidad(int cantidad) {
        if (revisarDisponibilidad(cantidad)) {
            cantidadDisponible -= cantidad;
        } else {
            // Manejar error, lanzar excepción o devolver false
            System.err.println("Error: No hay suficientes entradas de tipo '" + nombreTipo + "' disponibles.");
            // throw new IllegalStateException("No hay suficientes entradas disponibles.");
        }
    }

    public void aumentarDisponibilidad(int cantidad) {
        cantidadDisponible += cantidad;
        if (cantidadDisponible > cantidadTotal) {
            cantidadDisponible = cantidadTotal; // No exceder la cantidad total original
        }
    }

    @Override
    public String toString() {
        return "TipoEntrada{" +
               "nombreTipo='" + nombreTipo + '\'' +
               ", precioBase=" + precioBase +
               ", cantidadDisponible=" + cantidadDisponible +
               '/' + cantidadTotal +
               '}';
    }
}
