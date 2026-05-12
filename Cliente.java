import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter; // Para guardar el archivo
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths; // Para leer el archivo
import java.util.Base64;
import java.util.List;
import java.util.Scanner; // Para codificar/decodificar

public class Cliente {

    private static String ipPrincipal, ipRespaldo;
    private static int puertoPrincipal, puertoRespaldo;

    private static String miNombre;
    private static Socket socketActual;
    private static PrintWriter salida;
    private static BufferedReader entrada;

    private static volatile boolean conectado = false;
    private static volatile boolean enServidorPrincipal = false;

   public static void main(String[] args) {
        if (!cargarConfiguracion()) {
            System.out.println("Error: No se pudo cargar servidores.txt o el formato es incorrecto.");
            return;
        }

        try (Scanner teclado = new Scanner(System.in)) {
            System.out.print("Ingresa tu nombre de usuario: ");
            miNombre = teclado.nextLine();

            System.out.println("Iniciando sistema de Alta Disponibilidad...");

            Thread gestorConexion = new Thread(Cliente::gestionarConexion);
            gestorConexion.start();

            while (true) {
                String mensaje = teclado.nextLine();
                
                if (mensaje.equalsIgnoreCase("salir")) System.exit(0);

                if (!conectado || salida == null) {
                    System.out.println("[!] Reconectando... por favor espera.");
                    continue;
                }

                if (mensaje.startsWith("/enviar ")) {
                    procesarEnvioArchivo(mensaje);
                    continue;
                }

                salida.println(mensaje);
            }
        }
    }

    private static boolean cargarConfiguracion() {
        try {
            List<String> lineas = Files.readAllLines(Paths.get("servidores.txt"));
            if (lineas.size() < 2) return false;

            String[] p1 = lineas.get(0).split(":");
            ipPrincipal = p1[0];
            puertoPrincipal = Integer.parseInt(p1[1]);

            String[] p2 = lineas.get(1).split(":");
            ipRespaldo = p2[0];
            puertoRespaldo = Integer.parseInt(p2[1]);

            System.out.println("Configuración cargada:");
            System.out.println("  Principal: " + ipPrincipal + ":" + puertoPrincipal);
            System.out.println("  Respaldo:  " + ipRespaldo + ":" + puertoRespaldo + "\n");
            return true;
        } catch (IOException | NumberFormatException e) {
            return false;
        }
    }

    private static void gestionarConexion() {
        while (true) {
            if (!conectado) {
                intentarConexion(ipPrincipal, puertoPrincipal, true);

                if (!conectado) {
                    intentarConexion(ipRespaldo, puertoRespaldo, false);
                }
            } else if (!enServidorPrincipal) {
                try {
                    Thread.sleep(5000); // Check cada 5 seg
                    try (Socket check = new Socket(ipPrincipal, puertoPrincipal)) {
                        System.out.println("\n[!] SERVIDOR PRINCIPAL DETECTADO. Regresando...");
                        if (socketActual != null) socketActual.close();
                        conectado = false;
                    }
                } catch (Exception e) { /* Sigue caído */ }
            }
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        }
    }

    private static void intentarConexion(String ip, int puerto, boolean esPrincipal) {
        try {
            socketActual = new Socket(ip, puerto);
            salida = new PrintWriter(socketActual.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socketActual.getInputStream()));

            salida.println(miNombre); // Autoregistro
            if ("ACEPTADO".equals(entrada.readLine())) {
                conectado = true;
                enServidorPrincipal = esPrincipal;
                System.out.println("\n>>> CONECTADO A: " + (esPrincipal ? "PRINCIPAL" : "RESPALDO"));
                new Thread(Cliente::escucharServidor).start();
            }
        } catch (IOException e) {
            conectado = false;
        }
    }

    private static void escucharServidor() {
        try {
            String msg;
            while ((msg = entrada.readLine()) != null) {
                if (msg.startsWith("/archivo ")) {
                    recibirArchivo(msg);
                } else {
                    System.out.println(msg);
                }
            }
        } catch (IOException e) { 
            // Salto aquí cuando se cae el servidor
        } finally {
            conectado = false;
        }
    }

    private static void procesarEnvioArchivo(String mensaje) {
        String[] partes = mensaje.split(" ", 3);
        if (partes.length == 3) {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(partes[2]));
                String base64 = Base64.getEncoder().encodeToString(bytes);
                String nombre = Paths.get(partes[2]).getFileName().toString();
                salida.println("/file " + partes[1] + " " + nombre + " " + base64);
                System.out.println("Subiendo archivo...");
            } catch (IOException e) { System.out.println("Error al leer archivo."); }
        }
    }

    private static void recibirArchivo(String msg) {
        String[] partes = msg.split(" ", 4);
        try {
            byte[] bytes = Base64.getDecoder().decode(partes[3]);
            try (FileOutputStream fos = new FileOutputStream("RECIBIDO_" + partes[2])) {
                fos.write(bytes);
            }
            System.out.println("\n[!] Archivo recibido de " + partes[1] + " como: RECIBIDO_" + partes[2]);
        } catch (IOException e) { System.out.println("Error al guardar archivo."); }
    }
}