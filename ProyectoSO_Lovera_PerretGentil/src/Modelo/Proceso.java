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

    public Proceso(String nombre, int instrucciones, boolean esCpuBound, 
                   int ciclosParaExcepcion, int ciclosAtencionExcepcion) {
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

        // Por defecto, arrivalTime = 0; se setea en Planificador cuando se ingresa a la cola
        this.arrivalTime = 0;
    }

    // Simular la ejecución de un ciclo
    public void ejecutarCiclo() {
        if (pcb.getEstado() == PCB.Estado.RUNNING) {
            pcb.incrementarPC(); 
            if (pcb.getProgramCounter() >= instrucciones) {
                pcb.setEstado(PCB.Estado.FINISHED);
            }
            // Lógica de bloqueo (I/O) no detallada aquí, 
            // puede usarse ciclosParaExcepcion para forzar un BLOCKED en cierto momento.
        }
    }

    // Simula la resolución de una excepción
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

    // Para HRRN
    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    @Override
    public String toString() {
        return "Proceso{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", estado=" + pcb.getEstado() +
                ", PC=" + pcb.getProgramCounter() +
                ", MAR=" + pcb.getMar() +
                '}';
    }
}
