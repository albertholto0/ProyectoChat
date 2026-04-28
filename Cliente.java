import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        String ipServidor = "172.25.3.38"; 
        int puerto = 5000; 
        Scanner teclado = new Scanner(System.in);

        try {
            try (Socket socket = new Socket(ipServidor, puerto)) {
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
                
                String miNombre;
                
                while (true) {
                    System.out.print("Ingresa tu nombre de usuario para el chat: ");
                    miNombre = teclado.nextLine();
                    salida.println(miNombre);
                    String respuesta = entrada.readLine();
                    
                    if (respuesta != null && respuesta.equals("ACEPTADO")) {
                        System.out.println("\n¡Nombre aceptado! ------------------------");
                        System.out.println(" COMANDOS:");
                        System.out.println(" - Escribe normal para hablar con todos.");
                        System.out.println(" - /msg [usuario] : Inicia chat privado.");
                        System.out.println(" - /global        : Vuelve al chat global.");
                        System.out.println(" - salir          : Desconectarse.");
                        System.out.println("------------------------------------------\n");
                        break;
                    } else {
                        System.out.println("Error: Usuario conectado o inválido. Intenta otro.\n");
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
            }
            teclado.close();
            System.exit(0); 
            
        } catch (IOException e) {
            System.out.println("Error: No se pudo conectar al servidor.");
        }
    }
}