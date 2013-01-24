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
import javax.imageio.*;


/**
 *
 * @author Rodrigo Aguilera
 * @author Ernest Pastor
 */
public class Codec {
    
    // estructura de compresion
    public static void savePTM(String path,ArrayList<Imagen> colimage, 
            boolean motion, int tam_tesela,int intervalIframes,boolean dynamicInterval, int dynamicIntervalValue,
            int spiral_limit){
        try {   
                
                GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(path));
                //ImageIO no entiende de separadores, por lo que los escribimos como objetos
                BufferedOutputStream bos = new BufferedOutputStream(out);
                ObjectOutputStream oos = new ObjectOutputStream(bos);
               //cabecera de archivo
                //para indicar cuantas imagenes vienen
                oos.writeInt(colimage.size());
                //aplicamos el algoritmo o no
                oos.writeBoolean(motion);
                if (motion){
                    //el algoritmo necesita un tamaño de tesela
                    oos.writeInt(tam_tesela);                    
                }
                
                //ajustamos las variables segun si el intervalo es dinamico o fijo
                if (dynamicInterval){
                    intervalIframes=10000000;
                }else{
                    dynamicIntervalValue = 10000000;
                }
                
                //tenemos siempre un frame de referencia
                BufferedImage motionRef=null;
                
                Iterator<Imagen> it= colimage.iterator();
                int accumulatedDiff=0;
                for (int i=0;it.hasNext();i++){  
                    
                    Imagen ima =it.next();
                    BufferedImage bi = ima.getBi();                    
                    if(motion){
                        //si se ha llegado al maximo de frames del GOP o hay mucha degradacion de la imagen guardaremosun keyframe
                        if(i%intervalIframes==0 || accumulatedDiff>dynamicIntervalValue){
                            //reseteamos la degradacion
                            accumulatedDiff=0;
                            motionRef=bi;
                            //lo marcamos como frame sin compesacion
                            oos.writeBoolean(true);
                            
                            //nos ayudamos del bytearrayoutputstream para encapsular los escripto en un objeto
                            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                            //aplicamos la compresion jpeg
                            ImageIO.write(bi, "jpeg", byteArray); 
                            oos.writeObject(byteArray.toByteArray());
                            byteArray.close();
                            
                        }else{
                            // frame compensado
                            oos.writeBoolean(false);
                            //calculo del numero de teselas
                            int num_teselas = (bi.getWidth()/tam_tesela) * (bi.getHeight()/ tam_tesela);
                            //vectores de movimiento a rellenaruna (x,y) por tesela
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
                        //no hay estimacion de movimiento
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
    
    //descompresion
    public static ArrayList<Imagen> loadPTM(String path){
        ArrayList<Imagen> colimage = new ArrayList<>();
        try {
            
            BufferedImage bi;
            
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(path));
            BufferedInputStream bin= new BufferedInputStream(in);
            ObjectInputStream ois = new ObjectInputStream(bin); 
            
            BufferedImage motionPrev=null;
            //leeamos los parametros necesarios
            int numframes=ois.readInt();
            boolean motion = ois.readBoolean();
            int tam_tesela=0;
            
            if (motion){
               tam_tesela=ois.readInt();                
            }
            int num_teselas=0;
            
            //intermedio para guardar los streams de bytess
            byte[] aux ;
            //leemos cada frame
            for(int i = 0; i< numframes;i++){
               
                if(motion){
                   // es un frame completo?
                   boolean keyframe = ois.readBoolean();
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
    

    private static int motionEstim(BufferedImage bi, BufferedImage motionPrev, int tam_tesela, int num_teselas, byte[] vm_x, byte[] vm_y, int spiral_limit) {
        //inicializamos el degradado
        int accumulatedDiff=0;
        //recorremos todas las teselas
        for (int i=0;i<num_teselas;i++){
            //donde comienza esta tesela
            int iniX = (i%(bi.getWidth()/tam_tesela))*tam_tesela;
            int iniY = (int)Math.floor(i/(bi.getWidth()/tam_tesela))*tam_tesela;
            
            
            // guardaremos tantos candidatos como spiral limit
            int [] candidatos_diff;
            candidatos_diff = new int[spiral_limit];
            int[] candidatosX = new int[spiral_limit];
            int[] candidatosY = new int[spiral_limit];
            //saca tesela del cuadro que analizamos (no del referencia)
            int[] tesela = new int[tam_tesela*tam_tesela];            
            for(int j = 0; j< tam_tesela*tam_tesela;j++){
                //guardamo el rgb del pixel
                tesela[j]=bi.getRGB(iniX +(j%tam_tesela), iniY+((int)Math.floor(j/tam_tesela)));
            }
            //la espiral comienza desde el mismo frame que hay en la referencia
            int spiralX = 0;
            int spiralY = 0;
            //la dirección se tiene que determinar
            int dirX=0;
            int dirY=0;
            for(int j = 0;j<spiral_limit;j++){
                
                    
                
                int iniXref = iniX + spiralX;
                int iniYref = iniY + spiralY;
                
                //saca tesela referencia     
                int sum =0;
                for(int k = 0; k< tam_tesela*tam_tesela;k++){
                    Color colorref=new Color(motionPrev.getRGB(iniXref +(k%tam_tesela), iniYref+((int)Math.floor(k/tam_tesela))));
                    Color colortes=new Color(tesela[k]);
                    //diferencias entre cada canal
                    sum+=
                            Math.abs(colorref.getRed()-colortes.getRed()) +
                            Math.abs(colorref.getGreen()-colortes.getGreen()) + 
                            Math.abs(colorref.getBlue()-colortes.getBlue());
                }
                       
                // guarda la diferencia y el vector de movimiento de este candidato
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
                //que sea una tesela valida, es decir, que no esté fuera del cuadro
                }while(!(
                        (iniX + spiralX)>=0 && 
                        (iniY + spiralY)>=0 &&
                        (iniX + spiralX +tam_tesela)<= bi.getWidth() &&
                        (iniY + spiralY +tam_tesela)<= bi.getHeight()
                        ));
                
            }
            //devolver posicion del candidato con menor diferencia
            int pos=0;
            for(int j = 1;j <candidatos_diff.length && candidatos_diff[pos]>1; j++){
                if(candidatos_diff[j]<candidatos_diff[pos]){
                    pos=j;
                }    
            }
            //sumamos la degradacion que ha sufrido esta tesela
            accumulatedDiff+=Math.floor(candidatos_diff[pos]/num_teselas);
            //guardamos el vector correspondiente al mejor candidato
            vm_x[i] = (byte) candidatosX[pos];
            vm_y[i] = (byte) candidatosY[pos];           

            
        }
        //devolvemos la degradacion del cuadro
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
                            //problema con openJDK y el canal alpha (ver informe)
                            if(bi.getType()!=BufferedImage.TYPE_3BYTE_BGR){
                                //convertimos a RGB sin canal alpha
                                BufferedImage convertedImg = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                                convertedImg.getGraphics().drawImage(bi, 0, 0, null);
                                bi=convertedImg;
                            }
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
    
    /*
     * ALGORITMO NO FUNCIONAL 
     * no funciona la descompresion por un problema de ajuste de bytes
     * resta los valores de una imagen con su anterior, 
     * obteniendo una imagen mayormente negra
     */
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
    //ignora el ultimo bit para usarlo como signo
    private static int shift(int color){
        return (color<0)?(((color*-1)>>1)<<1)+1:(color>>1)<<1;
    }
    //toma el ultimo bit para aplicarlo como signo
    private static int unshift(int color){
        return (color%2==0)?color:((color-1)*-1);
    }

}
