package controller;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import utilities.*;
import javafx.stage.Stage;
import javafx.scene.media.AudioClip;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 *Controlador principal del juego.
 *<p>
 * Como funciona el programa:
 * El programa trabaja seleccionando preguntas aleatorias, es decir, de todos los mundiales (22) y partidos de
 * la liga italiana escogidos (5) selecciona 7 y 1 respectivamente. Por lo tanto, los labels y botones asociados al respecto
 * necesitan cambiar dependiendo del partido que aparezca en pantalla.
 * <p>
 * Estos objetos siguen el siguiente formato:
 * Labels:
 * - lblTitulo_n: 7 partidos en total, por eso va de 0 a 6, aquí se pueden apreciar el título del partido. Ej. MONDIALE FRANCIA 2006
 * - lblLocal_n: todos los equipos de casa ubicados a la izquierda en cada casilla.
 * - lblVisit_n: todos los equipos de visita ubicados a la derecha en cada casilla.
 * <p>
 * Buttons:
 * Los botones 1, X, y 2 toman el siguiente formato: btn_partido_opcion
 * El centro es el partido al cual pertenece el botón, si tengo 7 (del 0 al 6) partidos, la primera pregunta tendre btn_0_opcion
 * La opción varía entre 1, X, y 2, siguiente el ejemplo anterior, hay tres posibilidades: btn_0_1, btn_0_X, btn_0_2.
 * <p>
 *  Estructura visual donde n es el número de pregunta:
 * <p>
 *  Formato General:
 *                              lblTitulo_n
 *      lblLocal_n       (label que dice "vs.")       lblVisit_n
 *      btn_n_1                  btn_n_X                btn_n_2
 * <p>
 * Nota: Este formato no aplica con la septima pregunta, pero unicamente por tema de ubicación (layout) de los objetos.
 *       De ahi, emplea la misma nomenclatura.
 */
public class TotocalcioController {
    //El label que tiene el número del concurso tiene autoincremento, por lo que es indispensable tener una variable para ello.
    private int numeroConcursoActual = 0;

    //Se eligió la estructura de datos 'Max-Heap' o cola de prioridad
    ComparadorParticipante cmp=new ComparadorParticipante();
    MaxHeap<Participante> tablaPosiciones =  new MaxHeap<>(cmp);
    private String[] apuestasUsuario = new String[7];

    //Se creará un temporizador para el reinicio de la app
    private PauseTransition temporizadorReinicio;
    // Un temporizador exclusivo para ocultar la notificación
    private PauseTransition temporizadorNotificacion;
    // Esta lista guardará los 7 partidos (6 mundiales + 1 bonus) que salieron en pantalla
    private List<Partido> rondaActual = new ArrayList<>();
    private Label[] titulos;
    private Label[] locales;
    private Label[] visitantes;
    private ImageView[] imgLocales;
    private ImageView[] imgVisitantes;


    @FXML private Label lblTitulo_0, lblTitulo_1, lblTitulo_2, lblTitulo_3, lblTitulo_4, lblTitulo_5, lblTitulo_6;
    @FXML private Label lblLocal_0, lblLocal_1, lblLocal_2, lblLocal_3, lblLocal_4, lblLocal_5, lblLocal_6;
    @FXML private Label lblVisit_0, lblVisit_1, lblVisit_2, lblVisit_3, lblVisit_4, lblVisit_5, lblVisit_6;

    @FXML private Button btn_0_1, btn_1_1, btn_2_1, btn_3_1, btn_4_1, btn_5_1, btn_6_1;
    @FXML private Button btn_0_2, btn_1_2, btn_2_2, btn_3_2, btn_4_2, btn_5_2, btn_6_2;
    @FXML private Button btn_0_X, btn_1_X, btn_2_X, btn_3_X, btn_4_X, btn_5_X, btn_6_X;


    @FXML private ImageView imgLocal_0, imgLocal_1, imgLocal_2, imgLocal_3, imgLocal_4, imgLocal_5, imgLocal_6;
    @FXML private ImageView imgVisit_0, imgVisit_1, imgVisit_2, imgVisit_3, imgVisit_4, imgVisit_5, imgVisit_6;
    @FXML
    private Button btnSiguienteJugador;
    @FXML
    private ImageView imgView_maximizar;
    @FXML
    private AnchorPane idPantallaCarga;
    @FXML
    private Button idBotonIniziare;
    @FXML
    private VBox vboxListaRanking;
    @FXML
    private AnchorPane idPanelJuego;

