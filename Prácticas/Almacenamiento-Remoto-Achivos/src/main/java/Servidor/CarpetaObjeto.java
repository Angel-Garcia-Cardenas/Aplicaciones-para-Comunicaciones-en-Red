package Servidor;

import java.io.Serializable;

public class CarpetaObjeto implements Serializable {
    String lista [];
    public CarpetaObjeto(String lista[]){
        this.lista = lista;
    }
    
    public String[] getLista(){
        return lista;
    }
    
}
