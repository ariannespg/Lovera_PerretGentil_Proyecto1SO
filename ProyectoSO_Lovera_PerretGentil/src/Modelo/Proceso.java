/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author adrianlovera
 */
public class Proceso {
    private static int contadorProcesos = 1; // ID autoincremental
    private int id;
    private String nombre;
    private PCB pcb;
    private boolean esCpuBound;
    private int cantidadInstrucciones;
    private int ciclosParaExcepcion;
    private int ciclosAtencionExcepcion;
    private int ciclosRestantesBloqueado;
    private int ciclosEjecutadosDesdeUltimoBloqueo;
    private int ciclosEjecutados;
    private int cpuIdThread;
    private int cicloEnqueCola;
    private int instruccionesEjecutadas;
    private boolean tomado; // Indica si el proceso fue tomado por un CPU

    // Constructor
    public Proceso(String nombre, int cantidadInstrucciones, boolean esCpuBound, Integer ciclosParaExcepcion, Integer ciclosAtencionExcepcion) {
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo o vacío.");
        }
        if (cantidadInstrucciones <= 0) {
            throw new IllegalArgumentException("La cantidad de instrucciones debe ser mayor que 0.");
        }

        this.id = contadorProcesos++;
        this.nombre = nombre;
        this.cantidadInstrucciones = cantidadInstrucciones;
        this.esCpuBound = esCpuBound;
        this.pcb = new PCB(id, nombre);
        this.cpuIdThread = 0;
        this.cicloEnqueCola = -1;
        this.instruccionesEjecutadas = 0;
        this.tomado = false;

        if (!esCpuBound) {
            if (ciclosParaExcepcion == null || ciclosAtencionExcepcion == null) {
                throw new IllegalArgumentException("Ciclos de excepción requeridos para I/O-bound.");
            }
            this.ciclosParaExcepcion = ciclosParaExcepcion;
            this.ciclosAtencionExcepcion = ciclosAtencionExcepcion;
        } else {
            this.ciclosParaExcepcion = 0;
            this.ciclosAtencionExcepcion = 0;
        }

        this.ciclosRestantesBloqueado = 0;
        this.ciclosEjecutadosDesdeUltimoBloqueo = 0;
        this.ciclosEjecutados = 0;
    }

    // Método que simula la ejecución del proceso
    public void ejecutarCiclo() {
        if (pcb.getEstado() == PCB.Estado.RUNNING) {
            pcb.incrementarPC();
            ciclosEjecutados++;
            instruccionesEjecutadas++;
            ciclosEjecutadosDesdeUltimoBloqueo++;

            if (!esCpuBound && ciclosEjecutadosDesdeUltimoBloqueo >= ciclosParaExcepcion) {
                setEstado(PCB.Estado.BLOCKED);
                ciclosRestantesBloqueado = ciclosAtencionExcepcion;
                System.out.println("Proceso " + nombre + " bloqueado por I/O.");
            }
        }
    }

    // Método para reanudar el proceso tras una excepción
    public void resolverExcepcion() {
        try {
            Thread.sleep(ciclosAtencionExcepcion * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ciclosRestantesBloqueado = 0;
        ciclosEjecutadosDesdeUltimoBloqueo = 0;
        setEstado(PCB.Estado.READY);
        System.out.println("Proceso " + nombre + " reanudado tras I/O.");
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidadInstrucciones() {
        return cantidadInstrucciones;
    }

    public void setCantidadInstrucciones(int cantidadInstrucciones) {
        this.cantidadInstrucciones = cantidadInstrucciones;
    }

    public PCB getPcb() {
        return pcb;
    }

    public boolean isCpuBound() {
        return esCpuBound;
    }

    public int getCiclosEjecutados() {
        return ciclosEjecutados;
    }

    public int getCiclosRestantesBloqueado() {
        return ciclosRestantesBloqueado;
    }

    public void setCiclosRestantesBloqueado(int ciclosRestantesBloqueado) {
        this.ciclosRestantesBloqueado = ciclosRestantesBloqueado;
    }

    public int getCiclosEjecutadosDesdeUltimoBloqueo() {
        return ciclosEjecutadosDesdeUltimoBloqueo;
    }

    public void incrementarCiclosEjecutados() {
        this.ciclosEjecutadosDesdeUltimoBloqueo++;
    }

    public int getCiclosParaExcepcion() {
        return ciclosParaExcepcion;
    }

    public int getCiclosParaSatisfacerExcepcion() {
        return ciclosAtencionExcepcion;
    }

    public int getPC() {
        return pcb.getProgramCounter();
    }

    public void setPC(int PC) {
        this.pcb.setProgramCounter(PC);
    }

    public int getMAR() {
        return pcb.getMar();
    }

    public void setMAR(int MAR) {
        this.pcb.setMar(MAR);
    }

    public int getcpuIdThread() {
        return cpuIdThread;
    }

    public void setcpuIdThread(int cpuIdThread) {
        this.cpuIdThread = cpuIdThread;
    }

    public boolean isTomado() {
        return tomado;
    }

    public void setTomado(boolean tomado) {
        this.tomado = tomado;
    }

    // 📌 **RESTAURADO: getEstado() y setEstado()**
    public PCB.Estado getEstado() {
        return pcb.getEstado();
    }

    public void setEstado(PCB.Estado estado) {
        this.pcb.setEstado(estado);
    }

    public int getTiempoRestante() {
        return cantidadInstrucciones - instruccionesEjecutadas;
    }

    @Override
    public String toString() {
        return "Proceso{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", cantidadInstrucciones=" + cantidadInstrucciones +
                ", tipo=" + (esCpuBound ? "CPU-bound" : "I/O-bound") +
                ", ciclosParaExcepcion=" + ciclosParaExcepcion +
                ", ciclosParaSatisfacerExcepcion=" + ciclosAtencionExcepcion +
                ", estado=" + getEstado() +
                ", PC=" + getPC() +
                ", MAR=" + getMAR() +
                ", instruccionesEjecutadas=" + instruccionesEjecutadas +
                ", tiempoRestante=" + getTiempoRestante() +
                '}';
    }
}