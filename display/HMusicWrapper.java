package display;

import display.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.WindowConstants;

public class HMusicWrapper extends Frame implements MouseListener {

    HMusic hm;

    // This is used to hide the mouse.
    BufferedImage cursorImg;
    Cursor blankCursor;

    // Fullscreen
    boolean fullscreen = false;

    public HMusicWrapper() {
	// Initialize mouse-hiding
	cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB );
	blankCursor = Toolkit.getDefaultToolkit().createCustomCursor( cursorImg,
								      new Point( 0, 0 ),
								      "blank cursor" );
	
	constructWindow();

	// Initialize fullscreen
	if ( fullscreen )
	    java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);
    }
    
    private void constructWindow() {
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });

	setLayout(new BorderLayout());
	setTitle("HMusic");

	// Fullscreen w/o the chrome.
	if ( fullscreen ) 
	    setUndecorated( true );

	// Turn off the cursor in this Frame.
	// This hack taken from:
	//
	// http://stackoverflow.com/questions/1984071/how-to-hide-cursor
	BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB );
	Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor( cursorImg,
									     new Point( 0, 0 ),
									     "blank cursor" );
	this.setCursor( blankCursor );

	hm = new HMusic();
	hm.init();

	hm.setCursor( blankCursor);

	setResizable(false);
	setMinimumSize(new Dimension(hm.getWidth(),
				     hm.getHeight()));
	setPreferredSize(new Dimension(hm.getWidth(),
				       hm.getHeight()));

	add(hm, BorderLayout.CENTER);

	hm.addMouseListener( this );
    }

    public static void main(String[] args) {
	java.awt.EventQueue.invokeLater(new Runnable() {
		public void run() {
		    HMusicWrapper hmw = new HMusicWrapper();
		    hmw.setVisible(true);
		}
	    });
    }

    public void mouseExited( MouseEvent e ) {
	hm.setCursor( blankCursor );
	this.setCursor( blankCursor );
    }
    
    public void mouseEntered( MouseEvent e ) {
	hm.setCursor( blankCursor );
	this.setCursor( blankCursor );
    }

    public void mouseReleased( MouseEvent e ) {
	hm.setCursor( blankCursor );
	this.setCursor( blankCursor );
    }

    public void mousePressed( MouseEvent e ) {
	hm.setCursor( blankCursor );	    
	this.setCursor( blankCursor );
    }

    public void mouseClicked( MouseEvent e ) {
	hm.setCursor( blankCursor );
	this.setCursor( blankCursor );
    }
    
}