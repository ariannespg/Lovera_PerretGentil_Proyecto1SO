package Modelo;

public class PCB {
    public enum Estado {
        READY, RUNNING, BLOCKED, FINISHED
    }
    
    private int processId;
    private String nombreProceso;
    private int programCounter;
    private int mar;
    private Estado estado;
    
    public PCB(int processId, String nombreProceso) {
        this.processId = processId;
        this.nombreProceso = nombreProceso;
        this.programCounter = 0;
        this.mar = 0;
        this.estado = Estado.READY;
    }
    
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