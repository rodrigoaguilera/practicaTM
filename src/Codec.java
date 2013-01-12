
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.imageio.ImageIO;


/**
 *
 * @author Rodrigo Aguilera
 * @author Ernest Pastor
 */
public class Codec {
    
    
    public static void savePTM(String path,ArrayList<Imagen> colimage, boolean motion){
        try {   
                GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(path));
                //ImageIO no entiende de separadores, por lo que los escribimos como objetos
                ObjectOutputStream oos = new ObjectOutputStream(out);
               
                
                //para indicar cuantas imagenes vienen
                oos.writeInt(colimage.size());
                
                BufferedImage motionRef=null;
                BufferedImage motionPrev=null;
                BufferedImage t_bi=null;
                
                Iterator<Imagen> it= colimage.iterator();
                for (int i=0;it.hasNext();i++){            
                    Imagen ima =it.next();
                    BufferedImage bi = ima.getBi();
                    if(motion){
                        if(i==0){
                            motionRef=bi;
                            motionPrev=bi;
                        }else{
                            t_bi = bi;
                            bi=motionDiff(bi,motionPrev);
                            motionPrev = t_bi;
                        }
                    }
                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                    ImageIO.write(bi, "jpeg", byteArray); 
                    oos.writeObject(byteArray.toByteArray());
                    byteArray.close();
                }
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(aplicacion_principal.class.getName()).log(Level.SEVERE, null, ex);
            } 
    }
    public static ArrayList<Imagen> loadPTM(String path){
        ArrayList<Imagen> colimage = new ArrayList<>();
        try {
            
            BufferedImage bi ;
            //BufferedInputStream in;
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(path));                
            ObjectInputStream ois = new ObjectInputStream(in); 
            //int numframes = in.read();
            int numframes=ois.readInt();
            
            
            byte[] aux ;
            for(int i = 0; i< numframes;i++){
                aux=(byte[])ois.readObject();
                ByteArrayInputStream ba = new ByteArrayInputStream(aux);
                bi = ImageIO.read(ba);
                Imagen p = new Imagen(bi, Integer.toString(i));
                colimage.add(p);                    
                ba.close();
            }
            in.close(); 
        }  catch (ClassNotFoundException | IOException  ex) {
            Logger.getLogger(aplicacion_principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return colimage;
    }
    private static BufferedImage motionDiff(BufferedImage b1,BufferedImage b2){
        BufferedImage diffbi;
        diffbi = new BufferedImage(b1.getWidth(),b1.getHeight(),b1.getType());
        for (int i =0; i<b1.getWidth();i++){
            for(int j=0;j<b1.getHeight();j++){
                Color c1= new Color(b1.getRGB(i, j));
                Color c2= new Color(b2.getRGB(i, j));
                
                //hacemos un shift para eliminar el bit menos significativo
                int red = shift(c1.getRed()-c2.getRed());
                
                int green = shift(c1.getGreen()-c2.getGreen());
                
                int blue = shift(c1.getBlue()-c2.getBlue());
                
                
                c1 = new Color(red,green,blue);
                diffbi.setRGB(i,j,c1.getRGB());                
            }
        }
        return diffbi;
    }
    private static int shift(int red){
        return (red<0)?(((red*-1)>>1)+1)<<1:(red>>1)<<1;
    }
}
