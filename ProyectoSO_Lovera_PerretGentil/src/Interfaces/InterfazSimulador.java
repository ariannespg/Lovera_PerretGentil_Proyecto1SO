package Interfaces;

import Modelo.Planificador;
import Modelo.Proceso;
import Modelo.Planificador.Algoritmo;

import javax.swing.*;
import java.awt.*;

/**
 * Interfaz gráfica del simulador de planificación.
 * Se ha mejorado para reflejar correctamente los procesos en ejecución y su estado.
 * 
 * @author adrianlovera & arianneperret
 */
public class InterfazSimulador extends JFrame {
    private Planificador planificador;
    private DefaultListModel<String> modeloListaProcesos;
    private JList<String> listaProcesos;
    private JLabel[] etiquetasCPU;
    private JComboBox<String> selectorAlgoritmo;
    private JButton btnAgregarProceso;
    private static int contadorProcesos = 1; // Contador estático para nombres únicos

    public InterfazSimulador(Planificador planificador) {
        this.planificador = planificador;
        setTitle("Simulador de Planificación");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridLayout(2, 1));

        // Panel superior: Cola de listos
        modeloListaProcesos = new DefaultListModel<>();
        listaProcesos = new JList<>(modeloListaProcesos);
        JScrollPane scrollProcesos = new JScrollPane(listaProcesos);
        panelPrincipal.add(new JLabel("Cola de Listos:"));
        panelPrincipal.add(scrollProcesos);

        // Panel inferior: CPUs
        JPanel panelCPUs = new JPanel(new GridLayout(planificador.getNumCPUs(), 1));
        etiquetasCPU = new JLabel[planificador.getNumCPUs()];
        for (int i = 0; i < planificador.getNumCPUs(); i++) {
            etiquetasCPU[i] = new JLabel("CPU " + (i + 1) + ": [IDLE]");
            panelCPUs.add(etiquetasCPU[i]);
        }

        // Selector de algoritmo de planificación
        selectorAlgoritmo = new JComboBox<>(new String[]{"FCFS", "SJF", "Round Robin"});
        selectorAlgoritmo.addActionListener(e -> {
            String seleccionado = (String) selectorAlgoritmo.getSelectedItem();
            switch (seleccionado) {
                case "FCFS": planificador.setAlgoritmo(Algoritmo.FCFS); break;
                case "SJF": planificador.setAlgoritmo(Algoritmo.SJF); break;
                case "Round Robin": planificador.setAlgoritmo(Algoritmo.ROUND_ROBIN); break;
            }
        });

        // Botón para agregar procesos
        btnAgregarProceso = new JButton("Agregar Proceso");
        btnAgregarProceso.addActionListener(e -> {
            agregarProceso();
            System.out.println("Proceso agregado correctamente.");
        });

        // Panel de controles
        JPanel panelControles = new JPanel();
        panelControles.add(new JLabel("Algoritmo:"));
        panelControles.add(selectorAlgoritmo);
        panelControles.add(btnAgregarProceso);

        // Agregar elementos a la GUI
        add(panelControles, BorderLayout.NORTH);
        add(panelPrincipal, BorderLayout.CENTER);
        add(panelCPUs, BorderLayout.SOUTH);

        // Hilo de actualización de la interfaz
        new Thread(this::actualizarInterfaz).start();
    }

    // Método para agregar un proceso con valores aleatorios
    private void agregarProceso() {
        String nombre = "P" + contadorProcesos++; // Genera un nombre único
        boolean esCpuBound = Math.random() > 0.5;
        int cantidadInstrucciones = (int) (Math.random() * 20) + 5; // Entre 5 y 25 instrucciones
        int ciclosParaExcepcion = esCpuBound ? 0 : (int) (Math.random() * 5) + 2;
        int ciclosAtencionExcepcion = esCpuBound ? 0 : (int) (Math.random() * 3) + 1;

        Proceso proceso = new Proceso(nombre, cantidadInstrucciones, esCpuBound, ciclosParaExcepcion, ciclosAtencionExcepcion);
        planificador.agregarProceso(proceso);

        // Actualizar la GUI correctamente
        actualizarListaProcesos();
    }

    // Método de actualización continua de la interfaz
    private void actualizarInterfaz() {
        while (true) {
            actualizarListaProcesos();

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

    // Método para actualizar la lista de procesos en la interfaz
    private void actualizarListaProcesos() {
    modeloListaProcesos.clear();
    for (Proceso proceso : planificador.getListaProcesos()) { // ✅ Obtener lista de procesos correctamente
        modeloListaProcesos.addElement(proceso.toString());  // ✅ Convertir proceso en String antes de agregarlo
    }
    listaProcesos.setModel(modeloListaProcesos);
}
}