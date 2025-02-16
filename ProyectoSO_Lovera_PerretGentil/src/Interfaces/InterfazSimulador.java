package Interfaces;

import Modelo.Planificador;
import Modelo.Proceso;
import Modelo.Planificador.Algoritmo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Interfaz gráfica optimizada del simulador de planificación.
 * Ahora incluye una tabla para la cola de listos y otra para procesos bloqueados.
 * 
 * @author adrianlovera & arianneperret
 */
public class InterfazSimulador extends JFrame {
    private Planificador planificador;
    private JTable tablaProcesosListos;
    private JTable tablaProcesosBloqueados;
    private DefaultTableModel modeloTablaListos;
    private DefaultTableModel modeloTablaBloqueados;
    private JLabel[] etiquetasCPU;
    private JComboBox<String> selectorAlgoritmo;
    private JButton btnAgregarProceso;
    private static int contadorProcesos = 1; // Contador estático para nombres únicos

    public InterfazSimulador(Planificador planificador) {
        this.planificador = planificador;
        setTitle("Simulador de Planificación");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior: Controles
        JPanel panelControles = new JPanel();
        selectorAlgoritmo = new JComboBox<>(new String[]{"FCFS", "SJF", "Round Robin"});
        selectorAlgoritmo.addActionListener(e -> {
            String seleccionado = (String) selectorAlgoritmo.getSelectedItem();
            switch (seleccionado) {
                case "FCFS": planificador.setAlgoritmo(Algoritmo.FCFS); break;
                case "SJF": planificador.setAlgoritmo(Algoritmo.SJF); break;
                case "Round Robin": planificador.setAlgoritmo(Algoritmo.ROUND_ROBIN); break;
            }
        });

        btnAgregarProceso = new JButton("Agregar Proceso");
        btnAgregarProceso.addActionListener(e -> agregarProceso());

        panelControles.add(new JLabel("Algoritmo:"));
        panelControles.add(selectorAlgoritmo);
        panelControles.add(btnAgregarProceso);

        // Panel central: Tablas de procesos
        JPanel panelTablas = new JPanel(new GridLayout(2, 1));

        // Tabla de procesos listos
        modeloTablaListos = new DefaultTableModel(new Object[]{"ID", "Nombre", "Estado", "PC", "MAR"}, 0);
        tablaProcesosListos = new JTable(modeloTablaListos);
        panelTablas.add(new JScrollPane(tablaProcesosListos));

        // Tabla de procesos bloqueados
        modeloTablaBloqueados = new DefaultTableModel(new Object[]{"ID", "Nombre", "Ciclos Restantes"}, 0);
        tablaProcesosBloqueados = new JTable(modeloTablaBloqueados);
        panelTablas.add(new JScrollPane(tablaProcesosBloqueados));

        // Panel inferior: Estado de CPUs
        JPanel panelCPUs = new JPanel(new GridLayout(planificador.getNumCPUs(), 1));
        etiquetasCPU = new JLabel[planificador.getNumCPUs()];
        for (int i = 0; i < planificador.getNumCPUs(); i++) {
            etiquetasCPU[i] = new JLabel("CPU " + (i + 1) + ": [IDLE]");
            panelCPUs.add(etiquetasCPU[i]);
        }

        // Agregar elementos a la GUI
        add(panelControles, BorderLayout.NORTH);
        add(panelTablas, BorderLayout.CENTER);
        add(panelCPUs, BorderLayout.SOUTH);

        // Hilo de actualización de la interfaz
        new Thread(this::actualizarInterfaz).start();
    }

    // Método para agregar un proceso con valores aleatorios
    private void agregarProceso() {
    String nombre = "P" + contadorProcesos++;
    boolean esCpuBound = Math.random() > 0.5;
    int cantidadInstrucciones = (int) (Math.random() * 20) + 5;
    int ciclosParaExcepcion = esCpuBound ? 0 : (int) (Math.random() * 5) + 2;
    int ciclosAtencionExcepcion = esCpuBound ? 0 : (int) (Math.random() * 3) + 1;

    Proceso proceso = new Proceso(nombre, cantidadInstrucciones, esCpuBound, ciclosParaExcepcion, ciclosAtencionExcepcion);
    planificador.agregarProceso(proceso);

    System.out.println("📌 Se ha agregado: " + proceso);
    actualizarListaProcesos();
}

    // Método de actualización continua de la interfaz
    private void actualizarInterfaz() {
        while (true) {
            actualizarListaProcesos();
            actualizarListaBloqueados();

            // Actualizar el estado de las CPUs
            for (int i = 0; i < etiquetasCPU.length; i++) {
                String estadoCPU = planificador.getEstadoCPU(i);
                etiquetasCPU[i].setText("CPU " + (i + 1) + ": " + estadoCPU);
            }

            try {
                Thread.sleep(1000); // Actualizar cada segundo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para actualizar la tabla de procesos listos
    private void actualizarListaProcesos() {
    modeloTablaListos.setRowCount(0);
    Proceso[] procesos = planificador.getListaProcesos();

    System.out.println("📌 Procesos en la cola: " + procesos.length);

    for (Proceso proceso : procesos) {
        modeloTablaListos.addRow(new Object[]{
            proceso.getId(),
            proceso.getNombre(),
            proceso.getEstado(),
            proceso.getPC(),
            proceso.getMAR()
        });
    }
}

    // Método para actualizar la tabla de procesos bloqueados
    private void actualizarListaBloqueados() {
        modeloTablaBloqueados.setRowCount(0);
        for (Proceso proceso : planificador.getListaProcesosBloqueados()) { // Debemos crear este método en Planificador
            modeloTablaBloqueados.addRow(new Object[]{
                proceso.getId(),
                proceso.getNombre(),
                proceso.getCiclosRestantesBloqueado()
            });
        }
    }
}