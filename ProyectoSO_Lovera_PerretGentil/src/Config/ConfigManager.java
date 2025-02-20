/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Esta clase se encarga de cargar y guardar la configuración 
 * en formato JSON usando la librería GSON.
 */
public class ConfigManager {

    // Carga la configuración desde un archivo JSON
    public static Configuracion cargarConfiguracion(String rutaArchivo) {
    Gson gson = new Gson();
    java.io.File file = new java.io.File(rutaArchivo);

    if (!file.exists()) {
        System.out.println("Archivo de configuración no encontrado. Creando uno nuevo...");
        Configuracion nuevaConfig = new Configuracion();
        guardarConfiguracion(nuevaConfig, rutaArchivo);
        return nuevaConfig;
    }

    try (FileReader reader = new FileReader(file)) {
        return gson.fromJson(reader, Configuracion.class);
    } catch (IOException e) {
        System.err.println("Error al leer la configuración: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}


    // Guarda la configuración en un archivo JSON
    public static void guardarConfiguracion(Configuracion config, String rutaArchivo) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("No se pudo guardar la configuración: " + e.getMessage());
        }
    }
}
