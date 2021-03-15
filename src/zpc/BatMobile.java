package zpc;

import java.awt.Color;
import java.util.HashMap;
import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class BatMobile extends AdvancedRobot {

	final double PI = Math.PI; // â~é¸ó¶ÇÃíËêîâª
	// Map<String,GravPoint> gravPoints = new HashMap<>();
	HashMap<String, Z_GravPoint> gravPoints = new HashMap<String, Z_GravPoint>();
	int enemyNumber = 3;
	double hitRate = 0;
	double fireCount = 1;
	double hitCount = 0;
	int radarTurnDirection = 1;
	String onlyName = null;
	String closestName = null;

	@Override
	public void run() {
		setAllColors(Color.black);
		enemyNumber = getOthers();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		turnRadarRight(360);

		while (true) {
			// System.out.println("start-----------------------------------------------------------");
			enemyNumber = getOthers();

			if (enemyNumber == 1) {
				doSweepScan();
			} else {
				doFullScan();
			}
			doFire();
			fireCount++;

			antiGravityMove();
			execute();
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		System.out.println("enemy found!!!");

		double absBearing = e.getBearingRadians() + getHeadingRadians();
		Z_GravPoint g = new Z_GravPoint(getX() + e.getDistance() * Math.sin(absBearing),
				getY() + e.getDistance() * Math.cos(absBearing), e.getEnergy(),
				gravPoints.containsKey(e.getName()) ? gravPoints.get(e.getName()).power : e.getEnergy(),
				e.getDistance(), e.getBearingRadians());
		gravPoints.put(e.getName(), g);

		// sweep
		if (enemyNumber == 1) {

			onlyName = e.getName();

		}
	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		// super.onRobotDeath(event);
		gravPoints.remove(event.getName());
		closestName = null;
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
			Z_GravPoint p = gravPoints.get(onlyName);
			hitRate = hitCount / fireCount;
			double absBearing = getHeadingRadians() + p.bearingRadians;
			double gunTurn = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians());
			// double extraTurn = ;
			// gunTurn += extraTurn;
			setTurnGunRightRadians(gunTurn);
			setFire(p.distance / 500 * 3);

			out.println("fire: " + p.distance / 500 * 3);
			out.println("hitRate: " + hitRate);
			// out.println("extraTurn: " + extraTurn);

		} else {
			if (closestName != null) {
				Z_GravPoint p = gravPoints.get(closestName);
				double absBearing = getHeadingRadians() + p.bearingRadians;
				double gunTurn = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians());
				// double extraTurn = ;
				// gunTurn += extraTurn;
				setTurnGunRightRadians(gunTurn);
				setFire(1.0);
			}
		}
	}

	void doFullScan() {
		setTurnRadarRight(Double.POSITIVE_INFINITY * radarTurnDirection);
	}

	void doSweepScan() {
		Z_GravPoint p = gravPoints.get(onlyName);
		double absBearing = getHeadingRadians() + p.bearingRadians;
		double radarTurn = Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians());
		double extraTurn = Math.min(Math.atan(36.0 / p.distance), Rules.RADAR_TURN_RATE_RADIANS);
		radarTurn += (radarTurn < 0 ? -extraTurn : extraTurn);
		setTurnRadarRightRadians(radarTurn);
		out.println("extraTurn: " + extraTurn);

	}

	void antiGravityMove() {
		double minDistance = 2000;
		double xforce = 0;
		double yforce = 0;
		double force;
		double ang;
		Z_GravPoint p;
		for (String name : gravPoints.keySet()) {
			p = (Z_GravPoint) gravPoints.get(name);
			// Calculate the total force from this point on us
			force = p.power / Math.pow(getRange(getX(), getY(), p.x, p.y), 2);
			// Find the bearing from the point to us
			ang = Utils.normalRelativeAngle(PI / 2 - Math.atan2(getY() - p.y, getX() - p.x));
			// Add the components of this force to the total force in their
			// respective directions
			xforce += Math.sin(ang) * force;
			yforce += Math.cos(ang) * force;

			if (p.distance < minDistance) {
				minDistance = p.distance;
				closestName = name;
			}

			System.out.println("name: " + name);
			System.out.println("power: " + p.power);
			System.out.println("before power: " + p.powerBefore);
		}

		// ï«ÇîÇØÇÈ
		xforce -= 5000 / Math.pow(getRange(getX(), getY(), getBattleFieldWidth(), getY()), 3);
		xforce += 5000 / Math.pow(getRange(getX(), getY(), 0, getY()), 3);
		yforce -= 5000 / Math.pow(getRange(getX(), getY(), getX(), getBattleFieldHeight()), 3);
		yforce += 5000 / Math.pow(getRange(getX(), getY(), getX(), 0), 3);

		double forceAngleToUpDirection = Utils.normalAbsoluteAngle(PI / 2 - Math.atan2(yforce, xforce));
		double turnAngle = forceAngleToUpDirection - getHeadingRadians();

		turnAngle = Utils.normalRelativeAngle(turnAngle);

		double allForce = Math.sqrt(Math.pow(xforce, 2) + Math.pow(yforce, 2));

		
		/*
		 * if (turnAngle < -PI / 2) { setTurnRightRadians(PI + turnAngle); setBack(2 /
		 * allForce); System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		 * 
		 * } else if (turnAngle > PI / 2) { setTurnRightRadians(turnAngle - PI);
		 * setBack(2 / allForce);
		 * System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		 * 
		 * } else { setTurnRightRadians(turnAngle); setAhead(2 / allForce); }
		 */
		
		
		setTurnRightRadians(turnAngle);
		setAhead(3 / (allForce * enemyNumber));

		System.out.println("all Force = " + allForce);

	}

	/** Returns the distance between two points **/
	double getRange(double x1, double y1, double x2, double y2) {
		double x = x2 - x1;
		double y = y2 - y1;
		double range = Math.sqrt(x * x + y * y);
		return range;
	}

	class Z_GravPoint {
		public double x, y, power, powerBefore, distance, bearingRadians;

		public Z_GravPoint(double pX, double pY, double pPower, double pPowerBefore, double pDistance,
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
