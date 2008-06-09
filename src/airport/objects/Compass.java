package airport.objects;

/**
 * Merely a struct that has a position and a direction. It's like a vector, but not.
 * @author Henry Yuen - hello
 *
 */
public class Compass {
	private double x,y;
	private double angle;

	public Compass clone() {
		//hello
		Compass c = new Compass();
		c.setAngle(angle);
		c.setX(x);
		c.setY(y);
		return c;
	}


	public Compass(double x,double y,double angle) {
		this.x = x;
		this.y = y;
		this.angle = angle;
	}

	public Compass() {
		x = y = angle = 0.0;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getDistanceTo(Compass compass) {
		if (compass == null) return 0.0;

		double dx = (x - compass.getX());
		double dy = (y - compass.getY());

		return Math.sqrt(dx*dx + dy*dy);
	}

	public String toString() {
		return "Compass: [" + x + "," + y + "," + angle + "]";
	}

	public Compass add(double x2,double y2) {
		double dx = x + x2;
		double dy = y + y2;

		Compass difference = new Compass();
		difference.setX(dx);
		difference.setY(dy);

		//figure out the angle
		double ang = Math.atan2(dy,dx);
		difference.setAngle(ang);
		return difference;
	}
	public Compass add(Compass compass) {
		return add(compass.getX(),compass.getY());
	}

	public Compass subtract(Compass compass) {
		return add(-compass.getX(),-compass.getY());
	}

	public double dotProduct(Compass compass) {
		return compass.getX()*x + compass.getY()*y;
	}
}
