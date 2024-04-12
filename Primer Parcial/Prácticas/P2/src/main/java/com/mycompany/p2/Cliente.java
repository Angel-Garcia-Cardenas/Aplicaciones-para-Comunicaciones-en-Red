package com.mycompany.p2;

import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente {
    private static final String RUTA_ARCHIVOS = "C:\\Users\\zivan\\Documents\\Escuela\\6toSemestre\\Redes\\Prac3\\Archivo";
    private static final int BUFFER_SIZE = 1024;
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private static final int WINDOW_SIZE = 5; // Tamaño de la ventana

    public static void main(String[] args) {
        mostrarMenu();
    }

    private static void mostrarMenu() {
        File directorio = new File(RUTA_ARCHIVOS);
        File[] archivos = directorio.listFiles();

        if (archivos == null || archivos.length == 0) {
            System.out.println("No hay archivos en la ruta especificada.");
            return;
        }

        System.out.println("Archivos disponibles:");
        for (int i = 0; i < archivos.length; i++) {
            System.out.println((i + 1) + ". " + archivos[i].getName());
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Seleccione el número del archivo que desea enviar: ");
        int opcion = scanner.nextInt();

        if (opcion < 1 || opcion > archivos.length) {
            System.out.println("Opción inválida. Seleccione un número válido.");
            return;
        }

        File archivoSeleccionado = archivos[opcion - 1];
        enviarArchivo(archivoSeleccionado);
    }

    private static void enviarArchivo(File archivo) {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);

            FileInputStream fileInputStream = new FileInputStream(archivo);
            byte[] fileData = new byte[BUFFER_SIZE];
            int bytesRead;

            int fileSize = (int) archivo.length();
            String fileSizeStr = String.valueOf(fileSize);
            DatagramPacket fileSizePacket = new DatagramPacket(fileSizeStr.getBytes(), fileSizeStr.getBytes().length, serverAddress, SERVER_PORT);
            clientSocket.send(fileSizePacket);

            int packetsSent = 0; // Contador para el tamaño de la ventana

            while ((bytesRead = fileInputStream.read(fileData)) != -1) {
                DatagramPacket packet = new DatagramPacket(fileData, bytesRead, serverAddress, SERVER_PORT);
                clientSocket.send(packet);
                packetsSent++; // Incrementar el contador de paquetes enviados

                // Si el tamaño de la ventana se alcanza, esperar ACKs
                if (packetsSent == WINDOW_SIZE) {
                    // Esperar ACKs
                    for (int i = 0; i < WINDOW_SIZE; i++) {
                        byte[] ackData = new byte[BUFFER_SIZE];
                        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length);
                        clientSocket.receive(ackPacket);
                        int ackNum = Integer.parseInt(new String(ackPacket.getData()).trim());
                    }
                    packetsSent = 0; // Reiniciar el contador de paquetes enviados
                }
            }

            System.out.println("Archivo enviado correctamente.");

            clientSocket.close();
            fileInputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error al enviar el archivo.");
        }
    }
}
