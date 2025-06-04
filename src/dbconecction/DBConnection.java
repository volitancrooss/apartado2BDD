/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package dbconecction;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        
        while (true) {
            // Pedir datos al usuario
            System.out.println("Por favor, introduce tus datos:");
            System.out.print("Introduce tu nombre de usuario: ");
            String user = sc.nextLine();
            System.out.print("Introduce tu password: ");
            String pass = sc.nextLine();
            
            // Conectar a la base de datos
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                
                PreparedStatement pstmt = null;
                String sql;
                
                // Obtener información del usuario
                sql = "SELECT password, failed_attempts, last_attempt FROM users WHERE username = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, user);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    // Obtener valores de la base de datos
                    String hash = rs.getString("password");
                    int failedAttempts = rs.getInt("failed_attempts");
                    Timestamp lastAttemptTimestamp = rs.getTimestamp("last_attempt");
                    LocalDateTime lastAttempt = lastAttemptTimestamp != null ? lastAttemptTimestamp.toLocalDateTime() : null;
                    
                    // Verificar si la cuenta está bloqueada
                    if (failedAttempts >= 3 && lastAttempt != null && Duration.between(lastAttempt, LocalDateTime.now()).toMinutes() < 5) {
                        long minutosRestantes = 5 - Duration.between(lastAttempt, LocalDateTime.now()).toMinutes();
                        System.out.println("Cuenta bloqueada. Intentalo dentro de " + minutosRestantes + " minutos.");
                    } else {
                        // Verificar la contraseña
                        if (BCrypt.checkpw(pass, hash)) {
                            System.out.println("Bienvenido Champion " + user);
                            // Reiniciar contador de intentos fallidos
                            sql = "UPDATE users SET failed_attempts = 0, last_attempt = NULL WHERE username = ?";
                            pstmt = conn.prepareStatement(sql);
                            pstmt.setString(1, user);
                            pstmt.executeUpdate();
                            break; // Salir del bucle al login exitoso
                        } else {
                            System.out.println("Usuario o contraseña incorrectos.");
                            // Incrementar contador de intentos fallidos
                            sql = "UPDATE users SET failed_attempts = failed_attempts + 1, last_attempt = ? WHERE username = ?";
                            pstmt = conn.prepareStatement(sql);
                            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                            pstmt.setString(2, user);
                            pstmt.executeUpdate();
                            
                            // Verificar si se alcanzó el límite después de la actualización
                            sql = "SELECT failed_attempts FROM users WHERE username = ?";
                            pstmt = conn.prepareStatement(sql);
                            pstmt.setString(1, user);
                            ResultSet rsAttempts = pstmt.executeQuery();
                            if (rsAttempts.next()) {
                                int newAttempts = rsAttempts.getInt("failed_attempts");
                                if (newAttempts >= 3) {
                                    System.out.println("La cuenta ha sido bloqueada por 5 minutos, intentalo mas tarde.");
                                } else {
                                    System.out.println("Intento " + newAttempts + " de 3.");
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("Usuario no encontrado.");
                }
                
                System.out.println("[*] Volver a intentar? [s/n]: ");
                String respuesta = sc.nextLine();
                if (!respuesta.equalsIgnoreCase("s")) {
                    break;
                }
                
            } catch (SQLException ex) {
                System.err.println("Error al realizar la consulta: " + ex.getMessage());
            }
        }
        
        sc.close();
    }
}