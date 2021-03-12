package zpc;

import java.util.HashMap;

import robocode.*;
//import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html




/**
 * Test - a robot by (your name here)
 */
public class Test extends Robot {
	/**
	 * run: Test's default behavior
	 */
	
	final static double PI = Math.PI; // 円周率を定数化


	public static void main(String[] args) {
		double x = -1;
		double y = 0;
		double ang = normalAngle(Math.PI / 2 - Math.atan2(-100, 0.1));
		//System.out.println("ang: " + Math.toDegrees(ang));
		System.out.println("ang: " + Math.sin(Math.toRadians(15))/PI);


	}

	  static double normalAngle(double kaku) {
		if (kaku > PI) {
			kaku -= PI * 2;
		}
		if (kaku < -PI) {
			kaku += PI * 2;
		}
		return kaku;
	}
}
