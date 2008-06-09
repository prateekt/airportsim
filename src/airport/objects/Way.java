package airport.objects;

import java.util.*;

public class Way
{
	protected String name;
	protected int x,y;
	protected int width,length;
	protected String type;	//taxiway or runway
	protected double angle;

	protected ArrayList<String> connectedWays;	//these are all the runways connected to this
														//runway

	protected ArrayList<String> connectedGates;	//these are al the gates connected to this way

	protected Airport airport;

	protected Compass alpha,beta;

	public Way(String name,String type,Airport airport)
	{
		this.airport = airport;
		this.name = name;
		this.type = type;
		connectedWays = new ArrayList<String>();
		connectedGates = new ArrayList<String>();
		alpha = beta = null;
	}

	public Way()
	{
		name = "";
		type = "runway";
		connectedWays = new ArrayList<String>();
		connectedGates = new ArrayList<String>();
	}

	public String getName()
	{
		return name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		//convert to radians automatically, to save calculations
		this.angle = Math.toRadians(angle);
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void calculateEndpoints() {
		getEndpointCompass("alpha");
		getEndpointCompass("beta");
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String toString() {
		return name + "[" + x + "," + y + "]";
	}

	public void addConnectedGate(String gate) {
		if (gate == null) return;

		connectedGates.add(gate);
	}

	public void addConnectedWay(String way) {
		if (way == null) return;

		connectedWays.add(way);

	}

	public ArrayList<String> getConnectedWays() {
		return connectedWays;
	}

	public ArrayList<String> getConnectedGates() {
		return connectedGates;
	}

	public boolean isConnectedTo(String thing) {
		if (thing == null) return false;

		thing = thing.toLowerCase();

		for (String s : connectedWays) {
			if (thing.equals(s.toLowerCase())) return true;
		}

		//check if it's connected to a gate by the same name
		for (String s : connectedGates) {
			if (thing.equals(s.toLowerCase())) return true;
		}

		return false;
	}

	/**
	 * This gets the position and orientation of the endpoint, alpha or beta.
	 * Alpha is the "left" to the normal, and beta is the "right".
	 * @param end Takes the string "alpha" or "beta".
	 */
	public Compass getEndpointCompass(String end) {
		if (end == null) return null;

		double ex,ey,eangle;
		double half = length/2.0;

		if ("alpha".equals(end.toLowerCase())) {
			if (alpha != null) return alpha.clone();

			//this is the alpha endpoint. To the left of the normal
			ex = x - half*Math.cos(angle);
			ey = y - half*Math.sin(angle);

			eangle = angle;	//it is the exactly the same as the orientation of the runway
			alpha = new Compass(ex,ey,eangle);
			return alpha.clone();
		} else if ("beta".equals(end.toLowerCase())) {
			if (beta != null) return beta.clone();

			//this is the beta endpoint. To the right of the normal
			ex = x + half*Math.cos(angle);
			ey = y + half*Math.sin(angle);

			eangle = angle+Math.PI;	//it is the exactly the same as the orientation of the runway

			while (eangle > Math.PI*2) {
				eangle -= Math.PI*2;
			}

			beta = new Compass(ex,ey,eangle);
			return beta.clone();
		} else {
			return null;
		}

	}

	/*
	 * This computes distance from a point to a line
	 */
	public double distanceToPoint(Compass compass) {
		if (compass == null) return -1;

		if (alpha == null)
			alpha = getEndpointCompass("alpha");
		if (beta == null)
			beta = getEndpointCompass("beta");

		double x1 = alpha.getX();
		double y1 = alpha.getY();
		double x2 = beta.getX();
		double y2 = beta.getY();
		double x3 = compass.getX();
		double y3 = compass.getY();

		double numerator = (x3 - x1)*(x2 - x1) + (y3 - y1)*(y2 - y1);
		double denominator = length*length;

		if (denominator == 0) return -1;

		double u = numerator/denominator;

		if (u < -0.3 || u > 1.3) return -1;

		double xi = x1 + u*(x2-x1);
		double yi = y1 + u*(y2-y1);

		double dx = x3-xi;
		double dy = y3-yi;

		return Math.sqrt(dx*dx + dy*dy);
	}

	public Compass getWayIntersection(String connectedWay) {

		//see if this connectedRunway exists
		if (!isConnectedTo(connectedWay)) {
			return null;
		}


		//it exists
		//let's get the runway that it's connected with
		//Runway runway = SessionManager.getInstance().getCurrentScenario().getAirport().getRunway(connectedRunway);
		Way way = airport.getWay(connectedWay);

		//check that this way exists
		if (way == null) {
			return null;
		}


		//the only reason we don't error check above is because
		//if a Runway exists, an Airport also must exist

		//calculate the point of intersection between the runways
		if (alpha == null)
			alpha = getEndpointCompass("alpha");
		if (beta == null)
			beta = getEndpointCompass("beta");

		Compass a1 = alpha;
		Compass b1 = beta;

		Compass a2 = way.getEndpointCompass("alpha");
		Compass b2 = way.getEndpointCompass("beta");

		double x1 = a1.getX();	double x3 = a2.getX();
		double y1 = a1.getY();	double y3 = a2.getY();
		double x2 = b1.getX();	double x4 = b2.getX();
		double y2 = b1.getY();	double y4 = b2.getY();

		double denom = ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
		if (Math.abs(denom) < 0.0000001) {
			return null;
		}

		double u = ((x4 - x3)*(y1 - y3) - (y4-y3)*(x1-x3))/denom;
		double v = ((x2 - x1)*(y1 - y3) - (y2-y1)*(x1-x3))/denom;

		//check if they're out of range
		if (u < 0 || u > 1) return null;	//lies! they don't intersect!
		if (v < 0 || v > 1) return null;	//lies! they don't intersect!

		//the intersection compass is always oriented in the alpha direction (tail end towards beta)
		Compass intersection = new Compass(x1+u*(x2-x1),y1+u*(y2-y1),way.getAngle());
		return intersection;
	}

	public void setAirport(Airport airport) {
		this.airport = airport;
	}
}
