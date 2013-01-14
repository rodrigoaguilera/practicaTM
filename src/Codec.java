
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
                oos.writeBoolean(motion);
                
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
                            bi=motionDiff(bi,motionPrev,false);
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
            
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(path));                
            ObjectInputStream ois = new ObjectInputStream(in); 
            
            BufferedImage motionPrev=null;
            
            int numframes=ois.readInt();
            boolean motion = ois.readBoolean();
            
            byte[] aux ;
            for(int i = 0; i< numframes;i++){
                aux=(byte[])ois.readObject();
                ByteArrayInputStream ba = new ByteArrayInputStream(aux);
                bi = ImageIO.read(ba);
                //System.out.println(i);
                if (motion){
                    if(i==0){
                        motionPrev = bi;
                    }else{
                        //bi = motionDiff(bi,motionPrev,true);
                        motionPrev = bi;
                    }
                }
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
    private static BufferedImage motionDiff(BufferedImage b1,BufferedImage b2, boolean sum){
        BufferedImage diffbi;
        diffbi = new BufferedImage(b1.getWidth(),b1.getHeight(),b1.getType());
        for (int i =0; i<b1.getWidth();i++){
            for(int j=0;j<b1.getHeight();j++){
                Color c1= new Color(b1.getRGB(i, j));
                Color c2= new Color(b2.getRGB(i, j));
                int red;              
                int green;                
                int blue;
                if(sum){
                    red = c2.getRed()+ unshift(c1.getRed());                
                    green = c2.getGreen() + unshift(c1.getGreen());                
                    blue = c2.getBlue() + unshift(c1.getBlue());
                    if (red >255 || red<0){
                        //System.out.println(c2.getBlue() +" + "+unshift(c1.getBlue()) +" -"+i+"-"+j);
                        System.out.println(red);
                        red=0;
                    }
                    if (green >255 || green<0){
                        //System.out.println(c2.getBlue() +" + "+unshift(c1.getBlue()) +" -"+i+"-"+j);
                        System.out.println(green);
                        green=0;
                    }
                    if (blue >255 || blue<0){
                        //System.out.println(c2.getBlue() +" + "+unshift(c1.getBlue()) +" -"+i+"-"+j);
                        System.out.println(blue);
                        blue=0;
                    }
                    
                }else{                
                    //hacemos un shift para eliminar el bit menos significativo
                    red = shift(c1.getRed()-c2.getRed());                
                    green = shift(c1.getGreen()-c2.getGreen());                
                    blue = shift(c1.getBlue()-c2.getBlue());
                    if (i==0 && j ==9 ){
                        System.out.println(c1.getBlue()+" - "+c2.getBlue() +" shift: "+shift(c1.getBlue()-c2.getBlue()));
                        System.out.println("joder antes de guardar el azul:"+blue);
                    } 
                }                
                diffbi.setRGB(i,j, new Color(red,green,blue).getRGB());             
            }
        }        
        return diffbi;
    }
    private static int shift(int color){
        return (color<0)?(((color*-1)>>1)<<1)+1:(color>>1)<<1;
    }
    private static int unshift(int color){
        return (color%2==0)?color:((color-1)*-1);
    }
}
