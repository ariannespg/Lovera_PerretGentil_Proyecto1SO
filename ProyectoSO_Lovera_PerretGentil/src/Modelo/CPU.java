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

    // Constructor
    public CPU(int id, int duracionCiclo) {
        this.id = id;
        this.procesoActual = null;
        this.ejecutando = true;
        this.duracionCiclo = duracionCiclo;
    }

    // Asignar un proceso a la CPU
    public synchronized void asignarProceso(Proceso proceso) {
        this.procesoActual = proceso;
        this.procesoActual.setEstado(PCB.Estado.RUNNING);
        notify(); // Despierta la CPU si estaba esperando
    }

    // Método principal de la CPU optimizada
    @Override
    public void run() {
        while (ejecutando) {
            synchronized (this) {
                while (procesoActual == null) {
                    try {
                        wait(); // Espera hasta que se le asigne un proceso
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Ejecutar ciclo de proceso
            procesoActual.ejecutarCiclo();
            System.out.println("CPU " + id + " ejecutando: " + procesoActual.getNombre());

            if (procesoActual.getEstado() == PCB.Estado.FINISHED) {
                System.out.println("CPU " + id + " terminó proceso: " + procesoActual.getNombre());
                procesoActual = null;
            }

            // Simulación de duración del ciclo
            try {
                Thread.sleep(duracionCiclo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Interrumpir un proceso (para Round Robin)
    public synchronized void interrumpirProceso() {
        if (procesoActual != null) {
            System.out.println("CPU " + id + " interrumpiendo proceso: " + procesoActual.getNombre());
            procesoActual.setEstado(PCB.Estado.READY);
            procesoActual = null;
            notify();
        }
    }

    // Verificar si la CPU está ocupada
    public synchronized boolean estaOcupada() {
        return procesoActual != null;
    }

    public synchronized Proceso getProcesoActual() {
        return procesoActual;
    }

    public int getIdCPU() {
        return id;
    }
}
