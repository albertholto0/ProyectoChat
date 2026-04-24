import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        String ipServidor = "172.25.3.39";
        int puerto = 5000; 
        Scanner teclado = new Scanner(System.in);

        try {
            System.out.print("Ingresa tu nombre de usuario para el chat: ");
            String miNombre = teclado.nextLine();

            Socket socket = new Socket(ipServidor, puerto);
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // --- EL NUEVO HILO DE LECTURA ---
            // Este hilo correrá de fondo escuchando los mensajes que reenvía el servidor
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
            hiloLectura.start(); // Arrancamos el hilo "oyente"

            // --- EL HILO PRINCIPAL (Escritura) ---
            salida.println(miNombre); // Enviamos el nombre
            System.out.println("¡Conectado! (Escribe 'salir' para desconectarte)");

            while (true) {
                String mensaje = teclado.nextLine();
                if (mensaje.equalsIgnoreCase("salir")) {
                    break; 
                }
                salida.println(mensaje); // Enviamos lo que escribimos
            }
            
            socket.close();
            teclado.close();
            System.exit(0); // Forzamos el cierre completo del cliente
            
        } catch (IOException e) {
            System.out.println("Error: No se pudo conectar al servidor.");
            e.printStackTrace();
        }
    }
}