    @FXML
    private HBox panelNotificacion;
    @FXML
    private Label lblNotificacion;
    @FXML
    private Button btn_enviar_apuesta;
    @FXML
    private Label lblConcorso;

    // Efectos de sonido para mejorar la interacción del usuario
    private AudioClip sonidoSilbato;
    private AudioClip sonidoPerfecto;

    // Sonido que se usa cuando al final del duelo gana una persona Vincitore = Vencedor o Ganador
    private AudioClip sonidoVincitore;

    // Sonido que se usa cuando al final del duelo quedan en empate los concursantes Pareggio = Empate
    private AudioClip sonidoPareggio;

    public void initialize(){
        // 1. Agrupamos los elementos en orden (del slot 0 al 6)
        titulos = new Label[]{lblTitulo_0, lblTitulo_1, lblTitulo_2, lblTitulo_3, lblTitulo_4, lblTitulo_5, lblTitulo_6};
        locales = new Label[]{lblLocal_0, lblLocal_1, lblLocal_2, lblLocal_3, lblLocal_4, lblLocal_5, lblLocal_6};
        visitantes = new Label[]{lblVisit_0, lblVisit_1, lblVisit_2, lblVisit_3, lblVisit_4, lblVisit_5, lblVisit_6};
        imgLocales = new ImageView[]{imgLocal_0, imgLocal_1, imgLocal_2, imgLocal_3, imgLocal_4, imgLocal_5, imgLocal_6};
        imgVisitantes = new ImageView[]{imgVisit_0, imgVisit_1, imgVisit_2, imgVisit_3, imgVisit_4, imgVisit_5, imgVisit_6};

        // 2. Llenamos el tablero por primera vez
        llenarTablero();
        cargarLeaderboard();
        actualizarTablaPosicionesUI();
        idPantallaCarga.setVisible(true);
        numeroConcursoActual = ConexionBD.obtenerSiguienteConcurso();
        lblConcorso.setText(String.valueOf(numeroConcursoActual));
// Cargar sonidos
        try {
            sonidoSilbato = new AudioClip(getClass().getResource("/audios/silbato_inicio.mp3").toExternalForm());
            sonidoPerfecto = new AudioClip(getClass().getResource("/audios/perfetto.mp3").toExternalForm());
            sonidoVincitore = new AudioClip(getClass().getResource("/audios/vincitore.mp3").toExternalForm());
            sonidoPareggio = new AudioClip(getClass().getResource("/audios/pareggio.mp3").toExternalForm());
        } catch (Exception e) {
            System.out.println("Error cargando sonidos: " + e.getMessage());
        }
    }
    public void llenarTablero() {
        // Limpiamos la memoria de la partida anterior
        rondaActual.clear();

        // Traemos los datos frescos de la BD
        List<Partido> mundiales = ConexionBD.obtenerPartidos();
        Partido bonus = ConexionBD.obtenerPartidosBonus();

        // Juntamos en la lista maestra de la ronda (7 partidos)
        rondaActual.addAll(mundiales);
        if(bonus != null) {
            rondaActual.add(bonus);
        }

        // Llenamos los 7 slots en 5 líneas de código
        for (int i = 0; i < rondaActual.size(); i++) {
            Partido p = rondaActual.get(i);

            titulos[i].setText(p.getTituloPartido());
            locales[i].setText(p.getEquipoLocal());
            visitantes[i].setText(p.getEquipoVisitante());

            try {
                // Cargamos las banderas dinámicamente desde la carpeta resources
                imgLocales[i].setImage(new Image(getClass().getResourceAsStream("/imagenes/" + p.getRutaBanderaLocal())));
                imgVisitantes[i].setImage(new Image(getClass().getResourceAsStream("/imagenes/" + p.getRutaBanderaVisitante())));
            } catch (Exception e) {
                System.out.println("Error cargando imagen del partido " + i + ": " + e.getMessage());
            }
        }
    }
    /**
     * Metodo que carga la tabla de posiciones (tablaLideres) actual, guardandola en el Heap
     */
    public void cargarLeaderboard(){
        //llamo a la función de la base de datos
        String query = "SELECT * FROM fn_selectAll()";

        try(Connection connection = ConexionBD.conectar();
            PreparedStatement pstmt = connection.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery()){

            //iteración sobre el set de resultados rs
            while(rs.next()){
                String nombre = rs.getString("nombre_jugador");
                int puntos = rs.getInt("puntos_obtenidos");

                //se crea un objeto Participante y se lo guarda en el Heap
                Participante participante = new Participante(nombre, puntos);
                tablaPosiciones.insertar(participante);
            }
        }catch (SQLException e){
            System.out.println("Error al cargar la base de datos :( :" + e.getMessage());
        }
    }

