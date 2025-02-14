/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Config;

/**
 *
 * @author arianneperret-gentil
 */

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
        try (FileReader reader = new FileReader(rutaArchivo)) {
            return gson.fromJson(reader, Configuracion.class);
        } catch (IOException e) {
            System.err.println("No se pudo cargar la configuración: " + e.getMessage());
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

