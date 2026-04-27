import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Servidor {
    private static Map<String, PrintWriter> clientes = new HashMap<>();

    public static void main(String[] args) {
        int puerto = 5000; 

        try {
            ServerSocket servidor = new ServerSocket(puerto);
            System.out.println("--- SERVIDOR DE CHAT MULTIHILO INICIADO ---");
            System.out.println("Escuchando en el puerto " + puerto + "...\n");
            
            while (true) {
                Socket socketCliente = servidor.accept(); 
                ManejadorCliente manejador = new ManejadorCliente(socketCliente);
                Thread hilo = new Thread(manejador);
                hilo.start(); 
            }
            
        } catch (IOException e) {
            System.out.println("Error crítico en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized boolean registrarCliente(String nombre, PrintWriter escritor) {
        if (nombre == null || nombre.isBlank() || clientes.containsKey(nombre)) {
            return false; 
        }
        clientes.put(nombre, escritor); 
        return true; 
    }

    public static synchronized void removerCliente(String nombre) {
        if (nombre != null) {
            clientes.remove(nombre);
        }
    }

    public static synchronized void broadcast(String mensaje) {
        for (PrintWriter escritor : clientes.values()) {
            escritor.println(mensaje);
        }
    }

    public static synchronized boolean enviarPrivado(String remitente, String destinatario, String mensaje) {
        // Buscamos el canal de salida del destinatario en nuestro diccionario
        PrintWriter escritorDestino = clientes.get(destinatario);
        
        if (escritorDestino != null) {
            // Si existe, le enviamos el mensaje solo a él
            escritorDestino.println("(Privado de " + remitente + "): " + mensaje);
            return true; // Éxito
        }
        return false; // El usuario no existe o se desconectó
    }
}