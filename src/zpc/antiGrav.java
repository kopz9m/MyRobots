package zpc;

import java.util.HashMap;
import java.util.Vector;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

public class antiGrav extends AdvancedRobot {

 	final double PI = Math.PI;									// ‰~ü—¦‚Ì’è”‰»
	//Map<String,GravPoint> gravPoints = new HashMap<>();
	HashMap<String,GravPoint> gravPoints = new HashMap<String,GravPoint>();

	@Override
	public void run() {
		turnRight(360);

		while (true) {

			System.out.println("my position X: " + getX());
			System.out.println("my position Y: " + getY());

			makeMove();
			execute();
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		double absBearing = e.getBearingRadians() + getHeadingRadians();
		
		gravPoints.put(e.getName(),new GravPoint(getX() + e.getDistance() * Math.sin(absBearing),
				getY() + e.getDistance() * Math.cos(absBearing), e.getEnergy()));

		System.out.println("found boot" + gravPoints);
	}
	
	
	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		// TODO Auto-generated method stub
		//super.onRobotDeath(event);
		gravPoints.remove(event.getName());
	}
	
	void makeMove() {
		double xforce = 0;
		double yforce = 0;
		double force;
		double ang;
		GravPoint p;
		for (String name : gravPoints.keySet()) {
					
			p = (GravPoint) gravPoints.get(name);
			// Calculate the total force from this point on us
			force = p.power / Math.pow(getRange(getX(), getY(), p.x, p.y), 2);
			// Find the bearing from the point to us
			ang = normalAngle( PI / 2 - Math.atan2(getY() - p.y, getX() - p.x));
			// Add the components of this force to the total force in their
			// respective directions
			xforce += Math.sin(ang) * force;
			yforce += Math.cos(ang) * force;
		
		}

		System.out.println("points grav X: " + xforce);
		System.out.println("points grav Y: " + yforce);

		/**
		 * The following four lines add wall avoidance. They will only affect us if the
		 * bot is close to the walls due to the force from the walls decreasing at a
		 * power 3.
		 **/
		// ‰E•Ç
		xforce += 5000 / Math.pow(getRange(getX(), getY(), getBattleFieldWidth(), getY()), 3);
		// ¶•Ç
		xforce -= 5000 / Math.pow(getRange(getX(), getY(), 0, getY()), 3);
		// ã•Ç
		yforce += 5000 / Math.pow(getRange(getX(), getY(), getX(), getBattleFieldHeight()), 3);
		// ‰º•Ç
		yforce -= 5000 / Math.pow(getRange(getX(), getY(), getX(), 0), 3);

		System.out.println("sum grav X: " + xforce);
		System.out.println("sum grav Y: " + yforce);

		// Move in the direction of our resolved force.
		
		double kaku = getHeadingRadians() + Math.atan2(yforce, xforce) - PI / 2;
		
		System.out.println("number of gravPoints="+ gravPoints.size());

		System.out.println("make move to,x = " + xforce);
		System.out.println("make move to,y = " + yforce);
		System.out.println("make move to,angle = " + Math.toDegrees(kaku));
		
		int dir;
		if (kaku > PI / 2) {
			kaku -= PI;
			dir = -1;
		}
		else if (kaku < -PI / 2) {
			kaku += PI;
			dir = -1;
		}
		else {
			dir = 1;
		}

		setTurnRightRadians(kaku);
		setAhead(20 );
		setTurnGunRight(Double.POSITIVE_INFINITY);
		
		System.out.println("turn right = " + kaku);
		System.out.println("go ahead  = " + 20 * dir);


	}
	
	//goTo(getX() - xforce, getY() - yforce);

	/*
	 * void goTo(double x, double y) { double dist = 20; double angle =
	 * Math.toDegrees(Math.atan2(y-getY(), x-getX())); double r = turnTo(angle);
	 * setAhead(dist * r);
	 * }
	 */
	/**
	 * Turns the shortest angle possible to come to a heading, then returns the
	 * direction the bot needs to move in.
	 **/
	/*
	 * int turnTo(double angle) { double ang; int dir; ang =
	 * robocode.util.Utils.normalAbsoluteAngleDegrees(getHeading() - angle); if (ang
	 * > 90) { ang -= 180; dir = -1; } else if (ang < -90) { ang += 180; dir = -1; }
	 * else { dir = 1; } setTurnLeft(ang); return dir; }
	 */
	
	/** Returns the distance between two points **/
	double getRange(double x1, double y1, double x2, double y2) {
		double x = x2 - x1;
		double y = y2 - y1;
		double range = Math.sqrt(x * x + y * y);
		return range;
	}
	
	double normalAngle(double angle) {
		if(angle > Math.PI) {
			angle -= Math.PI * 2;
		}
		if(angle < -Math.PI) {
			angle += Math.PI * 2;
		}
		return angle;
	}

	class GravPoint {
		public double x, y, power;
		public GravPoint(double pX, double pY, double pPower) {
			x = pX;
			y = pY;
			power = pPower;
		}
	}

}
