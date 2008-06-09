//revamped by Henry Yuen

package airport.objects;


/**
 * Directly relates to Way.
 */
public class Runway extends Way{

	//private String type;

	public Runway(){
		super();
		type = "runway";
	}

	public Runway(String name,Airport airport) {
		super(name, "runway",airport);
    }

	public String toString(){return "Runway: "+ name;}
}
