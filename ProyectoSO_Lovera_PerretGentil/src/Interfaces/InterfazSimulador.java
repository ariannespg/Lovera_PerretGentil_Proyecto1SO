package Interfaces;

import Config.Configuracion;
import Modelo.Planificador;
import Modelo.Planificador.Algoritmo;
import Modelo.Proceso;
import Modelo.RelojGlobal;
import Modelo.PCB;
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
    private JLabel labelTiempoGlobal = new JLabel("Tiempo: 0");
    
    // Área de texto opcional para mostrar detalles del PCB
    private JTextArea areaPCB;

    public InterfazSimulador(Planificador planificador, Configuracion configuracion) {
        this.planificador = planificador;
        this.configuracion = configuracion;

        setTitle("Simulador de Planificación");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --------------------- PANEL SUPERIOR (Controles) ---------------------
        JPanel panelControles = new JPanel();

        // ComboBox con las 5 políticas de planificación
        selectorAlgoritmo = new JComboBox<>(new String[]{
            "FCFS", 
            "SJF", 
            "Round Robin",
            "SRT",
            "HRRN"
        });
        
        // Listener para cambio de algoritmo
        selectorAlgoritmo.addActionListener(e -> {
            String seleccionado = (String) selectorAlgoritmo.getSelectedItem();
            switch (seleccionado) {
                case "FCFS":
                    planificador.setAlgoritmo(Algoritmo.FCFS);
                    break;
                case "SJF":
                    planificador.setAlgoritmo(Algoritmo.SJF);
                    break;
                case "Round Robin":
                    planificador.setAlgoritmo(Algoritmo.ROUND_ROBIN);
                    break;
                case "SRT":
                    planificador.setAlgoritmo(Algoritmo.SRT);
                    break;
                case "HRRN":
                    planificador.setAlgoritmo(Algoritmo.HRRN);
                    break;
            }
        });

        // Selector de número de CPUs (2 o 3)
        selectorNumCPUs = new JComboBox<>(new Integer[]{2, 3});
        selectorNumCPUs.setSelectedItem(configuracion.getNumProcesadores());

        btnAplicarConfig = new JButton("Aplicar Configuración");
        btnAplicarConfig.addActionListener(e -> {
            int numCPUs = (Integer) selectorNumCPUs.getSelectedItem();
            configuracion.setNumProcesadores(numCPUs);
            planificador.configurarCPUs(numCPUs);
            actualizarPanelCPUs();
        });

        btnAgregarProceso = new JButton("Agregar Proceso");
        btnAgregarProceso.addActionListener(e -> agregarProceso());

        panelControles.add(new JLabel("Algoritmo:"));
        panelControles.add(selectorAlgoritmo);
        panelControles.add(new JLabel("CPUs:"));
        panelControles.add(selectorNumCPUs);
        panelControles.add(btnAplicarConfig);
        panelControles.add(btnAgregarProceso);
        panelControles.add(new JLabel("Tiempo Global:"));
        panelControles.add(labelTiempoGlobal);

        // Slider para modificar la duración del ciclo
        JSlider sliderDuracionCiclo = new JSlider(500, 5000, RelojGlobal.getDuracionCiclo());
        sliderDuracionCiclo.setMajorTickSpacing(500);
        sliderDuracionCiclo.setPaintTicks(true);
        sliderDuracionCiclo.setPaintLabels(true);
        sliderDuracionCiclo.addChangeListener(e -> {
            int nuevoValor = sliderDuracionCiclo.getValue();
            RelojGlobal.setDuracionCiclo(nuevoValor);
        });
        panelControles.add(new JLabel("Duración del Ciclo (ms):"));
        panelControles.add(sliderDuracionCiclo);

        // Agregamos el panel de controles en la parte superior
        add(panelControles, BorderLayout.NORTH);

        // --------------------- CREAR PANELES PARA TABLAS ---------------------
        // 1) Panel para la lista de procesos listos
        modeloTablaListos = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Estado", "PC", "MAR"}, 
                0
        );
        tablaProcesosListos = new JTable(modeloTablaListos);
        JPanel panelListos = new JPanel(new BorderLayout());
        panelListos.setBorder(BorderFactory.createTitledBorder("Lista de Procesos Listos"));
        panelListos.add(new JScrollPane(tablaProcesosListos), BorderLayout.CENTER);

        // 2) Panel para la lista de procesos bloqueados
        modeloTablaBloqueados = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Ciclos Restantes"}, 
                0
        );
        tablaProcesosBloqueados = new JTable(modeloTablaBloqueados);
        JPanel panelBloqueados = new JPanel(new BorderLayout());
        panelBloqueados.setBorder(BorderFactory.createTitledBorder("Lista de Procesos Bloqueados"));
        panelBloqueados.add(new JScrollPane(tablaProcesosBloqueados), BorderLayout.CENTER);

        // Los unimos en un panel vertical (GridLayout 2 filas, 1 columna)
        JPanel panelTablas = new JPanel(new GridLayout(2, 1));
        panelTablas.add(panelListos);
        panelTablas.add(panelBloqueados);

        // --------------------- PANEL PARA DETALLES PCB (opcional) ---------------------
        JPanel panelPCB = new JPanel(new BorderLayout());
        panelPCB.setBorder(BorderFactory.createTitledBorder("Detalles del PCB"));
        areaPCB = new JTextArea();
        areaPCB.setEditable(false);
        JScrollPane scrollPCB = new JScrollPane(areaPCB);
        panelPCB.add(scrollPCB, BorderLayout.CENTER);

        // --------------------- PANEL CENTRAL (Tablas a la izq, PCB a la der) ---------------------
        JPanel panelCentral = new JPanel(new GridLayout(1, 2));
        panelCentral.add(panelTablas);
        panelCentral.add(panelPCB);

        // Lo agregamos en el centro de la ventana
        add(panelCentral, BorderLayout.CENTER);

        // --------------------- PANEL INFERIOR (CPUs en ejecución) ---------------------
        actualizarPanelCPUs();

        // --------------------- Hilo de Actualización de Interfaz ---------------------
        new Thread(this::actualizarInterfaz).start();
    }

    private void actualizarPanelCPUs() {
        // Elimina el panel anterior si existe
        if (etiquetasCPU != null && etiquetasCPU.length > 0) {
            remove(etiquetasCPU[0].getParent());
        }
        // Panel con título para los CPUs
        JPanel panelCPUs = new JPanel(new GridLayout(configuracion.getNumProcesadores(), 1));
        panelCPUs.setBorder(BorderFactory.createTitledBorder("Procesos en Ejecución (CPUs)"));

        etiquetasCPU = new JLabel[configuracion.getNumProcesadores()];
        for (int i = 0; i < etiquetasCPU.length; i++) {
            etiquetasCPU[i] = new JLabel("CPU " + (i + 1) + ": [IDLE]");
            etiquetasCPU[i].setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            panelCPUs.add(etiquetasCPU[i]);
        }
        add(panelCPUs, BorderLayout.SOUTH);
        revalidate();
        repaint();
    }

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
        panel.add(new JLabel("Ciclos atención excepción (solo I/O bound):"));
        panel.add(ciclosAtencionField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Crear Proceso", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = nombreField.getText().trim();
                int instrucciones = Integer.parseInt(instruccionesField.getText().trim());
                boolean esCpuBound = cpuBoundCheck.isSelected();
                int ciclosExcepcion = 0;
                int ciclosAtencion = 0;

                // Si el proceso es I/O-bound, debe recibir los valores correctos
                if (!esCpuBound) {
                    if (ciclosExcepcionField.getText().trim().isEmpty() ||
                        ciclosAtencionField.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(
                                this, 
                                "Los ciclos de excepción y atención son obligatorios para I/O bound.", 
                                "Error", 
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                    ciclosExcepcion = Integer.parseInt(ciclosExcepcionField.getText().trim());
                    ciclosAtencion = Integer.parseInt(ciclosAtencionField.getText().trim());
                }

                // Crear el proceso y enviarlo al planificador
                Proceso proceso = new Proceso(
                        nombre, 
                        instrucciones, 
                        esCpuBound, 
                        ciclosExcepcion, 
                        ciclosAtencion
                );
                planificador.agregarProceso(proceso);
                System.out.println("Se ha agregado: " + proceso);
                actualizarListaProcesos();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this, 
                        "Por favor, ingresa valores numéricos válidos.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void actualizarInterfaz() {
        while (true) {
            actualizarListaProcesos();
            actualizarListaBloqueados();

            // Actualizar el estado de las CPUs
            for (int i = 0; i < etiquetasCPU.length; i++) {
                etiquetasCPU[i].setText("CPU " + (i + 1) + ": " + planificador.getEstadoCPU(i));
            }

            // Actualizar el tiempo global en la interfaz
            actualizarTiempoGlobal(RelojGlobal.getTiempoActual());

            try {
                Thread.sleep(1000); // Actualizar cada segundo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void actualizarListaProcesos() {
        modeloTablaListos.setRowCount(0);
        for (Proceso proceso : planificador.getListaProcesos()) {
            modeloTablaListos.addRow(new Object[]{
                proceso.getId(), 
                proceso.getNombre(), 
                proceso.getEstado(), 
                proceso.getPC(), 
                proceso.getMAR()
            });
        }
        // Actualiza el área de texto con la información del PCB
        actualizarPCB();
    }

    private void actualizarListaBloqueados() {
        modeloTablaBloqueados.setRowCount(0);
        for (Proceso proceso : planificador.getListaProcesosBloqueados()) {
            modeloTablaBloqueados.addRow(new Object[]{
                proceso.getId(), 
                proceso.getNombre(), 
                proceso.getCiclosRestantesBloqueado()
            });
        }
    }
    
    // Método para actualizar el tiempo global en la interfaz
    public void actualizarTiempoGlobal(int tiempo) {
        labelTiempoGlobal.setText("Tiempo: " + tiempo);
    }
    
    // Método para mostrar la información de los PCB con un formato más agradable
    private void actualizarPCB() {
        // Se asume que cada Proceso tiene un método getPCB() que devuelve su objeto PCB
        StringBuilder sb = new StringBuilder();
        for (Proceso proceso : planificador.getListaProcesos()) {
            PCB pcb = proceso.getPcb(); // Ajusta si tu clase Proceso usa otro nombre de método
            if (pcb != null) {
                sb.append("ID: ").append(pcb.getProcessId()).append("\n")
                  .append("Nombre: ").append(pcb.getNombreProceso()).append("\n")
                  .append("Estado: ").append(pcb.getEstado()).append("\n")
                  .append("PC: ").append(pcb.getProgramCounter()).append("\n")
                  .append("MAR: ").append(pcb.getMar()).append("\n")
                  .append("------------------------------\n");
            }
        }
        areaPCB.setText(sb.toString());
    }
}

