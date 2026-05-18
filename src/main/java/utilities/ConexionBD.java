package utilities;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que facilita la conexión a la base de datos.
 */

public class ConexionBD {
    /**
     * Metodo para conectar con la base, usa atributos guardados en la clase Paths
     *
     * @return la conexión con la base
     * @throws SQLException en caso de que no se pueda conectar
     */
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(Paths.UrlBaseDatos, Paths.USER, Paths.PASSWORD);
    }

    /**
     * Metodo para almacenar los participantes en la base de datos
     *
     * @param nombre
     * @param puntos
     */
    public static int guardarParticipante(String nombre, int puntos) {
        // Llamamos a la función SQL que creamos, pasándole los 2 parámetros
        String query = "SELECT fn_guardar_participante(?, ?)";
        int idGenerado = -1; // -1 será nuestro indicador de error

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nombre);
            pstmt.setInt(2, puntos);

            // Ejecutamos la consulta y leemos el resultado
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idGenerado = rs.getInt(1); // Atrapamos el ID real
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al guardar participante en la BD: " + e.getMessage());
        }

        return idGenerado;
    }

    /**
     * Este metodo permite que el número del concurso, ubicado en la esquina superior izquierda, crezca secuencialmente.
     *
     * @return el número del último registro más uno
     */
    public static int obtenerSiguienteConcurso() {
        String query = "SELECT obtenerSiguienteConcurso()";
        try (Connection conn = conectar();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener el número de concurso: " + e.getMessage());
        }

        return 1;
    }

    public static List<Partido> obtenerPartidos() {
        List<Partido> listaPartidos = new ArrayList<>();

        // Llamamos a la función de PostgreSQL
        String query = "SELECT * FROM fn_extraerPartidos()";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            // Iteramos sobre todos los partidos que nos devuelva la base de datos
            while (rs.next()) {
                String equipoLocal = rs.getString("equipo_local");
                String equipoVisitante = rs.getString("equipo_visitante");
                String rutaBanderaLocal = rs.getString("ruta_bandera_local");
                String rutaBanderaVisitante = rs.getString("ruta_bandera_visitante");
                String resultadoReal = rs.getString("resultado_real");
                String nombreMundial = rs.getString("nombre_mundial");

                // Creamos el objeto Partido y lo metemos a nuestra lista
                Partido partido = new Partido(
                        equipoLocal,
                        equipoVisitante,
                        rutaBanderaLocal,
                        rutaBanderaVisitante,
                        resultadoReal,
                        nombreMundial
                );

                listaPartidos.add(partido);
            }

        } catch (SQLException e) {
            System.out.println("Error al extraer los partidos de la BD: " + e.getMessage());
        }
        return listaPartidos;
    }

    public static Partido obtenerPartidosBonus() {
        Partido bonus = null;
        String query = "SELECT * FROM fn_extraerBonus()";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                bonus = new Partido(rs.getString("equipo_local"), rs.getString("equipo_visitante"),
                        rs.getString("ruta_bandera_local"), rs.getString("ruta_bandera_visitante"),
                        rs.getString("resultado_real"), rs.getString("titulo_partido"));
            }
        } catch (SQLException e) {
            System.out.println("Error bonus: " + e.getMessage());
        }
        return bonus;
    }

    public static List<Participante> obtenerUltimosDosParticipantes() {
        List<Participante> ultimosDos = new ArrayList<>();
        String query = "SELECT * FROM fn_last_two()";

        try (Connection connection = ConexionBD.conectar();
             PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Usamos los nuevos nombres de las columnas devueltas por la función
                String nombre = rs.getString("out_nombre");
                int puntos = rs.getInt("out_puntos");

                Participante participante = new Participante(nombre, puntos);
                ultimosDos.add(participante);
            }
        } catch (SQLException e) {
            System.out.println("Error al extraer el duelo: " + e.getMessage());
        }
        return ultimosDos;
    }
    public static boolean reiniciarBaseDeDatos() {
        // Llamamos a la función SQL de borrado
        String query = "SELECT fn_reiniciar_base_datos()";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            // Leemos la respuesta booleana que nos envía Postgres
            if (rs.next()) {
                return rs.getBoolean(1);
            }

        } catch (SQLException e) {
            System.out.println("Error de conexión al intentar reiniciar: " + e.getMessage());
        }

        return false; // Por defecto retornamos falso si algo falla
    }


    }
