/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sources;

import java.net.*;
import java.io.*;
import java.util.*;

public class Cliente {
    private static final int PORT = 8000;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int WINDOW_SIZE = 4;
    private static final int PACKET_SIZE = 1024;

    public static void main(String[] args) {
        String filePath = "C:\\Users\\zivan\\Documents\\Escuela\\6toSemestre\\Redes\\Prac3\\Archivo\\Inicial3D.pdf";
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("El archivo no existe.");
            return;
        }

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000); // Esperar 5 segundos
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            sendFile(socket, serverAddress, PORT, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   private static void sendFile(DatagramSocket socket, InetAddress serverAddress, int port, File file) throws IOException {
    FileInputStream fis = new FileInputStream(file);
    int totalPackets = (int) Math.ceil((double) file.length() / PACKET_SIZE);
    List<byte[]> packets = new ArrayList<>();

    byte[] buffer = new byte[PACKET_SIZE];
    int bytesRead;

    while ((bytesRead = fis.read(buffer)) != -1) {
        packets.add(Arrays.copyOf(buffer, bytesRead));
    }
    fis.close();

    int firstUnacknowledged = 0;
    int nextPacketToSend = 0;

    while (firstUnacknowledged < totalPackets) {
        while (nextPacketToSend < totalPackets && nextPacketToSend - firstUnacknowledged < WINDOW_SIZE) {
            System.out.println("Preparando para enviar paquetes: " + nextPacketToSend);
            sendPacket(socket, serverAddress, port, nextPacketToSend, packets.get(nextPacketToSend), file.getName(), totalPackets);
            nextPacketToSend++;
        }

        try {
            while (true) {
                int ack = receiveACK(socket);
                System.out.println("Procesando paquete ACK numero: " + ack);
                if (ack >= firstUnacknowledged) {
                    firstUnacknowledged = ack + 1;
                }
                if (firstUnacknowledged == totalPackets) {
                    System.out.println("Todos los paquetes se han recibido.");
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Tiempo fuera, re enviando paquete " + firstUnacknowledged);
            nextPacketToSend = firstUnacknowledged; // Resetear el paquete enviando el ultimo paquete confirmado
        }
    }
}

    private static void sendPacket(DatagramSocket socket, InetAddress address, int port, int packetNumber, byte[] data, String fileName, int totalPackets) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(new Dato(packetNumber, data, fileName, totalPackets));
        oos.flush();
        byte[] serializedData = baos.toByteArray();
        DatagramPacket packet = new DatagramPacket(serializedData, serializedData.length, address, port);
        socket.send(packet);
        System.out.println("Paquete enviado: " + packetNumber + " de " + totalPackets);
    }

    private static int receiveACK(DatagramSocket socket) throws IOException {
    byte[] buffer = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    socket.receive(packet);
    String ackString = new String(packet.getData(), 0, packet.getLength()).trim();
    System.out.println("Recibido: " + ackString); // Debug output

    if (ackString.startsWith("ACK ")) {
        int ackNum = Integer.parseInt(ackString.substring(4));
        System.out.println("ACK recibido para el paquete: " + ackNum); // Confirmar recibo de ACK 
        return ackNum;
    }
    System.err.println("ACK malformado recibido: " + ackString); // Manejo de ACK malformado
    return -1; // Manejo de errores
}

}
