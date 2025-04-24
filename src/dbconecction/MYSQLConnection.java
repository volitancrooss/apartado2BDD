/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dbconecction;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author alumno
 */
public class MYSQLConnection {
    public static Connection getConnection() {
    
        Connection connect = null;
        String host = "jdbc:mysql://localhost/jardineria";
        String user = "root";
        String pass = "root";
        
        System.out.println("Conectando...");
        
        try {
            // Cargar driver de MYSQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Conectarnos a la db
            connect = DriverManager.getConnection(host, user, pass);
            System.out.println("Conexión esitosa a la base de datos.");
        } catch (ClassNotFoundException e) {
           System.err.println("Error al caragar driver JDBC: " + e.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(MYSQLConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return connect;
    }
}
