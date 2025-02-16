/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import Estructuras.ListaEnlazada;
import java.util.concurrent.Semaphore;

/**
 *
 * @author adrianlovera
 */
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

        // Iniciar CPUs
        for (int i = 0; i < numCPUs; i++) {
            cpus[i] = new CPU(i + 1, 1000);
            cpus[i].start();
        }

        // Iniciar el manejo de procesos bloqueados
        new Thread(this::manejarProcesosBloqueados).start();
    }

    // 📌 Restaurado: Obtener la lista de procesos en la cola de listos
    public Proceso[] getListaProcesos() {
        return colaListos.obtenerTodosProcesos();
    }

    // 📌 Restaurado: Obtener el estado de una CPU específica
    public String getEstadoCPU(int i) {
        if (i < 0 || i >= cpus.length) return "[ERROR]";
        return cpus[i].estaOcupada() ? "Ejecutando: " + cpus[i].getProcesoActual().getNombre() : "[IDLE]";
    }

    // 📌 Restaurado: Obtener número de CPUs
    public int getNumCPUs() {
        return cpus.length;
    }

    // 📌 Restaurado: Obtener el estado de un proceso específico
    public String getEstadoProceso(int id) {
        Proceso[] procesos = colaListos.obtenerTodosProcesos();
        for (Proceso p : procesos) {
            if (p.getId() == id) {
                return p.getEstado().toString();
            }
        }
        return "No encontrado";
    }

    public void setAlgoritmo(Algoritmo nuevoAlgoritmo) {
        this.algoritmo = nuevoAlgoritmo;
        System.out.println("Algoritmo cambiado a: " + nuevoAlgoritmo);
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

    private void manejarProcesosBloqueados() {
        while (true) {
            try {
                semaforoAsignacion.acquire();

                Proceso proceso = colaBloqueados.obtenerPrimero();

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

                    proceso = colaBloqueados.obtenerPrimero();
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

                            if (procesoSeleccionado.getEstado() == PCB.Estado.BLOCKED) {
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
    public Proceso[] getListaProcesosBloqueados() {
        return colaBloqueados.obtenerTodosProcesos();
}
}