import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Color;
import javax.swing.*;
import java.io.*;
import java.util.Vector;
import java.math.*;

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

		frame.show();
	}
	
	private boolean readScene(String sceneFile) {
		// TODO
		
		return false;
	}
	
	private void initDummyScene() {
		scene = new Scene();

		Sphere sphere = new Sphere(new Vector3d(0, 0, 0), 3);
		scene.addObject(sphere);
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
			return new RayHit(ray, sphere, new Vector3d(0, 0, 0), new Vector3d(0, 0, 0));
		} else {
			return null;
		}
	}

	private Vector<RayHit> getRayHits(Scene scene, int screenX, int screenY) {
		Vector<RayHit> result = new Vector<RayHit>();
		
		Ray camRay = scene.getCamera().getCamRayForScreenPosition(screenX, screenY);
//		System.out.println("Cam ray for ["+screenX+", "+screenY+"]:");
//		System.out.println(camRay);
		
		// TODO
		for(SceneObject object: scene.getObjects()) {
			RayHit hit = getRayHit(camRay, object);
			if(hit != null) {
				result.add(hit);
			}
		}

		return result;
	}

	private void renderPixel(int i, int j) {
		Color color;

		Vector<RayHit> hits = getRayHits(scene, i, j);
		if(hits.size() > 0) {
			// TODO
			color = Color.WHITE;
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

	public Rayer(String sceneFile) {
		initFrame();
		
		if(!readScene(sceneFile)) {
			initDummyScene();
		}

		renderScene();
	}

	public static void main(String[] args) {
		Rayer rayer = new Rayer(args[0]);
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

	public SceneObject(Vector3d position) {
		this.position = position;
		
		type = "SceneObject";
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
		double y = Math.tan(Math.toRadians((j - screenHeight/2)*fovRatioY));
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

class RayHit {
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
