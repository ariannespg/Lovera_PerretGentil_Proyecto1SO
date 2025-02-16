/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interfaces;

/**
 *
 * @author adrianlovera
 */
import Modelo.Planificador;
import Modelo.Proceso;
import Modelo.Planificador.Algoritmo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Interfaz gráfica del simulador de planificación.
 * Muestra la cola de listos, procesos en ejecución y permite cambiar la planificación.
 * 
 * @author arianneperret-gentil
 */
import Modelo.Planificador;
import Modelo.Planificador.Algoritmo;

import javax.swing.*;
import java.awt.*;

/**
 * Interfaz gráfica del simulador de planificación.
 * Se ha modificado para utilizar la ListaEnlazada.
 * 
 * @author arianneperret
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
        setSize(600, 400);
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

    // Método para agregar un proceso aleatorio
    private void agregarProceso() {
        String nombre = "P" + contadorProcesos++; // Genera un nombre único
        boolean esCpuBound = Math.random() > 0.5;
        int ciclosParaExcepcion = esCpuBound ? 0 : (int) (Math.random() * 5) + 2;
        int ciclosAtencionExcepcion = esCpuBound ? 0 : (int) (Math.random() * 3) + 1;

        Proceso proceso = new Proceso(nombre, esCpuBound, ciclosParaExcepcion, ciclosAtencionExcepcion);
        planificador.agregarProceso(proceso);

        // Actualizar la GUI correctamente
        modeloListaProcesos.addElement(proceso.getNombre() + " - " + proceso.getEstado());
        listaProcesos.setModel(modeloListaProcesos);
    }

    // Método de actualización continua de la interfaz
    private void actualizarInterfaz() {
        while (true) {
            modeloListaProcesos.clear();
            for (String proceso : planificador.getListaProcesos()) {
                modeloListaProcesos.addElement(proceso);
            }
            listaProcesos.setModel(modeloListaProcesos);

            for (int i = 0; i < etiquetasCPU.length; i++) {
                etiquetasCPU[i].setText("CPU " + (i + 1) + ": " + planificador.getEstadoCPU(i));
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}