    /**
     * Metodo que actualiza la tabla de posiciones en el UI
     * Es la parte visual, también genera colores diferentes para los primeros tres puestos
     */
    public void actualizarTablaPosicionesUI() {
        // 1. Limpiar la interfaz previa
        vboxListaRanking.getChildren().clear();
        //2. Obtener la lista de los mejores N participantes
        List<Participante> topParticipantes = tablaPosiciones.obtenerTopN(10);

        int puesto = 1;
        for (Participante p : topParticipantes) {
            // 3. Crear el contenedor de la fila
            HBox fila = new HBox();
            fila.setSpacing(20);

            // Variables para guardar el estilo dinámico
            String estiloFila = "-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; ";
            String colorTextoPuesto = "-fx-font-weight: bold; ";

            // Lógica del Podio (Oro, Plata, Bronce)
            if (puesto == 1) {
                // ORO: Fondo amarillo muy suave, borde dorado
                estiloFila += "-fx-background-color: #FFFBE6; -fx-border-color: #FFD700; -fx-border-width: 2px;";
                colorTextoPuesto += "-fx-text-fill: #B8860B;"; // Oro oscuro para contraste
            } else if (puesto == 2) {
                // PLATA: Fondo gris muy suave, borde plateado
                estiloFila += "-fx-background-color: #F8F9FA; -fx-border-color: #8f8f8f; -fx-border-width: 2px;";
                colorTextoPuesto += "-fx-text-fill: #6C757D;"; // Gris oscuro
            } else if (puesto == 3) {
                // BRONCE: Fondo naranja muy suave, borde bronce
                estiloFila += "-fx-background-color: #FFF3E0; -fx-border-color: #CD7F32; -fx-border-width: 2px;";
                colorTextoPuesto += "-fx-text-fill: #8B4513;"; // Café oscuro
            } else {
                // RESTO: Blanco normal sin borde
                estiloFila += "-fx-background-color: white;";
                colorTextoPuesto += "-fx-text-fill: #1a2b4c;"; // Azul del diseño
            }

            fila.setStyle(estiloFila);

            // 4. Crear los Labels
            Label lblPuesto = new Label("#" + puesto);
            lblPuesto.setStyle(colorTextoPuesto); // Aplicamos el color del metal al número

            Label lblNombre = new Label(p.getNombre());
            lblNombre.setPrefWidth(150);

            Label lblPuntos = new Label(p.getPuntos() + " pts");
            lblPuntos.setStyle("-fx-font-weight: bold;");

            // 5. Agregar elementos a la fila y la fila al VBox
            fila.getChildren().addAll(lblPuesto, lblNombre, lblPuntos);
            vboxListaRanking.getChildren().add(fila);

            puesto++;
        }
    }

