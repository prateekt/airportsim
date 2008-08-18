//created by James Marsh

package airport.objects;

/**
 * Directly relates to Way.
 */
public class Taxiway extends Way{

	//private String type;

	public Taxiway(){
		super();
		type = "taxiway";
	}

	public Taxiway(String name,Airport airport) {
		super(name, "taxiway",airport);
    }

	public String toString(){return "Taxiway: "+ name;}
}
