/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.util.concurrent.Semaphore;

public class CPU extends Thread {
    private int id;
    private volatile Proceso procesoActual; 
    private volatile boolean ejecutando;     
    private int duracionCiclo;
    private final Object lock = new Object(); 
    private Semaphore semaforoCPU; // Controla acceso a la CPU
    private Planificador planificador;


    public CPU(int id, int duracionCiclo) {
        this.id = id;
        this.procesoActual = null;
        this.ejecutando = true;
        this.duracionCiclo = duracionCiclo;
        this.semaforoCPU = new Semaphore(1); // Permite un proceso a la vez
    }

    // Asigna un proceso a la CPU de forma sincronizada
    public void asignarProceso(Proceso proceso) {
        try {
            semaforoCPU.acquire();
            synchronized (lock) {
                this.procesoActual = proceso;
                this.procesoActual.setEstado(PCB.Estado.RUNNING);
                lock.notify(); 
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (ejecutando) {
            synchronized (lock) {
                while (procesoActual == null && ejecutando) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        if (!ejecutando) break;
                    }
                }
            }

            if (!ejecutando) break;

            // Ejecutar ciclo de proceso
            while (procesoActual != null && procesoActual.getEstado() == PCB.Estado.RUNNING) {
                procesoActual.ejecutarCiclo();
                System.out.println("CPU " + id + " ejecutando: " + procesoActual.getNombre());

                // Si el proceso se bloquea por I/O, lo libera
                if (procesoActual.getEstado() == PCB.Estado.BLOCKED) {
                    System.out.println("CPU " + id + " detectó bloqueo en proceso " + procesoActual.getNombre());
                    if (planificador != null) {
    planificador.agregarBloqueado(procesoActual);

    }
    procesoActual = null;
    semaforoCPU.release();
    break;
}


                // Si el proceso termina, la CPU lo libera
                if (procesoActual.getEstado() == PCB.Estado.FINISHED) {
                    System.out.println("CPU " + id + " terminó proceso: " + procesoActual.getNombre());
                    if (planificador != null) {
                        planificador.agregarProcesoTerminado(procesoActual);
                            }
                    procesoActual = null;
                    semaforoCPU.release();
                    break;
                }

                try {
                    Thread.sleep(duracionCiclo);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("CPU " + id + " se ha detenido.");
    }

    // Interrumpe el proceso actual (por ejemplo, para Round Robin o SRT)
    public void interrumpirProceso() {
        synchronized (lock) {
            if (procesoActual != null) {
                System.out.println("CPU " + id + " interrumpiendo proceso: " + procesoActual.getNombre());
                procesoActual.setEstado(PCB.Estado.READY);
                procesoActual = null;
                semaforoCPU.release();
                lock.notify();
            }
        }
    }

    // Detiene la CPU (para disminuir el número de CPUs)
    public void detener() {
        ejecutando = false;
        synchronized (lock) {
            lock.notifyAll();
        }
        this.interrupt();
    }

    // Verifica si la CPU está ocupada
    public boolean estaOcupada() {
        return procesoActual != null;
    }

    public Proceso getProcesoActual() {
        return procesoActual;
    }

    public int getIdCPU() {
        return id;
    }
    
    public void setPlanificador(Planificador p) {
    this.planificador = p;
}

}
