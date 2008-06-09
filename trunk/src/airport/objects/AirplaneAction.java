package airport.objects;

import java.util.HashMap;

/**
 * AirplaneAction holds a command to be performed
 * by the airplane. An airplane action has a name,
 * and an array of properties associated with the
 * particular action.
 * @author Henry Yuen
 *
 */
public class AirplaneAction {

	private long time;		//the time that this action is supposed to be executed
	private String name;	//the name of the action that is to be performed
	private HashMap<String,Object> properties;	//properties that are associated with the airplane
												//action
	private boolean active;

	private AirplaneActionCallback callback;

	public AirplaneAction() {
		active = false;
		callback = null;
		properties = new HashMap<String,Object>();
		time = 0;	//never to be executed
	}

	public AirplaneAction clone() {
		AirplaneAction action = new AirplaneAction();
		action.setName(name);
		for (String s : properties.keySet()) {
			action.addProperty(s,properties.get(s));
		}
		action.setTime(time);

		return action;
	}

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}


	public void addProperty(String key,Object property) {
		properties.put(key,property);
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}

	public void setActive(boolean b) {
		active = b;
	}

	public boolean getActive() {
		return active;
	}

	public String toString() {
		return "[Action:" + name + "]";
	}

	public static AirplaneAction parseVoiceOrder(String order) {
		//identify the action
		String tokens[] = order.trim().split(" ");

		if (tokens.length < 1) return null;
		String command = tokens[0].toLowerCase();

		if ("turn-at".equals(command)) {

			AirplaneAction action = new AirplaneAction();
			//the next few tokens should be a
			//String way = tokens[1];
			String way = "";
			for (int i=1;i<tokens.length-1;++i)
				way += tokens[i] + " ";

			way = way.trim();

			String point = tokens[tokens.length-1];

			action.setName("turninto");
			action.addProperty("wayname",way);
			action.addProperty("waypoint",point);

			return action;
		} else if ("cross".equals(command)) {

			AirplaneAction action = new AirplaneAction();
			//this is just a simply a taxi action
			action.setName("taxi");
			return action;
		} else if ("arrive".equals(command)) {

			AirplaneAction action = new AirplaneAction();
			action.setName("dock-at");

			//the rest of the tokens must be a gate
			String gate = "";
			for (int i=1;i<tokens.length;++i)
				gate += tokens[i] + " ";

			gate = gate.trim();
			action.addProperty("gate",gate);
			return action;
		}

		return null;
	}

	public AirplaneActionCallback getCallback() {
		return callback;
	}

	public void setCallback(AirplaneActionCallback callback) {
		this.callback = callback;
	}
}
