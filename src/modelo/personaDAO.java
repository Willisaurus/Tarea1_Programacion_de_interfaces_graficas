package modelo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class personaDAO {

    private File archivo;

    public personaDAO() {
        /*
         * ELIMINADO DEL CÓDIGO ANTERIOR:
         * - Dependencia de constructor personaDAO(persona persona)
         * - escritura orientada a una sola persona guardada en atributo interno
         *
         * MOTIVO:
         * En MVC conviene DAO independiente del formulario.
         * El controlador envía listas completas para consistencia.
         */

        File carpeta = new File(System.getProperty("user.home"), "gestionContactos");
        if (!carpeta.exists()) carpeta.mkdirs();

        archivo = new File(carpeta, "datosContactos.csv");

        if (!archivo.exists()) {
            try (FileWriter fw = new FileWriter(archivo)) {
                fw.write("NOMBRE;TELEFONO;EMAIL;CATEGORIA;FAVORITO\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<persona> leerArchivo() throws IOException {
        List<persona> lista = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primera = true;

            while ((linea = br.readLine()) != null) {
                if (primera) { primera = false; continue; }
                if (linea.trim().isEmpty()) continue;

                String[] p = linea.split(";");
                if (p.length == 5) {
                    lista.add(new persona(p[0], p[1], p[2], p[3], Boolean.parseBoolean(p[4])));
                }
            }
        }
        return lista;
    }

    public boolean guardarTodos(List<persona> contactos) {
        // NUEVO: reescribe archivo con estado completo actual
        try (FileWriter fw = new FileWriter(archivo, false)) {
            fw.write("NOMBRE;TELEFONO;EMAIL;CATEGORIA;FAVORITO\n");
            for (persona p : contactos) fw.write(p.datosContacto() + "\n");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportarCSV(List<persona> contactos, File destino) {
        // NUEVO REQ-3
        try (FileWriter fw = new FileWriter(destino, false)) {
            fw.write("NOMBRE;TELEFONO;EMAIL;CATEGORIA;FAVORITO\n");
            for (persona p : contactos) fw.write(p.datosContacto() + "\n");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}