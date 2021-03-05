package zpc;

//package  type.com.xalead;
import java.awt.geom.Point2D;
import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class YuanZhou extends AdvancedRobot {
	public static void main(String[] args) {
		// double a = Math.tan(Math.toRadians(135));
		// System.err.println(a);
		// double a = Math.toDegrees(Math.atan2(10, -10));
		// System.err.println(a);
		System.out.println(Math.random());
	}

	private double time1 = 0;
	private Enemy enemy = new Enemy();
	private boolean discover = false;
	private double heading = 0.0;
	private double radarHeading = 0.0;
	private double bPower = 3;
	private double time = 0;
	private double distance = 3000;

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		discover = true;
		preTime = time1;
		time1 = getTime();
		enemy.setBearing(e.getBearingRadians());
		enemy.setSpeed(e.getVelocity());
		enemy.setDistance(e.getDistance());
		preHeading = enemy.getHeading();
		enemy.setHeading(e.getHeadingRadians());
		time = distance / Rules.getBulletSpeed(bPower);
	}// 装饰方法

	private void dressing() {
	} // 旋转方法

	private void severance() {
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
	}// 简单移动的方法

	private void simpleMove() {
		double increment = 0;
		if (enemy.getBearing() > 0) {
			increment = Math.PI / 2 - enemy.getBearing();
			setTurnLeftRadians(increment);
		} else {
			increment = Math.PI / 2 + enemy.getBearing();
			setTurnRightRadians(increment);
		}
		setAhead(1000);
	}

	private double safDis = 100;// 安全距离
	// 高级移动方法

	private void movement() {
		if (getDistanceRemaining() < 1) {
			double nx = 0;
			double ny = 0;
			nx = Math.random() * (getBattleFieldWidth() - 2 * safDis) + safDis;
			ny = Math.random() * (getBattleFieldHeight() - 2 * safDis) + safDis;
			double headArg = 90 - Math.atan2(ny - getY(), nx - getX());
			headArg = Utils.normalAbsoluteAngle(headArg);
			double dis = Point2D.distance(getX(), getY(), nx, ny);
			if (headArg - getHeadingRadians() > Math.PI / 2) {
				setTurnRightRadians(headArg - getHeadingRadians() + Math.PI);
				setAhead(-dis);
			} else {
				setTurnRightRadians(headArg - getHeadingRadians());
				setAhead(dis);
			}
		}
	}

	private void doScan() {
		if (discover) {
			heading = this.getHeadingRadians();
			radarHeading = this.getRadarHeadingRadians();
			double temp = radarHeading - heading - enemy.getBearing();
			temp = Utils.normalRelativeAngle(temp);
			temp *= 1.2;
			setTurnRadarLeftRadians(temp);
		}
	}

	private double firePower() {
		return bPower;
	}

	/**
	 *  
	 */
	private double immidate() {
		double increment = heading + enemy.getBearing() - getGunHeadingRadians();
		increment %= 2 * Math.PI;
		increment = Utils.normalRelativeAngle(increment);
		return increment;
	}

	/**
	 * 开枪
	 */
	private void gun() {
		// double increment = immidate ();
		double increment = circle();
		setTurnGunRightRadians(increment);
	}

	private double preHeading = 0.0; // 锟斤拷锟斤拷

	private double preTime = 0.0; // 前一锟斤拷时锟斤拷

	/*
	 * 曲线射击
	 */
	private double circle() {
		double t = 0.0;
		double ea = Utils.normalAbsoluteAngle(getHeadingRadians() + enemy.getBearing());
		double ex = getX() + enemy.getDistance() * Math.sin(ea);
		double ey = getY() + enemy.getDistance() * Math.cos(ea);
		double offsetHeading = enemy.getHeading() - preHeading;
		double dv = offsetHeading / (time1 - preTime);
		if (Math.abs(dv) < 0.00001) {
			dv += 0.00001;
		}
		double r = enemy.getSpeed() / dv;
		double preDistance = enemy.getDistance();
		for (int i = 0; i < 8; i++) {
			double bulletTime = preDistance / Rules.getBulletSpeed(bPower);
			double nextHeading = enemy.getHeading() + dv * bulletTime;
			double nextx = ex + r * Math.cos(enemy.getHeading()) - r * Math.cos(nextHeading);
			double nexty = ey + r * Math.sin(nextHeading) - r * Math.cos(enemy.getHeading());
			preDistance = Point2D.distance(getX(), getY(), nextx, nexty);
			t = Math.atan2(nexty - getY(), nextx - getX());
		}
		return Utils.normalRelativeAngle((Math.PI / 2 - t - getGunHeadingRadians()) % (2 * Math.PI));
	}

	/**
	 * 直线射击
	 *
	 * @return
	 */
	private double line() {
		double ea = Utils.normalAbsoluteAngle(getHeadingRadians() + enemy.getBearing());
		double ex = getX() + enemy.getDistance() * Math.sin(ea);
		double ey = getY() + enemy.getDistance() * Math.cos(ea);
		double s = 0;
		if (enemy.getSpeed() >= Rules.MAX_VELOCITY - 0.1) {
			s = enemy.getSpeed() * time;
		} else if (enemy.getSpeed() > 0.0) {
			double as = (Math.pow(Rules.MAX_VELOCITY, 2) - Math.pow(enemy.getSpeed(), 2)) / 2 * Rules.ACCELERATION;
			double vs = (time - (Rules.MAX_VELOCITY - enemy.getSpeed()) / Rules.ACCELERATION) * Rules.MAX_VELOCITY;
			s = as + vs;
		} else {
			s = 0.0;
		}
		double nextx = ex + s * Math.sin(enemy.getHeading());
		double nexty = ey + s * Math.cos(enemy.getHeading());
		distance = Point2D.distance(getX(), getY(), nextx, nexty);
		double t = Math.atan2(nexty - getY(), nextx - getX());
		return Utils.normalRelativeAngle((Math.PI / 2 - t - getGunHeadingRadians()) % (2 * Math.PI));
	}

	public void run() {

		dressing();

		severance();

		// setTurnRadarLeft(400);
		// execute();
		while (true) {
			if (!discover) {
				setTurnRadarLeftRadians(Math.PI * 2.1);
				execute();
			} else {

				movement();

				double fire = firePower();

				doScan();

				gun();
				// if (getGunTurnRemaining() <= 0) {

				setFire(fire);
				execute();
				// }

				loseTarget();
				execute();
			}
		}
	}

	private void loseTarget() {
		if ((getTime() - time1) >= 4)
			discover = false;
	}
}
