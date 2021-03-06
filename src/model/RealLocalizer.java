package model;

import java.util.Random;
import control.EstimatorInterface;

public class RealLocalizer implements EstimatorInterface {

	private int rows, cols, head;
	private int realX, realY, realH;
	private double[][] showableGrid;
	private Random rand;
	private int[] latestReading;
	private double[] currentMaxProbPos;
	private double[][][] state;
	private int rounds=0;
	private int noReadings=0;
	private int sensorReportsTrueLocation;
	private int oneStepOff;
	private int twoStepsOff;
	private int moreThanTwoStepsOff;
	private int onPoint;
	
	public RealLocalizer( int rows, int cols, int head) {
		this.rows = rows;
		this.cols = cols;
		this.head = head;
		rand = new Random();
		
		realX = rand.nextInt(rows);
		realY = rand.nextInt(cols);
		realH = rand.nextInt(4);
		showableGrid = new double[rows][cols];
		latestReading = new int[2];
		currentMaxProbPos = new double[3];
		state = new double[rows][cols][4];
		
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
				showableGrid[i][j] = 1.0/(cols * rows);
				for(int k = 0; k < 4; k++){
					state[i][j][k] = 1.0/(cols * rows * 4);
				}
			}
		}
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
	
	//return the probability that the robot have/will move to square nX,nY with heading nH
	//assuming it has the previous position x, y, h
	public double getTProb( int x, int y, int h, int nX, int nY, int nH) {
				
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
		
		return (0.3/counter);

	}

	private boolean isWall(int nX, int nY){
		return nX < 0 || nX > rows -1 || nY < 0 || nY > cols -1;
	}
	
	private boolean isFacingWall(int nX, int nY, int nH){
		return ((nY == 0 && nH == 3) || (nY == cols -1 && nH == 1) || (nX == 0 && nH == 0) || (nX == rows -1 && nH == 2));
	}

		// returns the probability when robot is in cyanX,cyanY that we actually are in x,y
	public double getOrXY(int cyanX, int cyanY, int x, int y) {
		if(cyanX == x && cyanY == y){
			return 0.1;
		}
		
		if(Math.abs(cyanX-x) <= 1 && Math.abs(y-cyanY) <= 1){
			return 0.05;
		}
		
		if(Math.abs(cyanX-x) <= 2 && Math.abs(y-cyanY) <= 2){
			return 0.025;
		}

		return 0.0;
	}


	public int[] getCurrentTruePosition() {
		int[] ret = new int[2];
		ret[0] = realX;
		ret[1] = realY;
		return ret;

	}

	public int[] getCurrentReading() {
		if(isWall(latestReading[0],latestReading[1])){
			return null;
		}
		
		return latestReading;
	}


	public double getCurrentProb( int x, int y) {
		return showableGrid[x][y];	
	}

	public void update() {
		move();
		generateReadings();
		updateState();
	}
	
	private void generateReadings(){
		int randomValue = rand.nextInt(10);
		
		if(randomValue < 1){
			latestReading = getCurrentTruePosition();
		}else if(randomValue < 5){
			int randomStep = rand.nextInt(8);
			switch(randomStep){
			case 0: latestReading[0] = realX-1;
					latestReading[1] = realY-1;
					break;
			case 1: latestReading[0] = realX-1;
					latestReading[1] = realY;
					break;
			case 2: latestReading[0] = realX-1;
					latestReading[1] = realY+1;
					break;
			case 3: latestReading[0] = realX;
					latestReading[1] = realY-1;
					break;
			case 4: latestReading[0] = realX;
					latestReading[1] = realY+1;
					break;
			case 5: latestReading[0] = realX+1;
					latestReading[1] = realY-1;
					break;
			case 6: latestReading[0] = realX+1;
					latestReading[1] = realY;
					break;
			case 7: latestReading[0] = realX+1;
					latestReading[1] = realY+1;
					break;
			}
		}else if(randomValue < 9){
			int valueX, valueY;
			valueX = rand.nextInt(5) -2;
			if(Math.abs(valueX) <= 1){
				valueY = rand.nextInt(3) -1;
			}else{
				valueY = rand.nextInt(5) -2;
			}
			latestReading[0] = realX + valueX;
			latestReading[1] = realY + valueY;
//		Sensor didnt report anything
		}else{
			latestReading[0] = Integer.MAX_VALUE;
			latestReading[1] = Integer.MAX_VALUE;
			return;
		}
	}
	
	private void move(){
		double moveDouble = rand.nextDouble();
		double counter = 0;
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
				for(int k = 0; k < 4; k++){
					double tProb = getTProb(realX, realY, realH, i, j, k);
					if(tProb != 0.0){
						counter += tProb;
						if(moveDouble < counter){
							realX = i;
							realY = j;
							realH = k;
							return;
						}
					}
				}
				
			}
		}
	}
	
	private void updateState(){
		rounds++;
		System.out.println("Entered a new round");
		double normalizingSum = 0;
		boolean noReading = false;
		if(latestReading[0] == Integer.MAX_VALUE || latestReading[1] == Integer.MAX_VALUE){
			noReading = true;
			noReadings++;
			System.out.println("NoReading");
			System.out.println("No reading percentage: " + ((double)noReadings/rounds)* 100 + " %");
//			return;
		}		
		
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
//				if(!noReading){
//					For the current i,j, return the probability for the robot being there
					double sensorValue;
					if (noReading)
						sensorValue = getFakeOrXY(i, j);
					else
						sensorValue = getOrXY(latestReading[0], latestReading[1], i, j);
//					double sensorValue = getOrXY(latestReading[0], latestReading[1], i, j);					
					double[] headerValues = new double[4];
					double sum = 0;
					for(int k = 0; k < 4; k++){
						headerValues[k] = getHeaderProbability(i,j,k);
						sum += headerValues[k];
					}
					
					for(int k = 0; k < 4; k++){
						if(sum == 0 && sensorValue == 0){								
							state[i][j][k] = 0;
						}else if(sum == 0){
							state[i][j][k] += (sensorValue/4);
							normalizingSum += state[i][j][k];
						}else if(sensorValue == 0){
							state[i][j][k] = 0;
						}else{
							state[i][j][k] += (sensorValue/sum)*headerValues[k];
							normalizingSum += state[i][j][k];
						}
					}
				}
			}
