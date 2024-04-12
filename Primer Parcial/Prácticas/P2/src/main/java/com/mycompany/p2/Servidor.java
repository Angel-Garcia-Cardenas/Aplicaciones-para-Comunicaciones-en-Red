package com.mycompany.p2;

import java.io.*;
import java.net.*;

public class Servidor {
    private static final int BUFFER_SIZE = 1024;
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private static final int WINDOW_SIZE = 5; // Tamaño de la ventana deslizante

    public static void main(String[] args) {
        try {
            DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT);
            byte[] receiveData = new byte[BUFFER_SIZE];

            System.out.println("Servidor esperando conexiones...");

            DatagramPacket fileSizePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(fileSizePacket);
            int fileSize = Integer.parseInt(new String(fileSizePacket.getData()).trim());

            FileOutputStream fileOutputStream = new FileOutputStream("archivo_recibido.pdf");

            int expectedSeqNum = 0;
            int packetsReceived = 0;

            while (expectedSeqNum < fileSize) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                int seqNum = expectedSeqNum;

                fileOutputStream.write(receivePacket.getData(), 0, receivePacket.getLength());

                String ackStr = String.valueOf(seqNum);
                byte[] ackData = ackStr.getBytes();
                DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, receivePacket.getAddress(), receivePacket.getPort());
                serverSocket.send(ackPacket);

                expectedSeqNum += receivePacket.getLength();
                packetsReceived++;

                if (packetsReceived == WINDOW_SIZE || expectedSeqNum >= fileSize) {
                    // Enviar ACKs
                    for (int i = 0; i < packetsReceived; i++) {
                        ackStr = String.valueOf(seqNum);
                        ackData = ackStr.getBytes();
                        ackPacket = new DatagramPacket(ackData, ackData.length, receivePacket.getAddress(), receivePacket.getPort());
                        serverSocket.send(ackPacket);
                        seqNum += BUFFER_SIZE;
                    }
                    packetsReceived = 0;
                }
            }

            System.out.println("Archivo recibido y guardado como 'archivo_recibido.pdf'.");

            serverSocket.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
