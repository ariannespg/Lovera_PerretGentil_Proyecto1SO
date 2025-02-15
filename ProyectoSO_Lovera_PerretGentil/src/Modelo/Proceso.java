/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author adrianlovera
 */
public class Proceso {
    private static int contadorProcesos = 1; // ID autoincremental
    private int id;
    private String nombre;
    private PCB pcb;
    private boolean esCpuBound;
    private int ciclosParaExcepcion;
    private int ciclosAtencionExcepcion;
    private int ciclosEjecutados;

    // Constructor
    public Proceso(String nombre, boolean esCpuBound, int ciclosParaExcepcion, int ciclosAtencionExcepcion) {
        this.id = contadorProcesos++;
        this.nombre = nombre;
        this.pcb = new PCB(id, nombre);
        this.esCpuBound = esCpuBound;
        this.ciclosParaExcepcion = esCpuBound ? -1 : ciclosParaExcepcion; // Solo I/O-bound tiene excepciones
        this.ciclosAtencionExcepcion = esCpuBound ? -1 : ciclosAtencionExcepcion;
        this.ciclosEjecutados = 0;
    }

    // Método que simula la ejecución del proceso
    public void ejecutarCiclo() {
        if (pcb.getEstado() == PCB.Estado.RUNNING) {
            pcb.incrementarPC(); // Avanza el PC y MAR
            ciclosEjecutados++;

            if (!esCpuBound && ciclosEjecutados % ciclosParaExcepcion == 0) {
                pcb.setEstado(PCB.Estado.BLOCKED);
                System.out.println("Proceso " + nombre + " bloqueado por I/O.");
            }
        }
    }

    // Método para reanudar el proceso tras una excepción
    public void resolverExcepcion() {
        try {
            Thread.sleep(ciclosAtencionExcepcion * 1000); // Simula el tiempo de espera
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pcb.setEstado(PCB.Estado.READY);
        System.out.println("Proceso " + nombre + " reanudado tras I/O.");
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public PCB getPcb() {
        return pcb;
    }

    public boolean isCpuBound() {
        return esCpuBound;
    }

    public int getCiclosEjecutados() {
        return ciclosEjecutados;
    }

    public PCB.Estado getEstado() {
        return pcb.getEstado();
    }

    public void setEstado(PCB.Estado estado) {
        this.pcb.setEstado(estado);
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