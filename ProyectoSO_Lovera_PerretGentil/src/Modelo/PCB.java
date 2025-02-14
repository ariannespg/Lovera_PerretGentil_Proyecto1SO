/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author arianneperret-gentil
 */

/**
 * Representa el "Process Control Block" de un proceso.
 * Almacena estado, registros, PC, MAR, etc.
 */
public class PCB {
    public enum Estado {
        READY, RUNNING, BLOCKED, FINISHED
    }

    private int processId;
    private String nombreProceso;
    private int programCounter;  // PC
    private int mar;             // Memory Address Register (ejemplo)
    private Estado estado;

    public PCB(int processId, String nombreProceso) {
        this.processId = processId;
        this.nombreProceso = nombreProceso;
        this.programCounter = 0;
        this.mar = 0;
        this.estado = Estado.READY;
    }

    // Getters y setters
    public int getProcessId() {
        return processId;
    }

    public String getNombreProceso() {
        return nombreProceso;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public void setProgramCounter(int pc) {
        this.programCounter = pc;
    }

    public int getMar() {
        return mar;
    }

    public void setMar(int mar) {
        this.mar = mar;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public void incrementarPC() {
        this.programCounter++;
        this.mar++;
    }
}

