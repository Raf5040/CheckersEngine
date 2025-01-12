// Contributions by: Matthew Proetsch

import java.util.ArrayList;
import java.util.Vector;
import java.awt.*;


// Stores the game board as a 2D array of Squares.
// Also provides functionality to move the
// piece of one Square to another Square
public class Board {

	// Number of rows
    public static final int rows = 8;
    // Number of columns
    public static final int cols = 8;
    // An array of Squares that represents the game board
    private final Square[][] gameBoard;
	// An array of shorts that represent the game board while allowing
	// quicker calculations and less memory usage
	private final ProtoBoard protoBoard;


    // Constructor takes no args and produces a Board of
	// size rows x cols with alternating background colors
    public Board() {
    	gameBoard = new Square[rows][cols];

    	//Set up the game board with alternating colors
    	boolean lastcolor = false;
    	for(int i = 0; i < rows; i++) {
    		for(int j = 0; j < cols; j++) {
    			if(lastcolor)
					gameBoard[i][j] = new Square(Square.BackgroundColor.DARK, i, j);
    			else
					gameBoard[i][j] = new Square(Square.BackgroundColor.LIGHT, i, j);

    			//Toggle lastcolor
    			lastcolor = !lastcolor;
    		}
    		//Switch starting color for next row
    		lastcolor = !lastcolor;
    	}

		protoBoard = new ProtoBoard();
    }

    public static boolean inBounds(int row, int col) {
    	if(row >= 0 && row < rows && col >= 0 && col < cols)
    		return true;

    	return false;
    }

    public Square getSquare(int row, int col) {
        if(inBounds(row, col))
        	return gameBoard[row][col];

        return null;
    }

	// Returns the underlying (more efficient) protoboard of the board array
	public ProtoBoard getProtoBoard(){
		return protoBoard;
	}

    // Fill this Board with Black pieces on top, and Red pieces on bottom
    public void placeStartingPieces() {

		//Have the Black side on top, Red side on bottom
		//Establish the Black side first
		for(int row = 0; row < 3; row++) {
			for (int col = 1 - (row % 2); col < cols; col+= 2) {
				protoBoard.setSquare(row, col, (short) 2);
				getSquare(row, col).setOccupant(new Piece(Color.BLACK, row, col));
			}
		}

		//Now establish the Red side
		for(int row = 5; row < 8; row++) {
			for (int col = 1 - (row % 2); col < 8; col+= 2) {
				protoBoard.setSquare(row, col, (short) -2);
				getSquare(row, col).setOccupant(new Piece(Color.RED, row, col));
			}
		}
    }

	// General instructions for piece movement
	// yDir == +1 for down. yDir == -1 for up
	// xDir == +1 for right. xDir == -1 for left

	// assumes piece can move
	// performs move of a piece
	public void movePiece(int row, int col, int yDir, int xDir) {
		Piece piece = gameBoard[row][col].getOccupant();
		// update gameBoard array
		gameBoard[row][col].setOccupant(null);
		gameBoard[row + yDir][col + xDir].setOccupant(piece);
		// update graphics
		try {
			gameBoard[row][col].update(gameBoard[row][col].getGraphics());
			gameBoard[row + yDir][col + xDir].update(gameBoard[row + yDir][col + xDir].getGraphics());
		} catch (Exception ignored){

		}
		// update piece
		piece.setLoc(row + yDir, col + xDir);
	}

	// performs capture of a piece
	// assumes piece can capture
	public void capturePiece(int row, int col, int yDir, int xDir) {
		Piece piece  = gameBoard[row][col].getOccupant();
		//update board array
		gameBoard[row + yDir][col + xDir].setOccupant(null);
		gameBoard[row + 2 * yDir][col + 2 * xDir].setOccupant(piece);
		gameBoard[row][col].setOccupant(null);
		//update graphics
		try {
			gameBoard[row][col].update(gameBoard[row][col].getGraphics());
			gameBoard[row + yDir][col + xDir].update(gameBoard[row + yDir][col + xDir].getGraphics());
			gameBoard[row + 2 * yDir][col + 2 * xDir].update(gameBoard[row + 2 * yDir][col + 2 * xDir].getGraphics());
		} catch (Exception ignored){
		}
		//update piece
		piece.setLoc(row + 2*yDir, col + 2*xDir);
	}

	//assumes move is possible
	public void makeMove(AbstractMove abstractMove){
		// let protoBoard make move
		protoBoard.makeMove(abstractMove);

		if(abstractMove.getType()){
			Move move = (Move) abstractMove;
			int row = move.getRow();
			int col = move.getCol();
			int yDir = move.getYDir();
			int xDir = move.getXDir();
			Piece piece = gameBoard[row][col].getOccupant();
			// make move
			movePiece(row, col, yDir, xDir);
			// turn piece to king if necessary
			if(row + yDir == 7 || row + yDir == 0){
				piece.setIsKing(true);
			}
		}
		else{
			Capture capture = (Capture) abstractMove;
			for (int i = 0; i < capture.getSize(); i++) {
				int row = capture.getRow()[i];
				int col = capture.getCol()[i];
				int yDir = capture.getYDir()[i];
				int xDir = capture.getXDir()[i];
				Piece piece = gameBoard[row][col].getOccupant();
				// make capture
				capturePiece(row, col, yDir, xDir);
				// turn piece to king if necessary
				if(row + 2*yDir == 7 || row + 2*yDir == 0){
					piece.setIsKing(true);
				}
			}
		}
	}

	// Find all possible Squares to which this piece can move
	// Built upon generateLegalMoves
	public Vector<Square> getPossibleSquares(Piece p) {
		Color pColor = p.getColor();
		int color;
		if(pColor == Color.BLACK){
			color = 1;
		} else {
			color = -1;
		}
		ArrayList<AbstractMove> legalMoves = protoBoard.generateLegalMoves(color);
		Vector<Square> possibleSquares = new Vector<Square>();

		int row = p.getRow();
		int col = p.getCol();

		if(!legalMoves.isEmpty()) {
			//if legalMoves are captures
			if (!legalMoves.get(0).getType()) {
				for (AbstractMove legalMove : legalMoves) {
					Capture capture = (Capture) legalMove;
					//if location of capture coincides with location of piece
					if (capture.getRow()[0] == row && capture.getCol()[0] == col) {
						//add last square the piece lands on
						int lastIndex = capture.getSize() - 1;
						int finalRow = capture.getRow()[lastIndex] + 2 * capture.getYDir()[lastIndex];
						int finalCol = capture.getCol()[lastIndex] + 2 * capture.getXDir()[lastIndex];
						possibleSquares.add(gameBoard[finalRow][finalCol]);
					}
				}
			}
			//if legalMoves are moves
			else {
				for (AbstractMove legalMove : legalMoves) {
					Move move = (Move) legalMove;
					//if location of move coincides with location of piece
					if (move.getRow() == row && move.getCol() == col) {
						//add square piece moves to
						int finalRow = move.getRow() + move.getYDir();
						int finalCol = move.getCol() + move.getXDir();
						possibleSquares.add(gameBoard[finalRow][finalCol]);
					}
				}
			}
		}
		return possibleSquares;
	}


	// Highlight all the possible moves that can be made
	public void setSquaresHighlighted(Piece p, boolean doHighlight) {

		Vector<Square> possibleMoves = getPossibleSquares(p);

		if(doHighlight) {
			for(Square highlight : possibleMoves)
				highlight.setHighlight(true);
		}

		else {
			for(Square highlight : possibleMoves)
				highlight.setHighlight(false);
		}
	}
}
