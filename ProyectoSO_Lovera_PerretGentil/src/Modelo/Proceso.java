/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

public class Proceso {
    private static int contador = 1;
    private final int id;
    private String nombre;
    private final int instrucciones;
    private boolean esCpuBound;
    private int ciclosParaExcepcion;      // Solo para I/O bound
    private int ciclosAtencionExcepcion;  // Solo para I/O bound
    private int ciclosRestantesBloqueado;
    
    // Ahora usamos un PCB para gestionar el PC, MAR y estado
    private PCB pcb;

    public Proceso(String nombre, int instrucciones, boolean esCpuBound, int ciclosParaExcepcion, int ciclosAtencionExcepcion) {
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
        // Inicializamos el PCB con el id y nombre del proceso
        this.pcb = new PCB(this.id, this.nombre);
    }

    // Método para simular la ejecución de un ciclo
    public void ejecutarCiclo() {
        if (pcb.getEstado() == PCB.Estado.RUNNING) {
            pcb.incrementarPC(); // Incrementa PC y MAR
            if (pcb.getProgramCounter() >= instrucciones) {
                pcb.setEstado(PCB.Estado.FINISHED);
            }
            // Aquí puedes ampliar la lógica para procesos I/O bound (por ejemplo, cambiar el estado a BLOCKED)
        }
    }

    // Simula la resolución de una excepción
    public void resolverExcepcion() {
        try {
            Thread.sleep(ciclosAtencionExcepcion * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pcb.setEstado(PCB.Estado.READY);
    }

    // Getters y setters delegados en PCB
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    // Devuelve el PC almacenado en el PCB
    public int getPC() {
        return pcb.getProgramCounter();
    }

    // Devuelve el MAR almacenado en el PCB
    public int getMAR() {
        return pcb.getMar();
    }

    // Devuelve el estado actual (según el PCB)
    public PCB.Estado getEstado() {
    return pcb.getEstado();
}

    // Permite cambiar el estado (usando el PCB)
    public void setEstado(PCB.Estado estado) {
        pcb.setEstado(estado);
    }

    public int getCiclosRestantesBloqueado() {
        return ciclosRestantesBloqueado;
    }

    public void setCiclosRestantesBloqueado(int n) {
        this.ciclosRestantesBloqueado = n;
    }

    // Método para obtener el PCB (necesario para SJF en ListaEnlazada)
    public PCB getPcb() {
        return pcb;
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
