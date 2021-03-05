package zpc;



import robocode.*;

public class TestBot extends AdvancedRobot {


	@Override
	public void run() {
		while (true) {
			setAhead(500);
			setTurnRight(60);
			execute();
		}
	
	}
}
