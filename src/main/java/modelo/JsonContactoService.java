package modelo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

/*
Clase apra el manejo de archivos JSON usando GSON
para la serialización de la lista de objetos persona
 */
public class JsonContactoService {

    // Configuración de Gson con formato legible (pretty printing)
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /*
     * Exporta una lista de contactos a un archivo JSON.
     * @param contactos Lista de personas a guardar
     * @param archivo   Archivo de destino (se sobreescribe si existe)
     * @throws IOException Si hay error de escritura
     */
    public void exportarContactos(List<persona> contactos, File archivo) throws IOException {
        try (FileWriter writer = new FileWriter(archivo)) {
            gson.toJson(contactos, writer);
        }
    }

    /*
     * Importa una lista de contactos desde un archivo JSON.
     * @param archivo Archivo JSON de origen
     * @return Lista de personas leída (puede estar vacía)
     * @throws IOException Si hay error de lectura o el formato no es válido
     */
    public List<persona> importarContactos(File archivo) throws IOException {
        try (FileReader reader = new FileReader(archivo)) {
            // TypeToken necesario para que Gson sepa que es una List<persona>
            Type tipoLista = new TypeToken<List<persona>>(){}.getType();
            return gson.fromJson(reader, tipoLista);
        }
    }
}