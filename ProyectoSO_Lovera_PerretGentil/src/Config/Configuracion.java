/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Config;

/**
 *
 * @author arianneperret-gentil
/**
 * Clase que define los parámetros de configuración 
 * para la simulación.
 */
public class Configuracion {
    
    private int duracionCiclo;
    private int numProcesadores; // Entre 2 y 3
    private int instruccionesPorProceso;
    private boolean cpuBound;
    private int ciclosParaExcepcion;
    private int ciclosAtencionExcepcion;

    public Configuracion() {
        this.duracionCiclo = 1000; // 1 seg
        this.numProcesadores = 2;  // Valor por defecto
        this.instruccionesPorProceso = 10;
        this.cpuBound = true;
        this.ciclosParaExcepcion = 3;
        this.ciclosAtencionExcepcion = 2;
    }

    public int getDuracionCiclo() {
        return duracionCiclo;
    }

    public void setDuracionCiclo(int duracionCiclo) {
        this.duracionCiclo = duracionCiclo;
    }

    public int getNumProcesadores() {
        return numProcesadores;
    }

    public void setNumProcesadores(int numProcesadores) {
        if (numProcesadores >= 2 && numProcesadores <= 3) {
            this.numProcesadores = numProcesadores;
        } else {
            throw new IllegalArgumentException("Número de CPUs debe ser 2 o 3.");
        }
    }

    public int getInstruccionesPorProceso() {
        return instruccionesPorProceso;
    }

    public void setInstruccionesPorProceso(int instruccionesPorProceso) {
        this.instruccionesPorProceso = instruccionesPorProceso;
    }

    public boolean isCpuBound() {
        return cpuBound;
    }

    public void setCpuBound(boolean cpuBound) {
        this.cpuBound = cpuBound;
    }

    public int getCiclosParaExcepcion() {
        return ciclosParaExcepcion;
    }

    public void setCiclosParaExcepcion(int ciclosParaExcepcion) {
        this.ciclosParaExcepcion = ciclosParaExcepcion;
    }

    public int getCiclosAtencionExcepcion() {
        return ciclosAtencionExcepcion;
    }

    public void setCiclosAtencionExcepcion(int ciclosAtencionExcepcion) {
        this.ciclosAtencionExcepcion = ciclosAtencionExcepcion;
    }
}
