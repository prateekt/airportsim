package gui;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class ExplosiveFactory {
	private final static int MAX_EXPLOSIONS = 3;
	private final static double DEFAULT_RATE = 0.001;
	private static ExplosiveFactory instance = null;
	private Image explosionImage;

	class Explosion {
		public int x,y;
		public double progress;
		public double rate;

		public Explosion() {
			x = y = 0;
			progress = 0;
			rate = 1.0;
		}

		public boolean update(long timeElapsed) {
			progress += rate*timeElapsed;

			if (progress >= 1.0)	//one hundred percent
				return false;

			return true;
		}

		public void paint(Graphics2D g2d,double zoomFactor) {

			float alpha = 1.0f - (float)progress;
			double scale = 1.0 + progress;

			if (alpha > 1.0f) alpha = 1.0f;
			if (alpha < 0) alpha = 0;

			Composite comp = g2d.getComposite();
			g2d.setComposite(
					AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));

			double swidth = explosionImage.getWidth(null)*scale;
			double sheight = explosionImage.getHeight(null)*scale;

			AffineTransform at = new AffineTransform();

			//get the zoomfactor
			at.scale(zoomFactor, zoomFactor);
			at.translate(x - swidth/2, y - sheight/2);
			at.scale(scale, scale);

			g2d.drawImage(explosionImage,at,null);

			g2d.setComposite(comp);

		}
	}

	private ArrayList<Explosion> explosions;
	private ArrayList<Explosion> garbage;

	private ExplosiveFactory(){
		explosions = new ArrayList<Explosion>();
		garbage = new ArrayList<Explosion>();
		//load the images
		ImageIcon icon = new ImageIcon("resource/img/explosion.png");
		explosionImage = icon.getImage();

	}

	public static ExplosiveFactory getInstance() {
		if (instance == null) instance = new ExplosiveFactory();
		return instance;
	}

	public void createExplosion(int x,int y) {
		if (explosions.size() > MAX_EXPLOSIONS) return;

		Explosion exp = new Explosion();
		exp.x = x;
		exp.y = y;
		exp.rate = DEFAULT_RATE;
		explosions.add(exp);
	}

	public void update(long timeElapsed) {

		for (Explosion e : explosions) {
			if (e.update(timeElapsed) == false) {
				//we gotta add this to the delete list
				garbage.add(e);
			}
		}

		synchronized (explosions) {
			for (Explosion e : garbage) {
				explosions.remove(e);
			}
		}

		garbage.clear();
	}

	public void paint(Graphics2D g2d,double zoomFactor) {
		synchronized (explosions) {
			for (Explosion e : explosions) {
				e.paint(g2d,zoomFactor);
			}
		}
	}


}
