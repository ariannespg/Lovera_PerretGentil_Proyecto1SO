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
    
    // Quantum para Round Robin y SRT
    private int quantum = 5; 

    public enum Algoritmo {
        FCFS,       // First-Come, First-Served
        SJF,        // Shortest Job First (no expulsivo)
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
        
        // Iniciar las CPUs e inyectar la referencia al planificador
        for (int i = 0; i < numCPUs; i++) {
            cpus[i] = new CPU(i + 1, duracionCiclo);
            cpus[i].setPlanificador(this);
            cpus[i].start();
        }
        
        // Hilo para manejar procesos bloqueados (I/O)
        new Thread(this::manejarProcesosBloqueados).start();
        
        // Hilo principal de planificación
        new Thread(this::planificar).start();
        
        // Hilo de interrupciones (Round Robin, SRT)
        iniciarHiloInterrupciones();
    }

    // ---------------------------------------------------------------------------------
    // Métodos de configuración
    // ---------------------------------------------------------------------------------

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

    public void setAlgoritmo(Algoritmo nuevoAlgoritmo) {
        this.algoritmo = nuevoAlgoritmo;
        System.out.println("Algoritmo cambiado a: " + nuevoAlgoritmo);
    }

    // ---------------------------------------------------------------------------------
    // Métodos para agregar procesos a colas
    // ---------------------------------------------------------------------------------

    public void agregarProceso(Proceso proceso) {
        try {
            semaforoAsignacion.acquire();
            proceso.setEstado(PCB.Estado.READY);
            colaListos.agregar(proceso);
            System.out.println("✅ Proceso " + proceso.getNombre() + " agregado a la cola de listos.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaforoAsignacion.release();
        }
    }

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

    // ---------------------------------------------------------------------------------
    // Métodos de consulta (para la interfaz)
    // ---------------------------------------------------------------------------------

    public Proceso[] getListaProcesos() {
        return colaListos.obtenerTodosProcesos();
    }

    public Proceso[] getListaProcesosBloqueados() {
        return colaBloqueados.obtenerTodosProcesos();
    }

    public String getEstadoCPU(int i) {
    if (i < 0 || i >= cpus.length) return "[ERROR]";
    if (!cpus[i].estaOcupada()) {
        // Si la CPU no está ocupada, se asume que ejecuta el SO
        return "[SO] Sistema Operativo en ejecución";
    } else {
        Proceso p = cpus[i].getProcesoActual();
        if (p != null) {
            if (p.isEsSistema()) {
                return "SO: " + p.getNombre();
            } else {
                return "Usuario: " + p.getNombre();
            }
        } else {
            return "[Desconocido]";
        }
    }
}


    public int getNumCPUs() {
        return cpus.length;
    }

    // ---------------------------------------------------------------------------------
    // Hilo para manejar los procesos bloqueados
    // ---------------------------------------------------------------------------------

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
                Thread.sleep(1000); // Verificación cada 1s
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // ---------------------------------------------------------------------------------
    // Hilo principal de planificación: se ejecuta en bucle
    // ---------------------------------------------------------------------------------

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
            // Pausa breve para no saturar la CPU
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // ---------------------------------------------------------------------------------
    // Políticas de planificación
    // ---------------------------------------------------------------------------------

    // FCFS: asigna el primer proceso de la cola de listos a cada CPU libre
    private void planificarFCFS() {
        for (CPU cpu : cpus) {
            if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
                Proceso p = colaListos.remover();
                cpu.asignarProceso(p);
            }
        }
    }

    // SJF: asigna el proceso con el menor tiempo restante a cada CPU libre
    private void planificarSJF() {
        // NOTA: En tu código original creabas un Thread. 
        // Aquí lo simplificamos para que sea la hebra planificar() la que haga todo.
        for (CPU cpu : cpus) {
            if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
                Proceso procesoMasCorto = colaListos.obtenerSJF();
                if (procesoMasCorto != null) {
                    cpu.asignarProceso(procesoMasCorto);
                }
            }
        }
    }

    // Round Robin: aquí solo asignamos procesos a CPUs libres;
    // la expulsión la hará el hilo de interrupciones (iniciarHiloInterrupciones()).
    private void planificarRoundRobin() {
        for (CPU cpu : cpus) {
            if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
                Proceso proceso = colaListos.remover();
                if (proceso != null) {
                    cpu.asignarProceso(proceso);
                }
            }
        }
    }

    // SRT: versión expulsiva de SJF. 
    private void planificarSRT() {
        // Asignar procesos a CPUs libres con la misma lógica SJF
        for (CPU cpu : cpus) {
            if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
                Proceso procesoMasCorto = colaListos.obtenerSJF();
                if (procesoMasCorto != null) {
                    cpu.asignarProceso(procesoMasCorto);
                }
            }
        }
        // Aquí podríamos verificar si hay que expulsar procesos,
        // pero lo hacemos en el hilo de interrupciones también.
    }

    // HRRN: asigna el proceso con mayor ratio (tiempoEspera + tiempoServicio)/tiempoServicio
    private void planificarHRRN() {
    for (CPU cpu : cpus) {
        if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
            Proceso p = obtenerHRRN();
            if (p != null) {
                cpu.asignarProceso(p);
                System.out.println("✅ CPU " + cpu.getIdCPU() + " ejecutando " + p.getNombre() + " (HRRN)");
            }
        }
    }
}

    // ---------------------------------------------------------------------------------
    // Métodos auxiliares de planificación
    // ---------------------------------------------------------------------------------

    private Proceso obtenerHRRN() {
    Proceso[] procesos = colaListos.obtenerTodosProcesos();
    if (procesos.length == 0) return null;

    long now = System.currentTimeMillis(); // Obtener el tiempo actual
    Proceso mejor = null;
    double mejorRatio = -1;

    for (Proceso p : procesos) {
        long tiempoEspera = now - p.getArrivalTime();  // Tiempo de espera

        int tiempoServicio = p.getInstrucciones() - p.getPC();
        if (tiempoServicio <= 0) tiempoServicio = 1;  // Evitar división por cero

        double ratio = (tiempoEspera + tiempoServicio) / (double) tiempoServicio;

        // Seleccionar el proceso con el mejor ratio
        if (ratio > mejorRatio) {
            mejorRatio = ratio;
            mejor = p;
        }
    }

    if (mejor != null) {
        colaListos.removerProceso(mejor);  // Eliminar de la cola
        System.out.println("📌 Proceso seleccionado por HRRN: " + mejor.getNombre() + " (Ratio: " + mejorRatio + ")");
    }

    return mejor;
}

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

    // ---------------------------------------------------------------------------------
    // Hilo de interrupciones (Round Robin y SRT)
    // ---------------------------------------------------------------------------------
    private void iniciarHiloInterrupciones() {
        new Thread(() -> {
            while (true) {
                try {
                    // Dormir el tiempo de quantum * duracionCiclo
                    Thread.sleep(quantum * duracionCiclo);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    semaforoAsignacion.acquire();

                    // ROUND ROBIN: se interrumpen todos los procesos en CPU
                    if (algoritmo == Algoritmo.ROUND_ROBIN) {
                        for (CPU cpu : cpus) {
                            if (cpu.estaOcupada()) {
                                Proceso p = cpu.getProcesoActual();
                                cpu.interrumpirProceso();
                                // Si no ha terminado, regresa a la cola
                                if (p != null && p.getEstado() != PCB.Estado.FINISHED) {
                                    p.setEstado(PCB.Estado.READY);
                                    colaListos.agregar(p);
                                }
                            }
                        }
                    }

                    // SRT: si hay un proceso más corto en la cola, interrumpir el actual
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
                                            actual.setEstado(PCB.Estado.READY);
                                            colaListos.agregar(actual);
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

    // ---------------------------------------------------------------------------------
    // Método para actualizar el tiempo global (opcional)
    // ---------------------------------------------------------------------------------
    public void actualizarTiempoGlobal(int tiempo) {
        // Si deseas hacer alguna lógica adicional por cada tick, hazlo aquí
        // (Por ahora, no hacemos nada específico para Round Robin / SRT, 
        //  ya que lo maneja iniciarHiloInterrupciones()).
    }
    
    public Proceso getProcesoEnCPU(int cpuIndex) {
    if (cpuIndex < 0 || cpuIndex >= cpus.length) return null;
    return cpus[cpuIndex].getProcesoActual();
}
    
    
}