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
    
    // Quantum para Round Robin y SRT (según requerimiento, Q = 5 ciclos)
    private int quantum = 5; 

    public enum Algoritmo {
        FCFS,       // First-Come, First-Served
        SJF,        // Shortest Job First (SPN, no expulsivo)
        ROUND_ROBIN, 
        SRT,        // Shortest Remaining Time (SJF expulsivo)
        HRRN        // Highest Response Ratio Next
    }

    public Planificador(int numCPUs, Algoritmo algoritmo, int duracionCiclo) {
        this.colaListos = new ListaEnlazada();
        this.colaBloqueados = new ListaEnlazada();
        this.cpus = new CPU[numCPUs];
        this.semaforoAsignacion = new Semaphore(1, true);
        this.algoritmo = algoritmo;
        this.duracionCiclo = duracionCiclo;
        
        // Iniciar las CPUs e inyectar la referencia al planificador para manejar bloqueados
        for (int i = 0; i < numCPUs; i++) {
            cpus[i] = new CPU(i + 1, duracionCiclo);
            cpus[i].setPlanificador(this);  // IMPORTANTE: para que la CPU pueda agregar a bloqueados
            cpus[i].start();
        }
        
        // Iniciar el manejo de procesos bloqueados (I/O)
        new Thread(this::manejarProcesosBloqueados).start();
        
        // Iniciar el hilo principal de planificación
        new Thread(this::planificar).start();
        
        // Iniciar el hilo de interrupciones para Round Robin y SRT
        iniciarHiloInterrupciones();
    }

    // Permite cambiar el número de CPUs dinámicamente
    public void configurarCPUs(int numCPUs) {
        try {
            semaforoAsignacion.acquire();
            if (numCPUs > cpus.length) {
                // Aumentar CPUs
                CPU[] newCPUs = new CPU[numCPUs];
                for (int i = 0; i < cpus.length; i++) {
                    newCPUs[i] = cpus[i];
                }
                for (int i = cpus.length; i < numCPUs; i++) {
                    newCPUs[i] = new CPU(i + 1, duracionCiclo);
                    newCPUs[i].setPlanificador(this);
                    newCPUs[i].start();
                }
                cpus = newCPUs;
                System.out.println("Se han configurado " + numCPUs + " CPUs (aumento).");
            } else if (numCPUs < cpus.length) {
                // Disminuir CPUs
                for (int i = numCPUs; i < cpus.length; i++) {
                    Proceso procesoEnCurso = cpus[i].getProcesoActual();
                    if (procesoEnCurso != null) {
                        procesoEnCurso.setEstado(PCB.Estado.READY);
                        colaListos.agregar(procesoEnCurso);
                        System.out.println("Proceso " + procesoEnCurso.getNombre() + " reubicado en la cola de listos.");
                    }
                    cpus[i].detener();
                }
                CPU[] newCPUs = new CPU[numCPUs];
                for (int i = 0; i < numCPUs; i++) {
                    newCPUs[i] = cpus[i];
                }
                cpus = newCPUs;
                System.out.println("Se han configurado " + numCPUs + " CPUs (disminución).");
            } else {
                System.out.println("Número de CPUs sin cambios.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaforoAsignacion.release();
        }
    }

    // Agrega un proceso a la cola de listos
    public void agregarProceso(Proceso proceso) {
    try {
        semaforoAsignacion.acquire();
        proceso.setEstado(PCB.Estado.READY);
        colaListos.agregar(proceso);

        // 📌 Verificar si el proceso realmente se agregó
        System.out.println("✅ Proceso " + proceso.getNombre() + " agregado a la cola de listos.");

    } catch (InterruptedException e) {
        e.printStackTrace();
    } finally {
        semaforoAsignacion.release();
    }
}
    
    // Método para agregar un proceso a la cola de bloqueados
    public void agregarBloqueado(Proceso proceso) {
        try {
            semaforoAsignacion.acquire();
            colaBloqueados.agregar(proceso);
            System.out.println("Proceso " + proceso.getNombre() + " agregado a la cola de bloqueados.");
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

    // Retorna el estado de la CPU (para la interfaz gráfica)
    public String getEstadoCPU(int i) {
        if (i < 0 || i >= cpus.length) return "[ERROR]";
        return cpus[i].estaOcupada()
                ? "Ejecutando: " + cpus[i].getProcesoActual().getNombre()
                : "[IDLE]";
    }

    public int getNumCPUs() {
        return cpus.length;
    }

    public void setAlgoritmo(Algoritmo nuevoAlgoritmo) {
        this.algoritmo = nuevoAlgoritmo;
        System.out.println("Algoritmo cambiado a: " + nuevoAlgoritmo);
    }
    
    // Hilo que maneja los procesos bloqueados (disminuye los ciclos de bloqueo y, al terminar, los retorna a READY)
    private void manejarProcesosBloqueados() {
    while (true) {
        try {
            semaforoAsignacion.acquire();
            Proceso[] procesos = colaBloqueados.obtenerTodosProcesos();
            
            for (Proceso proceso : procesos) {
                if (proceso.getCiclosRestantesBloqueado() > 0) {
                    proceso.setCiclosRestantesBloqueado(proceso.getCiclosRestantesBloqueado() - 1);
                }

                if (proceso.getCiclosRestantesBloqueado() == 0) {
                    proceso.setEstado(PCB.Estado.READY);
                    colaListos.agregar(proceso);
                    colaBloqueados.removerProceso(proceso);
                    System.out.println("🔵 Proceso " + proceso.getNombre() + " ha salido de bloqueado y pasó a READY.");
                }
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
    
    // Hilo principal de planificación: selecciona el proceso a asignar según la política elegida
    private void planificar() {
        while (true) {
            try {
                semaforoAsignacion.acquire();
                switch (algoritmo) {
                    case FCFS:
                        planificarFCFS();
                        break;
                    case SJF:
                        planificarSJF();
                        break;
                    case ROUND_ROBIN:
                        planificarRoundRobin();
                        break;
                    case SRT:
                        planificarSRT();
                        break;
                    case HRRN:
                        planificarHRRN();
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaforoAsignacion.release();
            }
            try {
                Thread.sleep(500); // Pausa breve para no saturar la CPU
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    // ----------------- Implementaciones de políticas de planificación -----------------

    // FCFS: asigna el primer proceso de la cola de listos a una CPU libre.
    private void planificarFCFS() {
        for (CPU cpu : cpus) {
            if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
                Proceso p = colaListos.remover();
                cpu.asignarProceso(p);
            }
        }
    }
    
    // SJF: asigna el proceso con el menor tiempo restante (instrucciones - PC).
    private void planificarSJF() {
        for (CPU cpu : cpus) {
            if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
                Proceso p = colaListos.obtenerSJF();
                if (p != null) {
                    cpu.asignarProceso(p);
                }
            }
        }
    }
    
    // Round Robin: similar a FCFS, pero se interrumpe cada 'quantum' ciclos (ver hilo de interrupciones).
    private void planificarRoundRobin() {
        for (CPU cpu : cpus) {
            if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
                Proceso p = colaListos.remover();
                cpu.asignarProceso(p);
            }
        }
    }
    
    // SRT: similar a SJF, pero con preempción. La expulsión se maneja en el hilo de interrupciones.
    private void planificarSRT() {
    // 📌 Asignar procesos a CPUs libres
    for (CPU cpu : cpus) {
        if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
            Proceso procesoMasCorto = colaListos.obtenerSJF();
            if (procesoMasCorto != null) {
                cpu.asignarProceso(procesoMasCorto);
                System.out.println("✅ CPU " + cpu.getIdCPU() + " ejecutando " + procesoMasCorto.getNombre());
            }
        }
    }

    // 📌 Verificar interrupciones y agregar procesos interrumpidos a colaListos
    for (CPU cpu : cpus) {
        if (cpu.estaOcupada()) {
            Proceso procesoMasCorto = colaListos.obtenerSJF();
            Proceso procesoActual = cpu.getProcesoActual();

            if (procesoMasCorto != null && procesoActual != null) {
                int restanteActual = procesoActual.getInstrucciones() - procesoActual.getPC();
                int restanteNuevo = procesoMasCorto.getInstrucciones() - procesoMasCorto.getPC();

                if (restanteNuevo < restanteActual) {
                    System.out.println("⚠️ SRT: Interrumpiendo " + procesoActual.getNombre() + " en CPU " + cpu.getIdCPU() + " por " + procesoMasCorto.getNombre());

                    cpu.interrumpirProceso();

                    // 📌 Asegurar que el proceso interrumpido regresa a la cola de listos
                    procesoActual.setEstado(PCB.Estado.READY);
                    colaListos.agregar(procesoActual);
                    System.out.println("✅ Proceso " + procesoActual.getNombre() + " agregado nuevamente a la cola de listos.");

                    // 📌 Asignar el nuevo proceso más corto a la CPU
                    cpu.asignarProceso(procesoMasCorto);
                }
            }
        }
    }

    // 📌 Reasignar procesos interrumpidos cuando un CPU queda libre
    for (CPU cpu : cpus) {
        if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
            Proceso siguienteProceso = colaListos.obtenerSJF();
            if (siguienteProceso != null) {
                cpu.asignarProceso(siguienteProceso);
                System.out.println("🔄 CPU " + cpu.getIdCPU() + " retomando ejecución de " + siguienteProceso.getNombre());
            }
        }
    }
}
    // 📌 Nuevo método para asignar procesos a CPUs libres después de una interrupción
    private void asignarProcesosLibres() {
        for (CPU cpu : cpus) {
            if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
                Proceso procesoMasCorto = colaListos.obtenerSJF();
                if (procesoMasCorto != null) {
                    cpu.asignarProceso(procesoMasCorto);
                    System.out.println("🔄 CPU " + cpu.getIdCPU() + " retomando " + procesoMasCorto.getNombre());
                }
            }
        }
    }
    
    // HRRN: asigna el proceso con mayor ratio de respuesta: (tiempoEspera + tiempoServicio) / tiempoServicio.
    private void planificarHRRN() {
        for (CPU cpu : cpus) {
            if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
                Proceso p = obtenerHRRN();
                if (p != null) {
                    cpu.asignarProceso(p);
                }
            }
        }
    }
    
    // Método auxiliar para HRRN: calcula el ratio y retorna el proceso con mayor ratio.
    private Proceso obtenerHRRN() {
        Proceso[] procesos = colaListos.obtenerTodosProcesos();
        if (procesos.length == 0) return null;
        
        long now = System.currentTimeMillis();
        Proceso mejor = null;
        double mejorRatio = -1;
        for (Proceso p : procesos) {
            int servicioRestante = p.getInstrucciones() - p.getPC();
            if (servicioRestante <= 0) servicioRestante = 1;
            long espera = now - p.getArrivalTime();
            double ratio = (espera + servicioRestante) / (double) servicioRestante;
            if (ratio > mejorRatio) {
                mejorRatio = ratio;
                mejor = p;
            }
        }
        if (mejor != null) {
            colaListos.removerProceso(mejor);
        }
        return mejor;
    }
    
    // Método auxiliar para SRT: encuentra el proceso con el menor tiempo restante en la cola.
    private Proceso findShortestJobInQueue() {
        Proceso[] procesos = colaListos.obtenerTodosProcesos();
        if (procesos.length == 0) return null;
        Proceso masCorto = procesos[0];
        int tiempoRestanteMasCorto = masCorto.getInstrucciones() - masCorto.getPC();
        for (Proceso p : procesos) {
            int restante = p.getInstrucciones() - p.getPC();
            if (restante < tiempoRestanteMasCorto) {
                masCorto = p;
                tiempoRestanteMasCorto = restante;
            }
        }
        return masCorto;
    }
    
    // Hilo que, cada cierto tiempo (quantum * duracionCiclo), interrumpe procesos en Round Robin y SRT.
    private void iniciarHiloInterrupciones() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(quantum * duracionCiclo);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    semaforoAsignacion.acquire();
                    
                    // Para Round Robin: interrumpir todos los procesos en CPU
                    if (algoritmo == Algoritmo.ROUND_ROBIN) {
                        for (CPU cpu : cpus) {
                            if (cpu.estaOcupada()) {
                                cpu.interrumpirProceso();
                            }
                        }
                    }
                    
                    // Para SRT: si existe un proceso en la cola con menor tiempo restante que el que se está ejecutando, se fuerza la preempción.
                    if (algoritmo == Algoritmo.SRT) {
                        Proceso masCorto = findShortestJobInQueue();
                        if (masCorto != null) {
                            for (CPU cpu : cpus) {
                                if (cpu.estaOcupada()) {
                                    Proceso actual = cpu.getProcesoActual();
                                    if (actual != null) {
                                        int actualRestante = actual.getInstrucciones() - actual.getPC();
                                        int colaRestante = masCorto.getInstrucciones() - masCorto.getPC();
                                        if (colaRestante < actualRestante) {
                                            cpu.interrumpirProceso();
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaforoAsignacion.release();
                }
            }
        }).start();
    }
    
    public void actualizarTiempoGlobal(int tiempo) {
    // Verificar si algún proceso en ejecución debe ser interrumpido (SRT, RR)
    if (algoritmo == Algoritmo.SRT || algoritmo == Algoritmo.ROUND_ROBIN) {
        verificarInterrupciones();
    }
}

// Método para verificar si hay procesos que deben ser interrumpidos
private void verificarInterrupciones() {
    for (CPU cpu : cpus) {
        if (cpu.estaOcupada()) {
            Proceso procesoActual = cpu.getProcesoActual();
            Proceso procesoMasCorto = colaListos.obtenerSJF();

            if (algoritmo == Algoritmo.SRT && procesoMasCorto != null) {
                int restanteActual = procesoActual.getInstrucciones() - procesoActual.getPC();
                int restanteNuevo = procesoMasCorto.getInstrucciones() - procesoMasCorto.getPC();

                if (restanteNuevo < restanteActual) {
                    System.out.println("⚠️ SRT: Interrumpiendo " + procesoActual.getNombre() + " por " + procesoMasCorto.getNombre());
                    cpu.interrumpirProceso();
                    colaListos.agregar(procesoActual);
                    cpu.asignarProceso(procesoMasCorto);
                }
            }
        }
    }
}
    
}

