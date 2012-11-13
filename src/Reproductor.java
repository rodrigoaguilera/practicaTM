import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

public class Reproductor extends JComponent {
  private BufferedImage[] video;

  private Dimension mida;

  private volatile int actual;

  private Thread internalThread;

  private volatile boolean noStopRequested;

  public Reproductor(int ancho,int alto) {
    actual = 0;
    
    //buildSlides();
    mida = new Dimension(ancho, alto);
    setMinimumSize(mida);
    setPreferredSize(mida);
    setMaximumSize(mida);
    setSize(mida);

    noStopRequested = true;
    Runnable r = new Runnable() {
        @Override
        public void run() {           
          play();           
        }
    };

    internalThread = new Thread(r, "Video");
    internalThread.start();
  }
  

    @Override
    public void paint(Graphics g) {
      g.drawImage(video[actual], 0, 0, this);
    }

  private void play() {
    while (noStopRequested) {
      try {
        Thread.sleep(100); // 10 frames per second
        actual = (actual + 1) % video.length;
        repaint();
      } catch (InterruptedException x) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public void stopRequest() {
    noStopRequested = false;
    internalThread.interrupt();
  }

  public boolean isAlive() {
    return internalThread.isAlive();
  }

    /**
     * @param video the video to set
     */
    public void setVideo(BufferedImage[] video) {
        this.video = video;
    }

 
}