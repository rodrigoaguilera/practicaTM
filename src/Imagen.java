import java.awt.image.BufferedImage;

/**
 *
 * @author Rodrigo Aguilera
 * @author Ernest Pastor
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
     * Implementa esta funcion para poder ser un objeto ordenable
     */
    @Override
    public int compareTo(Object t) {
       Imagen im = (Imagen) t; 
       return filename.compareTo(im.getFilename());
    }
    
    

}