    /**
     * Metodo que registra el boton pulsado por el usuario, me lo recomendó la IA y me parece sumamente fascinante
     * Identifica que boton fue presionado y gracias al formato del id (btn_fila_opcion) permite saber a qué fila exacta
     * pertenece
     * @param event
     */
    @FXML
public void registrarApuesta(ActionEvent event){
    //1. Identificar que botón se presionó
    Button botonPresionado = (Button) event.getSource();
    String idBoton = botonPresionado.getId();

    //Condición de seguridad
    if (idBoton == null) {
        System.out.println("Error: Al botón le falta el ID");
        return;
    }

    //2. Extraer el número de fila (del 0 al 7) basado en el ID
    //Se aprovechará el formato de los id de los botones. Ej: btn_fila_opción
    //se usara un split por guion bajo "_"
    String[] partes = idBoton.split("_");
    //como la fila esta en la segunda posición ahora sabemos donde estamos ubicados
    int indiceFila = Integer.parseInt(partes[1]);

    //Obtenemos el texto del boton para saber que presionó el usuario
    String valorElegido = botonPresionado.getText();

    // 3. Guardamos en memoria sus apuestas
    apuestasUsuario[indiceFila]=valorElegido;

    //4. Limpiamos los botones de la fila y cambiamos el boton seleccionado
    limpiarBotonesDeLaFila(indiceFila);
    botonPresionado.getStyleClass().add("boton-seleccionado");
}
//Metodo auxiliar para limpiar los botones
private void limpiarBotonesDeLaFila(int fila){
        String[] opciones = { "1", "X", "2"};
        for (String opcion:opciones){
            //Buscamos el botón aprovechando el ID
            String idBuscado = "#btn_" + fila + "_" + opcion;
            Node nodo = idPanelJuego.lookup(idBuscado);

            if(nodo !=null){
                //Quitamos el estilo CSS
                nodo.getStyleClass().remove("boton-seleccionado");
            }
        }
}

    /**
     * Esto es para cuando el usuario de en el botón "INIZIARE" se oculte la pantalla de bloqueo
     * @param event
     */
    @FXML
    void ocultarPantalla(ActionEvent event) {
        if (sonidoSilbato != null) sonidoSilbato.play(); // ¡Piiiiip!
        idPantallaCarga.setVisible(false);
    }

