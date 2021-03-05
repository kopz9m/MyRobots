package zpc;
import java.util.HashMap;

import robocode.*;
//import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Test - a robot by (your name here)
 */
public class Test extends Robot
{
	/**
	 * run: Test's default behavior
	 */

	public static void main(String[] args) {
		double x = -1;
		double y = 0;
		double ang = Math.atan2(y, x)/Math.PI;
		double ab = robocode.util.Utils.normalAbsoluteAngle(ang);
		System.out.println("ang = " + ang );
		
		
		HashMap<Integer, String> Sites = new HashMap<Integer, String>();
        // 添加键值对
        Sites.put(1, "Google");
        Sites.put(2, "Runoob");
        Sites.put(3, "Taobao");
        Sites.put(3, "Zhihu");
        Sites.remove(5);
        System.out.println(Sites);
	}
}
