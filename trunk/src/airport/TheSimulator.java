package airport;

import gui.GUI;
import gui.ExplosiveFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import airport.objects.Airplane;
import airport.objects.AirplaneAction;
import airport.objects.AirplaneActionCallback;
import airport.objects.Airport;
import airport.objects.Compass;
import airport.objects.Gate;
import airport.objects.Way;

/**
 * This runs the simulations and dynamics of planes
 * moving around.
 * @author Henry Yuen
 *
 */
public class TheSimulator extends Thread {
	private static TheSimulator instance = null;

	private boolean running;	//is the simulation running
	private long currentTime;
	private long stepTimer;
	private long maxStepTime;
	private boolean stepMode;	//are we stepping through or just playing the whole shebang?
	private boolean finished;	//are we finished?

	//SOME SIMULATOR SETTINGS
	private static double DEFAULT_LANDING_VELOCITY = 0.8;
	private static double DEFAULT_LANDING_ACCEL = -0.001;

	private static double DEFAULT_TAXI_VELOCITY = 0.08;

	private static double DEFAULT_TAKEOFF_VELOCITY = 0;
	private static double DEFAULT_TAKEOFF_ACCEL = 0.0005;
	private static double MAX_TAKEOFF_VELOCITY = 0.8;	//this is the velocity before it catches air

	private static double DEFAULT_TURN_VELOCITY = 0.0008;

	private static double THRESHOLD_RADIUS = 5;	//in pixels
	private static double THRESHOLD_ANGLE = 3*Math.PI/180.0;	//in radians

	private static long MAX_STEP_TIME = 1000;	//one second
	private static long MAX_ACTION_TIME = 30000;	//max time for an action to execute

	private static double PLANE_RADIUS = 25;

	private static double MULTIPLIER = 45;

	ArrayList<Airplane> planesToKill = null;
	HashMap<Way,ArrayList<Airplane>> planesOnWay = null;

	//the simulator should have a list of planes
	List<Airplane> airplanes = null;

	//set the airport
	Airport airport = null;

	//handle to the GUI
	GUI gui;

	private TheSimulator () {
		super("The Simulator Thread");
		//start();
		reset();
	}


	public void setAirport(Airport airport) {
		this.airport = airport;
	}

	public void setGUI(GUI gui) {
		this.gui = gui;
	}

	/**
	 * This resets the simulator to initial values.
	 * @param towerControlFrame Handle to the TowerControlFrame object
	 */
	public synchronized void reset() {
		running = false;
		currentTime = 0;
		maxStepTime = MAX_STEP_TIME;
		stepTimer = 0;
		finished = false;
		stepMode = false;
		planesToKill = new ArrayList<Airplane>();
		planesOnWay = new
			HashMap<Way,ArrayList<Airplane>>();
		airplanes = Collections.synchronizedList(new ArrayList<Airplane>());
	}


	public void addAirplane(Airplane airplane) {
		airplanes.add(airplane);

		if (gui != null)
			gui.addPlane(airplane);
	}

	/**
	 * Retrieves the current simulation time (in milliseconds)
	 * @return current time, in milliseconds
	 */
	public long getCurrentTime() {
		return currentTime;
	}

	/**
	 * Singleton operator
	 * @return returns the Simulator instance
	 */
	public static TheSimulator getInstance() {
		if (instance == null) {
			instance = new TheSimulator();
		}
		return instance;
	}

	/**
	 * This sets the simulator running
	 * @param r A boolean indicating running/not running
	 * @param step Is it operating in step mode? If yes, then it runs for 1 seconds.
	 * 				 If not, it runs for the entire duration of the simulation.
	 */
	public synchronized void setRunning(boolean r,boolean step) {
		running = r;
		if (running == true) {
			stepTimer = 0;
			stepMode = step;
		}
	}

	/**
	 *
	 * @return Returns whether the simulation is running or not
	 */
	public boolean getRunning() {
		return running;
	}

