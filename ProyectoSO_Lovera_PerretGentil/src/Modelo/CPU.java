/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;
import java.util.concurrent.Semaphore;


/**
 *
 * @author arianneperret-gentil y Adrian!!!
 */
public class CPU extends Thread {
    private int id;
    private Proceso procesoActual;
    private boolean ejecutando;
    private int duracionCiclo;
    private final Object lock = new Object(); // Para sincronización
    private Semaphore semaforoCPU; // Controla acceso a la CPU

    public CPU(int id, int duracionCiclo) {
        this.id = id;
        this.procesoActual = null;
        this.ejecutando = true;
        this.duracionCiclo = duracionCiclo;
        this.semaforoCPU = new Semaphore(1); // Solo un proceso a la vez
    }

    // Asignar un proceso a la CPU de forma sincronizada
    public void asignarProceso(Proceso proceso) {
        try {
            semaforoCPU.acquire(); // Bloquea la CPU hasta que se libere
            synchronized (lock) {
                this.procesoActual = proceso;
                this.procesoActual.setEstado(PCB.Estado.RUNNING);
                lock.notify(); // Despierta la CPU si estaba esperando
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (ejecutando) {
            synchronized (lock) {
                while (procesoActual == null) {
                    try {
                        lock.wait(); // Espera hasta que se le asigne un proceso
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Ejecutar ciclo de proceso
            while (procesoActual != null && procesoActual.getEstado() == PCB.Estado.RUNNING) {
                procesoActual.ejecutarCiclo();
                System.out.println("CPU " + id + " ejecutando: " + procesoActual.getNombre());

                // Si el proceso se bloquea por I/O, la CPU lo libera
                if (procesoActual.getEstado() == PCB.Estado.BLOCKED) {
                    System.out.println("CPU " + id + " bloqueada por I/O en proceso " + procesoActual.getNombre());
                    procesoActual.resolverExcepcion();
                    procesoActual = null; // Libera la CPU
                    semaforoCPU.release();
                    break; // Sale del bucle para esperar otro proceso
                }

                // Si el proceso termina, la CPU lo libera
                if (procesoActual.getEstado() == PCB.Estado.FINISHED) {
                    System.out.println("CPU " + id + " terminó proceso: " + procesoActual.getNombre());
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
    }

    // Interrumpir un proceso (para Round Robin)
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

    // Verificar si la CPU está ocupada
    public boolean estaOcupada() {
        return procesoActual != null;
    }

    public Proceso getProcesoActual() {
        return procesoActual;
    }

    public int getIdCPU() {
        return id;
    }
}