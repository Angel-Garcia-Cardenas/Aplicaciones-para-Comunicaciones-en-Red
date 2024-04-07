/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Servidor;

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
        public static void main(String args[]) {
            try{
                DatagramSocket s = new DatagramSocket(1234);
                System.out.println("Servidor iniciado, experando cliente ...");
                s.setReuseAddress(true);
                
                for(;;){
                    DatagramPacket p = new DatagramPacket(new byte[65535],65535);
                    s.receive(p);
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(p.getData()));
                    
                    //Objeto o1 = (Objeto)ois.readObject();
                }//for
            }//try
            catch(Exception e){
                e.printStackTrace();
            }//catch
    }//main
}//class Servidor
