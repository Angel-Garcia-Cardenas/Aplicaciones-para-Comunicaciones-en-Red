package Cliente;

import Servidor.CarpetaObjeto;
import java.net.Socket;
import java.io.*;
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

            rutaCarpetaLocal = "C:\\Users\\garci\\OneDrive\\Documentos\\NetBeansProjects\\Almacenamiento-Remoto-Achivos\\CarpetaLocal";
            rutaCarpetaRemota = "C:\\Users\\garci\\OneDrive\\Documentos\\NetBeansProjects\\Almacenamiento-Remoto-Achivos\\CarpetaRemota";

            
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
            dos.writeInt(11);
            dos.flush();

            File carpetaEnviar = new File(rutaCarpetaEnviar);
            if (carpetaEnviar.exists() && carpetaEnviar.isDirectory()) {
                try {
                    String nombreCarpeta = carpetaEnviar.getName();
                    File carpetaComprimida = new File(nombreCarpeta + ".zip");
                    ZipUtil.comprimirCarpeta(carpetaEnviar, carpetaComprimida);
                    long tam = carpetaComprimida.length();
                    System.out.println("Preparándose para enviar carpeta " + nombreCarpeta + " de " + tam + " bytes\n\n");
                    dos.writeUTF(nombreCarpeta);
                    dos.flush();
                    dos.writeLong(tam);
                    dos.flush();
                    try (FileInputStream fis = new FileInputStream(carpetaComprimida);
                            ZipInputStream zis = new ZipInputStream(fis)) {
                        ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                            byte[] buffer = new byte[3500];
                            int leidos;
                            while ((leidos = zis.read(buffer)) != -1) {
                                dos.write(buffer, 0, leidos);
                                dos.flush();
                            }
                        }
                    }
                    System.out.println("\nCarpeta enviada.");
                } catch (IOException e) {
                    System.err.println("Error al enviar la carpeta: " + e.getMessage());
                }

            } else {
                System.out.println("La carpeta no existe o no es válida.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        public void descargarArchivoRemoto(String nombreArchivo) {
        try {
            dos.writeInt(7);
            dos.flush();

            dos.writeUTF(nombreArchivo);
            dos.flush();

            String mensajeDescarga = dis.readUTF();
            if (mensajeDescarga.equals("EXISTE")) {
                String rutaDescarga = rutaCarpetaLocal + File.separator + nombreArchivo;
                long tamDescarga = dis.readLong();

                try (FileOutputStream fos = new FileOutputStream(rutaDescarga);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    byte[] buffer = new byte[1500];
                    int leidos;
                    long recibidos = 0;
                    int porcentaje = 0;
                    while (recibidos < tamDescarga) {
                        leidos = dis.read(buffer);
                        bos.write(buffer, 0, leidos);
                        bos.flush();
                        recibidos += leidos;
                        porcentaje = (int) ((recibidos * 100) / tamDescarga);
                        System.out.print("\rRecibido el " + porcentaje + " % del archivo");
                    }
                    System.out.println("\n\n Descarga completada.");
                } catch (IOException e) {
                    System.err.println("Error al recibir el archivo: " + e.getMessage());
                }
            } else {
                System.out.println("El archivo no existe en el servidor.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void descargarCarpetaLocal(String rutaDescarga) {

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