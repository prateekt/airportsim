package gui;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.File;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;

import airport.*;
import airport.objects.*;

import java.util.*;

/**
 * @author Henry Yuen
 * Edited by Julian Vergel de Dios on 04/07/08
 */
public class AirportViewPanel extends JPanel implements Scrollable, ChangeListener, MouseListener {

	public enum Mode {
		WAYPOINT,
		NORMAL,
	}

	private Mode mode;

	public final static int WORLD_WIDTH = 3000;
	public final static int WORLD_HEIGHT = 3000;

	public final static int WAYPOINT_RADIUS = 8;

	double zoomFactor;	//this is the factor at which the map is zoomed
	Image planeImage,planeWreckImage,hangarImage,terminalImage;
	Image jetImage,jetWreckImage;
	Image shuttleImage,shuttleWreckImage;
	Image snowTileImage,grassTileImage;
	Image waypointImage;
	ImageIcon imageIcon;
	JScrollPane scrollPane;

	private int maxUnitIncrement = 1;

	//status JLabels
	private JLabel scenarioLabel,timeLabel,weatherLabel,finishedLabel,zoomLabel;
	private JPanel statusPanel;

	//width of the screen
	private int screenWidth,screenHeight;

	private GUI gui = null;

	private WaypointMakerPanel waypointMaker = null;

	//for waypointing
	ArrayList<AirportNode> waypointList;


	public AirportViewPanel(GUI gui) {
		this.gui = gui;
		this.setBackground(new Color(192,192,192)); //set the background color to white.

		zoomFactor = 0.8;
		screenWidth = WORLD_WIDTH;
		screenHeight = WORLD_HEIGHT;

		//this.setPreferredSize(new Dimension(screenWidth,screenHeight));

		//get the current scenario from the session Manager
		imageIcon = new ImageIcon("resource/img/747.gif");
		planeImage = imageIcon.getImage(); //gets the airplane image

		imageIcon = new ImageIcon("resource/img/hangar.png");
		hangarImage = imageIcon.getImage();

		imageIcon = new ImageIcon("resource/img/grass.png");
		grassTileImage = imageIcon.getImage();

		imageIcon = new ImageIcon("resource/img/waypoint.png");
		waypointImage = imageIcon.getImage();

		scrollPane = null;
		statusPanel = null;
		scenarioLabel = timeLabel = finishedLabel = null;

		waypointList = new ArrayList<AirportNode>();
		mode = Mode.NORMAL;

		//add a mouse listener
		addMouseListener(this);

	}

	public void setScrollPane(JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
		if (scrollPane != null)
			scrollPane.getViewport().addChangeListener(this);
	}

	public void stateChanged(ChangeEvent e) {
		//let's recheck
		if (scrollPane == null) return;

		//force a repaint
		repaint();

	}

	public void setLabels(JPanel status,JLabel scenario,JLabel time,JLabel weather,JLabel finished,JLabel zoom) {
		statusPanel = status;
		scenarioLabel = scenario;
		timeLabel = time;
		weatherLabel = weather;
		finishedLabel = finished;
		zoomLabel = zoom;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        PrintManager.getInstance().setGraphics(g2d);

		//render the airport!
		//first, fill the background
		//drawBackground(g2d);
        //g2d.setColor(new Color(32,128,32));
		//g2d.fillRect(0, 0, this.getWidth(), this.getHeight());

		//render the runways first
		drawRunways(g2d);

		drawNodes(g2d);

		//draw terminals
		//drawTerminals(g2d);
		//then render the airplanes on top.
		drawAirplanes(g2d);
		//draw hangars
		drawHangars(g2d);

		drawRunwayLabels(g2d);


		//display the explosives
		//ExplosiveFactory.getInstance().paint(g2d,zoomFactor);

		//rain god!
		//Scenario scenario = SessionManager.getInstance().getCurrentScenario();

		//if (scenario != null && "weather".equals(scenario.getWeather())) {
		//	RainGod.getInstance().paint(g2d,zoomFactor);
		//}

		//update
		//updateStatusMessages();

	}

