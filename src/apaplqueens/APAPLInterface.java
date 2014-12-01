package apaplqueens;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
 
import javax.swing.JFileChooser;
import javax.swing.JFrame; 
import javax.swing.JOptionPane;
 

import core.GameLogic;
import GUI.QueensGUI;
import apapl.Environment;
import apapl.ExternalActionFailedException;
import apapl.data.APLIdent;
import apapl.data.APLList;
import apapl.data.APLNum;
import apapl.data.Term; 
/**
 * 2APL Interface to this class. To use N-Queens in 2APL, export the source code with this class as its entry point. I.e.
 * the run configuration should use this class' main function. Available actions are vertical movement, perceiving
 * the board, checking whether the solution is found, adapting the movement speed and turning the GUI off.
 * @author Bas Testerink 
 *
 */
public class APAPLInterface extends Environment {
	private GameLogic gl = null;												// The real world
	private HashMap<String, Integer> agents = new HashMap<String, Integer>();	// Registered agents
	private HashMap<Integer, String> agents2 = new HashMap<Integer, String>();	// Registered agents
	private static Term YES = new APLIdent("yes"), NO = new APLIdent("no");		
	private boolean show_gui = true;											// Can turn off GUI
	private QueensGUI g = null; 												// The GUI
	private int move_delay = 500;
	private String solutions_folder = "./resources/solutions/";					// Folder where solutions are stored
	
	/**
	 * Constructor. Here the queens world and GUI are initialized as well.
	 */
	public APAPLInterface(){  
		String s = (String)JOptionPane.showInputDialog(null,"Number of queens:","N-Queens puzzle",JOptionPane.PLAIN_MESSAGE,null,null,"8");
		int i = Integer.parseInt(s);											// Get the amount of allowed queens
		gl = new GameLogic(i);													// Create world
		if(show_gui){															// When a GUI is required
			g = new QueensGUI(gl, 30);											// Create GUI and set FPS
			JFrame f = new JFrame();											// If you want to change the interface then you want to customize this frame
			g.setPreferredSize(new Dimension(720, 720));						
			f.add(g);	
			f.addWindowListener(new StopTheAnimation());
			f.pack();
			f.setVisible(true);
			g.start_animation();												// Starts the animation thread
		} 
	}
	
	/** 
	 * Small class to stop the animation thread when closing the frame. If you reload the MAS then you do not want stack 
	 * these threads.
	 * @author Bas Testerink, Utrecht University, The Netherlands
	 *
	 */
	class StopTheAnimation extends WindowAdapter {
		  public void windowClosing(WindowEvent evt){g.stop_animation();} 		// Stop the animation  
	}

	
	/**
	 * Register an agent.
	 */
	protected void addAgent(String agName) {
		if(agents.size()<gl.get_board().length){				// If there are more queens allowed then the agent can register
			agents.put(agName,agents.size()); 					// Agents are identified by the order in which they entered
			agents2.put(agents.size()-1, agName);
		}
	}  
	
	/** Needed for compilation purposes. */
	public static void main(String [] args) { } 
	
	/**
	 * Move an agent to an adjacent tile.
	 * @param agName Name of the agent that performs the action.
	 * @param dir The direction must be up or down.
	 * @return Always returns 'yes' upon success.
	 * @throws ExternalActionFailedException Fails when agent is not registered, the direction is illegal, or if the move was not possible.
	 */
	public Term move(String agName, APLIdent dir) throws ExternalActionFailedException {
		Integer agent = agents.get(agName);											// Get the registration number
		if(agent!=null){															// If the agent is registered
			boolean success = false;
			if(dir.getName().equals("up")) success = gl.move(agent, true);			// Try to move accordingly
			else if(dir.getName().equals("down")) success = gl.move(agent, false);	// Note that y=0 is the bottom 
			else throw new ExternalActionFailedException("Not a valid move direction: "+dir.getName()); // Illegal argument
			if(show_gui&&move_delay>0){
				try{Thread.sleep(move_delay);}catch(Exception e){}					// Allow the animation to finish 
			}
			if(success) return YES;						
			else throw new ExternalActionFailedException("Could not move.");
		} else  throw new ExternalActionFailedException("Agent not registered."); 
	}
	
	/**
	 * Obtain the game's state.
	 * @param agName Agent that is perceiving.
	 * @return List containing first the agent's own id, and then the others' positions.
	 */
	public Term perceive(String agName) throws ExternalActionFailedException {
		Integer agent = agents.get(agName);									// Get the registration number 
		LinkedList<Term> terms = new LinkedList<Term>(); 					// Initialize list arguments
		if(agent!=null){
			terms.add(new APLNum(agent));									// Add agent's own number
			for(int q = 0; q < gl.get_board().length; q++){					// Add positions of other queens
				terms.add(new APLIdent(agents2.get(q)));
				terms.add(new APLNum(gl.get_board()[q]));
			}
		}
		return new APLList(terms);										// Build and return list 
	}
	
	/**
	 * Check whether configuration is solved.
	 * @param agName Agent that is perceiving.
	 * @return Whether answer is found.
	 */
	public Term finished(String agName) throws ExternalActionFailedException {
		Integer agent = agents.get(agName);												// Get the registration number
		if(agent!=null){																// If the agent was registered  
			if(gl.is_solution()){														// If the solution is found
				if(show_gui){															// Save picture if GUI is shown
					try{Thread.sleep(500);}catch(Exception e){}							// Allow animation to finish
					File f = new File(solutions_folder);
					if(!f.exists()){													// If the icon map does not exist
						JFileChooser jfc = new JFileChooser(".");						// Make file chooser in root map
						jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		// Directory only
						jfc.setAcceptAllFileFilterUsed(false);
						jfc.setDialogTitle("Choose icon directory");
						jfc.setSelectedFile(f); 										// Set the given selected file
						if(jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) 	// Get the user's choice
							solutions_folder = jfc.getSelectedFile().getPath()+"/"; 	// Set the directory with the images
					}
					g.saveImage(solutions_folder+"/sol"+gl.get_solution_count());		// Save the image
				}
				return YES;
			} else return NO; 
		} else  throw new ExternalActionFailedException("Agent not registered."); 		// Agent was not registered
	}

	/**
	 * Set the time it takes to move agents before control is returned.
	 * @param agName Acting agent.
	 * @return Always yes.
	 */
	public Term set_move_delay(String agName, APLNum amount) throws ExternalActionFailedException {
		Integer agent = agents.get(agName);											// Get the registration number
		if(agent!=null){															// If the agent was registered  
			move_delay = amount.getVal().intValue();
			return YES;
		} else  throw new ExternalActionFailedException("Agent not registered.");	// Agent was not registered
	}
		
	/**
	 * Turn the GUI on/off.
	 * @param agName Acting agent.
	 * @return Always yes.
	 */
	public Term set_show_gui(String agName, APLIdent value) throws ExternalActionFailedException {
		Integer agent = agents.get(agName);											// Get the registration number
		if(agent!=null){															// If the agent was registered  
			show_gui = value.getName().equalsIgnoreCase("yes");
			return YES;
		} else  throw new ExternalActionFailedException("Agent not registered."); 	// Agent was not registered
	}
} 