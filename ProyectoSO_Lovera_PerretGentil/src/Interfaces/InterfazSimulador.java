package Interfaces;

import Config.Configuracion;
import Modelo.Planificador;
import Modelo.Planificador.Algoritmo;
import Modelo.Proceso;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InterfazSimulador extends JFrame {
    private final Planificador planificador;
    private Configuracion configuracion;
    private final JTable tablaProcesosListos;
    private final JTable tablaProcesosBloqueados;
    private final DefaultTableModel modeloTablaListos;
    private final DefaultTableModel modeloTablaBloqueados;
    private JLabel[] etiquetasCPU;
    private JComboBox<String> selectorAlgoritmo;
    private JComboBox<Integer> selectorNumCPUs;
    private final JButton btnAgregarProceso;
    private final JButton btnAplicarConfig;
    private static int contadorProcesos = 1;

    public InterfazSimulador(Planificador planificador, Configuracion configuracion) {
        this.planificador = planificador;
        this.configuracion = configuracion;

        setTitle("Simulador de Planificación");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel de controles
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

        // Selector de número de CPUs (2 o 3)
        selectorNumCPUs = new JComboBox<>(new Integer[]{2, 3});
        selectorNumCPUs.setSelectedItem(configuracion.getNumProcesadores());

        btnAplicarConfig = new JButton("Aplicar Configuración");
        btnAplicarConfig.addActionListener(e -> {
            int numCPUs = (Integer) selectorNumCPUs.getSelectedItem();
            configuracion.setNumProcesadores(numCPUs);
            planificador.configurarCPUs(numCPUs); // Actualiza las CPUs en el planificador
            actualizarPanelCPUs();
            // Aquí también puedes llamar a un método para guardar la configuración en un archivo (TXT, CSV o JSON)
        });

        btnAgregarProceso = new JButton("Agregar Proceso");
        btnAgregarProceso.addActionListener(e -> agregarProceso());

        panelControles.add(new JLabel("Algoritmo:"));
        panelControles.add(selectorAlgoritmo);
        panelControles.add(new JLabel("CPUs:"));
        panelControles.add(selectorNumCPUs);
        panelControles.add(btnAplicarConfig);
        panelControles.add(btnAgregarProceso);

        // Panel de tablas para visualizar procesos
        JPanel panelTablas = new JPanel(new GridLayout(2, 1));
        modeloTablaListos = new DefaultTableModel(new Object[]{"ID", "Nombre", "Estado", "PC", "MAR"}, 0);
        tablaProcesosListos = new JTable(modeloTablaListos);
        panelTablas.add(new JScrollPane(tablaProcesosListos));

        modeloTablaBloqueados = new DefaultTableModel(new Object[]{"ID", "Nombre", "Ciclos Restantes"}, 0);
        tablaProcesosBloqueados = new JTable(modeloTablaBloqueados);
        panelTablas.add(new JScrollPane(tablaProcesosBloqueados));

        actualizarPanelCPUs();

        add(panelControles, BorderLayout.NORTH);
        add(panelTablas, BorderLayout.CENTER);

        new Thread(this::actualizarInterfaz).start();
    }

    // Actualiza el panel inferior que muestra el estado de cada CPU
    private void actualizarPanelCPUs() {
        if (etiquetasCPU != null && etiquetasCPU.length > 0) {
            remove(etiquetasCPU[0].getParent());
        }
        JPanel panelCPUs = new JPanel(new GridLayout(configuracion.getNumProcesadores(), 1));
        etiquetasCPU = new JLabel[configuracion.getNumProcesadores()];
        for (int i = 0; i < etiquetasCPU.length; i++) {
            etiquetasCPU[i] = new JLabel("CPU " + (i + 1) + ": [IDLE]");
            panelCPUs.add(etiquetasCPU[i]);
        }
        add(panelCPUs, BorderLayout.SOUTH);
        revalidate();
        repaint();
    }

    // Método para crear un proceso mediante un formulario de entrada
    private void agregarProceso() {
        JPanel panel = new JPanel(new GridLayout(0, 2));

        JTextField nombreField = new JTextField();
        JTextField instruccionesField = new JTextField();
        JCheckBox cpuBoundCheck = new JCheckBox("CPU Bound (marcar si es CPU bound)");
        JTextField ciclosExcepcionField = new JTextField();
        JTextField ciclosAtencionField = new JTextField();

        panel.add(new JLabel("Nombre del proceso:"));
        panel.add(nombreField);
        panel.add(new JLabel("Cantidad de instrucciones:"));
        panel.add(instruccionesField);
        panel.add(new JLabel("Tipo de proceso:"));
        panel.add(cpuBoundCheck);
        panel.add(new JLabel("Ciclos para excepción (solo I/O bound):"));
        panel.add(ciclosExcepcionField);
        panel.add(new JLabel("Ciclos para atender excepción (solo I/O bound):"));
        panel.add(ciclosAtencionField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Crear Proceso", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = nombreField.getText().trim();
                int instrucciones = Integer.parseInt(instruccionesField.getText().trim());
                boolean esCpuBound = cpuBoundCheck.isSelected();
                int ciclosExcepcion = 0;
                int ciclosAtencion = 0;
                if (!esCpuBound) {
                    ciclosExcepcion = Integer.parseInt(ciclosExcepcionField.getText().trim());
                    ciclosAtencion = Integer.parseInt(ciclosAtencionField.getText().trim());
                }
                Proceso proceso = new Proceso(nombre, instrucciones, esCpuBound, ciclosExcepcion, ciclosAtencion);
                planificador.agregarProceso(proceso);
                System.out.println("📌 Se ha agregado: " + proceso);
                actualizarListaProcesos();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, ingresa valores numéricos válidos para instrucciones y ciclos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actualizarInterfaz() {
        while (true) {
            actualizarListaProcesos();
            actualizarListaBloqueados();
            for (int i = 0; i < etiquetasCPU.length; i++) {
                etiquetasCPU[i].setText("CPU " + (i + 1) + ": " + planificador.getEstadoCPU(i));
            }
            try {
                Thread.sleep(1000); // Actualiza cada segundo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void actualizarListaProcesos() {
        modeloTablaListos.setRowCount(0);
        for (Proceso proceso : planificador.getListaProcesos()) {
            modeloTablaListos.addRow(new Object[]{
                proceso.getId(), proceso.getNombre(), proceso.getEstado(), proceso.getPC(), proceso.getMAR()
            });
        }
    }

    private void actualizarListaBloqueados() {
        modeloTablaBloqueados.setRowCount(0);
        for (Proceso proceso : planificador.getListaProcesosBloqueados()) {
            modeloTablaBloqueados.addRow(new Object[]{
                proceso.getId(), proceso.getNombre(), proceso.getCiclosRestantesBloqueado()
            });
        }
    }
}
