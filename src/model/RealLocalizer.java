package model;

import java.util.Random;

import control.EstimatorInterface;

public class RealLocalizer implements EstimatorInterface {

	private int rows, cols, head;
	private int realX, realY;
	private double[][] state;

	public RealLocalizer( int rows, int cols, int head) {
		this.rows = rows;
		this.cols = cols;
		this.head = head;
		Random rand = new Random();
		
		realX = rand.nextInt(rows);
		realY = rand.nextInt(cols);
		state = new double[rows][cols];
	}	

	public int getNumRows() {
		return rows;
	}

	public int getNumCols() {
		return cols;
	}

	public int getNumHead() {
		return head;
	}

	public double getTProb( int x, int y, int h, int nX, int nY, int nH) {
//		return nH; 
		
		// Is the next step further away then one step or diagonal
		if(Math.abs(x-nX) > 1 || Math.abs(y-nY) > 1 || (x != nX && y != nY) || (x == nX && y == nY)){
			return 0.0;
		}
		
		if(((nY == y-1 && nH == 3) || (nY == y+1 && nH == 1) || (nX == x-1 && nH == 0) || (nX == x+1 && nH == 2)) && nH == h){
			return 0.7;
		}

		// Are the next step in a adjacent square and the direction does not correspond to an legal turn
		if((nX == x-1 && nH != 0) || (nX == x+1 && nH != 2) || (nY == y-1 && nH != 3) || (nY == y+1 && nH != 1)){
			return 0;
		}
		
		int counter = 0;
		for(int i = x-1; i <= x+1; i += 2){
			if(!isWall(i,y)) 
				counter++;
		}
		
		for(int j = y-1; j <= y+1; j += 2){
			if(!isWall(x,j)) 
				counter++;
		}
		
		if(!isFacingWall(x, y, h)){
			counter--;
		}else{
			return 1.0/counter;
		}
		System.out.println("counter: " + counter);
		
		return (0.3/counter);
	}

	private boolean isWall(int nX, int nY){
		return nX < 0 || nX > rows -1 || nY < 0 || nY > cols -1;
	}
	
	private boolean isFacingWall(int nX, int nY, int nH){
		return ((nY == 0 && nH == 3) || (nY == cols -1 && nH == 1) || (nX == 0 && nH == 0) || (nX == rows -1 && nH == 2));
	}

	public double getOrXY(int cyanX, int cyanY, int x, int y) {
		if(cyanX == x && cyanY == y){
			return 0.1;
		}
		
		if(Math.abs(cyanX-x) <= 1 && Math.abs(y-cyanY) <= 1){
//			int counter = 0;
//			for(int i = cyanY-1; i <= cyanY+1; i++){
//				for(int j = cyanX; j <= cyanX+1; j++){
//					if(isWall(j, x)){
//						counter++;
//					}
//				}
//			}
			return 0.05;
		}
		
		if(Math.abs(cyanX-x) <= 2 && Math.abs(y-cyanY) <= 2){
			return 0.025;
		}
		
		return 0.1;
	}


	public int[] getCurrentTruePosition() {
		int[] ret = new int[2];
		ret[0] = realX;
		ret[1] = realY;
		return ret;

	}

	public int[] getCurrentReading() { // TO DO
		int[] ret = null;
		return ret;
	}


	public double getCurrentProb( int x, int y) {
		return state[x][y];	
	}

	public void update() { // vi ska ändra posistion på vår real robot, använd föregående state+sensor för att uppdatera vår state.
		// kan vi använda getOrXY för att göra något.
		System.out.println("Nothing is happening, no model to go for...");
	}


}