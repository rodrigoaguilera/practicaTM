

import java.awt.Dimension;
import java.awt.image.BufferedImage;

/*
 * 
 * gzipoutputstream
 *  para comparar el grado de compresion hay que saltarse el proceso de compresion
 * es decir, saltarse la compresion y pillar la coleection y aplicar jpeg+gzip
 * 
 * para la funcion hash que compara teselas, por ejemplo vale la suma de los valores
 *rgb y dividir por el numero de pixeles
 * 
 * /

/**
 *
 * @author Rodrigo Aguilera
 */
public class Imagen implements Comparable{
    private BufferedImage bi;
    private String filename;
    
    public Imagen(BufferedImage bi, String name){
        this.bi=bi;
        this.filename=name;        
    }

    /**
     * @return the bi
     */
    public BufferedImage getBi() {
        return bi;
    }

    /**
     * @param bi the bi to set
     */
    public void setBi(BufferedImage bi) {
        this.bi = bi;
    }

    /**
     * @return the name
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param name the name to set
     */
    public void setFilename(String name) {
        this.filename = name;
    }
    
    /**
     * @return the ancho
     */
   
    @Override
    public int compareTo(Object t) {
       Imagen im = (Imagen) t; 
       return filename.compareTo(im.getFilename());
    }
    
    

}
