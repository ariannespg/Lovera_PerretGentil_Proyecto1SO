/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;
import Interfaces.InterfazSimulador;
import java.util.concurrent.Semaphore;

/**
 *
 * @author adrianlovera
 */

public class RelojGlobal extends Thread {
    private static int tiempoActual = 0; // Ciclos globales del sistema
    private static int duracionCiclo = 1000; // Default en milisegundos
    private static Semaphore mutex = new Semaphore(1); // Controla acceso al tiempo
    private static Semaphore onPlay = new Semaphore(0); // Controla la ejecución del reloj
    private Planificador planificador;
    private InterfazSimulador interfaz;

    public RelojGlobal(Planificador planificador, InterfazSimulador interfaz) {
        this.planificador = planificador;
        this.interfaz = interfaz;
    }

    @Override
    public void run() {
        try {
            onPlay.acquire(); // Espera hasta que el reloj se inicie
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                Thread.sleep(duracionCiclo); // Espera el tiempo del ciclo
                mutex.acquire();

                tiempoActual++; // 📌 Incrementar el tiempo global
                planificador.actualizarTiempoGlobal(tiempoActual);
                if (interfaz != null) {
                    interfaz.actualizarTiempoGlobal(tiempoActual);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mutex.release();
            }
        }
}

    public static int getTiempoActual() {
        return tiempoActual;
    }

    public static void setDuracionCiclo(int duracion) {
        duracionCiclo = duracion;
    }

    public static int getDuracionCiclo() {
        return duracionCiclo;
    }

    public void iniciarReloj() {
        onPlay.release(); // Inicia el reloj
    }
}