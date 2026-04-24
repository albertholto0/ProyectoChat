import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ManejadorCliente implements Runnable {
    private Socket socketCliente;
    private String nombreCliente;
    private PrintWriter salida; // Canal de salida hacia ESTE cliente específico

    public ManejadorCliente(Socket socket) {
        this.socketCliente = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            salida = new PrintWriter(socketCliente.getOutputStream(), true);
            
            // 1. Registramos este canal de salida en la lista global del servidor
            Servidor.agregarEscritor(salida);

            // 2. Leemos el nombre y avisamos A TODOS
            nombreCliente = entrada.readLine();
            System.out.println(nombreCliente + " se ha conectado."); // Imprime en consola del server
            Servidor.broadcast("📢 El usuario '" + nombreCliente + "' ha entrado al chat.");

            // 3. Bucle para escuchar mensajes de este cliente y reenviarlos A TODOS
            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                Servidor.broadcast("[" + nombreCliente + "]: " + mensaje);
            }

        } catch (IOException e) {
            // Error de conexión, se maneja en el finally
        } finally {
            // Si el cliente se desconecta (Punto 8 del proyecto)
            if (salida != null) {
                Servidor.removerEscritor(salida); // Lo quitamos de la lista
            }
            try {
                socketCliente.close();
                System.out.println(nombreCliente + " se ha desconectado."); // Consola server
                if (nombreCliente != null) {
                    Servidor.broadcast("🚪 El usuario '" + nombreCliente + "' ha salido del chat.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}