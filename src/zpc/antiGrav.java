package zpc;

import java.awt.Color;
import java.util.HashMap;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class antiGrav extends AdvancedRobot {

	final double PI = Math.PI; // â~é¸ó¶ÇÃíËêîâª
	// Map<String,GravPoint> gravPoints = new HashMap<>();
	HashMap<String, Z_GravPoint> gravPoints = new HashMap<String, Z_GravPoint>();
	int enemyNumber = 3;
	double hitRate = 0;
	double fireCount = 1;
	double hitCount = 0;
	double lastFireTime = 0;
	String onlyName = null;

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

			if (enemyNumber == 1)
				doSweepScan();
			else
				doFullScan();
			//doFire();

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
				e.getDistance(), e.getBearingRadians(), e.getVelocity());
		gravPoints.put(e.getName(), g);

		// sweep
		if (enemyNumber == 1) {

			onlyName = e.getName();

		}
	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		// super.onRobotDeath(event);
		if (gravPoints.size() > 1 && gravPoints.containsKey(event.getName())) {
			gravPoints.remove(event.getName());
		}
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
		Z_GravPoint p;
		if (gravPoints.size() < 2 && gravPoints.containsKey(onlyName)) {
			if (getTime() - lastFireTime > 3) {

				p = gravPoints.get(onlyName);
				hitRate = hitCount / fireCount;
				double absBearing = getHeadingRadians() + p.bearingRadians;
				double gunTurn = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians());
				// double extraTurn = ;
				// gunTurn += extraTurn;
				setTurnGunRightRadians(gunTurn);
				setFire(300 / p.distance);
				lastFireTime = getTime();
			}
		} else {
			for (String name : gravPoints.keySet()) {
				p = (Z_GravPoint) gravPoints.get(name);
				if (getTime() - lastFireTime > 10 && Math.abs(p.velocity) < 0.1 ) {
					double absBearing = getHeadingRadians() + p.bearingRadians;
					double gunTurn = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians());
					// double extraTurn = ;
					// gunTurn += extraTurn;
					setTurnGunRightRadians(gunTurn);
					setFire(300 / p.distance);
					lastFireTime = getTime();
				}

			}
		}

	}

	void doFullScan() {
		setTurnRadarRight(999);
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		// TODO Auto-generated method stub
		// super.onBulletMissed(event);
		out.println("bullet: " + event.getBullet());
	}

	void doSweepScan() {
		if (gravPoints.containsKey(onlyName)) {
			Z_GravPoint p = gravPoints.get(onlyName);
			double absBearing = getHeadingRadians() + p.bearingRadians;
			double radarTurn = Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians());
			double extraTurn = Math.min(Math.atan(36.0 / p.distance), Rules.RADAR_TURN_RATE_RADIANS);
			radarTurn += (radarTurn < 0 ? -extraTurn : extraTurn);
			setTurnRadarRightRadians(radarTurn);
			out.println("extraTurn: " + extraTurn);
		} else
			setTurnRadarRight(999);

	}

	void antiGravityMove() {
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

	}

	/** Returns the distance between two points **/
	double getRange(double x1, double y1, double x2, double y2) {
		double x = x2 - x1;
		double y = y2 - y1;
		double range = Math.sqrt(x * x + y * y);
		return range;
	}

	class Z_GravPoint {
		double x, y, power, powerBefore, distance, bearingRadians, velocity;

		Z_GravPoint(double pX, double pY, double pPower, double pPowerBefore, double pDistance, double pBearingRadians,
				double pVelocity) {
			x = pX;
			y = pY;
			power = pPower;
			powerBefore = pPowerBefore;
			distance = pDistance;
			bearingRadians = pBearingRadians;
			velocity = pVelocity;
		}
	}

}
