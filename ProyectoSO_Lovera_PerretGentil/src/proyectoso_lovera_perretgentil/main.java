/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package proyectoso_lovera_perretgentil;

import Config.Configuracion;
import Interfaces.InterfazSimulador;
import Modelo.Planificador;
import Modelo.Planificador.Algoritmo;
import Modelo.RelojGlobal;
import javax.swing.SwingUtilities;

public class main {
    public static void main(String[] args) {
        Configuracion configuracion = new Configuracion();
        int numCPUs = configuracion.getNumProcesadores();

        Planificador planificador = new Planificador(numCPUs, Planificador.Algoritmo.FCFS, configuracion.getDuracionCiclo());
        RelojGlobal reloj = new RelojGlobal(planificador, null); // Asegurar que el reloj tiene el planificador

        SwingUtilities.invokeLater(() -> {
            InterfazSimulador ventana = new InterfazSimulador(planificador, configuracion);
            reloj.iniciarReloj(); // 📌 Iniciar el reloj global
            ventana.setVisible(true);
        });

        reloj.start(); // 📌 Iniciar el hilo del reloj
}
}
