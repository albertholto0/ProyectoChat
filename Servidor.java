import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Servidor {
    private static Set<PrintWriter> escritores = new HashSet<>();
    private static Set<String> nombresUsuarios = new HashSet<>();

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

    public static synchronized void broadcast(String mensaje) {
        for (PrintWriter escritor : escritores) {
            escritor.println(mensaje);
        }
    }

    public static synchronized void agregarEscritor(PrintWriter escritor) {
        escritores.add(escritor);
    }

    public static synchronized void removerEscritor(PrintWriter escritor) {
        escritores.remove(escritor);
    }

    public static synchronized boolean registrarNombre(String nombre) {
        if (nombresUsuarios.contains(nombre) || nombre.isBlank()) {
            return false; 
        }
        nombresUsuarios.add(nombre);
        return true; 
    }

    public static synchronized void removerNombre(String nombre) {
        if (nombre != null) {
            nombresUsuarios.remove(nombre);
        }
    }
}