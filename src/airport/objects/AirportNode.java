package airport.objects;

import java.util.HashMap;

public class AirportNode {

	public enum NodeType {
		INTERSECTION,
		GATE,
		ENDPOINT,
	}

	//it has a position
	private Compass compass;

	//it has a name
	private String name;

	private NodeType type;

	//it has a list of other airport nodes
	private HashMap<String,AirportNode> neighbors;
	private HashMap<AirportNode,Way> edges;

	public AirportNode(String name,Compass compass,NodeType type) {
		this.name = name;
		this.compass = compass;
		this.type = type;
		neighbors = new HashMap<String,AirportNode>();
		edges = new HashMap<AirportNode,Way>();
	}

	public NodeType getType() {
		return type;
	}

	public Compass getCompass() {
		return compass;
	}

	public String getName() {
		return name;
	}

	public void addNeighbor(AirportNode neighbor,Way way) {
		String neighborName = neighbor.getName();
		if (neighbors.containsKey(neighborName.toLowerCase()) == false) {
			neighbors.put(neighborName.toLowerCase(),neighbor);
			edges.put(neighbor,way);
		}


	}

	public boolean hasNeighbor(String neighborName) {
		return neighbors.containsKey(neighborName.toLowerCase());
	}

	public AirportNode getNeighbor(String neighborName) {
		return neighbors.get(neighborName.toLowerCase());
	}

	public Way getWayTo(String neighborName) {
		return edges.get(getNeighbor(neighborName));
	}

	public Way getWayTo(AirportNode node) {
		return edges.get(node);
	}

	public HashMap<AirportNode,Way> getEdges() {
		return edges;
	}

	public HashMap<String,AirportNode> getNeighbors() {
		return neighbors;
	}

	public int hashCode() {
		return name.hashCode();
	}


}
