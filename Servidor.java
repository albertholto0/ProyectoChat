import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

// 172.25.3.39

public class Servidor {
    // Esta es nuestra "libreta de direcciones". Guarda los canales de salida de cada cliente.
    private static Set<PrintWriter> escritores = new HashSet<>();

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

    // --- NUEVOS MÉTODOS PARA BROADCASTING ---

    // Este método toma un mensaje y lo dispara a todos los clientes en la libreta
    public static synchronized void broadcast(String mensaje) {
        for (PrintWriter escritor : escritores) {
            escritor.println(mensaje);
        }
    }

    // Registra a un nuevo cliente
    public static synchronized void agregarEscritor(PrintWriter escritor) {
        escritores.add(escritor);
    }

    // Elimina a un cliente cuando se va
    public static synchronized void removerEscritor(PrintWriter escritor) {
        escritores.remove(escritor);
    }
}