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

public class main {
    public static void main(String[] args) {
    String archivoJSON = "procesos_terminados.json";

    Configuracion configTemp = ConfigManager.cargarConfiguracion("config.json");
    if (configTemp == null) {
        configTemp = new Configuracion();
    }
    final Configuracion configuracion = configTemp;

    Planificador planificador = new Planificador(
        configuracion.getNumProcesadores(),
        Planificador.Algoritmo.FCFS,
        configuracion.getDuracionCiclo()
    );

    // 📌 Cargar procesos terminados desde JSON al iniciar el programa
    planificador.cargarProcesosDesdeJSON(archivoJSON);

    RelojGlobal reloj = new RelojGlobal(planificador, null);

    SwingUtilities.invokeLater(() -> {
        InterfazSimulador ventana = new InterfazSimulador(planificador, configuracion);
        reloj.iniciarReloj();
        ventana.setVisible(true);
    });

    reloj.start();

    // 📌 Guardar procesos en JSON al cerrar el programa
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("📌 Guardando procesos antes de salir...");
        planificador.guardarProcesosEnJSON(archivoJSON);
        ConfigManager.guardarConfiguracion(configuracion, "config.json");
        System.out.println("📌 Configuración y procesos guardados correctamente.");
    }));
}
}
