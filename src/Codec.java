
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;


/**
 *
 * @author Rodrigo Aguilera
 * @author Ernest Pastor
 */
public class Codec {
    
    
    public static void savePTM(String path,ArrayList<Imagen> colimage, 
            boolean motion, int tam_tesela,int intervalIframes,boolean dynamicInterval, int dynamicIntervalValue,
            int spiral_limit){
        try {   
                
                GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(path));
                //ImageIO no entiende de separadores, por lo que los escribimos como objetos
                BufferedOutputStream bos = new BufferedOutputStream(out);
                ObjectOutputStream oos = new ObjectOutputStream(bos);
               
               
                //para indicar cuantas imagenes vienen
                oos.writeInt(colimage.size());
                oos.writeBoolean(motion);
                if (motion){
                    oos.writeInt(tam_tesela);                    
                }
                
                BufferedImage motionRef=null;
                if (dynamicInterval){
                    intervalIframes=10000000;
                }else{
                    dynamicIntervalValue = 10000000;
                }
                
                System.out.println("motion:"+motion+"tam_tese:"+tam_tesela+"inter:"+intervalIframes+"spi:"+spiral_limit+"dina:"+dynamicInterval);
                Iterator<Imagen> it= colimage.iterator();
                int accumulatedDiff=0;
                for (int i=0;it.hasNext();i++){  
                    System.out.println("-----------accu"+accumulatedDiff);
                    Imagen ima =it.next();
                    BufferedImage bi = ima.getBi();                    
                    if(motion){
                        if(i%intervalIframes==0 || accumulatedDiff>dynamicIntervalValue){
                            accumulatedDiff=0;
                            motionRef=bi;
                            oos.writeBoolean(true);
                            
                            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                            ImageIO.write(bi, "jpeg", byteArray); 
                            oos.writeObject(byteArray.toByteArray());
                            byteArray.close();
                            
                        }else{                            
                            oos.writeBoolean(false);
                            int num_teselas = (bi.getWidth()/tam_tesela) * (bi.getHeight()/ tam_tesela);                            
                            byte[] vm_x = new byte[num_teselas];
                            byte[] vm_y = new byte[num_teselas];
                            accumulatedDiff += motionEstim(bi,motionRef,tam_tesela,num_teselas,vm_x,vm_y,spiral_limit);
                            //escribimos los vectores de movimiento
                            oos.writeObject(vm_x); 
                            oos.writeObject(vm_y); 
                            
                            // la imagen actuala será nuestra nueva referencia
                            motionRef=bi;
                        }
                    }else{
                        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                        ImageIO.write(bi, "jpeg", byteArray); 
                        oos.writeObject(byteArray.toByteArray());
                        byteArray.close();
                    }
                    
                }
                oos.close();
                bos.close();
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
            BufferedInputStream bin= new BufferedInputStream(in);
            ObjectInputStream ois = new ObjectInputStream(bin); 
            
            BufferedImage motionPrev=null;
            
            int numframes=ois.readInt();
            boolean motion = ois.readBoolean();
            int tam_tesela=0;
            
            if (motion){
               tam_tesela=ois.readInt();                
            }
            int num_teselas=0;
            
            byte[] aux ;
            for(int i = 0; i< numframes;i++){
               
                if(motion){
                   boolean keyframe = ois.readBoolean();
                   System.out.println(keyframe);
                   if(keyframe){
                        aux=(byte[])ois.readObject();
                        ByteArrayInputStream ba = new ByteArrayInputStream(aux);
                        bi = ImageIO.read(ba);
                        ba.close();
                        motionPrev = bi;
                        num_teselas = (bi.getWidth()/tam_tesela) * (bi.getHeight()/ tam_tesela);
                   }else{
                       
                       byte[] vm_x = (byte[])ois.readObject();
                       byte[] vm_y = (byte[])ois.readObject();
                       
                       bi = new BufferedImage(motionPrev.getWidth(),motionPrev.getHeight(),motionPrev.getType());
                       for (int j = 0; j<num_teselas;j++){
                            //inix e iniY de la tesela actual
                            int iniX = (j%(bi.getWidth()/tam_tesela))*tam_tesela;
                            int iniY = (int)Math.floor(j/(bi.getWidth()/tam_tesela))*tam_tesela;
                            //recuperamos tesela de referencia
                            int iniXref = iniX + vm_x[j];
                            int iniYref = iniY + vm_y[j];
                            
                            for(int k = 0; k< tam_tesela*tam_tesela;k++){
                                bi.setRGB(
                                        iniX + (k%tam_tesela), 
                                        iniY + ((int)Math.floor(k/tam_tesela)), 
                                  motionPrev.getRGB(iniXref +(k%tam_tesela), iniYref+((int)Math.floor(k/tam_tesela)))
                                        );
                                
                            }
                            
                       }
                       motionPrev = bi;
                   }
                }else{
                    aux=(byte[])ois.readObject();
                    ByteArrayInputStream ba = new ByteArrayInputStream(aux);
                    bi = ImageIO.read(ba);
                    ba.close();
                }
                
                
                
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
                
            }
            ois.close();
            bin.close();
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
                    
                    
                }else{                
                    //hacemos un shift para eliminar el bit menos significativo
                    red = shift(c1.getRed()-c2.getRed());                
                    green = shift(c1.getGreen()-c2.getGreen());                
                    blue = shift(c1.getBlue()-c2.getBlue());
                    
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


    private static int motionEstim(BufferedImage bi, BufferedImage motionPrev, int tam_tesela, int num_teselas, byte[] vm_x, byte[] vm_y, int spiral_limit) {
        BufferedImage newbi;
        newbi = new BufferedImage(bi.getWidth(),bi.getHeight(),bi.getType());
        int accumulatedDiff=0;
        for (int i=0;i<num_teselas;i++){
            int iniX = (i%(bi.getWidth()/tam_tesela))*tam_tesela;
            int iniY = (int)Math.floor(i/(bi.getWidth()/tam_tesela))*tam_tesela;
            
            
            
            int [] candidatos_diff;
            candidatos_diff = new int[spiral_limit];
            int[] candidatosX = new int[spiral_limit];
            int[] candidatosY = new int[spiral_limit];
            //saca tesela
            int[] tesela = new int[tam_tesela*tam_tesela];
            for(int j = 0; j< tam_tesela*tam_tesela;j++){
                tesela[j]=bi.getRGB(iniX +(j%tam_tesela), iniY+((int)Math.floor(j/tam_tesela)));
            }
            
            int spiralX = 0;
            int spiralY = 0;
            int dirX=0;
            int dirY=0;
            for(int j = 0;j<spiral_limit;j++){
                
                    
                
                int iniXref = iniX + spiralX;
                int iniYref = iniY + spiralY;
                //saca tesela referencia
                //int[] teselaref = new int[tam_tesela*tam_tesela];
                int sum =0;
                for(int k = 0; k< tam_tesela*tam_tesela;k++){
                    Color colorref=new Color(motionPrev.getRGB(iniXref +(k%tam_tesela), iniYref+((int)Math.floor(k/tam_tesela))));
                    Color colortes=new Color(tesela[k]);
                    sum+=
                            Math.abs(colorref.getRed()-colortes.getRed()) +
                            Math.abs(colorref.getGreen()-colortes.getGreen()) + 
                            Math.abs(colorref.getBlue()-colortes.getBlue());
                }
                //rellena diferencia con tesela actual
               
                
                candidatos_diff[j]=sum;
                candidatosX[j] = spiralX;
                candidatosY[j] = spiralY;
                
                //codigo espiral
                do{ 
                    //estamos en esquina izquierda inferior
                    if(spiralX<=0 && spiralY<=0 && spiralX==spiralY){
                        spiralX--;
                        //norte
                        dirX=0;
                        dirY=1;
                    }else if(spiralX<=0 && spiralY>=0 && Math.abs(spiralX)==spiralY){
                        //este   
                        spiralX++;
                        dirX=1;
                        dirY=0;
                    }else if(spiralX>=0 && spiralY>=0 && spiralX==spiralY){
                        //sur
                        spiralY--;
                        dirX=0;
                        dirY=-1;
                    }else if(spiralX>=0 && spiralY<=0 && spiralX==Math.abs(spiralY)){
                        //oeste
                        spiralX--;
                        dirX=-1;
                        dirY=0;
                    }else{
                        // si no estamos en una esquina seguimos la direccion
                        spiralX+=dirX;
                        spiralY+=dirY;
                    }
                //que sea una tesela valida    
                }while(!(
                        (iniX + spiralX)>=0 &&
                        (iniY + spiralY)>=0 &&
                        (iniX + spiralX +tam_tesela)<= bi.getWidth() &&
                        (iniY + spiralY +tam_tesela)<= bi.getHeight()
                        ));
                
            }
            //devolver posicion del menor candidato
            int pos=0;
            for(int j = 1;j <candidatos_diff.length && candidatos_diff[pos]>1; j++){
                if(candidatos_diff[j]<candidatos_diff[pos]){
                    pos=j;
                }    
            }
            //System.out.println("diff: "+candidatos_diff[pos]);
            accumulatedDiff+=Math.floor(candidatos_diff[pos]/num_teselas);
            vm_x[i] = (byte) candidatosX[pos];
            vm_y[i] = (byte) candidatosY[pos];           

            
        }
        return accumulatedDiff;
    }

    

    static ArrayList<Imagen> openZip(String path) {
        ArrayList<Imagen> colimage = new ArrayList<>();
        ZipEntry entry;
        try {
        try (ZipFile zipFile = new ZipFile(path)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while(entries.hasMoreElements()){ /* Mientras haya entradas */
                        /* Y no sean directorios */
                        entry = entries.nextElement();
                        BufferedImage bi ;
                        if(!entry.isDirectory()){                            
                            //leemos la entry y la convertimos en una bufferedimage
                            bi = ImageIO.read(zipFile.getInputStream(entry));                       
                            Imagen p = new Imagen(bi, entry.getName());
                            System.out.println(entry.getName());
                            colimage.add(p);  /* Añadimos el nuevo objeto imagen a la collection */                         
                        }                    
                    }
                    zipFile.close();
                }
                //ordenamos la colección
                Collections.sort(colimage);
          } catch (IOException ex) {
            Logger.getLogger(aplicacion_principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return colimage;
    }
}
