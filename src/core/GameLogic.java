package core;
/**
 * Core game logic of the N-Queens puzzle. Handles all game rules (which basically only tell
 * us that agents may not move off the board). Also keeps track of the amount of found
 * solutions.
 * @author Bas Testerink 
 *
 */
public class GameLogic {
	private int[] board = null; 		// Holds the game data
	private int solution_counter = 0; 	// Amount of found solutions
	
	public GameLogic(){}
	public GameLogic(int nrQueens){ init(nrQueens); }
	
	/**
	 * Initialize the puzzle for n queens.
	 * @param nrQueens Number of queens on the board.
	 */
	public void init(int nrQueens){
		board = new int[nrQueens];
		solution_counter = 0;
	}
	
	/**
	 * Move a queen up or down.
	 * @param queen Number of the queen (position from left to right, first is 0).
	 * @param up Whether to move up or down.
	 * @return Whether move was successful.
	 */
	public boolean move(int queen, boolean up){ 
		if(up&&board[queen]>=1) board[queen]--;							// Cannot move in the negative
		else if(!up&&board[queen]<(board.length-1)) board[queen]++;		// Board is square cannot move beyond it
		else return false;												// Was illegal move
		return true;													// Was successful
	}
	
	/**
	 * Check whether the current configuration is a solution.
	 * @return Whether the current configuration is a solution.
	 */
	public boolean is_solution(){
		for(int q1 = 0; q1 < board.length; q1++)
			for(int q2 = q1+1; q2 < board.length; q2++)
				if(board[q1] == board[q2] || (Math.abs(board[q1]-board[q2]) == q2-q1))
					return false; 
		solution_counter++;
		return true;
	}  
	
	/**
	 * Get the amount of found solutions.
	 * @return The solution counter.
	 */
	public int get_solution_count(){
		return solution_counter;
	}
	
	/**
	 * Obtain the board.
	 * @return The board data.
	 */
	public int[] get_board(){
		return board;
	}
}