	/**
	 * The Thread's run function. This performs all the processing of the
	 * airplane AI. It sequentially executes every planes' Action queue.
	 */
	public void run() {
		long time = System.currentTimeMillis();

		while (true) {

			if (gui != null) {
				//gui.updateScenarioUI();
				gui.updateWorld();
			}


			long timeDelta = System.currentTimeMillis()-time;
			time += timeDelta;

			//run the explosion machine!
			ExplosiveFactory.getInstance().update(timeDelta);

			/*			if (scenario != null) {
				//run the rain machine
				if ("weather".equals(scenario.getWeather())) {
					//update the rain god
//					RainGod.getInstance().update(timeDelta);
				}
			}
			 */
			if (running == false) {
				continue;
			}


			if (finished) break;

			stepTimer += timeDelta;
			currentTime += timeDelta;

			boolean existsAnAction = false;
			boolean existsALivingPlane = false;

			synchronized (airplanes) {
				for (Airplane airplane : airplanes) {

					AirplaneAction action = airplane.getFrontAction();

					if (action != null) {
						existsAnAction = true;	//there exists at least one valid action

					} else {
						continue;
					}


					if (airplane.isAlive()) existsALivingPlane = true;

					if (currentTime < action.getTime()) continue;

					//perform action
					performAction(airplane,action,timeDelta);
				}
			}

			planesOnWay.clear();

			//perform our own risks and mitigation logic
			synchronized (airplanes) {
				for (Airplane plane1 : airplanes) {
					if (!plane1.getActive() || !plane1.isAlive()) continue;

					Compass c1 = plane1.getCompass();

					for (Airplane plane2 : airplanes) {
						if (plane2 == plane1) continue;
						if (!plane2.getActive()) continue;

						Compass c2 = plane2.getCompass();

						double dist = c1.getDistanceTo(c2);

						if (dist < PLANE_RADIUS*3) {

							if (dist < PLANE_RADIUS*2) {
								//they're colliding! oh shit!
								//planesToKill.add(plane1);
								//	planesToKill.add(plane2);

								//start an explosion!
							ExplosiveFactory.getInstance().createExplosion((int)c1.getX(),(int)c1.getY());
							} else {
								//just warn them


							}
						}

						for (Airplane a : planesToKill) {
							a.setAlive(false);
						}

						if (planesToKill.size() > 0)
							planesToKill.clear();

					} //end for

					//what runway is it on?
					Way way = plane1.getWay();
					if (planesOnWay.containsKey(way) == false) {
						ArrayList<Airplane> planeList =
							new ArrayList<Airplane>();
						planeList.add(plane1);
						planesOnWay.put(way,planeList);
					} else {
						ArrayList<Airplane> planeList =
							planesOnWay.get(way);
						if (planeList != null)
							planeList.add(plane1);
					}

				} //end for
			}

			planesToKill.clear();

//			generateRisksAndMitigations(scenario);


			//if there are no more actions
			//we have to say the simulation is finished!

/*			if (!existsAnAction|| !existsALivingPlane)
				finished = true;*/


			try {
				//if it's not running, then sleep longer
				//sleep((running == false || finished == true) ? 100 : 15);	//sleep for a second
				sleep(40);
			} catch (Exception e) {}

		}

		if (finished) {
			//tell the tower control frame we've finished the simulation!
			setRunning(false,false);
			//				towerControlFrame.finishedSimulation();
		}

		//check if we've exceeded our step time
		if (stepMode) {
			if (stepTimer > maxStepTime) {
				//we gotta disable this
				setRunning(false,stepMode);

				//tell the tower control we've finishedo ur step
				/*	if (towerControlFrame != null)
						towerControlFrame.finishedStep();*/
			}

		}
	}


