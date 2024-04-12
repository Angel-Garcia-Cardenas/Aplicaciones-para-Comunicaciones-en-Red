/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sources;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Servidor {
    private static final int PORT = 8000;
    private static final int TIMEOUT_MS = 5000;
    private static String RutaCarpetaDestino = "./Recibido";
    private static boolean[] receivedPackets; //Comentar para error

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Servidor iniciado, esperando cliente...");
            socket.setReuseAddress(true);
            FileOutputStream fos = null;
            // boolean[] receivedPackets = new boolean [34]; 

            while (true) {
                DatagramPacket packet = new DatagramPacket(new byte[65535], 65535);
                socket.receive(packet);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(packet.getData()));
                Dato receivedData = (Dato)ois.readObject();

                   //Inicializar la variable recevebPackets basandose en el total de paquetes
                if (receivedPackets == null) {
                    receivedPackets = new boolean[receivedData.getTotalPackage()];
                }

                int packetNumber = receivedData.getNumberPackage();
                if (fos == null) {
                    fos = new FileOutputStream(RutaCarpetaDestino + "/" + receivedData.getFileName());
                    System.out.println("Guardando archivo en: " + RutaCarpetaDestino + "/" + receivedData.getFileName());
                }

                if (!receivedPackets[packetNumber]) {
                    fos.write(receivedData.getData());
                    receivedPackets[packetNumber] = true;
                    System.out.println("Procesando paquete: " + packetNumber);
                    enviarConfirmacion(socket, packet.getAddress(), packet.getPort(), packetNumber);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void enviarConfirmacion(DatagramSocket socket, InetAddress address, int port, int packetNumber) {
        try {
            String response = "ACK " + packetNumber;
            byte[] responseBytes = response.getBytes();
            DatagramPacket ackPacket = new DatagramPacket(responseBytes, responseBytes.length, address, port);
            socket.send(ackPacket);
            System.out.println("Enviando ACK para paquete: " + packetNumber);
        } catch (IOException e) {
            System.out.println("Error al enviar ACK: " + e.getMessage());
        }
    }
}
