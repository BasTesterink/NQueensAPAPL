package GUI;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
 
import core.GameLogic;

/**
 * Graphical interface for the queens environment. 
 * @author Bas Testerink
 *
 */
public class QueensGUI extends JPanel{
	private GameLogic gl = null;									// The core game logic
	private String icon_dir = "./resources/icons/";					// Directory that holds the icons
	private BufferedImage queen = null;								// The queen icon
	private Font font = new Font( Font.SANS_SERIF,Font.BOLD, 20); 	// Used GUI font
	private FrameCaller frame_caller = null;						// For calling the repaint method
	private Thread frame_caller_thread = null;						// Thread that holds the runnable
	private int[] queens = null; 									// Pixel y positions of agents
	private int cell_size = 0;										// Size of each square
	
	/**
	 * Creates new GUI.
	 * @param gl The core game logic.
	 * @param fps Frames per seconds, is the amount of repaints per second.
	 */
	public QueensGUI(GameLogic gl, int fps){
		this.gl = gl;
		File f = new File(icon_dir);
		if(!f.exists()){													// If the icon map does not exist
			JFileChooser jfc = new JFileChooser(".");						// Make file chooser in root map
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		// Directory only
			jfc.setAcceptAllFileFilterUsed(false);
			jfc.setDialogTitle("Choose icon directory");
			jfc.setSelectedFile(f); 										// Set the given selected file
			if(jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) 	// Get the user's choice
				icon_dir = jfc.getSelectedFile().getPath()+"/"; 			// Set the directory with the images
		}
		queen = getIcon(icon_dir+"queen2.png");								// Queen sprite
		queens = new int[gl.get_board().length];							// Initialize positions
		frame_caller = new FrameCaller(this, fps);							// Create update runnable
		cell_size = queen.getHeight();
	}
	
	/** Start updates and animation */
	public void start_animation(){
		frame_caller.setHalt(false);					// Will prevent the run-loop from halting
		frame_caller_thread = new Thread(frame_caller); // Create new thread
		frame_caller_thread.start();					// Start it
	}
	
	/** Stop updates and animation */
	public void stop_animation(){
		frame_caller.setHalt(true); 					// Will stop the current run-loop
	}
	
	/** Update the position of agents. */
	public void frame_update(){
		for(int nr = 0; nr<queens.length; nr++){
			queens[nr] = queens[nr] + (int)((gl.get_board()[nr]*cell_size-queens[nr])*0.2d); // Update the queen positions
		}
	}
	
	public void paint(Graphics gr){
		Graphics2D g = (Graphics2D) gr;
		g.setBackground(Color.white); 
		g.clearRect(0, 0,getWidth(), getHeight()); 												// Clear the panel
		g.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,					// Anti-alias looks pretty
				RenderingHints.VALUE_ANTIALIAS_ON));
		
		// Draw lines and squares
		int z = gl.get_board().length;
		boolean dark = true;
		boolean dark2 = true;
		for(int y = 1; y <=z; y++){
			for(int x = 1; x <=z; x++){
				if(dark) g.setColor(Color.lightGray);
				else g.setColor(Color.white);
				g.fillRect(x*cell_size, y*cell_size, cell_size, cell_size);
				dark = !dark;
			}
			dark2 = !dark2;
			dark = dark2;
		}
		g.setColor(Color.black);
		for(int y = 1; y <= z+1; y++)
			g.drawLine(cell_size, y*cell_size, (z+1)*cell_size, y*cell_size); 			  	
		for(int x = 1; x <= z+1; x++)
			g.drawLine(x*cell_size,cell_size,x*cell_size,(z+1)*cell_size);					
		
		// Draw agents
		for(int i = 0; i < queens.length; i++)
			g.drawImage(queen, (i+1)*cell_size, queens[i]+cell_size, queen.getWidth(), queen.getHeight(), null);
		
		// Draw title
		g.setFont(font);
		g.drawString("N-Queens world. Found solutions: "+gl.get_solution_count(), cell_size, cell_size-3);
	}
	
	/**
	 * Save a screenshot of the draw panel.
	 * @param name Name of the screenshot in the file system.
	 */
	public void saveImage(String name){
		File file = new File(name+".png");
		try{
			if (!file.exists())file.createNewFile();
			BufferedImage image = new Robot().createScreenCapture(new Rectangle(getLocationOnScreen().x, getLocationOnScreen().y, getWidth(), getHeight()));
			ImageIO.write(image, "png", file);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Load an icon from the file system.
	 * @param icon_name File name to the icon.
	 * @return The BufferedImage that is created.
	 */
	public BufferedImage getIcon(String icon_name){
		try{ return ImageIO.read(new File(icon_name));
		} catch(Exception e){ System.out.println(icon_name);e.printStackTrace(); return null; }
	}

	/**
	 * Class to call the update method of the GUI.
	 * @author Bas Testerink
	 *
	 */
	private class FrameCaller implements Runnable {
		QueensGUI panel; 		// Panel on which the world is drawn
		int fps;		 		// Frames per second
		boolean halt = false;   // Halt condition
		
		/**
		 * Constructor.
		 * @param panel The WumpusGUI to call.
		 * @param fps Frames per second. 30 means 30 times a second WumpusGUI.frame_update() will be called.
		 */
		public FrameCaller(QueensGUI panel, int fps){
			this.panel = panel; 
			this.fps = fps;
		}
		
		public void run(){
			try{
				while(!halt){
					Thread.sleep(1000/fps); // Sleep a bit
					panel.frame_update();   // Update all data
					panel.repaint();		// Repaint the state
				}
			} catch(Exception e){ e.printStackTrace(); }
		}
		
		/**
		 * If this runnable is running and setHalt(true) is called, then this runnable will break the run loop.
		 * @param b New value of halt.
		 */
		public void setHalt(boolean b){
			halt = b;
		}
	}
}