    /**
     * Este metodo es para el botón de enviar apuesta, procesa los resultados enviados por parte del jugador
     * En caso de que no haya enviado alguuno, le llega una advertencia
     * @param event
     */
    @FXML
    void procesarApuesta(ActionEvent event) {
        //Verificar si el usuario respondió todas las preguntas
        for (int i = 0; i < apuestasUsuario.length; i++) {
            if(apuestasUsuario[i]==null){
                mostrarError("Attenzione! Devi rispondere tutti le partite");
                return;
            }
        }
        // Calculo de puntos dinámico
        int puntosObtenidos = 0;

        for (int i = 0; i < apuestasUsuario.length; i++) {
            // Extraemos la respuesta correcta directamente del objeto Partido en esa posición
            String resultadoCorrecto = rondaActual.get(i).getResultadoReal();

            // Comparamos lo que presionó el usuario con la respuesta de la base de datos
            if(apuestasUsuario[i].trim().equalsIgnoreCase(resultadoCorrecto)){
                if(i == 6){
                    puntosObtenidos += 7; // El partido Bonus vale 7 puntos
                } else {
                    puntosObtenidos += 5; // Los partidos de Mundial valen 5 puntos
                }
            }
        }

        //3.Generación de usuario y persistencia
        String nombreJugador = NameGenerator.generarNombreAleatorio();
        int idRealAsignado = ConexionBD.guardarParticipante(nombreJugador, puntosObtenidos);
        Participante participante =  new Participante(nombreJugador,puntosObtenidos);
        tablaPosiciones.insertar(participante);
        actualizarTablaPosicionesUI();

        // 1. CREAMOS LA ALERTA DE RESUMEN INDIVIDUAL
        String mensajeResumen = "Giocatore: " + nombreJugador + "\n" +
                "Punteggio: " + puntosObtenidos + " pts.\n\n" +
                "Grazie per aver partecipato al Totocalcio!";

        Alert alertaResumen = new Alert(Alert.AlertType.INFORMATION);
        alertaResumen.setTitle("Risultato della Giocata");

        // Eliminamos el encabezado y el logo por defecto para centrar todo
        alertaResumen.setHeaderText(null);
        alertaResumen.setGraphic(null);

        // --- CONTENEDOR PRINCIPAL ---
        VBox contenedorPrincipal = new VBox(0); // Sin espacio entre el header y el body
        contenedorPrincipal.setAlignment(javafx.geometry.Pos.CENTER);
        contenedorPrincipal.setStyle("-fx-background-color: #f4f4f4; -fx-background-radius: 10; -fx-overflow: hidden;");

        // --- NUEVO ENCABEZADO PERSONALIZADO (Azul con letras blancas) ---
        HBox headerPersonalizado = new HBox();
        headerPersonalizado.setAlignment(javafx.geometry.Pos.CENTER);
        headerPersonalizado.setPadding(new javafx.geometry.Insets(15, 20, 15, 20));
        headerPersonalizado.setStyle("-fx-background-color: #19436a; -fx-background-radius: 10 10 0 0;");
        headerPersonalizado.setPrefWidth(450);

        Label lblTituloBlanco = new Label("SCOMMESSA INVIATA CON SUCCESSO");
        lblTituloBlanco.setStyle("-fx-font-family: 'Roboto Black'; -fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");
        headerPersonalizado.getChildren().add(lblTituloBlanco);

        // --- CUERPO DE LA ALERTA (Contenido centrado) ---
        VBox cuerpoAlerta = new VBox(15);
        cuerpoAlerta.setAlignment(javafx.geometry.Pos.CENTER);
        cuerpoAlerta.setPadding(new javafx.geometry.Insets(25));

        if (puntosObtenidos == 37) {
            try {
                // GIF de Puntaje Perfecto (Nombre actualizado)
                ImageView gifCiao = new ImageView(new Image(getClass().getResourceAsStream("/imagenes/Punteggio_perfetto.gif")));
                gifCiao.setFitWidth(320);
                gifCiao.setPreserveRatio(true);

                Label lblPuntosPerfectos = new Label("PUNTEGGIO PERFETTO: " + puntosObtenidos + " pts");
                lblPuntosPerfectos.setStyle("-fx-font-size: 26px; -fx-font-family: 'Roboto Black'; -fx-text-fill: #d4af37; -fx-font-weight: bold;");

                Label lblUserPerfecto = new Label("Giocatore: " + nombreJugador);
                lblUserPerfecto.setStyle("-fx-font-size: 20px; -fx-font-family: 'Roboto'; -fx-text-fill: #19436a;");

                cuerpoAlerta.getChildren().addAll(gifCiao, lblPuntosPerfectos, lblUserPerfecto);

                if (sonidoPerfecto != null) sonidoPerfecto.play();
            } catch (Exception e) {
                System.out.println("Error al cargar Punteggio_perfetto.gif: " + e.getMessage());
            }
        } else {
            // Caso Normal: Puntaje gigante y Usuario debajo
            Label lblPuntosGrandes = new Label(puntosObtenidos + " pts");
            lblPuntosGrandes.setStyle("-fx-font-size: 60px; -fx-font-family: 'Roboto Black'; -fx-text-fill: #19436a; -fx-font-weight: bold;");

            Label lblUsuarioSub = new Label("Giocatore: " + nombreJugador);
            lblUsuarioSub.setStyle("-fx-font-size: 24px; -fx-font-family: 'Roboto'; -fx-text-fill: #444444;");

            Label lblClasifica = new Label("Sei in classifica! Controlla la tua posizione.");
            lblClasifica.setStyle("-fx-font-size: 15px; -fx-text-fill: #19436a; -fx-font-style: italic;");

            cuerpoAlerta.getChildren().addAll(lblPuntosGrandes, lblUsuarioSub, lblClasifica);
        }

        // Unimos el encabezado y el cuerpo
        contenedorPrincipal.getChildren().addAll(headerPersonalizado, cuerpoAlerta);

        // Inyectamos el contenedor en la alerta
        DialogPane dialogPane = alertaResumen.getDialogPane();
        dialogPane.setContent(contenedorPrincipal);
        dialogPane.setStyle("-fx-background-color: transparent;"); // Quitamos el fondo gris de la ventana

        // Botón OK estilizado
        Button botonOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (botonOk != null) {
            botonOk.setStyle("-fx-background-color: #19436a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 30 10 30;");
        }

        Stage stagePrincipal = (Stage) idPanelJuego.getScene().getWindow();
        alertaResumen.initOwner(stagePrincipal);
        alertaResumen.showAndWait();

        // Verificamos si es el final de una ronda de 2 jugadores
        if (idRealAsignado % 2 == 0) {
            evaluarDuelo();
        }

        if (idRealAsignado >= 20) {
            ejecutarReseteoLocalYRemoto();
        }

        btn_enviar_apuesta.setVisible(false); //Oculto el botón de enviar
        btnSiguienteJugador.setVisible(true); //Muestro el botón de siguiente

        //4. Finalmente, reiniciamos el tablero para el siguiente jugador
        temporizadorReinicio = new PauseTransition(Duration.seconds(15));
        temporizadorReinicio.setOnFinished(e -> reiniciarTablero());
        temporizadorReinicio.play();
    }

