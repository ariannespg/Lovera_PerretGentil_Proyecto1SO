/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package proyectoso_lovera_perretgentil;

import Config.ConfigManager;
import Config.Configuracion;
import Interfaces.InterfazSimulador;
import Modelo.Planificador;
import Modelo.Planificador.Algoritmo;
import Modelo.RelojGlobal;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
    Configuracion configTemp = ConfigManager.cargarConfiguracion("config.json");
    if (configTemp == null) {
        configTemp = new Configuracion();
    }
    final Configuracion configuracion = configTemp; // final o efectivamente final

    System.out.println("Ruta absoluta: " + new java.io.File("config.json").getAbsolutePath());

    int numCPUs = configuracion.getNumProcesadores();
    Planificador planificador = new Planificador(
        numCPUs,
        Planificador.Algoritmo.FCFS,
        configuracion.getDuracionCiclo()
    );

    RelojGlobal reloj = new RelojGlobal(planificador, null);

    SwingUtilities.invokeLater(() -> {
        InterfazSimulador ventana = new InterfazSimulador(planificador, configuracion);
        reloj.iniciarReloj();
        ventana.setVisible(true);
    });

    reloj.start();

    // Agregamos un shutdown hook para guardar la config
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        ConfigManager.guardarConfiguracion(configuracion, "config.json");
        System.out.println("Configuración guardada.");
    }));
}
}
