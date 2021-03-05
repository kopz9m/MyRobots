package zpc;

import java.util.HashSet;
import java.util.Set;

import robocode.*;

public class BatMobile extends AdvancedRobot {
	double previousEnergy = 100;
	int movementDirection = 1;
	int gunDirection = 1;
	Set<String> enemies = new HashSet<String>();
	public void run() {
		setTurnGunRight(99999);
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		// get enemy name
		enemies.add(e.getName());
		System.out.println("I have enemies:" + enemies.size());
		
		// battle strategy
		// only one enemy
		if (enemies.size() == 1) {
			// Stay at right angles to the opponent
			setTurnRight(e.getBearing() + 90 - 30 * movementDirection);
			
			// If the bot has small energy drop,
			// assume it fired
			double changeInEnergy = previousEnergy - e.getEnergy();
			if (changeInEnergy > 0 && changeInEnergy <= 3) {
				// Dodge!
				movementDirection = -movementDirection;
				setAhead((e.getDistance() / 4 + 25) * movementDirection);
			}
			// When a bot is spotted,
			// sweep the gun and radar
			gunDirection = -gunDirection;
			setTurnGunRight(99999 * gunDirection);

			// Fire directly at target
			fire(1);

			// Track the energy level
			previousEnergy = e.getEnergy();
			
		//@more than 1 enemy	
		// anti gravitation		
		} else {
			
			
			
		}
		
		
		
	}
	
	public void onRobotDeath(RobotDeathEvent e) {
		enemies.remove(e.getName());
	}
	
	public void onHitByBullet(HitByBulletEvent e) {
		//
	}
	
}