    /**
     * Metodo para reiniciar el tablero y dejarlo como estaba antes del juego
     */
    private void reiniciarTablero(){
        llenarTablero();
        //Si el usuario presionó el botón antes de los 12 segundos, cancelamos el reloj
        if (temporizadorReinicio != null) {
            temporizadorReinicio.stop();
        }
        //Se elimina todas las apuestas realizadas en memoria
        Arrays.fill(apuestasUsuario, null);
        //Se busca todos los botones dentro del panel y se retira el CSS de boton seleccionado
        idPanelJuego.lookupAll(".boton-seleccionado").forEach(nodo ->{
            nodo.getStyleClass().remove("boton-seleccionado");
        });
        //Dejamos los botones como estaban
        btn_enviar_apuesta.setVisible(true);
        btnSiguienteJugador.setVisible(false);

        //Volvemos a mostrar la pantalla carga
        idPantallaCarga.setVisible(true);
        numeroConcursoActual = ConexionBD.obtenerSiguienteConcurso();
        lblConcorso.setText(String.valueOf(numeroConcursoActual));

        //En caso se ejecute la aplicación en dos laptops, para la sincronización se deberá consultar de nuevo a la base
        // 1. Vaciamos el Heap actual creando uno nuevo
        tablaPosiciones = new MaxHeap<>(new ComparadorParticipante());
        // 2. Volvemos a consultar la nube
        cargarLeaderboard();
        // 3. Dibujamos el Top 5 actualizado
        actualizarTablaPosicionesUI();
    }
    @FXML
    void accionSiguienteJugador(ActionEvent event){
        reiniciarTablero();
    }

