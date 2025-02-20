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

public class main {
    public static void main(String[] args) {
        // Crear la configuración (puedes cargarla de un JSON, si lo deseas)
        Configuracion configuracion = new Configuracion();
        int numCPUs = configuracion.getNumProcesadores(); // 2 por defecto
        
        // Crear el planificador con FCFS por defecto (puedes cambiarlo)
        Planificador planificador = new Planificador(
                numCPUs,
                Algoritmo.FCFS,
                configuracion.getDuracionCiclo()
        );

        // Lanzar la interfaz gráfica
        SwingUtilities.invokeLater(() -> {
            InterfazSimulador ventana = new InterfazSimulador(planificador, configuracion);
            ventana.setVisible(true);
        });

        // El hilo planificador corre internamente en Planificador
    }
}
