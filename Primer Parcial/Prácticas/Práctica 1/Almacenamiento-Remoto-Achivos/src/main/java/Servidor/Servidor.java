package Servidor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;




public class Servidor {
    final static int BUFFER_SIZE = 2048;
    public static void main(String[] args) throws IOException{
        try{
            limpiarPantalla();
            int pto = 8000;
            int opc = 0;
            ServerSocket s = new ServerSocket(pto);
            s.setReuseAddress(true);
            System.out.println("\n\n Servidor iniciado esperando opcion..");
            File f = new File("");
            String ruta = f.getAbsolutePath(); 
            String carpeta = "CarpetaRemota"; 
            String ruta_archivos = ruta + "\\" + carpeta + "\\";
            //System.out.println("ruta:"+ruta_archivos);
            File f2 = new File(ruta_archivos);
            f2.mkdirs();
            f2.setWritable(true); 
           
       
            for(;;){
                
                
                Socket cl = s.accept();
                System.out.println("Cliente conectado desde "+cl.getInetAddress()+":"+cl.getPort());
                DataInputStream dis = new DataInputStream(cl.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(cl.getOutputStream());
                DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
                
                
                
                
                while(opc!=10){
                    opc = dis.readInt();
                    switch(opc){
                        case 1: 
                            System.out.println("Ver las carpetas y archivos almacenados local y remotamente");
                            CarpetaObjeto unaCarpeta = verCarpetas(f2);
                            oos.writeObject(unaCarpeta);
                            oos.flush();
                            
                        break;
                        case 2: 
                            System.out.println("Crear carpetas remotamente");
                            String mensajeEnviar = "";
                            String nombreCarpeta = dis.readUTF();
                            System.out.println("El nombre de la nueva carptea sera: " + nombreCarpeta);
                            mensajeEnviar = crearCarpeta(ruta_archivos, nombreCarpeta);
                            dos.writeUTF(mensajeEnviar);
                            dos.flush();
                            
                        break;
                        case 3:
                            System.out.println("Eliminar carpetas remotamente");
                            String mensajeEnviar2 = "";
                            String nombreCarpeta2 = dis.readUTF();
                            System.out.println("El nombre de la carptea a eliminar es: " + nombreCarpeta2);
                            mensajeEnviar2 = eliminarCarpeta(ruta_archivos,nombreCarpeta2);
                            dos.writeUTF(mensajeEnviar2);
                            dos.flush();
                        break;
                        case 4:
                            System.out.println("Cambiar la ruta del directorio");
                            String nuevaRuta = dis.readUTF();
                            File nuevoDirectorio = new File(nuevaRuta);
                            if (nuevoDirectorio.exists() && nuevoDirectorio.isDirectory()) {
                                ruta_archivos = nuevoDirectorio.getAbsolutePath() + File.separator;
                                dos.writeUTF("Ruta cambiada exitosamente a: " + ruta_archivos);
                            } else {
                                dos.writeUTF("Error al cambiar la ruta. Verifique la existencia del directorio.");
                            }
                            dos.flush();
                        break;
                        
                        ////////////////PUNTO DE RETORNO////////////////////
                        case 5: // Este caso maneja la subida de archivos al servidor
                            String nombre = dis.readUTF();
                            long tam = dis.readLong();
                            System.out.println("Comienza descarga del archivo " + nombre + " de " + tam + " bytes\n\n");
                            File archivoDestino = new File(ruta_archivos + nombre);
                            try (FileOutputStream fos = new FileOutputStream(archivoDestino); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                                byte[] buffer = new byte[1500];
                                int leidos, totalLeidos = 0;
                                while(totalLeidos < tam){
                                    leidos = dis.read(buffer);
                                    bos.write(buffer, 0, leidos);
                                    totalLeidos += leidos;
                                }
                                bos.flush();
                                System.out.println("Archivo recibido correctamente.");
                            } catch (IOException e) {
                                System.err.println("Error al recibir el archivo: " + e.getMessage());
                            }
                        break;
                        case 11:
                            System.out.println("Subir carpetas a la carpeta remota");
                            String nombre2 = dis.readUTF();
                            long tam2 = dis.readLong();
                            System.out.println("Comienza descarga del archivo " + nombre2 + " de " + tam2 + " bytes\n\n");
                            DataOutputStream dos3 = new DataOutputStream(new FileOutputStream(ruta_archivos + nombre2));
                            long recibidos2 = 0;
                            int l2 = 0, porcentaje2 = 0;
                            while(recibidos2<tam2){
                                byte[] b2 = new byte[1500];
                                l2 = dis.read(b2); //leer desde socket hasta 1500 bytes
                                System.out.println("leidos: " + l2);
                                dos3.write(b2, 0, l2); //escribe solo los del taamaÃ±o del archivo :)
                                dos3.flush();
                                recibidos2 = recibidos2 + l2;
                                porcentaje2 = (int) ((recibidos2 * 100) / tam2);
                                System.out.print("\rRecibido el " + porcentaje2 + " % del archivo");
                            }//while
                            System.out.println("\nCarpeta recibida..");
                            dos3.close();
                            
                            File carpetaComprimida = new File(ruta_archivos + nombre2);
                            System.out.println(carpetaComprimida.getAbsolutePath());

                            unzip(carpetaComprimida, ruta_archivos);
                            
                            carpetaComprimida.delete();
                           
                            
                            
                        break; 
                        case 6:
                            System.out.println("Cambiar nombre a una carpeta/archivo");
                        break;
                        case 7:
                            System.out.println("Descargar archivos/carpetas desde la carpeta remota");
                            
                            CarpetaObjeto carpetaAux = verCarpetas(f2);
                            String lista[] = carpetaAux.getLista();
                            int archivoDescarga = dis.readInt();
                            File fileAux = new File(ruta_archivos + lista[archivoDescarga]);
                            String nombre3 = fileAux.getName();
                            System.out.println("Imprimiendo el archivo a mandar" + nombre3);
                            
                            String path = ruta_archivos + nombre3; //nombre de la ruta relativa
                            //path = ruta_archivos + nombre3;
                            System.out.println(" Ubicacion: " + path + "\n");

                            DataInputStream disFile = new DataInputStream(new FileInputStream(path));
                            
                            System.out.println(" Nombre del archivo: " + nombre3);
                            long tam3 = fileAux.length();
                            System.out.println("Tamanoxd : " + tam3);
                            System.out.println("\n\n Preparandose pare enviar archivo " + path + " de " + tam3 + " bytes\n\n");
                            dos.writeUTF(nombre3);
                            dos.flush(); 
                            dos.writeLong(tam3);
                            dos.flush();
                            long enviados3 = 0; //acumulador (mandar por pedazos un archivo)
                            int l3 = 0;
                            int porcentaje3 = 0;
                            while(enviados3<tam3){
                                byte[] b3 = new byte[1500]; 
                                l3=disFile.read(b3);
                                System.out.println("enviados: " + l3);
                                dos.write(b3,0,l3);
                                dos.flush();
                                enviados3 = enviados3 + l3;
                                porcentaje3 = (int)((enviados3*100)/tam3); //calcular el porcentaje del archivo que se esta mandando
                                System.out.print("\rEnviado el "+porcentaje3+" % del archivo");
                            }//while
                            System.out.println("\nArchivo enviado..");
                            disFile.close();  
                        break;

                        case 8:
                            System.out.println("Descargar archivos/carpetas desde la carpeta remota");

                            CarpetaObjeto carpetaAux2 = verCarpetas(f2);
                            String lista2[] = carpetaAux2.getLista();
                            int archivoDescarga2 = dis.readInt();
                            nombreCarpeta = lista2[archivoDescarga2];

                            String rutaCarpeta = ruta_archivos + "//" + nombreCarpeta + "//";
                            File enviarCarpeta = new File(rutaCarpeta);

                            try{
                                // ZipFile carpetaComprimida = new ZipFile(enviarCarpeta.getAbsolutePath() + ".zip");
                                FileOutputStream fos = new FileOutputStream( enviarCarpeta.getAbsolutePath() + ".zip");
                                ZipOutputStream zipOut = new ZipOutputStream(fos);
                                File fileToZip = new File(enviarCarpeta.getAbsolutePath());
                                zipFile(fileToZip, fileToZip.getName(), zipOut);
                                zipOut.close();
                                fos.close();
                                DataInputStream disFile3 = new DataInputStream(new FileInputStream(enviarCarpeta.getAbsolutePath() + ".zip"));
                                File carpetaComprimida2 = new File(enviarCarpeta.getAbsolutePath() + ".zip");
                                
                                long tam4 = carpetaComprimida2.length();
                                 System.out.println("\n\n Preparandose pare enviar archivo " + carpetaComprimida2.getAbsolutePath() + " de " + tam4 + " bytes\n\n");
                                 dos.writeUTF(nombreCarpeta + ".zip");
                                 dos.flush(); 
                                 dos.writeLong(tam4);
                                 dos.flush();
                                 long enviados = 0; //acumulador (mandar por pedazos un archivo)
                                 int l4=0,porcentaje4=0;
                                 while(enviados<tam4){
                                     byte[] b = new byte[1500]; 
                                     l4=disFile3.read(b);
                                     System.out.println("enviados: "+l4);
                                     dos.write(b,0,l4);
                                     dos.flush();
                                     enviados = enviados + l4;
                                     porcentaje4 = (int)((enviados*100)/tam4); //calcular el porcentaje del archivo que se esta mandando
                                     System.out.print("\rEnviado el "+porcentaje4+" % del archivo");
                                 }//while
                                 System.out.println("\nArchivo enviado..");
                                 disFile3.close();
                                carpetaComprimida2.delete();
                             }catch(ZipException e){
                                 System.out.println(e.getMessage());
                                 e.printStackTrace();
                             }



                        break;

                        case 9:
                            System.out.println("Cambiar nombre a archivos/carpetas dentro de la carpeta remota");  
                            CarpetaObjeto carpetaAux3 = verCarpetas(f2);
                            String lista3[] = carpetaAux3.getLista();
                            int archivoCambio = dis.readInt();
                            String archivoNuevo = dis.readUTF();
                            File archivoViejo = new File(ruta_archivos + lista3[archivoCambio]);
                            String nombre4 = archivoViejo.getName();
                            File archivoNuevoF = new File(ruta_archivos + archivoNuevo);
                            
                            if (archivoViejo.renameTo(archivoNuevoF)) {
                                System.out.println("archivo renombrado");
                            } else {
                                System.out.println("error");
                            }
                            
                            
                        break;

                        
                        case 10:
                            System.out.println("Cerrando comunicacion con el cliente");
                            
                            
                        break;
                    }
                   
                }
                
           
                dos.close();
                oos.close();
                dis.close();
                cl.close(); 
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static CarpetaObjeto verCarpetas(File f){
        String[] lista = f.list();
        CarpetaObjeto misArchivos = new CarpetaObjeto(lista);
        if(lista == null || lista.length == 0){
            System.out.println("no hay archivos en la carpeta remota");
            return misArchivos;
        }else{
            for (int i=0; i< lista.length; i++) {
                //System.out.println(lista[i]);
            }   
            return misArchivos;   
        }
    }
    public static String crearCarpeta(String ruta_archivos, String nombreCarpeta) {
       
        String rutaCarpeta = ruta_archivos + "\\" + nombreCarpeta + "\\";
        File nuevaCarpeta = new File(rutaCarpeta);
        if (nuevaCarpeta.mkdirs()) {
            //System.out.println("Carpeta creada");        
            return "Carpeta creada exitosamente";
        } else {
            //System.out.println("Error al crear carpeta");
            return "Error al crear la carpeta :c";
          
        }

    }
    
    public static String eliminarCarpeta(String ruta_archivos, String nombreCarpeta2){
        String rutaCarpeta = ruta_archivos + "//" + nombreCarpeta2 + "//";
        File elimCarpeta = new File(rutaCarpeta);
        if (elimCarpeta.delete()) {
            return "La carpeta ha sido borrada correctamente";
        } else {
            return "Error al eliminar la carpeta ";
        }
    }
    public static void limpiarPantalla(){
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {}
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

    

    
    public static void unzip(File source, String out) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source))) {

        ZipEntry entry = zis.getNextEntry();

        while (entry != null) {

            File file = new File(out, entry.getName());

            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                File parent = file.getParentFile();

                if (!parent.exists()) {
                    parent.mkdirs();
                }

                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {

                    int bufferSize = Math.toIntExact(entry.getSize());
                    byte[] buffer = new byte[bufferSize > 0 ? bufferSize : 1];
                    int location;

                    while ((location = zis.read(buffer)) != -1) {
                        bos.write(buffer, 0, location);
                    }
                }
            }
            entry = zis.getNextEntry();
        }
    }
}
    public static void extract(ZipInputStream zip, File target) throws IOException {
        try {
            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                File file = new File(target, entry.getName());

                if (!file.toPath().normalize().startsWith(target.toPath())) {
                    throw new IOException("Bad zip entry");
                }

                if (entry.isDirectory()) {
                    file.mkdirs();
                    continue;
                }

                byte[] buffer = new byte[BUFFER_SIZE];
                file.getParentFile().mkdirs();
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                int count;

                while ((count = zip.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }

                out.close();
            }
        } finally {
            zip.close();
        }
    }
}