	/**
	 * Renders the runways from the given scenario file.
	 * Draw them as rotated rectangles.
	 */
	private void drawRunways(Graphics2D g2d) {
		//Scenario scenario = SessionManager.getInstance().getCurrentScenario();
		//if (scenario == null) return;
		if(gui.getAirport() == null) return;

		HashMap<String,Way> runwayMap = gui.getAirport().getWays();

		for (String r : runwayMap.keySet()) {
			Way runway = runwayMap.get(r);
			double angle = runway.getAngle();
			int x = runway.getX();
			int y = runway.getY();
			int length = runway.getLength();
			int width = runway.getWidth();

			//the runways' XY coordinate is at the runway's center.
			AffineTransform at = new AffineTransform();
			at.scale(zoomFactor,zoomFactor);
			at.translate(x-length/2,y-width/2);
			at.rotate(angle,length/2,width/2);


			//draw a rectangle
			Shape runwayRect;
			Rectangle rect = new Rectangle(0,0,length,width);
			runwayRect = at.createTransformedShape(rect);

			//set the stroke
			g2d.setColor(new Color(32,32,32));
			g2d.fill(runwayRect);

		}

		//DRAW ALL THE RUNWAY MARKS

		for (String r : runwayMap.keySet()) {
			Way runway = runwayMap.get(r);
			double angle = runway.getAngle();
			int x = runway.getX();
			int y = runway.getY();
			int length = runway.getLength();
			int width = runway.getWidth();

			AffineTransform at = new AffineTransform();
			at.scale(zoomFactor, zoomFactor);
			at.translate(x-length/2,y-width/18);
			at.rotate(angle,length/2,width/18);


			//draw the runway marks
			Shape marksRect;
			Rectangle rect = new Rectangle(0,0,length,width/9);
			marksRect = at.createTransformedShape(rect);

			g2d.setColor(Color.YELLOW);
			g2d.fill(marksRect);
		}

	}

	public void drawRunwayLabels(Graphics2D g2d) {
		//Scenario scenario = SessionManager.getInstance().getCurrentScenario();
		//if (scenario == null) return;
		if(gui.getAirport() == null) return;

		HashMap<String,Way> runwayMap = gui.getAirport().getWays();

		//DRAW ALL THE RUNWAY MARKS

		for (String r : runwayMap.keySet()) {
			Way runway = runwayMap.get(r);
			int x = runway.getX();
			int y = runway.getY();

			int x2 = (int)(x*zoomFactor);
			int y2 = (int)(y*zoomFactor);

			PrintManager.getInstance().print("medium",runway.getName(),x2+2,y2+2,
					Color.black);
			PrintManager.getInstance().print("medium",runway.getName(),x2,y2,
					Color.white);
		}

	}
	/*
	 * draws airplanes onto canvas, after rendering runways. Retrieves information from scenario files.
	 */
	public void drawAirplanes(Graphics2D g2d){
		//Scenario scenario = SessionManager.getInstance().getCurrentScenario();
		//if (scenario == null)
		//	return;
		if(gui.getAirplanes() == null) return;

		HashMap<String,Airplane> airplanes = gui.getAirplanes();

		//draws all the planes outlined in current timeslice.
		for (String s : airplanes.keySet()) {
			Airplane airplane = airplanes.get(s);
			if (airplane.getActive() == false) continue;

			Compass compass = airplane.getCompass();
			double angle = compass.getAngle();
			double x = compass.getX();
			double y = compass.getY();

			//the airplanes' XY coordinate is at its center.

			AffineTransform at = new AffineTransform();

			Image image = null;
			String model = airplane.getModel();

			if (airplane.isAlive()) {
				if ("747".equals(model)) {
					image = planeImage;
				} else if ("jet".equals(model)) {
					image = jetImage;
				} else if ("shuttle".equals(model)) {
					image = shuttleImage;
				} else {
					image = planeImage;
				}
			} else {
				if ("747".equals(model)) {
					image = planeWreckImage;
				} else if ("jet".equals(model)) {
					image = jetWreckImage;
				} else {
					image = planeWreckImage;
				}

			}

			image = planeImage;

			double scale = 0.25;

			//determine if the plane is going uber fast
			double v = airplane.getVelocity();
			if (v > 0.6) {
				double ratio = (v-0.6)/0.2;
				scale = 0.25 + (ratio*0.15);
			}

			double swidth = image.getWidth(this)*scale;
			double sheight = image.getHeight(this)*scale;

			at.scale(zoomFactor,zoomFactor);
			at.translate(x-swidth/2,y-sheight/2);
			at.rotate(angle,swidth/2,sheight/2);
			at.scale(scale,scale);

			g2d.drawImage(image, at, this);

			//print some text
			x = zoomFactor*(x);
			y = zoomFactor*(y - 15);

			PrintManager.getInstance().print("medium",airplane.getName(),(int)x + 2, (int)y + 2, Color.black);
			PrintManager.getInstance().print("medium",airplane.getName(),(int)x, (int)y, Color.PINK);
		}

	}

