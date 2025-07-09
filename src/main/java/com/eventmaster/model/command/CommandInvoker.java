package com.eventmaster.model.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CommandInvoker {
    private List<Command> historialComandos;
    private Stack<Command> comandosDeshechos; // Para la funcionalidad de rehacer
    private int posicionActualHistorial; // No es tan útil con Stacks, pero podría ser para un historial lineal con límite
    private int maxHistorial;

    // Para modo transaccional (no completamente implementado aquí, solo la idea)
    private boolean modoTransaccionalActivo = false;
    private List<Command> transaccionActual = new ArrayList<>();

    public CommandInvoker(int maxHistorial) {
        this.historialComandos = new ArrayList<>();
        this.comandosDeshechos = new Stack<>();
        this.maxHistorial = maxHistorial > 0 ? maxHistorial : 10; // Default de 10 si no es positivo
        this.posicionActualHistorial = -1;
    }

    public boolean ejecutarComando(Command comando) {
        if (modoTransaccionalActivo) {
            transaccionActual.add(comando);
            // La ejecución real se pospone hasta que se complete la transacción
            return true; // Asumimos que se añade correctamente a la transacción
        }

        boolean exito = comando.execute();
        if (exito) {
            agregarAlHistorial(comando);
            comandosDeshechos.clear(); // Al ejecutar un nuevo comando, se pierde la pila de rehacer
        }
        return exito;
    }

    private void agregarAlHistorial(Command comando) {
        historialComandos.add(comando);
        posicionActualHistorial++;
        // Mantener el tamaño máximo del historial
        if (historialComandos.size() > maxHistorial) {
            historialComandos.remove(0);
            posicionActualHistorial--; // Ajustar índice si se remueve el primero
        }
    }

    public boolean deshacerUltimoComando() {
        if (modoTransaccionalActivo) {
            System.err.println("No se puede deshacer mientras una transacción está activa.");
            return false;
        }
        if (posicionActualHistorial >= 0 && !historialComandos.isEmpty()) {
            Command ultimoComando = historialComandos.get(posicionActualHistorial);
            boolean exitoUndo = ultimoComando.undo();
            if (exitoUndo) {
                comandosDeshechos.push(ultimoComando);
                historialComandos.remove(posicionActualHistorial);
                posicionActualHistorial--;
                return true;
            }
        }
        System.err.println("No hay comandos para deshacer o el último comando no pudo deshacerse.");
        return false;
    }

    public boolean rehacerUltimoComandoDeshecho() {
        if (modoTransaccionalActivo) {
            System.err.println("No se puede rehacer mientras una transacción está activa.");
            return false;
        }
        if (!comandosDeshechos.isEmpty()) {
            Command comandoARehacer = comandosDeshechos.pop();
            boolean exitoRehacer = comandoARehacer.execute(); // Volver a ejecutar
            if (exitoRehacer) {
                agregarAlHistorial(comandoARehacer); // Añadir de nuevo al historial principal
                return true;
            } else {
                // Si falla el rehacer, lo devolvemos a la pila de deshechos para ser consistentes
                comandosDeshechos.push(comandoARehacer);
            }
        }
        System.err.println("No hay comandos para rehacer o el último comando deshecho no pudo rehacerse.");
        return false;
    }

    public List<Command> obtenerHistorial() {
        // Devuelve una copia para evitar modificaciones externas
        return new ArrayList<>(historialComandos.subList(0, posicionActualHistorial + 1));
    }

    public void limpiarHistorial() {
        historialComandos.clear();
        comandosDeshechos.clear();
        posicionActualHistorial = -1;
    }

    public void setMaxHistorial(int max) {
        this.maxHistorial = max > 0 ? max : 10;
        // Podría ajustar el tamaño del historial actual si el nuevo max es menor
        while(historialComandos.size() > this.maxHistorial) {
            historialComandos.remove(0);
            posicionActualHistorial--;
        }
    }

    public int contarComandosEjecutados() {
        return posicionActualHistorial + 1;
    }

    public Command obtenerUltimoComando() {
        if (posicionActualHistorial >= 0 && !historialComandos.isEmpty()) {
            return historialComandos.get(posicionActualHistorial);
        }
        return null;
    }

    // --- Métodos para Transacciones (Simplificado) ---
    public void iniciarTransaccion() {
        modoTransaccionalActivo = true;
        transaccionActual.clear();
        System.out.println("CommandInvoker: Transacción iniciada.");
    }

    public boolean commitTransaccion() {
        if (!modoTransaccionalActivo) {
            System.err.println("No hay transacción activa para hacer commit.");
            return false;
        }

        modoTransaccionalActivo = false;
        List<Command> comandosEjecutadosExitosamente = new ArrayList<>();
        boolean exitoTotal = true;

        for (Command comando : transaccionActual) {
            boolean exitoComando = comando.execute();
            if (exitoComando) {
                comandosEjecutadosExitosamente.add(comando);
            } else {
                exitoTotal = false;
                System.err.println("Fallo al ejecutar comando en transacción: " + comando.getDescription() + ". Iniciando rollback...");
                // Rollback de los comandos ya ejecutados en esta transacción
                for (int i = comandosEjecutadosExitosamente.size() - 1; i >= 0; i--) {
                    comandosEjecutadosExitosamente.get(i).undo();
                }
                transaccionActual.clear();
                System.out.println("CommandInvoker: Rollback de transacción completado.");
                return false;
            }
        }

        if (exitoTotal) {
            // Si todos los comandos de la transacción fueron exitosos, los añadimos al historial principal
            for (Command comando : comandosEjecutadosExitosamente) {
                agregarAlHistorial(comando);
            }
            comandosDeshechos.clear(); // Similar a la ejecución normal de un comando
            System.out.println("CommandInvoker: Commit de transacción exitoso. " + comandosEjecutadosExitosamente.size() + " comandos ejecutados.");
        }
        transaccionActual.clear();
        return exitoTotal;
    }

    public void rollbackTransaccion() {
        if (!modoTransaccionalActivo && transaccionActual.isEmpty()) {
            System.err.println("No hay transacción activa o comandos en la transacción para hacer rollback.");
            return;
        }
        // Si la transacción estaba activa pero no se hizo commit, simplemente se limpia.
        // Si se quiere deshacer comandos ya "ejecutados" dentro de la lógica de commit,
        // esa lógica ya está en commitTransaccion. Este rollback es más para descartar.
        modoTransaccionalActivo = false;
        transaccionActual.clear();
        System.out.println("CommandInvoker: Transacción abortada/rollback (comandos no ejecutados o descartados).");
    }
}
