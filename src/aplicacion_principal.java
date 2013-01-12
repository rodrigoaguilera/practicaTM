
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.*;
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
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Rodrigo Aguilera
 * @author Ernest Pastor
 * 
 */
public class aplicacion_principal extends javax.swing.JFrame {

    ArrayList<Imagen> colimage;
    Reproductor player;
    /**
     * Creates new form aplicacion_principal
     */
    public aplicacion_principal() {
        initComponents();
        //inicializar();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        playButton = new javax.swing.JButton();
        videoPanel = new javax.swing.JPanel();
        stopButton = new javax.swing.JButton();
        menuPrincipal = new javax.swing.JMenuBar();
        menuArchivo = new javax.swing.JMenu();
        openZipButton = new javax.swing.JMenuItem();
        openPtmButton = new javax.swing.JMenuItem();
        savePtmButton = new javax.swing.JMenuItem();
        closeVideoButton = new javax.swing.JMenuItem();
        closeButton = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        menuFiltroGris = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Tecnologías multimedia");

        playButton.setText("Play");
        playButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout videoPanelLayout = new javax.swing.GroupLayout(videoPanel);
        videoPanel.setLayout(videoPanelLayout);
        videoPanelLayout.setHorizontalGroup(
            videoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 320, Short.MAX_VALUE)
        );
        videoPanelLayout.setVerticalGroup(
            videoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 240, Short.MAX_VALUE)
        );

        stopButton.setText("Stop");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        menuArchivo.setText("Archivo");

        openZipButton.setText("Abrir Zip con imagenes");
        openZipButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openZipButtonActionPerformed(evt);
            }
        });
        menuArchivo.add(openZipButton);

        openPtmButton.setText("Abrir Vídeo en formato ptm");
        openPtmButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openPtmButtonActionPerformed(evt);
            }
        });
        menuArchivo.add(openPtmButton);

        savePtmButton.setText("Guardar Vídeo en formato ptm");
        savePtmButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePtmButtonActionPerformed(evt);
            }
        });
        menuArchivo.add(savePtmButton);

        closeVideoButton.setText("Cerrar video");
        closeVideoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeVideoButtonActionPerformed(evt);
            }
        });
        menuArchivo.add(closeVideoButton);

        closeButton.setText("Cerrar aplicación");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        menuArchivo.add(closeButton);

        menuPrincipal.add(menuArchivo);

        jMenu2.setText("Editar");

        menuFiltroGris.setText("Aplicar escala de grises");
        menuFiltroGris.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFiltroGrisActionPerformed(evt);
            }
        });
        jMenu2.add(menuFiltroGris);

        menuPrincipal.add(jMenu2);

        setJMenuBar(menuPrincipal);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(videoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(playButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stopButton)))
                .addContainerGap(151, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(videoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(playButton)
                    .addComponent(stopButton))
                .addContainerGap(54, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void playButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playButtonMouseClicked
        if(this.player != null && this.player.isNoStopRequested()){
            this.player.setNoStopRequested(false);
            playButton.setText("Play"); 
        }
        //que haya un video cargado
        else if (colimage != null && colimage.size()>0){
            BufferedImage bi = colimage.get(0).getBi();
            Dimension d = new Dimension(bi.getWidth(),bi.getHeight());
            //si todavia no tenemos reproductor o el que hay no tiene bien la dimension
            if(this.player==null || !this.player.getDim().equals(d)){                
                videoPanel.removeAll();     
                this.player = new Reproductor(d);
            }
            if(this.player.getVideo() == null){
                int i=0;
                BufferedImage[] video = new BufferedImage[colimage.size()];
                Iterator<Imagen> it= colimage.iterator();
                while (it.hasNext()){
                    Imagen ima =it.next();
                    video[i] = ima.getBi();
                    i++;

                }
                this.player.setVideo(video);
                videoPanel.add(this.player);
            }else{                
                this.player.restart();
            }  
            playButton.setText("Pause"); 
        }else{
            JOptionPane.showMessageDialog(rootPane, "No hay ningun video cargado");
        }
    }//GEN-LAST:event_playButtonMouseClicked

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void openZipButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openZipButtonActionPerformed
        this.colimage = new ArrayList<>();
        try {
            ZipEntry entry;
            BufferedImage bi ;
            JFileChooser fc = new JFileChooser();
            int returnVal;
            returnVal = fc.showOpenDialog(menuArchivo);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile(); 
                try (ZipFile zipFile = new ZipFile(file.getAbsolutePath())) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while(entries.hasMoreElements()){ /* Mientras haya entradas */
                        /* Y no sean directorios */
                        entry = entries.nextElement();
                        if(!entry.isDirectory()){                            
                            //leemos la entry y la convertimos en una bufferedimage
                            bi = ImageIO.read(zipFile.getInputStream(entry));                       
                            Imagen p = new Imagen(bi, entry.getName());
                            System.out.println(entry.getName());
                            this.colimage.add(p);  /* Añadimos el nuevo objeto imagen a la collection */                         
                        }                    
                    }
                    zipFile.close();
                }
                //ordenamos la colección
                Collections.sort(this.colimage);
            }
        } catch (IOException ex) {
            Logger.getLogger(aplicacion_principal.class.getName()).log(Level.SEVERE, null, ex);
        } 
       
    }//GEN-LAST:event_openZipButtonActionPerformed

    private void menuFiltroGrisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuFiltroGrisActionPerformed
        ArrayList<Imagen>  t_colimage=new ArrayList<>();
        Iterator<Imagen> it= colimage.iterator();
        while (it.hasNext()){            
            Imagen ima =it.next();
            BufferedImage bi = ima.getBi();
            for (int i =0; i<bi.getWidth();i++){
                for(int j=0;j<bi.getHeight();j++){  
                    //System.out.println(ima.getFilename()+" : "+bi.getRGB(i,j));
                    Color cl= new Color(bi.getRGB(i, j));
                    //hacemos la media de los colores
                    int grey = (cl.getRed() + cl.getGreen() + cl.getBlue())/3;
                    cl = new Color(grey,grey,grey);
                    bi.setRGB(i,j,cl.getRGB() );
                    //bi.setRGB(0, 0, 0);
                }
            }
            ima.setBi(bi);
            t_colimage.add(ima);
            
        }
        this.colimage=t_colimage;
    }//GEN-LAST:event_menuFiltroGrisActionPerformed

    private void closeVideoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeVideoButtonActionPerformed
        videoPanel.removeAll();
        colimage=null;
        this.player=null;
        videoPanel.repaint();
        playButton.setText("Play");
    }//GEN-LAST:event_closeVideoButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        videoPanel.removeAll();
        this.player=null;
        videoPanel.repaint();
        playButton.setText("Play");
    }//GEN-LAST:event_stopButtonActionPerformed

    private void savePtmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePtmButtonActionPerformed
        //abrimos el dialogo
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(menuArchivo);
        if (returnVal == JFileChooser.APPROVE_OPTION) {            
            //BufferedOutputStream out = null;
            Codec.savePTM(fc.getSelectedFile().getAbsolutePath(),colimage,true);            
                              
        } 


    }//GEN-LAST:event_savePtmButtonActionPerformed

    private void openPtmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openPtmButtonActionPerformed
        JFileChooser fc = new JFileChooser();
        int returnVal;
        returnVal = fc.showOpenDialog(menuArchivo);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.colimage = Codec.loadPTM(fc.getSelectedFile().getAbsolutePath());
               
            
        }
        
    }//GEN-LAST:event_openPtmButtonActionPerformed

       /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(aplicacion_principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
       
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new aplicacion_principal().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem closeButton;
    private javax.swing.JMenuItem closeVideoButton;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu menuArchivo;
    private javax.swing.JMenuItem menuFiltroGris;
    private javax.swing.JMenuBar menuPrincipal;
    private javax.swing.JMenuItem openPtmButton;
    private javax.swing.JMenuItem openZipButton;
    private javax.swing.JButton playButton;
    private javax.swing.JMenuItem savePtmButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JPanel videoPanel;
    // End of variables declaration//GEN-END:variables

//private void inicializar() {   }

 
}