	private void drawHangars(Graphics2D g2d) {
		//Scenario scenario = SessionManager.getInstance().getCurrentScenario();
		//if (scenario == null)
		//	return;
		if(gui.getAirport() == null) return;

		HashMap<String,Hangar> hangars = gui.getAirport().getHangars();

		//draws all the planes outlined in current timeslice.
		for (String s : hangars.keySet()) {
			Hangar hangar = hangars.get(s);

			double angle = hangar.getAngle();
			double x = hangar.getX();
			double y = hangar.getY();

			AffineTransform at = new AffineTransform();

			double scale = 1.0;
			double swidth = hangarImage.getWidth(this)*scale;
			double sheight = hangarImage.getHeight(this)*scale;

			at.scale(zoomFactor,zoomFactor);
			at.translate(x-swidth/2,y-sheight/2);
			at.rotate(angle,swidth/2,sheight/2);
			at.scale(scale,scale);

			g2d.drawImage(hangarImage, at, this);

			//print some text
			x = zoomFactor*(x);
			y = zoomFactor*(y - 20);

			//PrintManager.getInstance().print("medium",hangar.getName(),(int)x, (int)y, Color.RED);
		}
	}
/*
	private void drawTerminals(Graphics2D g2d) {
		Scenario scenario = SessionManager.getInstance().getCurrentScenario();
		if (scenario == null)
			return;

		HashMap<String,Terminal> terminals = scenario.getAirport().getTerminals();

		//draws all the planes outlined in current timeslice.
		for (String s : terminals.keySet()) {
			Terminal terminal = terminals.get(s);

			double angle = terminal.getAngle();
			double x = terminal.getX();
			double y = terminal.getY();

			AffineTransform at = new AffineTransform();

			double scale = 1.0;
			double swidth = terminalImage.getWidth(this)*scale;
			double sheight = terminalImage.getHeight(this)*scale;

			at.scale(zoomFactor,zoomFactor);
			at.translate(x-swidth/2,y-sheight/2);
			at.rotate(angle,swidth/2,sheight/2);
			at.scale(scale,scale);

			g2d.drawImage(terminalImage, at, this);

			//print some text
			x = zoomFactor*(x);
			y = zoomFactor*(y - 20);

			// Draw terminal name
			//PrintManager.getInstance().print("medium",terminal.getName(),(int)x, (int)y, Color.RED);
		}
	}*/

	private void drawBackground(Graphics2D g2d) {
		//Scenario scenario = SessionManager.getInstance().getCurrentScenario();
		//if (scenario == null)
		//	return;

		//int type = RainGod.getInstance().getType();

		AffineTransform at = new AffineTransform();

		//scaleFactor
		double sx = screenWidth/1000.0;
		double sy = screenHeight/1000.0;

		at.translate(0,0);
		at.scale(sx,sy);
/*
		if ("weather".equals(scenario.getWeather())) {
			//get the weather from the rain god
			if (type == RainGod.SNOW) {
				g2d.drawImage(snowTileImage,at,this);
			} else {
				g2d.drawImage(grassTileImage,at,this);
			}
		}
		else {
			g2d.drawImage(grassTileImage,at,this);
		}
		*/
		g2d.drawImage(grassTileImage,at,this);
	}

	private void drawNodes(Graphics2D g2d) {

		//get the connectivity graph from the airport
		Airport airport = gui.getAirport();
		if (airport == null) return;

		HashMap<String,AirportNode> connectivityGraph =
			airport.getConnectivityGraph();

		for (String name : connectivityGraph.keySet()) {
			AirportNode node = connectivityGraph.get(name);

			Compass compass = node.getCompass();

			int width = WAYPOINT_RADIUS*2;
			int height = WAYPOINT_RADIUS*2;
			int x = (int)compass.getX() - width/2;
			int y = (int)compass.getY() - height/2;

			x = (int)(x*zoomFactor);
			y = (int)(y*zoomFactor);

			if (node.getType() == AirportNode.NodeType.GATE)
				g2d.setColor(Color.red);
			else
				g2d.setColor(Color.green);

			g2d.fillOval(x,y,width,height);

			x -= 2;
			y -= 2;
			width += 4;
			height += 4;

			g2d.drawOval(x,y,width,height);

			if (node.getType() == AirportNode.NodeType.GATE) {
				PrintManager.getInstance().print("small",node.getName(),x+2,y-8,Color.black);
				PrintManager.getInstance().print("small",node.getName(),x,y-10,Color.yellow);

			}

			if (mode == Mode.WAYPOINT) {
				if (waypointList.contains(node)) {
					AffineTransform at = new AffineTransform();

					double scale = 1.0;
					double swidth = waypointImage.getWidth(this)*scale;
					double sheight = waypointImage.getHeight(this)*scale;

					y -= sheight - 3;

					//at.scale(zoomFactor,zoomFactor);
					at.translate(x,y);
					at.scale(scale,scale);

					g2d.drawImage(waypointImage,at,this);

					//use print manager babeh!
					int number = waypointList.indexOf(node) + 1;
					PrintManager.getInstance().print("small",""+number,x+4,y+12,Color.black);
				}
			}

		}

	}


