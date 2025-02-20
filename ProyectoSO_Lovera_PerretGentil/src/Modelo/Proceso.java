/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

public class Proceso {
    private static int contador = 1;
    private final int id;
    private String nombre;
    private final int instrucciones; // cantidad total de instrucciones
    private boolean esCpuBound;
    private int ciclosParaExcepcion;      // Solo para I/O bound
    private int ciclosAtencionExcepcion;  // Solo para I/O bound
    private int ciclosRestantesBloqueado;
    
    // PCB para manejar PC, MAR, estado, etc.
    private PCB pcb;

    // Para HRRN
    private long arrivalTime; // marca de tiempo cuando entra a READY
    
    // NUEVO: bandera para identificar procesos del sistema (SO) vs. usuario
    private boolean esSistema;

    // Constructor para procesos de usuario (por defecto, esSistema = false)
    public Proceso(String nombre, int instrucciones, boolean esCpuBound, 
                   int ciclosParaExcepcion, int ciclosAtencionExcepcion) {
        this(nombre, instrucciones, esCpuBound, ciclosParaExcepcion, ciclosAtencionExcepcion, false);
    }
    
    // Constructor extendido para especificar si es un proceso de sistema
    public Proceso(String nombre, int instrucciones, boolean esCpuBound, 
                   int ciclosParaExcepcion, int ciclosAtencionExcepcion, boolean esSistema) {
        this.id = contador++;
        this.nombre = nombre;
        this.instrucciones = instrucciones;
        this.esCpuBound = esCpuBound;
        if (!esCpuBound) {
            this.ciclosParaExcepcion = ciclosParaExcepcion;
            this.ciclosAtencionExcepcion = ciclosAtencionExcepcion;
        } else {
            this.ciclosParaExcepcion = 0;
            this.ciclosAtencionExcepcion = 0;
        }
        this.ciclosRestantesBloqueado = 0;
        this.pcb = new PCB(this.id, this.nombre);
        this.arrivalTime = 0;
        this.esSistema = esSistema;
    }
    
    // Métodos de ejecución y excepciones (sin cambios)
    public void ejecutarCiclo() {
        if (pcb.getEstado() == PCB.Estado.RUNNING) {
            pcb.incrementarPC();
            if (!esCpuBound && pcb.getProgramCounter() % ciclosParaExcepcion == 0 && pcb.getProgramCounter() > 0) {
                setEstado(PCB.Estado.BLOCKED);
                ciclosRestantesBloqueado = ciclosAtencionExcepcion;
                System.out.println("🔴 Proceso " + nombre + " bloqueado por I/O.");
            }
            if (pcb.getProgramCounter() >= instrucciones) {
                setEstado(PCB.Estado.FINISHED);
                System.out.println("✅ Proceso " + nombre + " ha terminado.");
            }
        }
    }

    public void resolverExcepcion() {
        try {
            Thread.sleep(ciclosAtencionExcepcion * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pcb.setEstado(PCB.Estado.READY);
    }
    
    // Getters y setters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPC() {
        return pcb.getProgramCounter();
    }

    public int getMAR() {
        return pcb.getMar();
    }

    public PCB.Estado getEstado() {
        return pcb.getEstado();
    }

    public void setEstado(PCB.Estado estado) {
        pcb.setEstado(estado);
    }

    public int getCiclosRestantesBloqueado() {
        return ciclosRestantesBloqueado;
    }

    public void setCiclosRestantesBloqueado(int n) {
        this.ciclosRestantesBloqueado = n;
    }

    public PCB getPcb() {
        return pcb;
    }

    public int getInstrucciones() {
        return instrucciones;
    }

    public boolean isEsCpuBound() {
        return esCpuBound;
    }

    public int getCiclosParaExcepcion() {
        return ciclosParaExcepcion;
    }

    public int getCiclosAtencionExcepcion() {
        return ciclosAtencionExcepcion;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    
    // NUEVO: Getter y setter para esSistema
    public boolean isEsSistema() {
        return esSistema;
    }
    
    public void setEsSistema(boolean esSistema) {
        this.esSistema = esSistema;
    }

    @Override
    public String toString() {
        return "Proceso{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tipo=" + (esSistema ? "SO" : "Usuario") +
                ", estado=" + pcb.getEstado() +
                ", PC=" + pcb.getProgramCounter() +
                ", MAR=" + pcb.getMar() +
                '}';
    }
}
