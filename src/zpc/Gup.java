package zpc;

import robocode.*;
import robocode.util.*;
import java.util.*;
import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * �����𐧌䂷��t���O�N���X
 */
class Direction {
	int Flag;

	// �R���X�g���N�^
	public Direction() {
		this.Flag = 1;
	}

	// �擾
	public int getDir() {
		return this.Flag;
	}

	// ���]
	public void flip() {
		this.Flag *= -1;
	}
}
/**
 * �G���{�b�g���Ǘ�����N���X
 */
class Enemy {
	String name;
	public boolean live;
	public double bearing;
	public double head;
	public long checkTime;
	public double speed;
	public double x, y;
	public double distance;
	public double energy, previousEnergy;
	public double nextX, nextY;

	// �R���X�g���N�^
	Enemy() {
		distance = 100000;
		previousEnergy = 100;
	}

	public void setNextXY() {
		nextX = x;
		nextY = y;
	}

	public void setEnergy(double engy) {
		previousEnergy = energy;
		energy = engy;
	}
}

/*
 * �G�̐i�H����`�\��
 */
class LinerEnemyRad extends Enemy {
	final double PI = Math.PI;

	public boolean setNextXY(double x0, double y0) {
		boolean flag;
		double dX = x - x0;
		double dY = y - y0;

		double targetOwnHeading = PI/2 - head;
		double vX = Math.cos(targetOwnHeading) * speed;
		double vY = Math.sin(targetOwnHeading) * speed;

		// Ax^2+Bx+C=0
		double A = vX * vX + vY * vY - 289;
		double B = 2 * vX * dX + 2 * vY * dY;
		double C = dX * dX + dY * dY;

		double t1, t2;

		if (B * B > 4 * A * C) {
			t1 = (- B - Math.sqrt(B * B - 4 * A * C)) / (2 * A);
			t2 = (- B + Math.sqrt(B * B - 4 * A * C)) / (2 * A);
			if (t1 < 0) {
				t1 = t2 + 1;
			}
			if (t2 < 0) {
				t2 = t1 + 1;
			}
			if (t1 > t2) {
				t1 = t2;
			}

			nextX = x + vX * t1;
			nextY = y + vY * t1;
			flag = true;
		} else {
			flag = false;
		}
		return flag;
	}
}

/*
 * �d�̓|�C���g�Ɨ͂��i�[����N���X
 */
class GravPoint {
	public double x, y, power, times;

	public GravPoint (double pX, double pY, double pPower) {
		x = pX;
		y = pY;
		power = pPower;
		times = 200;
	}
}

/*
 * ���ʏ������C�u����
 */
class Lib {
	final double PI = Math.PI;									// �~������萔��

	// 180�x(PI)�ȏ��]����ꍇ��␳
	public double normalAngle(double kaku) {
		if(kaku > PI) {
			kaku -= PI * 2;
		}
		if(kaku < -PI) {
			kaku += PI * 2;
		}
		return kaku;
	}

	// ���������߂�
	public double getRange(double x1, double y1, double x2, double y2) {
		double x0 = x2 - x1;
		double y0 = y2 - y1;
		double h = Math.sqrt(x0 * x0 + y0 * y0);
		return h;
	}
}

/**
 * GuP - a robot by WakuwakuP
 */
public class Gup extends AdvancedRobot {
	/**
	 * run: GuP's default behavior
	 */

 	final double PI = Math.PI;									// �~�����̒萔��

	LinerEnemyRad target = new LinerEnemyRad();	// �G�̏����Ǘ�����
	Hashtable targets = new Hashtable();				// �G���Ǘ��N���X���W���Ǘ�����

	double sweepKaku = PI;											// ���[�_�\�̉�����
	Direction movDir = new Direction();					// �i�s�����̊Ǘ�
	Direction sweepDir = new Direction();				// ���[�_�[�̕������Ǘ�
	Lib help = new Lib();

	// �萔
	static final int SEARCH_DEPTH = 30;					// �o�b�t�@�̌�����
	static final int MAX_RANGE = 800;						// �����͈�

	// �O���[�o��
	static int historyIndex;
	static double arcLength[] = new double[100000];
	static StringBuffer patternMatcher = new StringBuffer();

	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:
		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		turnRadarRight(360);

