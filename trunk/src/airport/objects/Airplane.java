/* Airplane class
 * Contains basic information on the airplane.
 * By Michael Hsiao
 * 11/9/2007
 */
package airport.objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.*;

import java.util.*;

public class Airplane
{	private String name;
	private String destination;
	private String source;
	private String type;
	private String model;
	private String status;
	private List<AirplaneAction> actions;	//this is the queue of actions

	private boolean active;
	private boolean alive;	//is it alive?

	//location information
	private Compass compass;
	private double velocity;
	private double acceleration;

	//rotational speeds
	private double angularVelocity;
	private double angularAcceleration;

	private Way way;	//the current way they are on


	public Way getWay() {
		return way;
	}

	public void setWay(Way way) {
		this.way = way;
	}

	public AirplaneAction getFrontAction() {
		if (actions.size() >= 1)
			return actions.get(0);
		else
			return null;
	}

	public AirplaneAction getNextAction() {
		if (actions.size() >= 2)
			return actions.get(1);
		else
			return null;
	}
	public void removeFrontAction() {
		synchronized (actions) {
			actions.remove(0);
		}
	}
	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getType() {
		return type;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model.toLowerCase();
	}

	public void setType(String type) {
		this.type = type.toLowerCase();
	}


	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean getActive() {
		return active;
	}

	public Airplane() {
		actions = Collections.synchronizedList(new ArrayList<AirplaneAction>());
		active = false;
		type = "";
		alive = true;
		velocity = 0;
		compass = new Compass();
		acceleration = 0;
		angularVelocity = 0;
		angularAcceleration = 0;
		model = "747";	//default
	}

	public String toString() {
		return name;
	}

	/**
	 * This reads in an airplane action from the XML node
	 * @param node The node that contains information about the airplane action
	 */
	public void readAction(Node node) {
		if (node == null) return;

		AirplaneAction action = new AirplaneAction();

		NodeList children = node.getChildNodes();
		if (children == null) return;

		//processing loop
		for (int i=0;i<children.getLength();i++) {
			//what kind of child is it?
			Node child = children.item(i);
			if (child == null) break;
			String nodeName = child.getNodeName();
			if (nodeName == null) break;
			nodeName = nodeName.toLowerCase();

			String textContent = child.getTextContent();

			if ("action".equals(nodeName)) {
				action.setName(textContent.toLowerCase());

				//check what action it is
				if ("land".equalsIgnoreCase(textContent)) {
					//then it is an arrival, fo sho
					setType("arrival");
				} else if ("takeoff".equalsIgnoreCase(textContent)) {
					setType("departure");
				}

			}
			if ("type".equals(nodeName)) {
				setType(textContent.toLowerCase());
			}
			if ("model".equals(nodeName)) {
				model = textContent.toLowerCase();
			}
			if ("time".equals(nodeName)) {
				action.setTime(Long.parseLong(textContent));
			}
			if ("location".equals(nodeName)) {
				//This is a coordinate specification of the airplane
				//all the information is stored in the attributes
				Node nx = child.getAttributes().getNamedItem("x");
				Node ny = child.getAttributes().getNamedItem("y");
				Node na = child.getAttributes().getNamedItem("angle");

				if (nx != null) {
					String sx = nx.getNodeValue();
					if (sx != null) {
						action.addProperty("x",Double.parseDouble(sx));
					}
				}
				if (ny != null) {
					String sy = ny.getNodeValue();
					if (sy != null) {
						action.addProperty("y",Double.parseDouble(sy));
					}
				}
				if (na != null) {
					String sa = na.getNodeValue();
					if (sa != null) {
						Double angle = Math.toRadians(Double.parseDouble(sa));
						action.addProperty("angle",angle);
					}
				}
			}
			if ("way".equals(nodeName)) {
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
							action.addProperty("wayname", textContent2);
						}
						if ("point".equals(nodeName2)) {
							action.addProperty("waypoint", textContent2.toLowerCase());
						}
					} //end for

				} //end if
			}
			if ("velocity".equals(nodeName)) {
				action.addProperty("velocity",Double.parseDouble(textContent));
			}
			if ("acceleration".equals(nodeName)) {
				action.addProperty("acceleration",Double.parseDouble(textContent));
			}
			if ("maxvelocity".equals(nodeName)) {
				action.addProperty("maxvelocity",Double.parseDouble(textContent));
			}
			if ("angularvelocity".equals(nodeName)) {
				action.addProperty("angularvelocity",Double.parseDouble(textContent));
			}
			if ("angularacceleration".equals(nodeName)) {
				action.addProperty("angularacceleration",Double.parseDouble(textContent));
			}
			if ("angle".equals(nodeName)) {
				//convert to radians
				//can't use Math.toRadians because it clamps it to 0 to 2pi
				double deg = Double.parseDouble(textContent);
				double rad = Math.PI * deg/180.0;

				action.addProperty("angle",rad);
			}
			if ("endless".equals(nodeName)) {
				action.addProperty("endless",new Boolean(true));
			}
			if ("source".equals(nodeName)) {
				setSource(textContent);
			}
			if ("destination".equals(nodeName)) {
				setDestination(textContent);
			}

		}

		//add the actions
		actions.add(action);
	}

	public double getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	public List<AirplaneAction> getActions() {
		return actions;
	}

	public void setActions(ArrayList<AirplaneAction> actions) {
		this.actions = actions;
	}

	public void addAction(AirplaneAction action) {
		this.actions.add(action);
	}

	public Compass getCompass() {
		return compass;
	}

	public void setCompass(Compass compass) {
		this.compass = compass;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	/**
	 * @return the angularAcceleration
	 */
	public double getAngularAcceleration() {
		return angularAcceleration;
	}

	/**
	 * @param angularAcceleration the angularAcceleration to set
	 */
	public void setAngularAcceleration(double angularAcceleration) {
		this.angularAcceleration = angularAcceleration;
	}

	/**
	 * @return the angularVelocity
	 */
	public double getAngularVelocity() {
		return angularVelocity;
	}

	/**
	 * @param angularVelocity the angularVelocity to set
	 */
	public void setAngularVelocity(double angularVelocity) {
		this.angularVelocity = angularVelocity;
	}

	/**
	 * @return the alive
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * @param alive the alive to set
	 */
	public void setAlive(boolean alive) {
		this.alive = alive;
	}




}
