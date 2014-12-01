import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import GUI.QueensGUI;
import core.GameLogic;

/**
 * Test class.
 * @author Bas Testerink, Utrecht University, The Netherlands
 *
 */
public class QueenTest extends JFrame {
	public static void main(String[] args){
		GameLogic gl = new GameLogic(7);
		QueensGUI g = new QueensGUI(gl, 30);
		gl.is_solution();
		QueenTest t = new QueenTest(g);
	}
	
	public JPanel content;
	public QueenTest(QueensGUI g){
		this.setLayout(new BorderLayout());
		add(g,BorderLayout.CENTER);
		setVisible(true);
	}
}
