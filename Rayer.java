import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.*;

import javax.swing.*;
import java.io.*;
import java.util.Vector;
import java.util.Collections;
import java.math.*;

import java.lang.Comparable;

import javax.imageio.ImageIO;
import java.io.File;

public class Rayer {
	private JFrame frame;
	private RayerPanel panel;
	private BufferedImage image;

	private Scene scene; 

	private void initFrame() {
		frame = new JFrame("Rayer");
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new RayerPanel();
		panel.setSize(800, 600);
		frame.setContentPane(panel);
		
		frame.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
					case KeyEvent.VK_F3:
						saveImage();
						break;
				}
			};
		});

		frame.show();
	}
	
	private void saveImage() {
		if (image != null) {
			File saveFile = new File("render.png");
			try {
				ImageIO.write(image, "png", saveFile);
			} catch(IOException e) {
				System.out.println(e);
			}
		}
	}

	private boolean readScene(String sceneFile) {
		if(sceneFile == null) {
			return false;
		} else {
			// TODO read scene from file
		}
		
		return false;
	}
	
	private void initDummyScene() {
		scene = new Scene();

		Sphere sphere = new Sphere(new Vector3d(1, 0, 0), 2);
		scene.addObject(sphere);

		Sphere sphere2 = new Sphere(new Vector3d(-0.5, 1, 1), 1);
		sphere2.material.diffuse = Color.GREEN;
		scene.addObject(sphere2);
		
		Sphere sphere3 = new Sphere(new Vector3d(-2, 1, -1), 1);
		sphere3.material.diffuse = Color.BLUE;
		scene.addObject(sphere3);
		
		Sphere sphere4 = new Sphere(new Vector3d(-2.7, 0.5, -1), 0.9);
		sphere4.material.diffuse = Color.YELLOW;
		scene.addObject(sphere4);
	}

	public void logSceneContents() {
		System.out.println("Scene contents: -------");
		System.out.println(scene.toString());
	}
	
	// abstract?
	private RayHit getRayHit(Ray ray, SceneObject object) {
		if(object instanceof Sphere) {
			return getRayHit(ray, (Sphere)object);
		} else {
			return null;
		}
	}

	private RayHit getRayHit(Ray ray, Sphere sphere) {
		double dist = ray.direction.normalize().cross(sphere.position.sub(ray.position)).getLength();
//		System.out.println("Ray sphere dist: "+dist);
		if(dist < sphere.radius) { 
//			System.out.println("Ray sphere hit with dist: "+dist);

			// TODO position and normal
			Vector3d minDistPoint = ray.position.add(ray.direction.normalize().scale(ray.direction.normalize().dot(sphere.position.sub(ray.position))));
			Vector3d hitPoint = minDistPoint.sub(ray.direction.normalize().scale(Math.sin(Math.acos(dist/sphere.radius))*sphere.radius));
			Vector3d normal = hitPoint.sub(sphere.position).normalize();

			return new RayHit(ray, sphere, hitPoint, normal);
		} else {
			return null;
		}
	}

	private Vector<RayHit> getRayHits(Scene scene, int screenX, int screenY) {
		Vector<RayHit> hits = new Vector<RayHit>();
		
		Ray camRay = scene.getCamera().getCamRayForScreenPosition(screenX, screenY);
//		System.out.println("Cam ray for ["+screenX+", "+screenY+"]:");
//		System.out.println(camRay);
		
		// TODO
		for(SceneObject object: scene.getObjects()) {
			RayHit hit = getRayHit(camRay, object);
			if(hit != null) {
				hits.add(hit);
			}
		}
		
		Collections.sort(hits);

		return hits;
	}

	private void renderPixel(int i, int j) {
		Color color;

		Vector<RayHit> hits = getRayHits(scene, i, j);
		if(hits.size() > 0) {
			// TODO order front to back, use first
			RayHit hit = hits.get(0);
			
			Color diffuse = hit.object.material.diffuse;

			// TODO normal is irrelevant for lambert, should use light direction
			//      for now light is directly at camera
			float factor = (float) Math.min(Math.max(hit.ray.direction.scale(-1).dot(hit.normal), 0.0), 1.0);
			color = new Color(
				(int) (diffuse.getRed()*factor),
				(int) (diffuse.getGreen()*factor),
				(int) (diffuse.getBlue()*factor)
			);
//			System.out.println("factor: "+factor+", color red: "+color.getRed());
		} else {
			// TODO background?
			color = Color.BLACK;
		}

		image.setRGB(i, j, color.getRGB());
	}
	
	public void renderScene() {
		logSceneContents();
		
		panel.clear();
		image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
		
		scene.getCamera().setScreenSize(image.getWidth(), image.getHeight());

		for(int j = 0; j < image.getHeight(); j++) {
			for(int i = 0; i < image.getWidth(); i++) {
				renderPixel(i, j);
			}
		}
		panel.setImage(image);

		panel.invalidate();
		panel.repaint();
	}
	
	public Rayer() {
		this(null);
	}

	public Rayer(String sceneFile) {
		initFrame();
		
		if(!readScene(sceneFile)) {
			initDummyScene();
		}

		renderScene();
	}
	
	public static void main(String[] args) {
		Rayer rayer;
		if(args.length > 0) {
			rayer = new Rayer(args[0]); // open scene
		} else {
			rayer = new Rayer();
		}
	}
}

