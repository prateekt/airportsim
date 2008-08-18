package airport.objects;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Airport contains all the runways and hangars.
 * @author Henry Yuen
 *
 */
public class Airport {
	private HashMap<String,Way> ways;	//all the runways
	private HashMap<String,Gate> gates;	//all the gates
	private HashMap<String,Hangar> hangars;	//all the gates
	private HashMap<String,AirportNode> connectivityGraph;
	private HashMap<String,ArrayList<AirportNode>> nearestIntersections;

	//special nodes
	private Way specialWay;

	private String name;	//name of the airport

	public Airport() {
		ways = new HashMap<String,Way>();
		gates = new HashMap<String,Gate>();
		hangars = new HashMap<String,Hangar>();
		specialWay = null;
		connectivityGraph = null;
		nearestIntersections = new HashMap<String,ArrayList<AirportNode>>();
	}

	/**
	 * Returns the Way object specified by the name string
	 * @param way The name of the runway requested
	 * @return The runway object requested
	 */
	public Way getWay(String way) {
		return ways.get(way);
	}

	public Gate getGate(String gate) {
		return gates.get(gate);
	}

	public HashMap<String,Gate> getGates() {
		return gates;
	}

	public HashMap<String,Hangar> getHangars() {
		return hangars;
	}

	public String getName() {
		return name;
	}

	public HashMap<String, Way> getWays() {
		return ways;
	}

	public HashMap<String, AirportNode> getConnectivityGraph() {
		return connectivityGraph;
	}

	public AirportNode getGateNode(String gate) {
		String name = "gate " + gate.toLowerCase();
		if (connectivityGraph.containsKey(name)) {
			return connectivityGraph.get(name);

		}
		return null;
	}

	public AirportNode getIntersectionNode(String way1,String way2) {
		String name = way1.compareTo(way2) > 0 ?
				way2.concat("-" + way1) : way1.concat("-" + way2);

		name = name.toLowerCase();

		if (connectivityGraph.containsKey(name)) {
			return connectivityGraph.get(name);
		}

		return null;
	}

	public AirportNode getWayPointNode(String way,String point) {
		String name = way + " " + point;
		name = name.toLowerCase();

		if (connectivityGraph.containsKey(name)) {
			return connectivityGraph.get(name);

		}
		return null;
	}

	public Way getSpecialWay() {
		return specialWay;
	}

	/**
	 * Picks a random intersection on a special way
	 * @return
	 */
	public AirportNode getSpecialWayLaunchPoint() {
		if (specialWay == null) return null;

/*		int wayIndex = (int)( Math.random() * specialWays.size());
		Way way = specialWays.get(wayIndex);
		if (way == null) return null;*/

		//now, pick an endpoint of the way
		//return getWayPointNode(specialWay.getName(),"alpha");

		ArrayList<AirportNode> intersections = getNearestIntersections(specialWay.getName());
		if (intersections == null) return null;

		if (intersections.size() == 0) return null;

		//int intersectionIndex = (int)(Math.random() * intersections.size());
		return intersections.get(0);


	}

	public HashMap<String,ArrayList<AirportNode>> getNearestIntersections() {
		return nearestIntersections;
	}

	public ArrayList<AirportNode> getNearestIntersections(String way) {
		return nearestIntersections.get(way.toLowerCase());
	}

