/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import Estructuras.ListaEnlazada;
import java.util.concurrent.Semaphore;

public class Planificador {
    private ListaEnlazada colaListos;
    private ListaEnlazada colaBloqueados;
    private CPU[] cpus;
    private Semaphore semaforoAsignacion;
    private Algoritmo algoritmo;
    private int duracionCiclo;

    public enum Algoritmo {
        FCFS, SJF, ROUND_ROBIN
    }

    public Planificador(int numCPUs, Algoritmo algoritmo, int duracionCiclo) {
        this.colaListos = new ListaEnlazada();
        this.colaBloqueados = new ListaEnlazada();
        this.cpus = new CPU[numCPUs];
        this.semaforoAsignacion = new Semaphore(1, true);
        this.algoritmo = algoritmo;
        this.duracionCiclo = duracionCiclo;

        // Iniciar las CPUs
        for (int i = 0; i < numCPUs; i++) {
            cpus[i] = new CPU(i + 1, duracionCiclo);
            cpus[i].start();
        }

        // Iniciar el manejo de procesos bloqueados
        new Thread(this::manejarProcesosBloqueados).start();
    }

    // Permite cambiar el número de CPUs en tiempo de ejecución
    public void configurarCPUs(int numCPUs) {
        if (numCPUs > cpus.length) {
            // Aumentar: crear e iniciar CPUs adicionales
            CPU[] newCPUs = new CPU[numCPUs];
            for (int i = 0; i < cpus.length; i++) {
                newCPUs[i] = cpus[i];
            }
            for (int i = cpus.length; i < numCPUs; i++) {
                newCPUs[i] = new CPU(i + 1, duracionCiclo);
                newCPUs[i].start();
            }
            cpus = newCPUs;
            System.out.println("Se han configurado " + numCPUs + " CPUs.");
        } else if (numCPUs < cpus.length) {
            // Disminuir: detener las CPUs excedentes
            for (int i = numCPUs; i < cpus.length; i++) {
                cpus[i].detener();
            }
            CPU[] newCPUs = new CPU[numCPUs];
            for (int i = 0; i < numCPUs; i++) {
                newCPUs[i] = cpus[i];
            }
            cpus = newCPUs;
            System.out.println("Se han configurado " + numCPUs + " CPUs.");
        } else {
            System.out.println("Número de CPUs sin cambios.");
        }
    }

    public void agregarProceso(Proceso proceso) {
        try {
            semaforoAsignacion.acquire();
            colaListos.agregar(proceso);
            System.out.println("Proceso " + proceso.getNombre() + " agregado a la cola de listos.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaforoAsignacion.release();
        }
    }

    public Proceso[] getListaProcesos() {
        return colaListos.obtenerTodosProcesos();
    }

    public Proceso[] getListaProcesosBloqueados() {
        return colaBloqueados.obtenerTodosProcesos();
    }

    public String getEstadoCPU(int i) {
        if (i < 0 || i >= cpus.length) return "[ERROR]";
        return cpus[i].estaOcupada() ? "Ejecutando: " + cpus[i].getProcesoActual().getNombre() : "[IDLE]";
    }

    public int getNumCPUs() {
        return cpus.length;
    }

    public void setAlgoritmo(Algoritmo nuevoAlgoritmo) {
        this.algoritmo = nuevoAlgoritmo;
        System.out.println("Algoritmo cambiado a: " + nuevoAlgoritmo);
    }

    private void manejarProcesosBloqueados() {
        while (true) {
            try {
                semaforoAsignacion.acquire();
                Proceso proceso = (Proceso) colaBloqueados.obtenerPrimero();
                while (proceso != null) {
                    if (proceso.getCiclosRestantesBloqueado() > 0) {
                        proceso.setCiclosRestantesBloqueado(proceso.getCiclosRestantesBloqueado() - 1);
                    }
                    if (proceso.getCiclosRestantesBloqueado() == 0) {
                        proceso.setEstado(PCB.Estado.READY);
                        colaListos.agregar(proceso);
                        colaBloqueados.removerProceso(proceso);
                        System.out.println("Proceso " + proceso.getNombre() + " ha salido de bloqueado y pasó a READY.");
                    }
                    proceso = (Proceso) colaBloqueados.obtenerPrimero();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaforoAsignacion.release();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void planificar() {
        new Thread(() -> {
            while (true) {
                try {
                    semaforoAsignacion.acquire();
                    for (CPU cpu : cpus) {
                        if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
                            Proceso procesoSeleccionado = colaListos.remover();
                            // Si el proceso se encuentra bloqueado, lo reubica en la cola de bloqueados
                            if (procesoSeleccionado.getEstado().equals(PCB.Estado.BLOCKED.toString())) {
                                colaBloqueados.agregar(procesoSeleccionado);
                                System.out.println("Proceso " + procesoSeleccionado.getNombre() + " sigue bloqueado.");
                            } else {
                                cpu.asignarProceso(procesoSeleccionado);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaforoAsignacion.release();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}