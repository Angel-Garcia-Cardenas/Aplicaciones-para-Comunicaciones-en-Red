/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sources;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 *
 * @author Ángel Alberto García Cárdenas
 */
public class Servidor {
    
    private static final int WINDOW_SIZE = 4;
    private static final int TIMEOUT_MS = 1000;
    private static String RutaCarpetaDestino;
    
        public static void main(String args[]) {
            try{
                DatagramSocket s = new DatagramSocket(8000);
                System.out.println("Servidor iniciado, experando cliente ...");
                s.setReuseAddress(true);
                
                FileOutputStream fileOutputStream = null;
                int paqueteEsperado = 0;
                int ultimoConfirmado = -1;
                
                RutaCarpetaDestino = "./AlojaEnvio";
    
                for(;;){
                    DatagramPacket p = new DatagramPacket(new byte[65535],65535);
                    s.receive(p);
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(p.getData()));
                    
                    Dato objetoRecibido = (Dato)ois.readObject();
                    
                    //System.out.println("Paquete recibido: " + objetoRecibido.getNumberPackage());
                    
                    if (objetoRecibido.getNumberPackage() == paqueteEsperado) {
                        if(fileOutputStream == null){
                            fileOutputStream = new FileOutputStream(RutaCarpetaDestino + "/" + objetoRecibido.getFileName());
                            System.out.println("Guardando archivo en: " + RutaCarpetaDestino + "/" + objetoRecibido.getFileName());
                        }// if ya existe archivo
                    
                        fileOutputStream.write(objetoRecibido.getData());
                        System.out.println("Enviando: " + objetoRecibido.getNumberPackage() * 100 / objetoRecibido.getTotalPackage() + "%");
                        enviarConfirmacion(s, p.getAddress(),p.getPort(), paqueteEsperado);
                        ultimoConfirmado = paqueteEsperado;

                        paqueteEsperado = (paqueteEsperado + 1) % WINDOW_SIZE;
                    
                        for(;;){
                            p = new DatagramPacket(new byte[65535], 65535);
                            s.setSoTimeout(TIMEOUT_MS);
                            try {
                                s.receive(p);
                                ois = new ObjectInputStream(new ByteArrayInputStream(p.getData()));
                                objetoRecibido = (Dato) ois.readObject();
                                if( objetoRecibido.getNumberPackage() == paqueteEsperado){
                                    fileOutputStream.write(objetoRecibido.getData());
                                    System.out.println("Enviando: " + objetoRecibido.getNumberPackage() * 100 / objetoRecibido.getTotalPackage() + "%");
                                    enviarConfirmacion(s, p.getAddress(), p.getPort(), paqueteEsperado);
                                    ultimoConfirmado = paqueteEsperado;

                                    paqueteEsperado = (paqueteEsperado + 1) % WINDOW_SIZE;
                                }//if
                                else{
                                    enviarConfirmacion(s, p.getAddress(), p.getPort(), ultimoConfirmado);
                                }//else
                                
                            } //try
                            catch (SocketTimeoutException e){
                                
                                break;
                            }//catch
                            
                        } //for
                        
                    } //if paquete recibido es el paquete esperado
                    else {
                        enviarConfirmacion(s, p.getAddress(), p.getPort(), ultimoConfirmado);
                    }// else
                    
                    if (objetoRecibido.getNumberPackage() == objetoRecibido.getTotalPackage() - 1) {
                        System.out.println("¡Archivo recibido completamente!");
                        break;
                    } //if
                    
                }//for
                fileOutputStream.close();
            }//try
            catch(Exception e){
                e.printStackTrace();
            }//catch
    }//main
        
    private static void enviarConfirmacion(DatagramSocket socket, InetAddress address, int port, int paqueteSiguiente) {
        try {
            String response = "ACK " + paqueteSiguiente;
            byte[] aux = response.getBytes();
            DatagramPacket p = new DatagramPacket(aux, aux.length, address, port);
            socket.send(p);
        } //try
        catch (IOException e) {
            e.printStackTrace();
        }//catch
    } //Void enviar confirmación
   
    
}//class Servidor
