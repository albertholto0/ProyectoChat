import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        // IP DEL SERVIDOR
        String ipServidor = "172.25.3.39"; 
        int puerto = 5000; 
        Scanner teclado = new Scanner(System.in);

        try {
            Socket socket = new Socket(ipServidor, puerto);
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String miNombre = "";
            
            while (true) {
                System.out.print("Ingresa tu nombre de usuario para el chat: ");
                miNombre = teclado.nextLine();
                
                salida.println(miNombre); 
                
                String respuesta = entrada.readLine(); 
                
                if (respuesta != null && respuesta.equals("ACEPTADO")) {
                    System.out.println("¡Nombre aceptado! (Escribe 'salir' para desconectarte)\n");
                    break;
                } else {
                    System.out.println("Error: Ese usuario ya está conectado o el nombre es inválido. Intenta con otro.\n");
                }
            }

            Thread hiloLectura = new Thread(() -> {
                try {
                    String mensajeServidor;
                    while ((mensajeServidor = entrada.readLine()) != null) {
                        System.out.println(mensajeServidor);
                    }
                } catch (IOException e) {
                    System.out.println("\nConexión con el servidor terminada.");
                }
            });
            hiloLectura.start(); 
                        
            while (true) {
                String mensaje = teclado.nextLine();
                if (mensaje.equalsIgnoreCase("salir")) {
                    break; 
                }
                salida.println(mensaje); 
            }
            
            socket.close();
            teclado.close();
            System.exit(0); 
            
        } catch (IOException e) {
            System.out.println("Error: No se pudo conectar al servidor.");
            e.printStackTrace();
        }
    }
}