//		}		
		// Insert values into the showGrid;
		// Summarize the heading values from each square and insert into showGrid		
		// Update currentMaxProbPos
		currentMaxProbPos[2] = 0;
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
				double headerSum = 0;
				for(int k = 0; k < 4; k++){
					headerSum += state[i][j][k];
				}

				showableGrid[i][j] = headerSum / normalizingSum;
				if(showableGrid[i][j] > currentMaxProbPos[2]){
					currentMaxProbPos[0] = i;
					currentMaxProbPos[1] = j;
					currentMaxProbPos[2] = showableGrid[i][j];
				}
			}
		}
		
		if(latestReading[0] == realX && latestReading[1] == realY){ // det var allt okeyt, d� testar vi igen
			sensorReportsTrueLocation++;
		}
		
		switch(howFarOff(currentMaxProbPos[0], currentMaxProbPos[1])){
		case 0: onPoint++;
				break;
		case 1: oneStepOff++;
				break;
		case 2:	twoStepsOff++;
				break;
		case 3: moreThanTwoStepsOff++;
				break;
		}
		
		System.out.println("No reading percentage: " + ((double) noReadings/rounds )* 100 + " %");
		System.out.println("Sensor reports correct location precentage " + ((double) sensorReportsTrueLocation/rounds )* 100 + " %");
		System.out.println("Estimation being on point: " + ((double) onPoint/rounds )* 100 + " %");
		System.out.println("Estimation being one step of: " + ((double) oneStepOff/rounds )* 100 + " %");
		System.out.println("Estimation being two steps of: " + ((double) twoStepsOff/rounds )* 100 + " %");
		System.out.println("Estimation being more than two steps of: " + ((double) moreThanTwoStepsOff/rounds )* 100 + " %");
	}
	
	private double getHeaderProbability(int x, int y, int h){
		double summarizedProb = 0;
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++) {
				for(int k = 0; k < 4; k++){
					double headerStateValue = state[i][j][k];
					summarizedProb += headerStateValue * getTProb(x, y, h, i, j, k);
				}
			}
		}
		return summarizedProb;
	}
	
	private int howFarOff(double estimatedLocationX, double estimatedLocationY){
		if(estimatedLocationX == realX && estimatedLocationY == realY){
			return 0;
		}else if(Math.abs(estimatedLocationX-realX) <= 1 && Math.abs(estimatedLocationY-realY) <= 1){
			return 1;
		}else if(Math.abs(estimatedLocationX-realX) <= 2 && Math.abs(estimatedLocationY-realY) <= 2){
			return 2;
		}else{
			return 3;
		}
	}
	
	private double getFakeOrXY(int i, int j) {
		int x = (int) currentMaxProbPos[0];
		int y = (int) currentMaxProbPos[1];
		int maxHeader = 0;
		// Find most probable direction based from state
		for(int k = 0; k < 4; k++){
				double currHeader = state[x][y][k];
				if (currHeader > state[x][y][maxHeader]){
					maxHeader = k;
				}
		}
		int fakeX = 0;
		int fakeY = 0;
		// Find the adjacent square from direction
		do {
			switch(maxHeader){
			case 0: fakeX = x-1; // up
					fakeY = y;
					break;
			case 1: fakeX = x; // right
					fakeY = y+1;
					break;
			case 2: fakeX = x+1; // down
					fakeY = y;
					break;
			case 3: fakeX = x;  // left
					fakeY = y-1;
					break;
			}
			if (maxHeader < 3){
				maxHeader += maxHeader;
			}
			else
				maxHeader = 0;
		}
		while(isWall(fakeX, fakeY));
		return getOrXY(fakeX, fakeY, i, j);
	}

}