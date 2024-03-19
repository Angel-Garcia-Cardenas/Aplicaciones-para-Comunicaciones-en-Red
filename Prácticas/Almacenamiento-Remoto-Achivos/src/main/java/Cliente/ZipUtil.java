package Cliente;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    public static void comprimirCarpeta(File carpetaOrigen, File archivoDestino) throws IOException {
        FileOutputStream fos = new FileOutputStream(archivoDestino);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        comprimirCarpetaRecursiva(carpetaOrigen, carpetaOrigen.getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    private static void comprimirCarpetaRecursiva(File carpetaOrigen, String rutaRelativa, ZipOutputStream zipOut) throws IOException {
        for (File archivo : carpetaOrigen.listFiles()) {
            if (archivo.isDirectory()) {
                comprimirCarpetaRecursiva(archivo, rutaRelativa + "/" + archivo.getName(), zipOut);
                continue;
            }
            FileInputStream fis = new FileInputStream(archivo);
            ZipEntry zipEntry = new ZipEntry(rutaRelativa + "/" + archivo.getPath().substring(carpetaOrigen.getPath().length() + 1));
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
    }
    
    public static void pack(String rutaCarpeta, String rutaCarpetaComprimida) {
        File carpetaOrigen = new File(rutaCarpeta);
        File archivoDestino = new File(rutaCarpetaComprimida);
        try {
            ZipUtil.comprimirCarpeta(carpetaOrigen, archivoDestino);
            System.out.println("Carpeta comprimida exitosamente.");
        } catch (IOException e) {
            System.out.println("Error al comprimir la carpeta: " + e.getMessage());
        }
    }
}
