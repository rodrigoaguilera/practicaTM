import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

public class Reproductor extends JComponent {
  private BufferedImage[] video;

  private Dimension dim;

  private volatile int actual;

  private Thread internalThread;

  private volatile boolean noStopRequested;

  public Reproductor(Dimension d) {
    actual = 0;
    
    //buildSlides();
    this.dim = d;
    setMinimumSize(dim);
    setPreferredSize(dim);
    setMaximumSize(dim);
    setSize(dim);

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
      g.drawImage(getVideo()[actual], 0, 0, this);
    }

  public void play() {
    noStopRequested = true;
    
    while (isNoStopRequested()) {
      try {
        Thread.sleep(40); //40 = 25 frames por segundo
        actual = (actual + 1) % getVideo().length;
        repaint();
      } catch (InterruptedException x) {
        Thread.currentThread().interrupt();
      }
    }
  }
  public void restart(){
    Runnable r = new Runnable() {
        @Override
        public void run() {           
          play();           
        }
    };
    internalThread = new Thread(r, "Video");
    internalThread.start();
}

  public void stopRequest() {
    setNoStopRequested(false);
        getInternalThread().interrupt();   
  }

  public boolean isAlive() {
    return getInternalThread().isAlive();
  }

    
    /**
     * @return the dim
     */
    public Dimension getDim() {
        return dim;
    }

    /**
     * @param dim the dim to set
     */
    public void setDim(Dimension dim) {
        this.dim = dim;
    }

    /**
     * @return the video
     */
    public BufferedImage[] getVideo() {
        return video;
    }

    /**
     * @param video the video to set
     */
    public void setVideo(BufferedImage[] video) {
        this.video = video;
    }

    /**
     * @param noStopRequested the noStopRequested to set
     */
    public void setNoStopRequested(boolean noStopRequested) {
        this.noStopRequested = noStopRequested;
    }

    /**
     * @return the internalThread
     */
    public Thread getInternalThread() {
        return internalThread;
    }

    /**
     * @param internalThread the internalThread to set
     */
    public void setInternalThread(Thread internalThread) {
        this.internalThread = internalThread;
    }

    /**
     * @return the noStopRequested
     */
    public boolean isNoStopRequested() {
        return noStopRequested;
    }

 
}