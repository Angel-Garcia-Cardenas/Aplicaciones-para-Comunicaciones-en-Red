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
        String filePath = "C:\\Users\\zivan\\Documents\\Escuela\\6toSemestre\\Redes\\Prac3\\Archivo\\Redes SDN y SAN.pdf";
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("El archivo no existe.");
            return;
        }

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000); // Wait for an ACK for at most 5 seconds
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
                sendPacket(socket, serverAddress, port, nextPacketToSend, packets.get(nextPacketToSend), file.getName(), totalPackets);
                nextPacketToSend++;
            }

            try {
                int ack = receiveACK(socket);
                if (ack >= firstUnacknowledged) {
                    firstUnacknowledged = ack + 1;
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout, reenviando desde el paquete " + firstUnacknowledged);
                nextPacketToSend = firstUnacknowledged; // Restart sending from the last unacknowledged packet
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
        if (ackString.startsWith("ACK ")) {
            return Integer.parseInt(ackString.substring(4));
        }
        return -1; // Should handle error scenario
    }
}