	private void updateStatusMessages() {
		Airport airport = gui.getAirport();
		if (airport == null)
			return;

		//draw the scenario name
		if (airport != null) {
			if (scenarioLabel != null)
				scenarioLabel.setText("Scenario: " + airport.getName());
		}

		double time = (double)TheSimulator.getInstance().getCurrentTime()/1000.0;

		if (timeLabel != null)
			timeLabel.setText("Time: " + time);

		if (TheSimulator.getInstance().isFinished())
			finishedLabel.setText("Finished");
		else
			finishedLabel.setText("");

/*	//see what weather it is
		if ("weather".equals(airport.getWeather())) {
			//get the weather from the rain god
			weatherLabel.setText("Weather: " + RainGod.getInstance().getStringType());
		}
		else {
			weatherLabel.setText("Weather: sunny");
		}*/

		zoomLabel.setText("Zoom: " + (int)(zoomFactor*100.0) + "%");

		if (statusPanel != null)
			statusPanel.repaint();
	}


	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect,int orientation,int direction) {
        //Get the current position.
        int currentPosition = 0;
        if (orientation == SwingConstants.HORIZONTAL) {
            currentPosition = visibleRect.x;
        } else {
            currentPosition = visibleRect.y;
        }


		//Return the number of pixels between currentPosition
        //and the nearest tick mark in the indicated direction.
        if (direction < 0) {
            int newPosition = currentPosition -
                             (currentPosition / maxUnitIncrement)
                              * maxUnitIncrement;
            return (newPosition == 0) ? maxUnitIncrement : newPosition;
        } else {
            return ((currentPosition / maxUnitIncrement) + 1)
                   * maxUnitIncrement
                   - currentPosition;
        }

	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,int orientation,int direction) {

		if (orientation == SwingConstants.HORIZONTAL) {
			return visibleRect.width - maxUnitIncrement;
		} else {
			return visibleRect.height - maxUnitIncrement;
		}
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public void setMaxUnitIncrement(int pixels) {
		maxUnitIncrement = pixels;
	}

	/**
	 * @return the zoomFactor
	 */
	public double getZoomFactor() {
		return zoomFactor;
	}

	public void setZoomFactor(double z) {
		//see if it's different
		if (zoomFactor != z) {
			//we gotta readjust man
			screenWidth = (int)(WORLD_WIDTH*z);
			screenHeight = (int)(WORLD_HEIGHT*z);

			this.setSize(screenWidth,screenHeight);
			this.setPreferredSize(new Dimension(screenWidth,screenHeight));
		}

		zoomFactor = z;

	}

	public void setWaypointMode() {
		mode = Mode.WAYPOINT;
		waypointList.clear();
	}

	public void setNormalMode() {
		mode = Mode.NORMAL;
	}

	public void removeWaypoint(AirportNode waypoint) {

		if (mode != Mode.WAYPOINT) return;
		waypointList.remove(waypoint);

	}

	public void mouseClicked(MouseEvent e) {
		if (mode == Mode.WAYPOINT) {
			//the mouse was clicked - where?
			Point p = e.getPoint();

			//see if this within the radius of an airportNode
			AirportNode closest = null;
			double minDistance = Double.POSITIVE_INFINITY;

			Airport airport = gui.getAirport();
			Collection<AirportNode> nodes = airport.getConnectivityGraph().values();
			for (AirportNode node : nodes) {
				//get distance from mousepoint
				Compass mouse = new Compass();
				mouse.setX(p.getX()/zoomFactor);
				mouse.setY(p.getY()/zoomFactor);
				double distance = node.getCompass().getDistanceTo(mouse);
				if (distance < WAYPOINT_RADIUS)
					if (distance < minDistance) {
						minDistance = distance;
						closest = node;
					}
			}

			if (closest != null) {
				//put this into the hashmap
				//make sure it already isn't in the map
				if (!waypointList.contains(closest)) {
					waypointList.add(closest);

					//update the waypointMaker
					waypointMaker.addWaypoint(closest);

					//repaint
					repaint();
				}
			}


		}
	}

    public void mousePressed(MouseEvent e) {
     }

     public void mouseReleased(MouseEvent e) {
     }

     public void mouseEntered(MouseEvent e) {
     }

     public void mouseExited(MouseEvent e) {
     }

     public void setWaypointMaker(WaypointMakerPanel wpf) {
    	 this.waypointMaker = wpf;
     }
}
