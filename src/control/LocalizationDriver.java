package control;

import view.*;

public class LocalizationDriver extends Thread {
	
	private RobotLocalizationViewer l;
	long timer;
	
	public LocalizationDriver( long stepTime, RobotLocalizationViewer v) {
		this.l = v;
		this.timer = stepTime;
	}
	
	public void run() {
		int c = 0;					// used for simulating 10000 steps and then stop
		while( !isInterrupted()) {
			if(c > 10000){				
				interrupt();
				break;
			}
			c++;
			
			try{
				l.updateContinuously();
				sleep( timer);
			} catch( InterruptedException e) {
				System.out.println( "oops");
				return;
			}

		}
	}
	
}