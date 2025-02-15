/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package proyectoso_lovera_perretgentil;

import Interfaces.InterfazSimulador;
import Modelo.CPU;
import Modelo.Planificador;
import Modelo.Planificador.Algoritmo;
import Modelo.Proceso;
import javax.swing.SwingUtilities;

/**
 *
 * @author arianneperret-gentil
 */
public class Main {
    public static void main(String[] args) {
        Planificador planificador = new Planificador(2, Algoritmo.FCFS, 2000);

        SwingUtilities.invokeLater(() -> {
            InterfazSimulador ventana = new InterfazSimulador(planificador);
            ventana.setVisible(true);
        });

        new Thread(planificador::planificar).start();
    }
}