class Scene {
	private Vector<SceneObject> objects;
	
	private Camera camera;
	
	public Scene() {
		objects = new Vector<SceneObject>();
		camera = new Camera(new Vector3d(0, 0, -10));
	}

	public void addObject(SceneObject object) {
		objects.add(object);
	}

	public Vector<SceneObject> getObjects() {
		return objects;
	}

	public Camera getCamera() {
		return camera;
	}

	public String toString() {
		String result = "Scene {\n";
		result += ("\t"+camera.toString()+"\n");
		for(SceneObject object: objects) {
			result += ("\t"+object.toString()+"\n");
		}
		result += "}";

		return result;
	}
}

class SceneObject {
	public String type;
	public Vector3d position;
	
	public Material material;

	public SceneObject(Vector3d position) {
		this.position = position;
		
		type = "SceneObject";
		material = new Material();
	}

	public String toString() {
		return "SceneObject (position: "+position.toString()+")";
	}
}

class Sphere extends SceneObject {
	public double radius = 1;
	
	public Sphere(Vector3d position) {
		super(position);
	}
	
	public Sphere(Vector3d position, double radius) {
		this(position);
		
		this.type = "Sphere";
		this.radius = radius;
	}

	public String toString() {
		return "Sphere (center: "+position.toString()+", radius: "+radius+")";
	}
}

class Material {
	public Color diffuse;

	public Material() {
		//default
		diffuse = Color.RED;
	}
}

class Camera extends SceneObject {
	private double fovH = 80;
	private double fovV = 80;
	
	private int screenWidth, screenHeight;
	private double fovRatioX, fovRatioY;

	public Camera(Vector3d position, int screenWidth, int screenHeight) {
		super(position);
		this.type = "Camera";

		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		calculateFOVRatio();
	}

	public Camera(Vector3d position) {
		this(position, 1, 1);
	}
	
	public void setScreenSize(int width, int height) {
		screenWidth = width;
		screenHeight = height;

		calculateFOVRatio();
	}

	private void calculateFOVRatio() {
		fovRatioX = fovH/screenWidth;
		fovRatioY = fovV/screenWidth;
	}

	public Ray getCamRayForScreenPosition(int i, int j) {
		double x = Math.tan(Math.toRadians((i - screenWidth/2)*fovRatioX));
		double y = Math.tan(Math.toRadians(((screenHeight - j) - screenHeight/2)*fovRatioY));
		return new Ray(position, new Vector3d(x, y, 1));
	}

	public String toString() {
		return "Camera (position: "+position.toString()+", FoV: "+fovH+"/"+fovV+")";
	}
}

class Vector3d {
	public double x, y, z;
	
	public Vector3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3d add(Vector3d other) {
		return new Vector3d(x + other.x, y + other.y, z + other.z);
	}

	public Vector3d sub(Vector3d other) {
		return new Vector3d(x - other.x, y - other.y, z - other.z);
	}
	
	public Vector3d scale(double scalar) {
		return new Vector3d(x*scalar, y*scalar, z*scalar);
	}

	public double getLength() {
		return Math.sqrt(x*x + y*y + z*z);
	}
	
	public Vector3d normalize() {
		double length = getLength();
		if(length > 0) {
			return scale(1/length);
		} else {
			return scale(1);
		}
	}

	public double dot(Vector3d other) {
		return (x*other.x + y*other.y + z*other.z);
	}

	public Vector3d cross(Vector3d other) {
		return new Vector3d(
			y*other.z - z*other.y,
			z*other.x - x*other.z,
			x*other.y - y*other.x
		);
	}

	public String toString() {
		return "["+x+", "+y+", "+z+"]";
	}
}

class Vector2d {
	public double x, y;

	public Vector2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return "["+x+", "+y+"]";
	}
}

class Ray {
	public Vector3d position;
	public Vector3d direction;

	public Ray(Vector3d position, Vector3d direction) {
		this.position = position;
		this.direction = direction;
	}

	public String toString() {
		return "Ray (position "+position+", direction"+direction+")";
	}
}

class RayHit implements Comparable<RayHit> {
	public Ray ray;
	public SceneObject object;
	public Vector3d position;
	public Vector3d normal;

	public RayHit(Ray ray, SceneObject object, Vector3d position, Vector3d normal) {
		this.ray = ray;
		this.object = object;
		this.position = position;
		this.normal = normal;
	}

	public int compareTo(RayHit other) {
		return Double.compare(this.position.sub(ray.position).getLength(), other.position.sub(other.ray.position).getLength());
	}
}

class RayerPanel extends JPanel {
	private BufferedImage image;
	
	public void paint(Graphics g) {
		super.paint(g);

		if(image != null) {
			g.drawImage(image, 0, 0, this);
		}
	}

	public void clear() {
		image = null;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}
}
