/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Config;

/**
 *
 * @author arianneperret-gentil
 */
public class Configuracion {
    
    // Parámetros principales del simulador
    private int duracionCiclo;             // en ms o segundos
    private int numProcesadores;           // entre 2 y 3
    private int instruccionesPorProceso;   // longitud por proceso
    private boolean cpuBound;             // true = CPU-bound, false = I/O-bound
    private int ciclosParaExcepcion;      // cada cuántos ciclos se genera la excepción
    private int ciclosAtencionExcepcion;  // cuántos ciclos tarda en resolverse

    // Constructor por defecto
    public Configuracion() {
        this.duracionCiclo = 1000; // 1 seg
        this.numProcesadores = 2;
        this.instruccionesPorProceso = 10;
        this.cpuBound = true;
        this.ciclosParaExcepcion = 3;
        this.ciclosAtencionExcepcion = 2;
    }

    // Getters y Setters
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
        this.numProcesadores = numProcesadores;
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