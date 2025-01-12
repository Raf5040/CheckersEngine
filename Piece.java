// Contributions by: Matthew Proetsch

import java.awt.*;

// A Piece on the game board that can be either red or black
// Pieces are taken into account when redrawing a Square on the game board
// and when a particular side runs out of pieces, the game is over
public class Piece {
	private int row; //Current row of the board that this Piece resides on
	private int col; //Current column of the board that this Piece resides on
	public Color color; //The color of this Piece
	private boolean isKing; //Indicates if the piece is a King or not


	//Initialize a new Piece with the given color and at the given position
	public Piece(Color c, int row, int col) {
		color = c;
		this.row = row;
		this.col = col;
		this.isKing = false;
	}

	// Copy constructor
	public Piece(Piece piece) {
		this.color = piece.getColor();
		this.row = piece.getRow();
		this.col = piece.getCol();
		this.isKing = piece.getIsKing();
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public Color getColor() {
		return color;
	}

	public boolean getIsKing(){
		return isKing;
	}

	//Set King status of this piece
	public void setIsKing(boolean b){
		isKing = b;
	}

	//Give this Piece a new location to live on in the game board
	public void setLoc(int row, int col) {
		this.row = row;
		this.col = col;
	}

	//Get the String representation of this Piece
	public String toString() {

		StringBuilder s = new StringBuilder();

		if(this.color == Color.BLACK)
			s.append("Black ");

		else
			s.append("Red ");

		s.append("piece at row " + this.getRow() +
				 ", col " + this.getCol());

		return s.toString();
	}
}