	/**
	 * This performs the front action in the airplane's action queue
	 * @param airplane The airplane in question
	 * @param action The action to be performed
	 * @param timeElapsed The time elapsed since the last frame loop
	 */
	private synchronized void performAction(Airplane airplane,AirplaneAction action,long timeElapsed) {
		if (airplane == null || action == null) return;

		if (airplane.isAlive() == false) return;	//never to be alive ever again

		if (action.getActive() == false) {
			//calculate initial conditions and end conditions
			if (!calculateActionConditions(airplane,action)) {

				//check if the front action is the same
				if (airplane.getFrontAction() != action) {
					return;
				} else {
					airplane.removeFrontAction();	//this action is useless
					return;	//something failed
				}
			}


			action.setActive(true);
		}

		timeElapsed = 40;

		//if it's not active anymore
		if (airplane.getActive() == false) return;

		double speed = airplane.getVelocity();	//it's actually a speed, d'oh!
		double accel = airplane.getAcceleration();
		Compass compass = airplane.getCompass();
		double angle = compass.getAngle();

		speed += accel*timeElapsed;
		airplane.setVelocity(speed);
		//udpate the position
		double vx = speed * Math.cos(angle);
		double vy = speed * Math.sin(angle);

		double x = compass.getX() + vx*timeElapsed;
		double y = compass.getY() + vy*timeElapsed;

		compass.setX(x);
		compass.setY(y);

		//calculate angular velocity
		double angularSpeed = airplane.getAngularVelocity();
		double angularAccel = airplane.getAngularAcceleration();

		angularSpeed += angularAccel*timeElapsed;
		airplane.setAngularVelocity(angularSpeed);

		angle += angularSpeed*timeElapsed;
		angle = normalizeAngle(angle);
		compass.setAngle(angle);

		airplane.setCompass(compass);

		//if we're taxiing, this is the perfect opporunity
		//to calculate which runway we are on
		double minDistance = Double.POSITIVE_INFINITY;
		Way closest = null;

		Way currentWay = airplane.getWay();

		if ("taxi".equals(action.getName())) {
			Collection<Way> ways = airport.getWays().values();

			for (Way way : ways) {
				//which ways are we aligned with?
				boolean isAligned = isAlignedWithWay(airplane,way);

				if (isAligned) {
					double distance = way.distanceToPoint(compass);

					if (distance < minDistance && distance > -1) {
						minDistance = distance;
						closest = way;
					}
				}
			}
			airplane.setWay(closest);

		}


		//we check if we've reached end conditions
		checkEndConditions(airplane,action);

	}

