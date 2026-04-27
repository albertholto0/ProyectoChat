import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ManejadorCliente implements Runnable {
    private Socket socketCliente;
    private String nombreCliente;
    private PrintWriter salida; 
    
    // NUEVO: Variables de estado para saber en qué modo está escribiendo el usuario
    private boolean enModoPrivado = false;
    private String destinatarioPrivado = "";

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
                if (nombreCliente == null) return;
                
                if (Servidor.registrarCliente(nombreCliente, salida)) {
                    salida.println("ACEPTADO"); 
                    break; 
                } else {
                    salida.println("RECHAZADO");
                }
            }

            System.out.println(nombreCliente + " se ha conectado."); 
            Servidor.broadcast("El usuario '" + nombreCliente + "' ha entrado al chat.");

            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                
                
                if (mensaje.startsWith("/msg ")) {
                    String[] partes = mensaje.split(" ", 2); 
                    if (partes.length == 2) {
                        destinatarioPrivado = partes[1].trim();
                        enModoPrivado = true; // Entramos en modo privado
                        salida.println("Has entrado al chat privado con: " + destinatarioPrivado);
                    } else {
                        salida.println("Uso incorrecto. Intenta: /msg usuario");
                    }
                } 
                else if (mensaje.equalsIgnoreCase("/global")) {
                    enModoPrivado = false; // Salimos del modo privado
                    destinatarioPrivado = "";
                    salida.println("Has vuelto al chat global.");
                } 
                else {
                    if (enModoPrivado) {
                        boolean exito = Servidor.enviarPrivado(nombreCliente, destinatarioPrivado, mensaje);
                        if (exito) {
                            // Te imprimimos el mensaje a ti mismo para que sepas qué enviaste
                            salida.println("(Para " + destinatarioPrivado + "): " + mensaje);
                        } else {
                            salida.println("Error: El usuario '" + destinatarioPrivado + "' no existe o se fue.");
                            enModoPrivado = false; // Lo regresamos al global por seguridad
                            salida.println("Has vuelto al chat global automáticamente.");
                        }
                    } else {
                        // Si no estamos en privado, es un mensaje global normal
                        Servidor.broadcast("[" + nombreCliente + "]: " + mensaje);
                    }
                }
            }

        } catch (IOException e) {
        } finally {
            try {
                socketCliente.close();
                if (nombreCliente != null) {
                    Servidor.removerCliente(nombreCliente); 
                    System.out.println(nombreCliente + " se ha desconectado."); 
                    Servidor.broadcast("🚪 El usuario '" + nombreCliente + "' ha salido del chat.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}