		while(true) {
			// do_Move();
			do_GravMove();
			do_Scanner();
			execute();
		}
	}

	void do_GravMove() {
		double xforce = 0;
		double yforce = 0;
		double force;
		double ang;
		GravPoint p;
		Enemy en;
		Enumeration e = targets.elements();

		while (e.hasMoreElements()) {
			en = (Enemy)e.nextElement();
			if (en.live) {
				final double ENEMYFORCE = 300;
				p = new GravPoint(en.x, en.y, ENEMYFORCE);
				force = p.power / Math.pow(help.getRange(getX(), getY(), p.x, p.y), 2);
				ang = help.normalAngle(PI / 2 - Math.atan2(getY() - p.y, getX() - p.x));
				xforce += Math.sin(ang) * force;
				yforce += Math.cos(ang) * force;
			}
		}

		final double WALLFORCE = 800;
		xforce += WALLFORCE / Math.pow(help.getRange(getX(), getY(), getBattleFieldWidth(), getY()), 3);
		xforce -= WALLFORCE / Math.pow(help.getRange(getX(), getY(), 0, getY()), 3);
		yforce += WALLFORCE / Math.pow(help.getRange(getX(), getY(), getX(), getBattleFieldHeight()), 3);
		yforce -= WALLFORCE / Math.pow(help.getRange(getX(), getY(), getX(), 0), 3);

		double changeInEnergy = target.previousEnergy - target.energy;
		if (0 != changeInEnergy) {
			p =  new GravPoint(getX() + movDir.getDir(),getY() + movDir.getDir(), 500);
			force = p.power / Math.pow(help.getRange(getX(), getY(), p.x, p.y), 1);
			ang = help.normalAngle(PI / 2 - Math.atan2(getY() - p.y, getX() - p.x));
			xforce += Math.sin(ang) * force;
			yforce += Math.cos(ang) * force;
		}

		double kaku = getHeadingRadians() + Math.atan2(yforce, xforce) - PI / 2;

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

		if (0 < changeInEnergy && changeInEnergy <= 3) {
			double moveDistance;
			if (target.distance > 400) {
				moveDistance = 100;
			} else {
				moveDistance = 300;
			}
			setTurnRightRadians(kaku);
			setAhead(moveDistance * dir);
		}
	}

	void do_Scanner() {
		double angleToEnemy = getHeadingRadians() + target.bearing;
		double radarTurn = Utils.normalRelativeAngle( angleToEnemy - getRadarHeadingRadians() );
		double extraTurn = Math.min( Math.atan( 36.0 / target.distance ), Rules.RADAR_TURN_RATE_RADIANS );
		radarTurn += (radarTurn < 0 ? -extraTurn : extraTurn);
		setTurnRadarRightRadians(radarTurn);
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	 public void onScannedRobot(ScannedRobotEvent e) {
 		//�G���{�b�g�̕��������߂�
 		double absbearing_rad = (getHeadingRadians()+e.getBearingRadians())%(2*PI);

 		//�G���{�b�g�̏����Z�b�g����
 		target.name = e.getName();
 		target.x = getX()+Math.sin(absbearing_rad)*e.getDistance(); //�G���{�b�g�̌��݈ʒuX�����߂�
 		target.y = getY()+Math.cos(absbearing_rad)*e.getDistance(); //�G���{�b�g�̌��݈ʒuY�����߂�
 		target.bearing = e.getBearingRadians();
 		target.head = e.getHeadingRadians();
 		target.checkTime = getTime();				//�����L�^��������
 		target.speed = e.getVelocity();
 		target.distance = e.getDistance();

 		target.setEnergy(e.getEnergy());			//�G���{�b�g�̃G�l���M�[���L�^
 		sweepKaku = PI/10;							//���[�_�[�̉����ʂ����Z�b�g

 		double targetBearing = e.getBearingRadians();
 		int matchIndex;
 		int searchDepth = SEARCH_DEPTH;
		int bulletSpeed = 0;
		int searchEndBuffer = 0;

 		//�U��
		if (0 < getEnergy()) {
			if (getEnergy() < 5) {
				setFire(0.8);
				bulletSpeed = 20 - (int)(3 * 0.1);
			} else {
				if (300 > target.distance) {
					setFire(3);
					bulletSpeed = 11;
				} else if (300 <= target.distance && target.distance <= 800) {
					setFire((-(target.distance - 300) + 500) / 227 + 0.8);
					bulletSpeed = 20 - (int)(3 * 0.1);
				} else {
					setFire(0.1);
					bulletSpeed = 20 - (int)(3 * 0.1);
				}
			}
			searchEndBuffer = searchDepth + MAX_RANGE / bulletSpeed;
		}
 		//�G�̈ړ��ʂ��v�Z�E�L�^
 		double arcMovement = e.getVelocity() * Math.sin(e.getHeadingRadians() - (targetBearing += getHeadingRadians()));
 		arcLength[++historyIndex] = arcLength[historyIndex - 1] + arcMovement;
 		patternMatcher.append((char)(arcMovement));

 		//�p�^�[���}�b�`���O
 		do
 		{
 			matchIndex = patternMatcher.lastIndexOf(
 							patternMatcher.substring(Math.max(historyIndex-searchDepth,0)),
 							historyIndex-searchEndBuffer);
 		} while (--searchDepth*matchIndex < -1);
 		matchIndex += searchDepth;

 		// �C����G�Ɍ�����
 		setTurnGunRightRadians(Math.sin(
 			(arcLength[matchIndex+((int)(e.getDistance()/bulletSpeed))]-arcLength[matchIndex])/e.getDistance() +
 			targetBearing - getGunHeadingRadians() ));
 	}
}