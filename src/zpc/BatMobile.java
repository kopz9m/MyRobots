package zpc;

import java.awt.Color;
import java.util.HashMap;
import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

public class BatMobile extends AdvancedRobot {

	final double PI = Math.PI; // â~é¸ó¶ÇÃíËêîâª
	// Map<String,GravPoint> gravPoints = new HashMap<>();
	HashMap<String, GravPoint> gravPoints = new HashMap<String, GravPoint>();
	int enemyNumber = 1;
	double hitRate = 0;
	int fireCount = 1;
	int hitCount = 0;
	String onlyName = null;

	@Override
	public void run() {
		setAllColors(Color.black);
		enemyNumber = getOthers();
		turnRight(360);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		while (true) {
			// System.out.println("start-----------------------------------------------------------");
			enemyNumber = getOthers();

			antiGravityMove();

			if (enemyNumber == 1) {
				//doFire();
				setFire(2);
			}

			execute();

		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		System.out.println("enemy found!!!");

		double absBearing = e.getBearingRadians() + getHeadingRadians();
		GravPoint g = new GravPoint(getX() + e.getDistance() * Math.sin(absBearing),
				getY() + e.getDistance() * Math.cos(absBearing), e.getEnergy(),
				gravPoints.containsKey(e.getName()) ? gravPoints.get(e.getName()).power : e.getEnergy(),
				e.getDistance(), e.getBearingRadians());
		gravPoints.put(e.getName(), g);

		// 1vs1
		if (gravPoints.size() == 1) {
			onlyName = e.getName();

		}
	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		// super.onRobotDeath(event);
		gravPoints.remove(event.getName());
	}

	@Override
	public void onBulletHit(BulletHitEvent event) {
		// TODO Auto-generated method stub
		super.onBulletHit(event);
		hitCount++;
	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
		// TODO Auto-generated method stub
		// super.onHitRobot(event);
		setTurnLeft(180);
	}

	void doFire() {
		if (gravPoints.size() == 1) {
			GravPoint p = gravPoints.get(onlyName);
			hitRate = hitCount / fireCount;
			double absBearing =  getHeadingRadians() + p.bearingRadians;
			setTurnGunRightRadians(normalAngle_from0to2PI(absBearing - getGunHeadingRadians()));
			out.println("fire: " + p.distance / 1000 * 3 * hitRate);
			out.println("bearingRadians: "  + p.bearingRadians);

			//setFire(1);
			fireCount++;
		}
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

		// ï«ÇîÇØÇÈ
		xforce -= 5000 / Math.pow(getRange(getX(), getY(), getBattleFieldWidth(), getY()), 3);
		xforce += 5000 / Math.pow(getRange(getX(), getY(), 0, getY()), 3);
		yforce -= 5000 / Math.pow(getRange(getX(), getY(), getX(), getBattleFieldHeight()), 3);
		yforce += 5000 / Math.pow(getRange(getX(), getY(), getX(), 0), 3);

		double forceAngleToUpDirection = normalAngle_from0to2PI(PI / 2 - Math.atan2(yforce, xforce));
		double turnAngle = forceAngleToUpDirection - getHeadingRadians();

		turnAngle = normalAngle(turnAngle);

		double allForce = Math.sqrt(Math.pow(xforce, 2) + Math.pow(yforce, 2));

		setTurnRightRadians(turnAngle);
		setAhead(10 / (allForce * enemyNumber));
		System.out.println("all Force = " + allForce);

		setTurnRadarRight(Double.POSITIVE_INFINITY);

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
		public double x, y, power, powerBefore, distance, bearingRadians;

		public GravPoint(double pX, double pY, double pPower, double pPowerBefore, double pDistance,
				double pBearingRadians) {
			x = pX;
			y = pY;
			power = pPower;
			powerBefore = pPowerBefore;
			distance = pDistance;
			bearingRadians = pBearingRadians;
		}
	}

}
