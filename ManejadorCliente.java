import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ManejadorCliente implements Runnable {
    private Socket socketCliente;
    private String nombreCliente;
    private PrintWriter salida; 

    public ManejadorCliente(Socket socket) {
        this.socketCliente = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            salida = new PrintWriter(socketCliente.getOutputStream(), true);
            
            while (true) {
                nombreCliente = entrada.readLine();
                if (nombreCliente == null) {
                    return;
                }
                
                
                if (Servidor.registrarNombre(nombreCliente)) {
                    salida.println("ACEPTADO"); 
                    break; 
                } else {
                    salida.println("RECHAZADO");
                }
            }

            
            Servidor.agregarEscritor(salida);
            System.out.println(nombreCliente + " se ha conectado."); 
            Servidor.broadcast("---> El usuario '" + nombreCliente + "' ha entrado al chat.");

            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                Servidor.broadcast("[" + nombreCliente + "]: " + mensaje);
            }

        } catch (IOException e) {
        } finally {
            if (salida != null) {
                Servidor.removerEscritor(salida); 
            }
            try {
                socketCliente.close();
                if (nombreCliente != null) {
                    
                    Servidor.removerNombre(nombreCliente); 
                    System.out.println(nombreCliente + " se ha desconectado."); 
                    Servidor.broadcast("<--- El usuario '" + nombreCliente + "' ha salido del chat.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}