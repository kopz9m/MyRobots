package zpc;

import java.awt.Color;
import java.util.HashMap;
import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

public class antiGrav extends AdvancedRobot {

	final double PI = Math.PI; // â~é¸ó¶ÇÃíËêîâª
	// Map<String,GravPoint> gravPoints = new HashMap<>();
	HashMap<String, GravPoint> gravPoints = new HashMap<String, GravPoint>();
	int enemyNumber = 1;

	@Override
	public void run() {

		enemyNumber = getOthers();
		setAllColors(Color.WHITE);

		turnRight(360);

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		while (true) {
			enemyNumber = getOthers();
			antiGravityMove();
			execute();

		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		// îΩèdóÕçÏêÌ
		double absBearing = e.getBearingRadians() + getHeadingRadians();
		GravPoint g = new GravPoint(getX() + e.getDistance() * Math.sin(absBearing),
				getY() + e.getDistance() * Math.cos(absBearing), e.getEnergy(),
				gravPoints.containsKey(e.getName()) ? gravPoints.get(e.getName()).power : e.getEnergy());
		gravPoints.put(e.getName(), g);

	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		// super.onRobotDeath(event);
		gravPoints.remove(event.getName());
	}

	@Override
	public void onHitWall(HitWallEvent event) {
		// TODO Auto-generated method stub
		// super.onHitWall(event);
		System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
		// TODO Auto-generated method stub
		super.onHitRobot(event);
		setBack(5);
	}

	void antiGravityMove() {
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
			ang = normalAngle(PI / 2 - Math.atan2(getY() - p.y, getX() - p.x));
			// Add the components of this force to the total force in their
			// respective directions
			xforce += Math.sin(ang) * force;
			yforce += Math.cos(ang) * force;

			System.out.println("name: " + name);
			System.out.println("power: " + p.power);
			System.out.println("before power: " + p.powerBefore);

		}

		/**
		 * The following four lines add wall avoidance. They will only affect us if the
		 * bot is close to the walls due to the force from the walls decreasing at a
		 * power 3.
		 **/

		// âEï«
		xforce -= 5000 / Math.pow(getRange(getX(), getY(), getBattleFieldWidth(), getY()), 3);
		// ç∂ï«
		xforce += 5000 / Math.pow(getRange(getX(), getY(), 0, getY()), 3);
		// è„ï«
		yforce -= 5000 / Math.pow(getRange(getX(), getY(), getX(), getBattleFieldHeight()), 3);
		// â∫ï«
		yforce += 5000 / Math.pow(getRange(getX(), getY(), getX(), 0), 3);

		// Move in the direction of our resolved force.

		double forceAngleToUpDirection = normalAngle_from0to2PI(PI / 2 - Math.atan2(yforce, xforce));
		double turnAngle = forceAngleToUpDirection - getHeadingRadians();
		// double kaku = getHeadingRadians() + Math.atan2(yforce, xforce) - PI / 2;
		// turnAngle = normalAngle(turnAngle);

		System.out.println("getHeading " + getHeading());

		System.out.println("force anngle = " + Math.toDegrees(PI / 2 - Math.atan2(yforce, xforce)));
		System.out.println("turnAngle = " + Math.toDegrees(turnAngle));

		/*
		 * if (turnAngle > PI / 2) { turnAngle -= PI; dir = -1; } else if (turnAngle <
		 * -PI / 2) { turnAngle += PI; dir = -1; } else { dir = 1; }
		 */

		turnAngle = normalAngle(turnAngle);
		System.out.println("turnAngle after normal = " + Math.toDegrees(turnAngle));

		/*
		 * if (turnAngle > PI) { turnAngle -= 2 * PI; } else if (turnAngle <= -PI) {
		 * turnAngle += 2 * PI;
		 * 
		 * }
		 */
		double allForce = Math.sqrt(Math.pow(xforce, 2) + Math.pow(yforce, 2));

		if (turnAngle < -PI / 2) {
			setTurnRightRadians(PI + turnAngle);
			setBack(2 / allForce);
			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

		} else if (turnAngle > PI / 2) {
			setTurnRightRadians(turnAngle - PI);
			setBack(2 / allForce);
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		} else {
			setTurnRightRadians(turnAngle);
			setAhead(2 / allForce);
		}

		setTurnRadarRight(Double.POSITIVE_INFINITY);

		// System.out.println("turn right = " + kaku);
		// System.out.println("go ahead = " + 20 * dir);

	}

	/** Returns the distance between two points **/
	double getRange(double x1, double y1, double x2, double y2) {
		double x = x2 - x1;
		double y = y2 - y1;
		double range = Math.sqrt(x * x + y * y);
		return range;
	}

	double normalAngle(double angle) {
		if (angle >= Math.PI) {
			angle -= Math.PI * 2;
		}
		if (angle < -Math.PI) {
			angle += Math.PI * 2;
		}
		return angle;
	}

	double normalAngle_from0to2PI(double angle) {
		if (angle >= 2 * PI) {
			angle -= 2 * PI;
		}
		if (angle < 0) {
			angle += 2 * PI;
		}
		return angle;
	}

	class GravPoint {
		public double x, y, power, powerBefore;

		public GravPoint(double pX, double pY, double pPower, double pPowerBefore) {
			x = pX;
			y = pY;
			power = pPower;
			powerBefore = pPowerBefore;
		}
	}

}
