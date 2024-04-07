/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Servidor;

import java.net.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;

/**
 *
 * @author Ángel Alberto García Cárdenas
 */
public class Servidor {
    
    private static final int WINDOW_SIZE = 4;
    private static final int TIMEOUT_MS = 1000;
    
        public static void main(String args[]) {
            try{
                DatagramSocket s = new DatagramSocket(1234);
                System.out.println("Servidor iniciado, experando cliente ...");
                s.setReuseAddress(true);
                
                FileOutputStream fileOutputStream = null;
                int expectedSequenceNumber = 0;
                int lastAcknowledgedSequenceNumber = -1;
    
                for(;;){
                    DatagramPacket p = new DatagramPacket(new byte[65535],65535);
                    s.receive(p);
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(p.getData()));
                    
                    Dato objetoRecibido = (Dato)ois.readObject();
                    
                    System.out.println("Paquete recibido: " + objetoRecibido.getNumberPackage());
                    
                    if (objetoRecibido.getNumberPackage() == expectedSequenceNumber) {
                        if(fileOutputStream == null){
                            fileOutputStream = new FileOutputStream(objetoRecibido.getFileName());
                            System.out.println("Guardando archivo en: " + objetoRecibido.getFileName());
                        }// if existe archivo
                    
                    fileOutputStream.write(objetoRecibido.getData());
                    System.out.println("Enviando: " + objetoRecibido.getNumberPackage() * 100 / objetoRecibido.getTotalPackage() + "%");
                    enviarConfirmacion(s, p.getAddress(),p.getPort(), expectedSequenceNumber);
                    lastAcknowledgedSequenceNumber = expectedSequenceNumber;

                    expectedSequenceNumber = (expectedSequenceNumber + 1) % WINDOW_SIZE;
                    
                        for(;;){
                            p = new DatagramPacket(new byte[65535], 65535);
                            s.setSoTimeout(TIMEOUT_MS);
                            try {
                                s.receive(p);
                                ois = new ObjectInputStream(new ByteArrayInputStream(p.getData()));
                                objetoRecibido = (Dato) ois.readObject();
                                if( objetoRecibido.getNumberPackage() == expectedSequenceNumber){
                                    fileOutputStream.write(objetoRecibido.getData());
                                    System.out.println("Enviando: " + objetoRecibido.getNumberPackage() * 100 / objetoRecibido.getTotalPackage() + "%");
                                    enviarConfirmacion(s, p.getAddress(), p.getPort(), expectedSequenceNumber);
                                    lastAcknowledgedSequenceNumber = expectedSequenceNumber;

                                    expectedSequenceNumber = (expectedSequenceNumber + 1) % WINDOW_SIZE;
                                }//if
                                else{
                                    enviarConfirmacion(s, p.getAddress(), p.getPort(), lastAcknowledgedSequenceNumber);
                                }//else
                                
                            } //try
                            catch (SocketTimeoutException e){
                                
                                break;
                            }//catch
                            
                        } //for
                        
                    } //if
                    else {
                    enviarConfirmacion(s, p.getAddress(), p.getPort(), lastAcknowledgedSequenceNumber);
                    }// else
                }//for
            }//try
            catch(Exception e){
                e.printStackTrace();
            }//catch
    }//main
        
    private static void enviarConfirmacion(DatagramSocket socket, InetAddress address, int port, int sequenceNumber) {
        try {
            String response = "ACK " + sequenceNumber;
            byte[] aux = response.getBytes();
            DatagramPacket p = new DatagramPacket(aux, aux.length, address, port);
            socket.send(p);
        } //try
        catch (IOException e) {
            e.printStackTrace();
        }//catch
    } //Void enviar confirmación
   
    
}//class Servidor
