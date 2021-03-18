package zpc;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
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
	double lastFireTime = 0;
	String onlyName = null;
	// int gravityDirection = 1;

	@Override
	public void run() {
		setAllColors(Color.black);
		setBulletColor(Color.white);
		enemyNumber = getOthers();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		turnRadarRight(360);

		while (true) {
			// System.out.println("start-----------------------------------------------------------");
			enemyNumber = getOthers();
			antiGravityMove();
			if (enemyNumber == 1) {
				doSweepScan();
			} else {
				doFullScan();
			}

			doFire();
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
			p = gravPoints.get(onlyName);
			out.println("11__distance: " + p.distance);
			double extraTurn = 0;
			if (getTime() - lastFireTime > 5) {
			//if (p.distance < 100) {

				double absBearing = getHeadingRadians() + p.bearingRadians;
				double gunTurn = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians());
				// gunTurn += extraTurn;
				if ((getGunHeadingRadians() - Utils.normalAbsoluteAngle(absBearing)) > 0) {
					//extraTurn = -Math.atan(36.0/p.distance);

				} else{
					//extraTurn = Math.atan(36.0/p.distance);
				};
				
				setTurnGunRightRadians(gunTurn + extraTurn);

				setFire(1000 / p.distance);
				lastFireTime = getTime();
			}
		} else {
			for (String name : gravPoints.keySet()) {
				p = (Z_GravPoint) gravPoints.get(name);
				double absBearing = getHeadingRadians() + p.bearingRadians;
				double gunTurn = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians());
				// double extraTurn = ;
				// gunTurn += extraTurn;
				setTurnGunRightRadians(gunTurn);
				if (getTime() - lastFireTime > 10 && Math.abs(p.velocity) == 0 && p.distance < 200) {

					setFire(100 / p.distance);
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
		// out.println("bullet: " + event.getBullet());
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
		Boolean parallel;

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