	/**
	 * Calculates the initial conditions for the airplane. For example, if the action is
	 * "place", then it will calculate where and how to place the airplane. Then, it calculates
	 * the end conditions for the airplane, and stores them. For example, the end condition for
	 * the action "taxi" is either the end of the runway, or the intersection of a connected
	 * runway.
	 * @param scenario The current scenario
	 * @param airplane The airplane in question
	 * @param action The action to perform
	 * @return Whether the calculation was successful. If not, the invalid action will be removed from the queue.
	 */
	public boolean calculateActionConditions(Airplane airplane,AirplaneAction action) {
		if (airplane == null || action == null) return false;

		String actionName = action.getName();

		//see if this runway exists in the database

		//no matter what, all actions have a start time
		action.addProperty("starttime", (Long)currentTime);

		if ("land".equals(actionName)) {
			//when the airplane is landing, find out its initial position

			//in order to do that, find what runway it's supposed to be on
			String wayname = (String)action.getProperty("wayname");

			if (wayname == null) {
				//there's no ruwnay specified!
				return false;
			}

			Way way = airport.getWay(wayname);
			if (way == null) { //I guess not
				return false;
			}

			airplane.setWay(way);
			//the runway exists, alright, let's get the endpoint
			String waypoint = (String)action.getProperty("waypoint");

			if (waypoint == null) {
				return false;
			}

			Compass endpoint = way.getEndpointCompass(waypoint);

			if (endpoint == null) {	//no such endpoint
				return false;
			}

			//we have our endpoint, let's align our plane that way
			airplane.setCompass(endpoint);

			//check if we have a velocity
			Double velocity = (Double)action.getProperty("velocity");
			if (velocity == null) {
				//we have to come up with a default velocity
				velocity = DEFAULT_LANDING_VELOCITY;
			}

			airplane.setVelocity(velocity);
			//set the acceleration
			airplane.setAcceleration(DEFAULT_LANDING_ACCEL);

			//the end conditions for land is when it slows down to zero
			action.addProperty("endvelocity", new Double(0.0));

			//System.out.println("***DEBUG***: Landing... at "  + way.getName());

		} else if ("dock".equals(actionName)) {
			//docking is simply like taxiing
			//except you just head down the runway you're in
			//and stop when your distance to the gate is increasing

			String gateName = (String)action.getProperty("gate");
			if (gateName == null) {
				return false;
			}

			Gate gate = airport.getGate(gateName);

			if (gate == null) {
				//System.out.println("Gate " + gate + " doesn\'t exist!");
				return false;
			}

			//System.out.println("***DEBUG***: Docking ... at " + gateName);

			//set the plane at a slow trotting place
			//check if we have a velocity
			Double velocity = (Double)action.getProperty("velocity");
			if (velocity == null) {
				//we have to come up with a default velocity
				velocity = DEFAULT_TAXI_VELOCITY;
			}

			airplane.setVelocity(velocity);
			//set the acceleration
			airplane.setAcceleration(0);

			//get the distance to the gate
			Compass gateCompass = new Compass();
			gateCompass.setX(gate.getX());
			gateCompass.setY(gate.getY());

			Compass airplaneCompass = airplane.getCompass();

			//check that the dot product doesn't yield <= 0
			Compass displacement = gateCompass.subtract(airplaneCompass);

			Compass airplaneDir = new Compass();
			airplaneDir.setX(Math.cos(airplaneCompass.getAngle()));
			airplaneDir.setY(Math.sin(airplaneCompass.getAngle()));

			double dot = airplaneDir.dotProduct(displacement);

			if (dot <= 0) {
				//there is no way the plane can ever get close to this place
				//System.out.println("Dock - the plane is facing the incorrect direction! " + dot);
				return false;
			}

			double dist = airplaneCompass.getDistanceTo(gateCompass);
			action.addProperty("distancetogate",dist);

		} else if ("taxi".equals(actionName)) {

			//initial conditions
//			the end conditions here for taxi are complex
			//it is either the end of the runway
			//or it is the intersection point of the runway?
			//It depends on the "lookahead".
			AirplaneAction nextAction = airplane.getNextAction();

			Compass endpoint = null;	//this is the calculated end position and orientation
			boolean alreadyHaveEndpoint = false;	//did the user already specify
													//the endpoint of this taxiing?

			//check if there's an endpoint assigned already
			endpoint = (Compass) action.getProperty("endpoint");
			alreadyHaveEndpoint = endpoint != null;

			if (!alreadyHaveEndpoint) {
				Compass compass = airplane.getCompass();
				if (compass == null) {
					//this shouldn't happen
					return false;
				}

				Way way = airplane.getWay();
				if (way != null) {
					if (getAngleDistance(compass.getAngle(),way.getAngle()) < 0.1) {
						//then we're getting the beta endpoint
						endpoint = way.getEndpointCompass("beta");
					} else {
						endpoint = way.getEndpointCompass("alpha");
					}
				}
				action.addProperty("endpoint", endpoint);


			} else {
				//let's figure out if we're angled towards the endpoint correctly
				//if not, let's create an intermediate action "turn" and stick it in the front

				//vector towards the endpoint
				Compass vector = endpoint.subtract(airplane.getCompass());

				//normalize this vector
				double ang = normalizeAngle(vector.getAngle());
				vector.setAngle(ang);

				double airang = airplane.getCompass().getAngle();
				//are we exactly this angle? (doubt it)
				if (getAngleDistance(airang,ang) > 0.1) {
					return false;
				}

			}

			//otherwise
			double distance = airplane.getCompass().getDistanceTo(endpoint);
			action.addProperty("distancetoendpoint",distance);

			//set the plane at a slow trotting place
			//check if we have a velocity
			Double velocity = (Double)action.getProperty("velocity");
			if (velocity == null) {
				//we have to come up with a default velocity
				velocity = DEFAULT_TAXI_VELOCITY;
			}

			airplane.setVelocity(velocity);
			//set the acceleration
			airplane.setAcceleration(0);

			//System.out.println("***DEBUG***: Taxiing towards compass: " + endpoint);

		} else if ("turntowards".equals(actionName)) {
			//given an endpoint
			//turn towards it
			Compass endpoint = (Compass)action.getProperty("endpoint");

			if (endpoint != null) {
				//let's figure out if we're angled towards the endpoint correctly
				//if not, let's create an intermediate action "turn" and stick it in the front

				//vector towards the endpoint
				Compass vector = endpoint.subtract(airplane.getCompass());

				//normalize this vector
				double ang = normalizeAngle(vector.getAngle());
				vector.setAngle(ang);

				double airang = airplane.getCompass().getAngle();
				//are we exactly this angle? (doubt it)
				if (getAngleDistance(airang,ang) > 0.1) {
					//we have to create a turn action first

					//figure out which direction to turn
					double diff = ang - airang;

					diff = normalizeAngle(diff);

					double angvel = DEFAULT_TURN_VELOCITY;

					if (diff > Math.PI) {
						angvel = -angvel;
					}

					action.addProperty("angularvelocity",angvel);
					action.addProperty("angle",diff);
					action.addProperty("endangle",ang);
					airplane.setAngularVelocity(angvel);
				} else {
					return false;
				}
			}

			//System.out.println("***DEBUG*** Turning towards " + endpoint);
		} else if ("place".equals(actionName)) {

			//we just put it someplace
			//let's see what kind of properties we have
			Double x = (Double)action.getProperty("x");
			if (x != null) {
				Double y = (Double)action.getProperty("y");
				Double angle = (Double)action.getProperty("angle");

				angle = normalizeAngle(angle);

				Compass compass = new Compass(x,y,angle);
				airplane.setCompass(compass);
			} else {
				String wayname = (String)action.getProperty("wayname");

				if (wayname == null) {
					//there's no ruwnay specified!
					return false;
				}

				Way way = airport.getWay(wayname);
				if (way == null) { //I guess not
					return false;
				}

				airplane.setWay(way);


				//the runway exists, alright, let's get the endpoint
				String waypoint = (String)action.getProperty("waypoint");

				if (waypoint == null) {
					return false;
				}

				Compass endpoint = way.getEndpointCompass(waypoint);
				//System.out.println(endpoint);
				if (endpoint == null) {	//no such endpoint
					return false;
				}

				//we have our endpoint, let's align our plane that way
				airplane.setCompass(endpoint);
			}

			//no velocity
			airplane.setVelocity(0);
			//set the acceleration
			airplane.setAcceleration(0);

		} else if ("takeoff".equals(actionName)) {
			//System.out.println("***DEBUG***: takeoff!");

			//check if we have a velocity
			Double velocity = (Double)action.getProperty("velocity");
			if (velocity == null) {
				//we have to come up with a default velocity
				velocity = DEFAULT_TAKEOFF_VELOCITY;
			}

			airplane.setVelocity(velocity);
			//set the acceleration
			airplane.setAcceleration(DEFAULT_TAKEOFF_ACCEL);

//			we have a velocity check
			action.addProperty("endvelocity",new Double(MAX_TAKEOFF_VELOCITY));

		} else if ("turn".equals(actionName)) {
			//System.out.println("Turning..");
			//ensure that we have an angle to turn through
			Double angle = (Double)action.getProperty("angle");
			if (angle == null) {
				return false;
			}

			//set the angular acceleration and angular velocity
			Double angularSpeed = (Double)action.getProperty("angularvelocity");
			if (angularSpeed == null) {
				angularSpeed = DEFAULT_TURN_VELOCITY;
			}

			//no acceleration for now
			Double angularAccel = (Double)action.getProperty("angularacceleration");
			if (angularAccel == null) {
				angularAccel = 0.0;
			}

			airplane.setAngularAcceleration(angularAccel);
			airplane.setAngularVelocity(angularSpeed);

			//simply add the angles
			Compass compass = airplane.getCompass();

			//at this point we're assuming we have a valid angle, so we don't need to check
			angle = (Double)action.getProperty("angle");
			Double endangle = compass.getAngle() + angle;
			////System.out.println(endangle);
			//endangle = normalizeAngle(endangle);
			action.addProperty("endangle",endangle);

		} else if ("turninto".equals(actionName)) {

			//System.out.println("***DEBUG***: Turninto");

			Compass intersection;

			//so first, we calculate the position
			//of the intersection of runways, (if any)
			String wayname = (String)action.getProperty("wayname");

			if (wayname == null) {
				//there's no runway specified!
				return false;
			}
			//is the airplane on a runway?
			Way way = airplane.getWay();
			if (way == null) { //I guess not
				return false;
			}

			//now, let's do a connectivity test
			intersection = way.getWayIntersection(wayname);

			if (intersection == null) { 	//they don't intersect! Or it doesn't exist!
											//Those motherfuckers!
				return false;
			}

			//make sure the plane is actually within vicinity
			Compass compass = airplane.getCompass();

			double dist = compass.getDistanceTo(intersection);

			if (dist > THRESHOLD_RADIUS)
			{
				return false;
			}

			//by default, the compass will point in the "beta" direction
			//of the connectedRunway.

			//check which direction we want to go to

			String point = (String)action.getProperty("waypoint");
			if ("alpha".equals(point)) {

				//add PI to the angle
				double ang = intersection.getAngle();
				ang += Math.PI;

				ang = normalizeAngle(ang);
				intersection.setAngle(ang);
			}

			//set the angular velocities and stuff
			//set the angular acceleration and angular velocity
			Double angularSpeed = (Double)action.getProperty("angularvelocity");
			if (angularSpeed == null) {
				double intang = intersection.getAngle();
				double comang = compass.getAngle();

				intang = normalizeAngle(intang);
				comang = normalizeAngle(comang);

				//if (intang > Math.PI) intang = 2*Math.PI - intang;
				//if (comang > Math.PI) comang = 2*Math.PI - comang;

				double b = Math.max(intang, comang);
				double a = Math.min(intang,comang);
				//depending on the angle difference, choose a turn velocity
				if ((b-a) > (2*Math.PI - b + a)){
					if (a == comang)
						angularSpeed = -DEFAULT_TURN_VELOCITY;
					else
						angularSpeed = DEFAULT_TURN_VELOCITY;
				}
				else{
					if (a == comang)
						angularSpeed = DEFAULT_TURN_VELOCITY;
					else
						angularSpeed = -DEFAULT_TURN_VELOCITY;
				}
			}

			//no acceleration for now
			Double angularAccel = (Double)action.getProperty("angularacceleration");
			if (angularAccel == null) {
				angularAccel = 0.0;
			}

			airplane.setAngularAcceleration(angularAccel);
			airplane.setAngularVelocity(angularSpeed);

			//set the endangle
			action.addProperty("endangle",normalizeAngle(intersection.getAngle()));

			//also, let's set the runway that the plane is on
			Way connectedWay = airport.getWay(wayname);
			airplane.setWay(connectedWay);
		} else if ("park".equals(actionName)) {
			airplane.setActive(false);	//make it disappear
			//let's just remove this from the queue immediately
			return false;
		}


		//set the airplane active
		airplane.setActive(true);

		return true;
	}

