
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
    
    
    public static void savePTM(String path,ArrayList<Imagen> colimage, boolean motion){
        try {   
                GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(path));
                //ImageIO no entiende de separadores, por lo que los escribimos como objetos
                ObjectOutputStream oos = new ObjectOutputStream(out);
               
                int tam_tesela=8;
                //para indicar cuantas imagenes vienen
                oos.writeInt(colimage.size());
                oos.writeBoolean(motion);
                if (motion){
                    oos.writeInt(tam_tesela);
                }
                
                BufferedImage motionRef=null;


                
                Iterator<Imagen> it= colimage.iterator();
                for (int i=0;it.hasNext();i++){            
                    Imagen ima =it.next();
                    BufferedImage bi = ima.getBi();
                    /*if(motion){
                        if(i==0){
                            motionRef=bi;
                            motionPrev=bi;
                        }else{
                            t_bi = bi;
                            bi=motionDiff(bi,motionPrev,false);
                            motionPrev = t_bi;
                        }
                    } */
                    if(motion){
                        if(i==0){
                            motionRef=bi;
                            
                            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                            ImageIO.write(bi, "jpeg", byteArray); 
                            oos.writeObject(byteArray.toByteArray());
                            byteArray.close();
                        }else{
                            
                            
                            int num_teselas = (bi.getWidth()/tam_tesela) * (bi.getHeight()/ tam_tesela);                            
                            byte[] vm_x = new byte[num_teselas];
                            byte[] vm_y = new byte[num_teselas];
                            motionEstim(bi,motionRef,tam_tesela,num_teselas,vm_x,vm_y);
                            
                            oos.write(vm_x);
                            oos.write(vm_y);
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
            int tam_tesela=0;
            if (motion){
               tam_tesela=ois.readInt(); 
            }
            int num_teselas=0;
            
            byte[] aux ;
            for(int i = 0; i< numframes;i++){
                if(motion){
                   if(i==0){
                        aux=(byte[])ois.readObject();
                        ByteArrayInputStream ba = new ByteArrayInputStream(aux);
                        bi = ImageIO.read(ba);
                        ba.close();
                        motionPrev = bi;
                        num_teselas = (bi.getWidth()/tam_tesela) * (bi.getHeight()/ tam_tesela);
                   }else{
                       byte[] vm_x = null;
                       byte[] vm_y = null;
                       ois.read(vm_x, 0, num_teselas);
                       ois.read(vm_y, 0, num_teselas);
                       bi = new BufferedImage(motionPrev.getWidth(),motionPrev.getHeight(),motionPrev.getType());
                       for (int j = 0; j<num_teselas;j++){
                           
                       }
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

    private static void motionEstim(BufferedImage bi, BufferedImage motionPrev, int tam_tesela, int num_teselas, byte[] vm_x, byte[] vm_y) {
        BufferedImage newbi;
        newbi = new BufferedImage(bi.getWidth(),bi.getHeight(),bi.getType());
        System.out.println("num teselas: "+num_teselas);
        for (int i=0;i<num_teselas;i++){
            int iniX = (i%(bi.getWidth()/tam_tesela))*tam_tesela;
            int iniY = (int)Math.floor(i/(bi.getWidth()/tam_tesela))*tam_tesela;
            
            System.out.println("i: "+i+" iniX: "+iniX +" iniy: "+iniY);
            int spiral_limit=1;
            int [] candidatos_diff;
            candidatos_diff = new int[spiral_limit];
            int[] candidatosX = new int[spiral_limit];
            int[] candidatosY = new int[spiral_limit];
            //saca tesela
            int[] tesela = new int[tam_tesela*tam_tesela];
            for(int j = 0; j< tam_tesela*tam_tesela;j++){
                tesela[j]=bi.getRGB(iniX +(j%tam_tesela), iniY+((int)Math.floor(j/tam_tesela)));
            }
            
            for(int j = 0;j<spiral_limit;j++){
                //codigo espiral
                int spiralX = 0;
                int spiralY = 0;
                
                int iniXref = iniX + spiralX;
                int iniYref = iniY + spiralY;
                //saca tesela referencia
                int[] teselaref = new int[tam_tesela*tam_tesela];
                for(int k = 0; k< tam_tesela*tam_tesela;k++){
                    teselaref[k]=motionPrev.getRGB(iniXref +(k%tam_tesela), iniYref+((int)Math.floor(k/tam_tesela)));
                }
                //rellena diferencia con tesela actual
                int sum =0;
                
                candidatos_diff[j]=sum;
                candidatosX[j] = spiralX;
                candidatosY[j] = spiralY;
            }
            //devolver posicion del menor candidato
            int pos=0;
            for(int j = 1;j <candidatos_diff.length; j++){
                if(candidatos_diff[j]<candidatos_diff[pos]){
                    pos=j;
                }    
            }
            vm_x[i] = (byte) candidatosX[pos];
            vm_y[i] = (byte) candidatosY[pos];
            //int[] tesela=bi.getRGB(iniX,iniY, tam_tesela, tam_tesela, null, 0, 0);
            //vm_x[i]=-1;
            /*if(searchTes(bi,tesela,vm_x,vm_y,i)){
                //poner a negro
            }else{
               //poner tesela
            }*/
            
        }
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
