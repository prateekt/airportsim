package airport.objects;

import java.util.Comparator;


public class AirportNodeComparator implements Comparator {
	Compass reference = null;

	public AirportNodeComparator(Compass reference) {
		this.reference = reference;
	}

	public int compare(Object a,Object b) {
		AirportNode an = (AirportNode)a;
		AirportNode bn = (AirportNode)b;
		double d1 = an.getCompass().getDistanceTo(reference);
		double d2 = bn.getCompass().getDistanceTo(reference);

		return (int)(d1-d2);
	}

}