    private void mostrarError(String mensaje){
        lblNotificacion.setText(mensaje);
        panelNotificacion.getStyleClass().add("notificacion-error");

        //4. Se muestra en pantalla
        panelNotificacion.setVisible(true);
        //5. Se configura un temporizador para que desaparezca solo
        if(temporizadorNotificacion!=null){
            temporizadorNotificacion.stop();
        }
        temporizadorNotificacion = new PauseTransition(Duration.seconds(4));
        temporizadorNotificacion.setOnFinished(e -> panelNotificacion.setVisible(false));
        temporizadorNotificacion.play();
    }
    private void evaluarDuelo() {
        List<Participante> ultimos = ConexionBD.obtenerUltimosDosParticipantes();

        if (ultimos.size() == 2) {
            Participante jugador2 = ultimos.get(0); // Último en jugar (ID Par)
            Participante jugador1 = ultimos.get(1); // Jugador anterior (ID Impar)

            // 1. Configuración de la Alerta
            Alert alertaDuelo = new Alert(Alert.AlertType.INFORMATION);
            alertaDuelo.setTitle("RISULTATO DEL DUELLO");
            alertaDuelo.setHeaderText(null);
            alertaDuelo.setGraphic(null);

            // --- CONTENEDOR PRINCIPAL ---
            VBox contenedorPrincipal = new VBox(0);
            contenedorPrincipal.setAlignment(javafx.geometry.Pos.CENTER);
            contenedorPrincipal.setStyle("-fx-background-color: #f4f4f4; -fx-background-radius: 10;");

            // --- CABECERA AZUL (Sincronizada con el resto de la app) ---
            HBox headerDuelo = new HBox();
            headerDuelo.setAlignment(javafx.geometry.Pos.CENTER);
            headerDuelo.setPadding(new javafx.geometry.Insets(15, 20, 15, 20));
            headerDuelo.setStyle("-fx-background-color: #19436a; -fx-background-radius: 10 10 0 0;");
            headerDuelo.setPrefWidth(500);

            Label lblTituloHeader = new Label("FINE DEL DUELLO: 1 vs 1");
            lblTituloHeader.setStyle("-fx-font-family: 'Roboto Black'; -fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");
            headerDuelo.getChildren().add(lblTituloHeader);

            // --- CUERPO DEL DUELO ---
            VBox cuerpoDuelo = new VBox(20);
            cuerpoDuelo.setAlignment(javafx.geometry.Pos.CENTER);
            cuerpoDuelo.setPadding(new javafx.geometry.Insets(30));

            Label lblGanador = new Label();
            Label lblDetalle = new Label();

            // Lógica de comparación y asignación de sonidos
            if (jugador1.getPuntos() > jugador2.getPuntos()) {
                lblGanador.setText("🏆 " + jugador1.getNombre().toUpperCase());
                lblDetalle.setText("Ha vinto con " + jugador1.getPuntos() + " pts contro i " + jugador2.getPuntos() + " di " + jugador2.getNombre());
                if (sonidoVincitore != null) sonidoVincitore.play();
            } else if (jugador2.getPuntos() > jugador1.getPuntos()) {
                lblGanador.setText("🏆 " + jugador2.getNombre().toUpperCase());
                lblDetalle.setText("Ha vinto con " + jugador2.getPuntos() + " pts contro i " + jugador1.getPuntos() + " di " + jugador1.getNombre());
                if (sonidoVincitore != null) sonidoVincitore.play();
            } else {
                lblGanador.setText("🤝 PAREGGIO!");
                lblDetalle.setText("Entrambi i giocatori hanno totalizzato " + jugador1.getPuntos() + " pts.");
                if (sonidoPareggio != null) sonidoPareggio.play();
            }

            // Estilos para los textos del cuerpo
            lblGanador.setStyle("-fx-font-size: 32px; -fx-font-family: 'Roboto Black'; -fx-text-fill: #19436a; -fx-font-weight: bold;");
            lblDetalle.setStyle("-fx-font-size: 18px; -fx-font-family: 'Roboto'; -fx-text-fill: #444444; -fx-text-alignment: center;");
            lblDetalle.setWrapText(true);

            cuerpoDuelo.getChildren().addAll(lblGanador, lblDetalle);
            contenedorPrincipal.getChildren().addAll(headerDuelo, cuerpoDuelo);

            // Inyectar en el DialogPane
            DialogPane dialogPane = alertaDuelo.getDialogPane();
            dialogPane.setContent(contenedorPrincipal);
            dialogPane.setStyle("-fx-background-color: transparent;");

            // Estilo del botón OK
            Button botonOk = (Button) dialogPane.lookupButton(ButtonType.OK);
            if (botonOk != null) {
                botonOk.setStyle("-fx-background-color: #19436a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 30 10 30;");
            }

            // Mantener Full Screen y mostrar
            Stage stagePrincipal = (Stage) idPanelJuego.getScene().getWindow();
            alertaDuelo.initOwner(stagePrincipal);
            alertaDuelo.showAndWait();
        }
    }
    @FXML
    void restaurarPantallaCompleta(MouseEvent event) {
        Stage stagePrincipal = (Stage) idPanelJuego.getScene().getWindow();
        stagePrincipal.setFullScreen(true);
    }
    @FXML
    void regresarPantallaInicio(MouseEvent event) {
        // Volvemos a hacer visible la pantalla de carga gigante de Ciao
        idPantallaCarga.setVisible(true);
        reiniciarTablero();
    }

