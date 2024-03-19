package Cliente;

import Servidor.CarpetaObjeto;
import java.net.Socket;
import java.io.*;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Cliente {
    private Socket cl;
    private DataOutputStream dos;
    private DataInputStream dis;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private String rutaCarpetaLocal;
    private String rutaCarpetaRemota;

    public Cliente(String dir, int pto) {
        try {
            cl = new Socket(dir, pto);
            System.out.println("Conexión con servidor establecida...");

            dos = new DataOutputStream(cl.getOutputStream());
            dis = new DataInputStream(cl.getInputStream());
            oos = new ObjectOutputStream(cl.getOutputStream());
            ois = new ObjectInputStream(cl.getInputStream());

            rutaCarpetaLocal = "./CarpetaLocal";
            rutaCarpetaRemota = "./CarpetaRemota";

            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getRutaCarpetaLocal() {
    return rutaCarpetaLocal;
    }

    public void listarContenidoLocal() {
        File localFolder = new File(rutaCarpetaLocal);
        String[] localContents = localFolder.list();
        for (String item : localContents) {
            System.out.println(item);
        }
    }

    public String[] listarContenidoRemoto() {
        try {
            dos.writeInt(1);
            dos.flush();

            System.out.println("Contenido de la carpeta remota:");

            CarpetaObjeto carpetaRecibida = (CarpetaObjeto) ois.readObject();
            String[] lista = carpetaRecibida.getLista();
            return lista; 
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new String[0]; 
    }

    public void crearCarpetaLocal() {
            String nombreCarpeta = JOptionPane.showInputDialog("Ingrese el nombre de la carpeta:");
            if (nombreCarpeta != null && !nombreCarpeta.isEmpty()) {
                File newFolder = new File(rutaCarpetaLocal + "/" + nombreCarpeta);
                if (newFolder.mkdir()) {
                    System.out.println("Carpeta creada exitosamente.");
                } else {
                    System.out.println("Error al crear la carpeta.");
                }
            } else {
                System.out.println("Nombre de carpeta inválido.");
            }
    }

    public void crearCarpetaRemota() {
        String nombreCarpetaRemota = JOptionPane.showInputDialog("Ingrese el nombre de la carpeta remota:");
        if (nombreCarpetaRemota != null && !nombreCarpetaRemota.isEmpty()) {
            try {
                dos.writeInt(2);
                dos.flush();

                dos.writeUTF(nombreCarpetaRemota);
                dos.flush();

                String mensajeCreacion = dis.readUTF();
                System.out.println(mensajeCreacion);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Nombre de carpeta remota inválido.");
        }
    }

    public void eliminarCarpetaLocal(String carpetaArchivoEliminar) {
        File itemEliminar = new File(rutaCarpetaLocal + "/" + carpetaArchivoEliminar);
        if (itemEliminar.exists()) {
            if (itemEliminar.isDirectory()) {
                if (itemEliminar.delete()) {
                    System.out.println("Carpeta eliminada exitosamente.");
                } else {
                    System.out.println("Error al eliminar la carpeta.");
                }
            } else {
                if (itemEliminar.delete()) {
                    System.out.println("Archivo eliminado exitosamente.");
                } else {
                    System.out.println("Error al eliminar el archivo.");
                }
            }
        } else {
            System.out.println("El archivo/carpeta no existe.");
        }
    }

    public void eliminarCarpetaRemota(String carpetaArchivoEliminarRemoto) {
        try {
            dos.writeInt(3);
            dos.flush();

            dos.writeUTF(carpetaArchivoEliminarRemoto);
            dos.flush();

            String mensajeEliminacion = dis.readUTF();
            System.out.println(mensajeEliminacion);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cambiarDirectorioLocal() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int seleccion = fileChooser.showOpenDialog(null);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            rutaCarpetaLocal = selectedFolder.getAbsolutePath();
            System.setProperty("user.dir", rutaCarpetaLocal);
            System.out.println("Ruta cambiada exitosamente.");
        }
    }

    public void cambiarDirectorioRemoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int seleccion = fileChooser.showOpenDialog(null);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            String nuevaRutaRemota = selectedFolder.getAbsolutePath();
            try {
                dos.writeInt(4);
                dos.flush();

                dos.writeUTF(nuevaRutaRemota);
                dos.flush();
                rutaCarpetaRemota = selectedFolder.getAbsolutePath();

                String mensajeCambioRuta = dis.readUTF();
                System.out.println(mensajeCambioRuta);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public String getRutaCarpetaRemota() {
        return rutaCarpetaRemota;
    }


    public void enviarArchivoRemoto(String rutaArchivo) {
        try {
            dos.writeInt(5);
            dos.flush();

            File archivoEnviar = new File(rutaArchivo);
            if (archivoEnviar.exists()) {
                String nombre = archivoEnviar.getName();
                long tam = archivoEnviar.length();
                System.out.println("Preparándose para enviar archivo " + nombre + " de " + tam + " bytes\n\n");
                dos.writeUTF(nombre);
                dos.flush();
                dos.writeLong(tam);
                dos.flush();
                try (FileInputStream fis = new FileInputStream(archivoEnviar)) {
                    byte[] buffer = new byte[3500];
                    int leidos;
                    while ((leidos = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, leidos);
                        dos.flush();
                    }
                    System.out.println("\nArchivo enviado.");
                } catch (IOException e) {
                    System.err.println("Error al enviar el archivo: " + e.getMessage());
                }
            } else {
                System.out.println("El archivo no existe.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarCarpetaRemota(String rutaCarpetaEnviar) {
        try {
            dos.writeInt(11); // Envía el código de operación al servidor
            dos.flush();

            File carpetaEnviar = new File(rutaCarpetaEnviar);
            if (carpetaEnviar.exists() && carpetaEnviar.isDirectory()) {
                String nombreCarpeta = carpetaEnviar.getName();
                File carpetaComprimida = new File(carpetaEnviar.getParent(), nombreCarpeta + ".zip");

                try {
                    // Comprimir la carpeta en un archivo ZIP
                    FileOutputStream fos = new FileOutputStream(carpetaComprimida);
                    ZipOutputStream zipOut = new ZipOutputStream(fos);
                    zipFile(carpetaEnviar, carpetaEnviar.getName(), zipOut);
                    zipOut.close();
                    fos.close();

                    // Obtener el tamaño del archivo ZIP
                    long tam = carpetaComprimida.length();

                    // Enviar nombre de carpeta y tamaño al servidor
                    dos.writeUTF(nombreCarpeta + ".zip");
                    dos.flush();
                    dos.writeLong(tam);
                    dos.flush();

                    // Enviar el contenido del archivo ZIP al servidor
                    try (DataInputStream disFile = new DataInputStream(new FileInputStream(carpetaComprimida))) {
                        byte[] buffer = new byte[1500];
                        int leidos;
                        long enviados = 0;
                        int porcentaje;
                        while ((leidos = disFile.read(buffer)) != -1) {
                            dos.write(buffer, 0, leidos);
                            dos.flush();
                            enviados += leidos;
                            porcentaje = (int) ((enviados * 100) / tam);
                            System.out.print("\rEnviado el " + porcentaje + "% del archivo");
                        }
                        System.out.println("\nCarpeta enviada.");
                    } catch (IOException e) {
                        System.err.println("Error al enviar la carpeta: " + e.getMessage());
                    }
                } catch (IOException e) {
                    System.err.println("Error al comprimir la carpeta: " + e.getMessage());
                } finally {
                    // Eliminar el archivo ZIP después de enviar la carpeta
                    carpetaComprimida.delete();
                }
            } else {
                System.out.println("La carpeta no existe o no es válida.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
        public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    public void descargarArchivoRemoto(int posicionArchivo) {
        try {
            dos.writeInt(7);
            dos.flush();

            dos.writeInt(posicionArchivo);
            dos.flush();

            String nombre = dis.readUTF();
            long tam = dis.readLong();
            System.out.println("Comienza descarga del archivo " + nombre + " de " + tam + " bytes\n\n");

            File archivoDestino = new File(rutaCarpetaLocal + File.separator + nombre);

            try (FileOutputStream fos = new FileOutputStream(archivoDestino); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                byte[] buffer = new byte[1500];
                int leidos, totalLeidos = 0;

                while (totalLeidos < tam && (leidos = dis.read(buffer, 0, (int) Math.min(buffer.length, tam - totalLeidos))) != -1) {
                    bos.write(buffer, 0, leidos);
                    totalLeidos += leidos;
                }
                bos.flush();
                System.out.println("Archivo recibido correctamente: " + nombre);
            } catch (IOException e) {
                System.err.println("Error al recibir el archivo " + nombre + ": " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void descargarCarpetaRemoto(int indiceArchivo, String rutaDescarga) {
        try {
            dos.writeInt(8);
            dos.flush();

            dos.writeInt(indiceArchivo);
            dos.flush();

            String nombreArchivoZip = dis.readUTF();
            long tam = dis.readLong();

            if (tam > 0) {
                System.out.println("Preparándose para recibir la carpeta de " + tam + " bytes\n\n");
                File carpetaRecibida = new File(rutaDescarga + "/" + nombreArchivoZip);
                try (FileOutputStream fos = new FileOutputStream(carpetaRecibida);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    byte[] buffer = new byte[3500];
                    int leidos;
                    while (tam > 0 && (leidos = dis.read(buffer, 0, (int) Math.min(buffer.length, tam))) != -1) {
                        bos.write(buffer, 0, leidos);
                        tam -= leidos;
                    }
                    System.out.println("\nCarpeta recibida.");
                } catch (IOException e) {
                    System.err.println("Error al recibir la carpeta: " + e.getMessage());
                }

                // Descomprimir el archivo ZIP
                try (ZipFile zipFile = new ZipFile(carpetaRecibida)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        File entryDestination = new File(rutaDescarga, entry.getName());
                        if (entry.isDirectory()) {
                            entryDestination.mkdirs();
                        } else {
                            try (InputStream is = zipFile.getInputStream(entry);
                                 OutputStream os = new FileOutputStream(entryDestination)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    os.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error al descomprimir la carpeta: " + e.getMessage());
                }

                // Eliminar el archivo ZIP
                carpetaRecibida.delete();
                System.out.println("Archivo ZIP eliminado después de la extracción.");
            } else {
                System.out.println("La carpeta no existe.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void cerrarConexion() {
        try {
            dos.close();
            oos.close();
            dis.close();
            ois.close();
            cl.close();
            System.out.println("Conexión con servidor cerrada.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            int pto = 8000;
            String dir = "127.0.0.1";
            Cliente cliente = new Cliente(dir, pto);

            // Crear la interfaz de usuario y conectar los eventos de los botones
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new Menu(cliente).setVisible(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}