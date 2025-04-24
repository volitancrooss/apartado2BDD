/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package dbconecction;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;
/**
 *
 * @author alumno
 */
public class DBConnection {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String url = "jdbc:mysql://localhost:3306/seguridad_db";
        String username = "root";
        String password = "root";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        //conexion mysql driver
        try (Connection conn = DriverManager.getConnection(url, username, password);) {
            
            System.out.println("Pueden Comenzar las operaciones con la base de datos.");
            
            //Variables del usuario
            System.out.print("Introduce tu nombre de usuario: ");
            String user = sc.nextLine();
            
            System.out.print("Introduce tu password: ");
            String pass = sc.nextLine();
            String hashed = BCrypt.hashpw(pass, BCrypt.gensalt(12));
            
            /*String sql = "ALTER TABLE users ADD COLUMN failed_attempts INT DEFAULT 0, ADD COLUMN last_attempt TIMESTAMP";
            pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate(sql);
             */
            String sql1 = "SELECT * FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(sql1);
            pstmt.setString(1, user);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int attempts = rs.getInt("failed_attempts");
                Timestamp lastAttempt = rs.getTimestamp("last_attempt");
                String userHash = rs.getString("password");
                
                if(attempts >= 3) {
                    //Ultimo intento
                    LocalDateTime ultimoAttempt = lastAttempt.toLocalDateTime();
                    //Intento actual
                    LocalDateTime attemptActual = LocalDateTime.now();
                    //Diferencia entre estos anteriores
                    Duration diff = Duration.between(ultimoAttempt, attemptActual);
                    
                    if(diff.toMinutes() < 5) {
                        System.out.println("La cuenta ha sido bloqueada por 5 minutos, intentalo mas tarde.");
                        return;
                    } else {
                        String reinicioAttempts = "UPDATE users SET failed_attempts = 0 WHERE username = ?";
                        PreparedStatement reset = conn.prepareStatement(reinicioAttempts);
                        pstmt.setString(1, user);
                        pstmt.executeUpdate();
                        attempts = 0;
                        
                    }
                }
                if (BCrypt.checkpw(pass, hashed)) {
                System.out.println("Bienvenido Champion " + hashed);
                String sqlupdate = "update users set failed_attempts = 0 WHERE username = ?";
                pstmt = conn.prepareStatement(sqlupdate);
                pstmt.setString(1, user);
                pstmt.executeUpdate();
                
                } else {
                    attempts++;
                    String sumaAttempts = "UPDATE users failed_attempts = 0, last_attempt = ? CURRENT_TIMESTAMP WHERE username = ?";
                    pstmt = conn.prepareStatement(sumaAttempts);
                    pstmt.setInt(1, attempts);
                    pstmt.setString(2, user);
                    pstmt.executeUpdate();
                    System.out.println("Password incorrecta intento " + attempts + " de 3.");
                }
            } else {
                System.out.println("Usuario Incorrecto.");
            }
            
        } catch (SQLException ex) {
            System.err.println("Error al realizar la consulta: " +
            ex.getMessage());
        }
        
    }
    
}
