package utilities;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ManejadorCSV implements GestorDatos {
    private static final String ARCHIVO_CSV = "participantes_temporales.csv";
    private int idLocalActual = 0; // Simulador del SERIAL de PostgreSQL
    private List<Participante> cacheLocal = new ArrayList<>();

    public ManejadorCSV() {
        // Al instanciar, leemos el CSV por si la app se cerró de golpe y hay datos previos
        cargarDatosDeCSV();
    }

    @Override
    public int guardarParticipante(String nombre, int puntos) {
        idLocalActual++; // 1. Incrementamos nuestro "SERIAL" manual en RAM

        Participante nuevo = new Participante(nombre, puntos);
        cacheLocal.add(nuevo); // Lo guardamos en la memoria rápida

        // 2. Escribimos la nueva línea en el archivo CSV (modo 'append' = true)
        try (FileWriter fw = new FileWriter(ARCHIVO_CSV, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            // Estructura: id,nombre,puntos
            out.println(idLocalActual + "," + nombre + "," + puntos);

        } catch (IOException e) {
            System.out.println("Error al escribir en CSV: " + e.getMessage());
        }

        // 3. Devolvemos el ID simulado para que la lógica de duelos (% 2 == 0) funcione perfecto
        return idLocalActual;
    }

    @Override
    public List<Participante> obtenerUltimosDosParticipantes() {
        List<Participante> ultimos = new ArrayList<>();
        int size = cacheLocal.size();

        // Simulamos la consulta SQL de obtener los 2 últimos ordenados descendentemente
        if (size >= 2) {
            ultimos.add(cacheLocal.get(size - 1)); // El último en jugar (Jugador 2)
            ultimos.add(cacheLocal.get(size - 2)); // El penúltimo en jugar (Jugador 1)
        }
        return ultimos;
    }

    @Override
    public boolean reiniciarBaseDeDatos() {
        // Borramos la memoria RAM y el ID local
        idLocalActual = 0;
        cacheLocal.clear();

        // Sobreescribimos el archivo físico en blanco (modo 'append' = false)
        try {
            new FileWriter(ARCHIVO_CSV, false).close();
            return true;
        } catch (IOException e) {
            System.out.println("Error al limpiar CSV: " + e.getMessage());
            return false;
        }
    }

    private void cargarDatosDeCSV() {
        File archivo = new File(ARCHIVO_CSV);
        if (!archivo.exists()) return; // Si es la primera vez que se abre, no hace nada

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 3) {
                    idLocalActual = Integer.parseInt(datos[0]); // El ID se actualiza a la última fila leída
                    cacheLocal.add(new Participante(datos[1], Integer.parseInt(datos[2])));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error al leer CSV temporal: " + e.getMessage());
        }
    }
}