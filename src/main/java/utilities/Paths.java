package utilities;
//En esta clase se guardan las rutas de archivo, usuario y contraseña de la base y el url de la base de datos.
public class Paths {
    public static final String TotocalcioApp = "/mainTotocalcio1.fxml";
    public static final String ImagenIconoApp = "/imagenes/logoTotocalcio.png";
    public static final String UrlBaseDatos= "jdbc:postgresql://ep-silent-glade-a40lqp0o-pooler.us-east-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_Ci5FUV8HZmfW&sslmode=require&channelBinding=require";
  //Esta parte de código permite colocar las credenciales de la base dentro de las variables de entorno
    public static final String USER = System.getenv("DB_USER") != null
            ? System.getenv("DB_USER")
            : "postgres";

    public static final String PASSWORD = System.getenv("DB_PASSWORD") != null
            ? System.getenv("DB_PASSWORD")
            : "123456";

//Usar este comando de git: git config --global core.autocrlf true
    //si sale este mensaje: warning: in the working copy of 'src/main/resources/mainTotocalcio1.fxml', LF will be replaced by CRLF the next time Git touches it
}
