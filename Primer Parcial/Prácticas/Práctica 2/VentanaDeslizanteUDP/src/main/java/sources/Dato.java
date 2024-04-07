/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sources;

import java.io.*;

/**
 *
 * @author garci
 */
public class Dato implements Serializable {
    private static final long serialVersionUID = 1L;
    private int numberPackage;
    private byte[] data;
    private String fileName;
    private int totalPackage;

    public Dato(int numberPackage, byte[] data, String fileName) {
        this.numberPackage = numberPackage;
        this.data = data;
        this.fileName = fileName;
        this.totalPackage = totalPackage;
    }

    public int getNumberPackage() {
        return numberPackage;
    }

    public byte[] getData() {
        return data;
    }

    public String getFileName() {
        return fileName;
    }
    
    public int getTotalPackage() {
        return totalPackage;
    }
}