    @FXML
    void cerrarAplicacion(MouseEvent event) {
        // Creamos una alerta de tipo CONFIRMACIÓN
        Alert alertaCerrar = new Alert(Alert.AlertType.CONFIRMATION);
        alertaCerrar.setTitle("Conferma di chiusura");
        alertaCerrar.setHeaderText("Stai per chiudere il Totocalcio");
        alertaCerrar.setContentText("Sei sicuro di voler uscire dall'applicazione?");

        // Protegemos el FullScreen asociando la alerta a la ventana principal
        Stage stagePrincipal = (Stage) idPanelJuego.getScene().getWindow();
        alertaCerrar.initOwner(stagePrincipal);

        // Estilizado opcional rápido para que no desentone
        DialogPane dialogPane = alertaCerrar.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #f4f4f4;");

        // showAndWait() devuelve un 'Optional' que nos dice qué botón se presionó
        Optional<ButtonType> resultado = alertaCerrar.showAndWait();

        // Si el usuario presionó OK, entonces cerramos todo
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            javafx.application.Platform.exit();
            System.exit(0);
        }
        // Si presionó Cancelar o cerró la ventana, no hace nada y sigue el juego
    }

    @FXML
    void mostrarAyuda(MouseEvent event) {
        // 1. Configuración básica de la Alerta
        Alert alertaAyuda = new Alert(Alert.AlertType.INFORMATION);
        alertaAyuda.setTitle("Guida Visiva");
        alertaAyuda.setHeaderText(null);
        alertaAyuda.setGraphic(null);

        // --- CONTENEDOR PRINCIPAL ---
        VBox contenedorPrincipal = new VBox(0);
        contenedorPrincipal.setAlignment(javafx.geometry.Pos.CENTER);
        contenedorPrincipal.setStyle("-fx-background-color: #f4f4f4; -fx-background-radius: 10; -fx-overflow: hidden;");

        // --- CABECERA AZUL ---
        HBox headerPersonalizado = new HBox();
        headerPersonalizado.setAlignment(javafx.geometry.Pos.CENTER);
        headerPersonalizado.setPadding(new javafx.geometry.Insets(15, 20, 15, 20));
        headerPersonalizado.setStyle("-fx-background-color: #19436a; -fx-background-radius: 10 10 0 0;");
        headerPersonalizado.setPrefWidth(550); // Un poco más ancho para el GIF

        Label lblTituloHeader = new Label("GUIDA VISIVA: COME GIOCARE");
        lblTituloHeader.setStyle("-fx-font-family: 'Roboto Black'; -fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");
        headerPersonalizado.getChildren().add(lblTituloHeader);

        // --- CUERPO DE LA AYUDA ---
        VBox cuerpoAyuda = new VBox(15);
        cuerpoAyuda.setAlignment(javafx.geometry.Pos.CENTER);
        cuerpoAyuda.setPadding(new javafx.geometry.Insets(25));

        try {
            // Cargamos el GIF animado de las instrucciones
            ImageView gifInstrucciones = new ImageView(new Image(getClass().getResourceAsStream("/imagenes/istruzioniCiao.gif")));
            gifInstrucciones.setFitWidth(450); // Tamaño ideal para que se aprecien los pasos
            gifInstrucciones.setPreserveRatio(true);

            cuerpoAyuda.getChildren().add(gifInstrucciones);
        } catch (Exception e) {
            System.out.println("Error al cargar istruzioniCiao.gif: " + e.getMessage());
        }

        // Texto de refuerzo debajo del GIF
        Label lblExplicacion = new Label("Segui i passaggi mostrati nell'animazione\nper completare la tua scommessa.");
        lblExplicacion.setStyle("-fx-font-size: 16px; -fx-font-family: 'Roboto'; -fx-text-fill: #444444; -fx-text-alignment: center;");
        lblExplicacion.setWrapText(true);

        cuerpoAyuda.getChildren().add(lblExplicacion);

        // Unimos cabecera y cuerpo
        contenedorPrincipal.getChildren().addAll(headerPersonalizado, cuerpoAyuda);

        // Inyectamos el diseño en la alerta
        DialogPane dialogPane = alertaAyuda.getDialogPane();
        dialogPane.setContent(contenedorPrincipal);
        dialogPane.setStyle("-fx-background-color: transparent;");

        // --- DETALLE DE UX: Cambiamos el texto del botón OK a "HO CAPITO" ---
        Button botonOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (botonOk != null) {
            botonOk.setText("HO CAPITO"); // Traduce a: "Ya entendí"
            botonOk.setStyle("-fx-background-color: #19436a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 30 10 30;");
        }

        // Proteger el FullScreen y mostrar
        Stage stagePrincipal = (Stage) idPanelJuego.getScene().getWindow();
        alertaAyuda.initOwner(stagePrincipal);
        alertaAyuda.showAndWait();
    }

    private void ejecutarReseteoLocalYRemoto() {
        boolean exito = ConexionBD.reiniciarBaseDeDatos();

        if (exito) {
            // 1. Limpiamos el Heap (Leaderboard visual y lógico)
            tablaPosiciones = new MaxHeap<>(new ComparadorParticipante());
            actualizarTablaPosicionesUI();

            // 2. Reiniciamos el número de ticket al #1
            numeroConcursoActual = 1;

            // Usamos runLater por si acaso esto se llama desde otro hilo
            javafx.application.Platform.runLater(() -> {
                lblConcorso.setText(String.valueOf(numeroConcursoActual));
            });

            System.out.println("El sistema ha sido reseteado correctamente.");
        }
    }
}

