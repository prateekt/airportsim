package agents;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import airport.TheSimulator;
import airport.objects.Airplane;
import airport.objects.Airport;
import airport.objects.AirportNode;
import airport.objects.Compass;
import airport.objects.Way;

/**
 * The Scenario class loads and contains information about the dynamic events of
 * an airport scenario.
 * @author Patrick Monroe & Henry Yuen
 *
 */
public class ScenarioParser {

	Airport airport;	//create an instance of the airport
	ArrayList<Flight> flights = new ArrayList<Flight>();
	ArrayList<PilotAgent> pilots = new ArrayList<PilotAgent>();
	GroundControlAgent gc;
	LocalControlAgent lc;
	ClearanceAgent ca;

	String name;
	public ScenarioParser() {
		airport = null;
	}

	/**
	 * Loads the scenario XML file, parsing all the airplaneActions and events.
	 * @param filename The name of the XML file to be loaded
	 */
	public void load(String filename) throws Exception {

		Document xmlDoc;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			xmlDoc = builder.parse(new File(filename));

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		//the first node should be the <Scenario> root node
		Node node = xmlDoc.getFirstChild();
		if (node == null) return;

		//ensure that this is a scenario file
		if (!"scenario".equals(node.getNodeName().toLowerCase())) {
			throw new Exception("**ERROR: Tried loading " + filename + " but is not a scenario file!**");
		}

		Node attr = node.getAttributes().getNamedItem("name");
		if (attr != null) {
			this.name = attr.getNodeValue();
		} else {
			//there's no name!
			throw new Exception("**ERROR: The scenario file " + filename + " does not have a name!**");
		}

		//now load the airport
		attr = node.getAttributes().getNamedItem("airport");
		if (attr != null) {
			String airportname = attr.getNodeValue();
			airport = new Airport();
			airport.load("resource/airports/" + airportname.toLowerCase() + ".xml");
		} else {
			//there's no airport attached!
			throw new Exception("**ERROR: The scenario file " + filename + " does not have an airport!**");
		}
		//processing loop
		NodeList children = node.getChildNodes();

		for (int n=0;n<children.getLength();n++) {
			//what kind of child is it?
			Node child = children.item(n);
			if (child == null) break;
			String nodeName = child.getNodeName();
			if (nodeName == null) break;
			nodeName = nodeName.toLowerCase();
			if ("flight".equals(nodeName)){
				parseFlight(child);
			}
			if("groundagent".equals(nodeName)){
				String name = child.getAttributes().getNamedItem("name").getNodeValue();
				System.out.println(name);
				gc = new GroundControlAgent(name,airport);	//null, for now
			}
			if("clearanceagent".equals(nodeName)){
				String name = child.getAttributes().getNamedItem("name").getNodeValue();
				System.out.println(name);
				ca = new ClearanceAgent(name,airport);
			}
			if("localagent".equals(nodeName)){
				String name = child.getAttributes().getNamedItem("name").getNodeValue();
				System.out.println(name);
				lc = new LocalControlAgent(name, airport); //airport bug
			}

		}

		//the set the airport immediamente
		TheSimulator.getInstance().setAirport(airport);
	}


	public void parseFlight(Node node) {
		if (node == null) return;

		NodeList children = node.getChildNodes();
		if (children == null) return;

		Flight flight = new Flight();

		String pilotName = null;

		boolean isDeparture = false;

		String wayname = null;
		String waypoint = null;

		//processing loop
		for (int i=0;i<children.getLength();i++) {
			//what kind of child is it?
			Node child = children.item(i);
			if (child == null) break;
			String nodeName = child.getNodeName();
			if (nodeName == null) break;
			nodeName = nodeName.toLowerCase();

			String textContent = child.getTextContent();

			if ("name".equals(nodeName)) {
				flight.setPlaneName(textContent);
			} else if ("pilot".equals(nodeName)) {

				//create a pilot
				pilotName = textContent;
			} else if ("type".equals(nodeName)) {
				String type = textContent.toLowerCase();

				if ("arrival".equals(type))
					flight.setType(Flight.FlightType.Arrival);
				else {
					flight.setType(Flight.FlightType.Departure);
					//this is a departure
					isDeparture = true;
				}

			} else if ("gate".equals(nodeName)) {
				flight.setGate(textContent);
			} else if ("mode".equals(nodeName)) {
				flight.setGuidanceMode(textContent);
			} else if ("origin".equals(nodeName)) {
				flight.setOrigin(textContent);
			} else if ("destination".equals(nodeName)) {
				flight.setDestination(textContent);
			} else if ("way".equals(nodeName)) {
				//run its own process
				NodeList children2 = child.getChildNodes();
				if (children2 != null) {
					for (int k=0;k<children2.getLength();k++) {
						Node child2 = children2.item(k);
						if (child2 == null) break;
						String nodeName2 = child2.getNodeName();
						if (nodeName2 == null) break;
						nodeName2 = nodeName2.toLowerCase();

						String textContent2 = child2.getTextContent();
						if ("name".equals(nodeName2)) {
							wayname = textContent2;
						}
						if ("point".equals(nodeName2)) {
							waypoint = textContent2;
						}
					} //end for

					String way = wayname + " " + waypoint;
					flight.setWay(way);
				} //end if

			} else if ("time".equals(nodeName)) {

				//this delays the time that the messages will be sent
				long time = Long.parseLong(textContent);

				flight.setTime(time);
			}

		}

		//create the flight
		flights.add(flight);

		//create the pilot
		if (pilotName != null) {
			PilotAgent pilot = new PilotAgent(pilotName,airport);
			pilot.setFlight(flight);
			pilots.add(pilot);

			if (isDeparture) {
				//we have to place the plane at the right place
				Airplane airplane = pilot.getAirplane();

				//well, we're going to assume that they're
				//going to be docked at the gates

				AirportNode gateNode = airport.getGateNode(flight.getGate());

				//place the airplane at the gatenode
				airplane.setCompass(gateNode.getCompass().clone());

				//get the way associated with runway
				Way way = airport.getWay(wayname);
				Compass endpoint = way.getEndpointCompass(waypoint);
				airplane.getCompass().setAngle(endpoint.getAngle());

				//the airplane is also active
				airplane.setActive(true);
				airplane.setAlive(true);
				airplane.setWay(way);
			}
		}

	}

	public ArrayList<Flight> getFlights(){
		return flights;
	}
	public ArrayList<PilotAgent> getPilots(){
		return pilots;
	}
	public GroundControlAgent getGroundControlAgent(){
		return gc;
	}
	public LocalControlAgent getLocalControlAgent(){
		return lc;
	}
	public ClearanceAgent getClearanceAgent(){
		return ca;
	}

	public Airport getAirport() {
		return airport;
	}

}