	/**
	 * Checks to see if the airplane has currently met its end conditions calculated in the
	 * previous function.
	 * @param airplane The airplane in question
	 * @param action The action being performed
	 */
	private void checkEndConditions(Airplane airplane,AirplaneAction action) {
		if (airplane == null || action == null) return;

		String actionName = action.getName();

		//we ended!
		boolean end = false;

		//check if it took way too long to execute an action
		Long startTime = (Long)action.getProperty("starttime");
		if (currentTime - startTime >= MAX_ACTION_TIME) end = true;

		//NOTE: one of the end conditions is that there is an action
		//ahead of it that can interrupt it.

		if ("land".equals(actionName)) {
			//check if the velocity reached zero
			Double endvelocity = (Double)action.getProperty("endvelocity");

			//check if it's within an epsilon range of the velocity
			if (airplane.getVelocity() <= endvelocity) {
				//aha!
				//System.out.println("Airplane " + airplane.getName() + " successfully landed!");
				end = true;

				//let's make sure the velocity and acceleration are 0
				airplane.setVelocity(0);
				airplane.setAcceleration(0);
			}

		} else if ("dock".equals(actionName)) {

			//check if the distance is increasing
			String gateName = (String)action.getProperty("gate");
			Gate gate = airport.getGate(gateName);

			Compass gateCompass = new Compass();
			gateCompass.setX(gate.getX());
			gateCompass.setY(gate.getY());

			double distance = airplane.getCompass().getDistanceTo(gateCompass);
			double oldDistance = (Double)action.getProperty("distancetogate");

			if (distance > oldDistance) {
				//the distance is increasing!
				//at this time the plane should be nearly parallel to the
				//gate
				end = true;
				//System.out.println("***DEBUG*** Plane docked");
			} else {
				//otherwise
				action.addProperty("distancetogate",distance);
			}

		} else if ("taxi".equals(actionName)) {

			AirplaneAction nextAction = airplane.getNextAction();

			//check if it's within a small epsilon radius within the endpoint
			Compass compass = airplane.getCompass();

			Compass endpoint = (Compass)action.getProperty("endpoint");
			if (endpoint != null) {

				double dist = compass.getDistanceTo(endpoint);
				double threshold = THRESHOLD_RADIUS;

				if (dist < threshold)	//10 pixels?
				{
					end = true;
				}

				//check that the distance is not increasing
				double oldThreshold = (Double)action.getProperty("distancetoendpoint");
				if (dist > oldThreshold) {
					end = true;
				} else {
					action.addProperty("distancetoendpoint",dist);
				}


			}
			//stop the plane as well
			if (end == true) {
				airplane.setVelocity(0);
				airplane.setAcceleration(0);
			}

		} else if ("takeoff".equals(actionName)) {
//			check if the velocity reached zero
			Double endvelocity = (Double)action.getProperty("endvelocity");

			if (airplane.getVelocity() >= endvelocity) {
				//aha!
				end = true;

				//also, we make the plane disappear
				airplane.setActive(false);
			}

		} else if ("place".equals(actionName)) {
			//after you palce it, it's done!
			end = true;
		} else if ("turn".equals(actionName) || "turntowards".equals(actionName)) {
			//turns are uninterruptable
			//check to see if the airplane has exceeded its turn
			Compass compass = airplane.getCompass();
			double speed = airplane.getAngularVelocity();
			Double endangle = (Double)action.getProperty("endangle");
			Double angle = (Double)action.getProperty("angle");

			//test if the angle is within a small, small delta
			//if the angle is greater than 2 pi, that means it wants to go forever
			if (angle <= 2*Math.PI)
				if (getAngleDistance(compass.getAngle(),endangle) < THRESHOLD_ANGLE) {
					compass.setAngle(endangle);
					airplane.setCompass(compass);
					end = true;
				}


			if (end) {
				//stop the turn
				airplane.setAngularAcceleration(0.0);
				airplane.setAngularVelocity(0.0);
			}

		} else if ("turninto".equals(actionName)) {
			//turns are uninterruptable
			//check to see if the airplane has exceeded its turn
			Compass compass = airplane.getCompass();
			double speed = airplane.getAngularVelocity();
			Double endangle = (Double)action.getProperty("endangle");

			//test if the angle is within a small, small delta
			if (getAngleDistance(compass.getAngle(),endangle) < THRESHOLD_ANGLE) {
				compass.setAngle(endangle);
				airplane.setCompass(compass);
				end = true;
			}

			if (end) {
				//stop the turn
				airplane.setAngularAcceleration(0.0);
				airplane.setAngularVelocity(0.0);
			}

		}

		//if the end is true
		if (end) {
			//do the callback
			AirplaneActionCallback callback = action.getCallback();

			if (callback != null) {
				callback.run();
			}

			///set the velocity and acceleration to zero
			//and remove the front action
			airplane.removeFrontAction();
			action = null;

		}
	}