	public void load(String filename) {
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

		Node attr = node.getAttributes().getNamedItem("name");
		if (attr != null) {
			this.name = attr.getNodeValue();
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

			if ("runway".equals(nodeName)) {
				processWay(child,"runway");
			} else if ("taxiway".equals(nodeName)) {
				processWay(child,"taxiway");
			} else if ("gate".equals(nodeName)) {
				processGate(child);
			} else if ("hangar".equals(nodeName)) {
				processHangar(child);
			}
		}


		HashMap<String,AirportNode> nodes = new HashMap<String,AirportNode>();

		//after we're done loading in all the data,
		//let's construct the connectivity graph
		for (Way way : ways.values()) {
			//compute the intersection points

			ArrayList<AirportNode> intersections = new ArrayList<AirportNode>();

			for (String s : way.getConnectedGates()) {
				String gateName = "Gate " + s;
				if (nodes.containsKey(gateName.toLowerCase())) {
					intersections.add(nodes.get(gateName.toLowerCase()));
				} else {
					Compass compass = new Compass();
					Gate gate = getGate(s);
					compass.setAngle(gate.getAngle());
					compass.setX(gate.getX());
					compass.setY(gate.getY());

					AirportNode anode = new AirportNode(gateName,compass,AirportNode.NodeType.GATE);
					intersections.add(anode);
					nodes.put(gateName.toLowerCase(),anode);
				}

			}

			for (String s : way.getConnectedWays()) {
				//see if this intersection exists already
				//we have to uniquely determine the name of the
				//intersection, it's done via
				//lexicographic ordering

				String intersectionName = s.compareTo(way.getName()) > 0 ?
								way.getName().concat("-" + s) : s.concat("-" + way.getName());

				if (nodes.containsKey(intersectionName.toLowerCase())) {
					AirportNode anode = nodes.get(intersectionName.toLowerCase());
					intersections.add(anode);
				} else {
					Compass intersection = way.getWayIntersection(s);
					if (intersection == null) continue;
					AirportNode anode = new AirportNode(intersectionName,intersection,AirportNode.NodeType.INTERSECTION);
					intersections.add(anode);
					nodes.put(intersectionName.toLowerCase(),anode);
				}

			}

			//add alpha and beta

			/*
			Compass alpha = way.getEndpointCompass("alpha");
			Compass beta = way.getEndpointCompass("beta");

			String pointName = way.getName() + " alpha";
			AirportNode anode = new AirportNode(pointName,alpha,AirportNode.NodeType.ENDPOINT);
			intersections.add(anode);
			nodes.put(pointName.toLowerCase(),anode);

			pointName = way.getName() + " beta";
			anode = new AirportNode(pointName,beta,AirportNode.NodeType.ENDPOINT);

			intersections.add(anode);
			nodes.put(pointName.toLowerCase(),anode);
			*/
			Compass alpha = way.getEndpointCompass("alpha");
			Collections.sort(intersections,new AirportNodeComparator(alpha));

			//alright, we have the intersections sorted in some kind of order
			//now go through them and connected 'em to each other
			for (int i=0;i<intersections.size()-1;++i) {
				AirportNode anode1 = intersections.get(i);
				AirportNode anode2 = intersections.get(i+1);
//				System.out.println("Intersections: " + anode1.getName() + " and " + anode2.getName());
				anode1.addNeighbor(anode2,way);
				anode2.addNeighbor(anode1,way);
			}
			nearestIntersections.put(way.getName().toLowerCase(),intersections);

		}	//end

		//we have built our connectivity graph
		connectivityGraph = nodes;


		/*
		for (String k : connectivityGraph.keySet()) {
			System.out.println(k);
		}*/



		//put all the "place"'d airplanes in their proper positions


	}



	/**
	 * Given an XML node that contains information about runways,
	 * save it into an Way object;
	 * @param node The node that has the runway information
	 */
	private void processWay(Node node,String type) {
		if (node == null) return;

		//get the name of the runway
		//create a new Runway object, and add it to the list of runways

		NodeList children = node.getChildNodes();
		if (children == null) return;

		Way way = null;

		if ("runway".equals(type)) {
			way = new Runway();
		} else if ("taxiway".equals(type)) {
			way = new Taxiway();
		} else {
			//egregious error!
			return;
		}

		way.setAirport(this);

		boolean special = false;

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
				way.setName(textContent);

			}
			if ("x".equals(nodeName)) {
				way.setX(Integer.parseInt(textContent));
			}
			if ("y".equals(nodeName)) {
				way.setY(Integer.parseInt(textContent));
			}
			if ("width".equals(nodeName)) {
				way.setWidth(Integer.parseInt(textContent));
			}
			if ("length".equals(nodeName)) {
				way.setLength(Integer.parseInt(textContent));
			}
			if ("angle".equals(nodeName)) {
				way.setAngle(Double.parseDouble(textContent));
			}
			if ("connectstoway".equals(nodeName)) {
				way.addConnectedWay(textContent);
			}
			if ("connectstogate".equals(nodeName)) {
				way.addConnectedGate(textContent);
			}
			if ("special".equals(nodeName)) {
				special = true;
			}


		}

		way.calculateEndpoints();
		ways.put(way.getName(),way);
		if (special) specialWay = way;
	}

	/**
	 * Given an XML node that contains information about terminals,
	 * save it into a gate object
	 * @param node The node that has the gateinformation
	 */
	private void processGate(Node node) {
		if (node == null) return;

		//get the name of the terminal
		//create a new terminal object, and add it to the list of terminals

		NodeList children = node.getChildNodes();
		if (children == null) return;

		Gate gate = new Gate();

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
				gate.setName(textContent);
			}
			if ("x".equals(nodeName)) {
				gate.setX(Integer.parseInt(textContent));
			}
			if ("y".equals(nodeName)) {
				gate.setY(Integer.parseInt(textContent));
			}
			if ("angle".equals(nodeName)) {
				gate.setAngle(Math.toRadians(Double.parseDouble(textContent)));
			}

		}

		gates.put(gate.getName(),gate);
	}

	/**
	 * Given an XML node that contains information about terminals,
	 * save it into a gate object
	 * @param node The node that has the gateinformation
	 */
	private void processHangar(Node node) {
		if (node == null) return;

		//get the name of the terminal
		//create a new terminal object, and add it to the list of terminals

		NodeList children = node.getChildNodes();
		if (children == null) return;

		Hangar hangar = new Hangar();

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
				hangar.setName(textContent);
			}
			if ("x".equals(nodeName)) {
				hangar.setX(Integer.parseInt(textContent));
			}
			if ("y".equals(nodeName)) {
				hangar.setY(Integer.parseInt(textContent));
			}
			if ("angle".equals(nodeName)) {
				hangar.setAngle(Math.toRadians(Double.parseDouble(textContent)));
			}

		}

		hangars.put(hangar.getName(),hangar);
	}

}