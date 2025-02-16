/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

public class PCB {
    // Enum para representar el estado del proceso
    public enum Estado {
        READY, RUNNING, BLOCKED, FINISHED
    }
    
    private int processId;
    private String nombreProceso;
    private int programCounter; // PC
    private int mar;            // Memory Address Register
    private Estado estado;
    
    // Constructor
    public PCB(int processId, String nombreProceso) {
        this.processId = processId;
        this.nombreProceso = nombreProceso;
        this.programCounter = 0;
        this.mar = 0;
        this.estado = Estado.READY;
    }
    
    // Getters y Setters
    public int getProcessId() {
        return processId;
    }
    
    public String getNombreProceso() {
        return nombreProceso;
    }
    
    public int getProgramCounter() {
        return programCounter;
    }
    
    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
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
    
    // Método para simular la ejecución de una instrucción
    // Incrementa tanto el PC como el MAR en 1
    public void incrementarPC() {
        programCounter++;
        mar++;
    }
    
    @Override
    public String toString() {
        return "PCB{" +
                "processId=" + processId +
                ", nombreProceso='" + nombreProceso + '\'' +
                ", programCounter=" + programCounter +
                ", mar=" + mar +
                ", estado=" + estado +
                '}';
    }
}