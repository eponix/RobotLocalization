package model;

import control.EstimatorInterface;

public class RealLocalizer implements EstimatorInterface {
		
	private int rows, cols, head;

	public RealLocalizer( int rows, int cols, int head) {
		this.rows = rows;
		this.cols = cols;
		this.head = head;
		
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
		if(Math.abs(x-nX) > 1 || Math.abs(y-nY) > 1 /* || compare h and nH */){
			return 0.0;
		}
		else if (/*h  */)
		return 0.0;
	}

	public double getOrXY( int rX, int rY, int x, int y) {
		return 0.1;
	}


	public int[] getCurrentTruePosition() {
		
		int[] ret = new int[2];
		ret[0] = rows/2;
		ret[1] = cols/2;
		return ret;

	}

	public int[] getCurrentReading() {
		int[] ret = null;
		return ret;
	}


	public double getCurrentProb( int x, int y) {
		double ret = 0.0;
		return ret;
	}
	
	public void update() {
		System.out.println("Nothing is happening, no model to go for...");
	}
	
	
}