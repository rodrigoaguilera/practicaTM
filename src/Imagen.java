
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Rodrigo Aguilera
 */
public class Imagen {
    private BufferedImage bi;
    private String name;
    private int ancho;
    private int alto;
    public Imagen(BufferedImage bi, String name){
        this.bi=bi;
        this.name=name;
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
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the ancho
     */
    public int getAncho() {
        return ancho;
    }

    /**
     * @param ancho the ancho to set
     */
    public void setAncho(int ancho) {
        this.ancho = ancho;
    }

    /**
     * @return the alto
     */
    public int getAlto() {
        return alto;
    }

    /**
     * @param alto the alto to set
     */
    public void setAlto(int alto) {
        this.alto = alto;
    }
    public void draw(Graphics g) {       
       g.drawImage(bi, getAncho(), getAlto(), null);
    }

}
