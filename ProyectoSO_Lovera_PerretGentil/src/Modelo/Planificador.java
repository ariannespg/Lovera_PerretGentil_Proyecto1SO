/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import Estructuras.ListaEnlazada;

/**
 *
 * @author adrianlovera
 */
public class Planificador {
    public enum Algoritmo {
        FCFS, SJF, ROUND_ROBIN
    }

    private ListaEnlazada colaListos;
    private Algoritmo algoritmo;
    private CPU[] cpus;
    private int quantum;  // Quantum de Round Robin

    // Constructor
    public Planificador(int numCPUs, Algoritmo algoritmo, int quantum) {
        this.colaListos = new ListaEnlazada();
        this.algoritmo = algoritmo;
        this.cpus = new CPU[numCPUs];
        this.quantum = quantum;

        // Inicializar CPUs
        for (int i = 0; i < numCPUs; i++) {
            cpus[i] = new CPU(i + 1, 1000);
            cpus[i].start(); // Iniciar las CPUs
        }
    }

    // Agregar proceso a la cola de listos
    public void agregarProceso(Proceso proceso) {
        synchronized (colaListos) {
            colaListos.agregar(proceso);
            System.out.println("Proceso " + proceso.getNombre() + " agregado a la cola de listos.");
        }
    }

    // Método que asigna procesos a CPUs según el algoritmo de planificación
    public void planificar() {
        while (true) {
            for (CPU cpu : cpus) {
                if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
                    Proceso procesoSeleccionado = seleccionarProceso();
                    ejecutarProceso(cpu, procesoSeleccionado);
                }
            }

            // Simulación de tiempo entre asignaciones
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Ejecutar un proceso en una CPU, manejando Round Robin
    private void ejecutarProceso(CPU cpu, Proceso proceso) {
        cpu.asignarProceso(proceso);
        
        if (algoritmo == Algoritmo.ROUND_ROBIN) {
            new Thread(() -> {
                try {
                    Thread.sleep(quantum); // Esperar el quantum de tiempo
                    if (cpu.estaOcupada() && cpu.getProcesoActual().getId() == proceso.getId()) {
                        System.out.println("Quantum expirado. Interrumpiendo proceso " + proceso.getNombre());
                        cpu.interrumpirProceso();
                        agregarProceso(proceso); // Reagregar el proceso al final de la cola
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    // Seleccionar proceso según el algoritmo actual
    private Proceso seleccionarProceso() {
        synchronized (colaListos) {
            if (colaListos.estaVacia()) return null;

            switch (algoritmo) {
                case FCFS:
                    return colaListos.remover();
                case SJF:
                    return colaListos.obtenerSJF();
                case ROUND_ROBIN:
                    return colaListos.remover(); // Se maneja en el ciclo de ejecución
                default:
                    return colaListos.remover();
            }
        }
    }

    // Cambiar el algoritmo de planificación en tiempo de ejecución
    public void setAlgoritmo(Algoritmo algoritmo) {
        this.algoritmo = algoritmo;
        System.out.println("Algoritmo cambiado a: " + algoritmo);
    }

    // Mostrar la cola de listos
    public void mostrarColaListos() {
        System.out.println("Cola de Listos:");
        Proceso p = colaListos.obtenerPrimero();
        while (p != null) {
            System.out.println(p);
            p = colaListos.obtenerPrimero();
        }
    }
    
    // Retorna los procesos en la cola de listos como un array de Strings
    public Proceso[] getListaProcesos() {
    return colaListos.obtenerTodosProcesos();
}
    
    // Retorna la cantidad de CPUs disponibles en el planificador
    public int getNumCPUs() {
        return cpus.length;
    }
    
    public String getEstadoCPU(int i) {
    if (i < 0 || i >= cpus.length) return "[ERROR]";
    return cpus[i].estaOcupada() ? "Ejecutando: " + cpus[i].getProcesoActual().getNombre() : "[IDLE]";
}
    
}
