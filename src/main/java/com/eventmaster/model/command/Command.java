package com.eventmaster.model.command;

import java.time.LocalDateTime;

// Interfaz para el patrón Command
public interface Command {
    boolean execute(); // Devuelve true si la ejecución fue exitosa, false en caso contrario
    boolean undo();    // Devuelve true si el deshacer fue exitoso
    String getDescription(); // Descripción del comando para historial o logging
    LocalDateTime getTiempoEjecucion(); // Momento en que se ejecutó o intentó ejecutar
}
