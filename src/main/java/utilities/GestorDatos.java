package utilities;

import java.util.List;

public interface GestorDatos {
    // Los 3 métodos vitales que usa TotocalcioController
    int guardarParticipante(String nombre, int puntos);
    List<Participante> obtenerUltimosDosParticipantes();
    boolean reiniciarBaseDeDatos();
}