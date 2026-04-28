import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter; // Para guardar el archivo
import java.net.Socket;
import java.nio.file.Files; // Para leer el archivo
import java.nio.file.Paths;
import java.util.Base64; // Para codificar/decodificar
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
                        System.out.println(" - /enviar [usuario] [ruta] : Transfiere un archivo.");
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
                            if (mensajeServidor.startsWith("/archivo ")) {
                                String[] partes = mensajeServidor.split(" ", 4);
                                if (partes.length == 4) {
                                    String remitente = partes[1];
                                    String nombreArch = partes[2];
                                    String base64 = partes[3];
                                    
                                    try {
                                        // Decodificamos de Base64 a bytes puros
                                        byte[] archivoBytes = Base64.getDecoder().decode(base64);
                                        try (
                                                FileOutputStream fos = new FileOutputStream("RECIBIDO_" + nombreArch)) {
                                            fos.write(archivoBytes);
                                        }
                                        System.out.println("\n¡Archivo recibido de " + remitente + "! Guardado como: RECIBIDO_" + nombreArch);
                                    } catch (IOException e) {
                                        System.out.println("\n Error al guardar el archivo.");
                                    }
                                }
                                continue;
                            }
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
                    if (mensaje.startsWith("/enviar ")) {
                        String[] partes = mensaje.split(" ", 3);
                        if (partes.length == 3) {
                            String destino = partes[1];
                            String ruta = partes[2];
                            try {
                                
                                byte[] bytes = Files.readAllBytes(Paths.get(ruta));
                                
                                String base64 = Base64.getEncoder().encodeToString(bytes);
                                String nombreArch = Paths.get(ruta).getFileName().toString();
                                
                                salida.println("/file " + destino + " " + nombreArch + " " + base64);
                                System.out.println("Subiendo archivo al servidor...");
                            } catch (IOException e) {
                                System.out.println("No se pudo leer el archivo. Verifica la ruta.");
                            }
                        } else {
                            System.out.println("Uso correcto: /enviar [usuario] [ruta_absoluta]");
                        }
                        continue;
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