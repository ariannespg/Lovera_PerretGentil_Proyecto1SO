/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package proyectoso_lovera_perretgentil;

import Config.Configuracion;
import Interfaces.InterfazSimulador;
import Modelo.Planificador;
import Modelo.Planificador.Algoritmo;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Crear la configuración
        Configuracion configuracion = new Configuracion();
        int numCPUs = configuracion.getNumProcesadores(); // Valor inicial (2)
        
        // Crear el planificador usando la configuración
        Planificador planificador = new Planificador(numCPUs, Algoritmo.FCFS, configuracion.getDuracionCiclo());

        SwingUtilities.invokeLater(() -> {
            InterfazSimulador ventana = new InterfazSimulador(planificador, configuracion);
            ventana.setVisible(true);
        });

        new Thread(planificador::planificar).start();
    }
}