	/**
	 * Normalizes the angle between 0 and 2pi
	 * @param r
	 * @return
	 */
	private double normalizeAngle(double r) {
		while (r > Math.PI*2) r -= Math.PI*2;
		while (r < 0) r += Math.PI*2;

		return r;
	}

	/**
	 * Retrieves the smallest angle distance between two angles
	 * @param a
	 * @param b
	 * @return
	 */
	private double getAngleDistance(double a,double b) {
		//normalize them first
		a = normalizeAngle(a);
		b = normalizeAngle(b);
		double d1 = Math.abs(a-b);
		double d2 = Math.abs(b-a + Math.PI*2);
		return d1 < d2 ? d1 : d2;
	}

	/**
	 * @return the finished
	 */
	public boolean isFinished() {
		return finished;
	}

	private void generateRisksAndMitigations() {

		//based on the current scenario data,
		//generate intelligent risks and mitigations

		//first, we want to generate runway incursions
		ArrayList<String> risks = new ArrayList<String>();
		HashMap<String,ArrayList<String>> mitigations = new HashMap<String,ArrayList<String>>(),
						  actions = new HashMap<String,ArrayList<String>>();

		//check if there are possibility of runway incursions
		for (Way r : planesOnWay.keySet()) {
			ArrayList<Airplane> list =
				planesOnWay.get(r);

			if (list.size() > 1) {
				//we have an incursion possibilty

				StringBuffer sb = new StringBuffer();
				sb.append("Runway incursion involving planes: ");

				for (int i=0;i<list.size();++i) {
					Airplane plane = list.get(i);
					if (i < list.size()-1)
						sb.append(plane.getName() + ", ");
					else
						sb.append("and " + plane.getName() + ".");

					//while we're at it, generate the mitigations!

				}

				String risk = sb.toString();
				risks.add(risk);

				//some of the mitigations
				ArrayList<String> mits = new ArrayList<String>();
				mitigations.put(risk,mits);

				for (Airplane plane : list) {
					String planeName = plane.getName();
					String mit = "Stop " + planeName;
					mits.add(mit);

					ArrayList<String> acts = new ArrayList<String>();
					actions.put(mit,acts);


					acts.add("Radio " + planeName);
					acts.add("Instant message " + planeName);
					acts.add("Send hologram to " + planeName);
				}


			}
		}

		//generate a risk based on the weather
/*		if ("weather".equals(scenario.getWeather())) {
			//get the type of weather from the RainGod
			int type = RainGod.getInstance().getType();

			String risk = null;

			switch (type) {
			case RainGod.FOG: {
				risk = "Fog might cause terrible accidents, like Tenerife";
				risks.add(risk);

			} break;
			case RainGod.SNOW: {
				risk = "Snow might cause terrible accidents";
				risks.add(risk);
			} break;
			case RainGod.RAIN: {
				risk = "Rain might cause airplanes to skid outta control";
				risks.add(risk);
			} break;
			}	//end switch


			if (risk != null) {
				ArrayList<String> mits = new ArrayList<String>();
				mitigations.put(risk, mits);

				String mit = "Tell the Rain God to change the weather";
				mits.add(mit);
				ArrayList<String> acts = new ArrayList<String>();
				actions.put(mit,acts);

				acts.add("Pray");
				acts.add("Send a letter to the RainGod");

				mits.add("Just deal with it");
			}

		}

		SessionManager.getInstance().updateRisksAndMitigations(risks, mitigations, actions);
*/
	}

	private boolean isAlignedWithWay(Airplane airplane,Way way) {

		Compass endpoint = way.getEndpointCompass("alpha");

		double airang = airplane.getCompass().getAngle();
		//are we exactly this angle? (doubt it)

		if (getAngleDistance(airang,endpoint.getAngle()) < THRESHOLD_ANGLE*2) {
			return true;
		}

		endpoint = way.getEndpointCompass("beta");

		airang = airplane.getCompass().getAngle();
		//are we exactly this angle? (doubt it)
		if (getAngleDistance(airang,endpoint.getAngle()) < THRESHOLD_ANGLE*2) {
			return true;
		}

		return false;
	}


	public List<Airplane> getAirplanes() {
		return airplanes;
	}

}
