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
	
	//test
	private static int t = 0;

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
						saveImage("render.png");
						break;
					case KeyEvent.VK_ESCAPE:
						System.exit(0);
						break;

					// TEST
					case KeyEvent.VK_ENTER:
						t++;
						initDummyScene(t);
						renderScene();
						saveImage("testrender_"+t+".png");
						break;
				}
			};
		});

		frame.show();
	}
	
	private void saveImage(String filename) {
		if (image != null) {
			File saveFile = new File(filename);
			try {
				ImageIO.write(image, "png", saveFile);
			} catch(IOException e) {
				System.out.println(e);
			}
		}
	}

	private boolean readScene(String sceneFile, int t) {
		if(sceneFile == null) {
			return false;
		} else {
			// TODO read scene from file, apply time
		}
		
		return false;
	}
	
	private void initDummyScene(int t) {
		scene = new Scene();
		
		Camera camera = new Camera(new Vector3d(Math.sin((double)t/100)*10, 0, -Math.cos((double)t/100)*10));
		camera.setDirection(new Vector3d(0, 0, 0).sub(camera.position).normalize());
		camera.setFOV(80, 80);
		scene.setCamera(camera);

		Light light = new Light(new Vector3d(-20, 20, -50));
		light.color = new Color(255, 255, 0);
		scene.addLight(light);

		Light light2 = new Light(new Vector3d(20, -20, -20));
		light2.color = new Color(255, 0, 255);
		scene.addLight(light2);

		Light light3 = new Light(new Vector3d(0, 50, -20));
		light3.color = new Color(255, 0, 255);
		scene.addLight(light3);

		Sphere sphere = new Sphere(new Vector3d(1, 0, 0), 2);
//		sphere.material.diffuse = Color.RED;
		scene.addObject(sphere);

		Sphere sphere2 = new Sphere(new Vector3d(-0.5, 1, 1), 1);
//		sphere2.material.diffuse = Color.GREEN;
		scene.addObject(sphere2);
		
		Sphere sphere3 = new Sphere(new Vector3d(-2, 1, -3), 1);
//		sphere3.material.diffuse = Color.BLUE;
		scene.addObject(sphere3);
		
		Sphere sphere4 = new Sphere(new Vector3d(-2.7, 0.5, -3), 0.9);
//		sphere4.material.diffuse = Color.YELLOW;
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
			
			double red = 0;
			double green = 0;
			double blue = 0;
			for(Light light: scene.getLights()) {
				boolean shadowed = false;
				Ray lightRay = new Ray(light.position, hit.position.sub(light.position));
				for(SceneObject object: scene.getObjects(hit.object)) {
					RayHit lightHit = getRayHit(lightRay, object);
					if(lightHit != null && lightHit.position.sub(light.position).getLength() < hit.position.sub(light.position).getLength()) {
						shadowed = true;
						break;
					}
				}
				
				if(!shadowed) {
					float factor = (float) light.position.sub(hit.position).normalize().dot(hit.normal);
					red += (diffuse.getRed()*factor*((double)light.color.getRed()/255));
					green += (diffuse.getGreen()*factor*((double)light.color.getGreen()/255));
					blue += (diffuse.getBlue()*factor*((double)light.color.getBlue()/255));
				}
			}
		
			color = new Color(
				(int) (Math.max(Math.min(red, 255), 0)),
				(int) (Math.max(Math.min(green, 255), 0)),
				(int) (Math.max(Math.min(blue, 255), 0))
			);
//			System.out.println("color: "+color);
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
		this(null, 0);
	}

	public Rayer(String sceneFile) {
		this(sceneFile, 0);
	}

	public Rayer(String sceneFile, int t) {
		initFrame();
		
		if(!readScene(sceneFile, t)) {
			initDummyScene(t);
		}

		renderScene();
	}
	
	public static void main(String[] args) {
		Rayer rayer;
		if(args.length > 1) {
			int t = Integer.parseInt(args[1]);
			rayer = new Rayer(args[0], t);
		} else if(args.length == 1) {
			rayer = new Rayer(args[0]); // open scene
		} else {
			rayer = new Rayer();
		}
	}
}

class Scene {
	private Vector<SceneObject> objects;
	private Camera camera;
	private Vector<Light> lights;

	public Scene() {
		objects = new Vector<SceneObject>();
		lights = new Vector<Light>();
	
		camera = new Camera();
	}

	public void addObject(SceneObject object) {
		objects.add(object);
	}

	public Vector<SceneObject> getObjects() {
		return objects;
	}

	public Vector<SceneObject> getObjects(SceneObject except) {
		Vector<SceneObject> result = (Vector<SceneObject>) objects.clone();
		result.remove(except);
		return result;
	}
	
	public void addLight(Light light) {
		lights.add(light);
	}

	public Vector<Light> getLights() {
		return lights;
	}

	public Camera getCamera() {
		return camera;
	}
	
	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public String toString() {
		String result = "Scene {\n";
		result += ("\t"+camera.toString()+"\n");
		for(Light light: lights) {
			result += ("\t"+light.toString()+"\n");
		}
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
		return "SceneObject { type: "+type+", position: "+position+", material: "+material+" } ";
	}
}

class Sphere extends SceneObject {
	public double radius = 1;
	
	public Sphere(Vector3d position) {
		super(position);
	
		this.type = "Sphere";
	}
	
	public Sphere(Vector3d position, double radius) {
		this(position);
		
		this.radius = radius;
	}

	public String toString() {
		return "Sphere { center: "+position.toString()+", radius: "+radius+" }";
	}
}

class Material {
	public Color diffuse;

	public Material() {
		//default
		diffuse = Color.LIGHT_GRAY;
	}

	public String toString() {
		return "Material { diffuse: "+diffuse+" }";
	}
}

class Camera extends SceneObject {
	private Vector3d direction;
	private Quaternion rotationToDirection;

	private double fovH = 80;
	private double fovV = 80;
	
	private int screenWidth, screenHeight;
	private double fovRatioX, fovRatioY;

	public Camera(Vector3d position, Vector3d direction, int screenWidth, int screenHeight) {
		super(position);

		setDirection(direction);
		this.type = "Camera";

		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		calculateFOVRatio();
	}

	public Camera(Vector3d position, int screenWidth, int screenHeight) {
		this(position, new Vector3d(0, 0, 1), screenWidth, screenHeight);
	}

	public Camera(Vector3d position) {
		this(position, 1, 1);
	}
	
	public Camera() {
		this(new Vector3d(0, 0, 0));
	}

	public void setFOV(double fovX, double fovY) {
		fovH = fovX;
		fovV = fovY;

		calculateFOVRatio();
	}
	
	public void setDirection(Vector3d direction) {
		rotationToDirection = new Vector3d(0, 0, 1).rotationTo(direction.normalize());
		this.direction = direction.rotate(rotationToDirection); // TODO check - should'nt this be just direction?
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
		return new Ray(position, new Vector3d(x, y, 1).normalize().rotate(rotationToDirection)); 
	}

	public String toString() {
		return "Camera { position: "+position+", direction: "+direction+", FoV: "+fovH+"/"+fovV+" }";
	}
}

class Light extends SceneObject {
	public Color color;
	public double intensity = 1;
	// TODO direction for sun lights;
	// TODO falloff for spotlights

	public Light(Vector3d position) {
		super(position);
		this.type = "Light";
		color = Color.WHITE; // default
	}

	public String toString() {
		return "Light { position: "+position+", color: "+color+", intensity: "+intensity+" }";
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
	
	public Quaternion rotationTo(Vector3d other) {
		return new Quaternion(this.normalize().cross(other.normalize()), 1 + this.normalize().dot(other.normalize()));
	}
	
	public Vector3d rotate(Quaternion q) {
		return q.rotate(this);
	}

	public String toString() {
		return "["+x+", "+y+", "+z+"]";
	}
}

class Quaternion {
	public double x, y, z, w;

	public Quaternion(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;

		normalize();
	}
	
	public Quaternion(Vector3d xyz, double w) {
		this(xyz.x, xyz.y, xyz.z, w);
	}

	private void normalize() {
		double quot = Math.sqrt(x*x + y*y + z*z + w*w);
		if(quot != 0) {
			x /= quot;
			y /= quot;
			z /= quot;
			w /= quot;
		}
	}
	
	public Quaternion inverse() {
		double quot = x*x + y*y + z*z + w*w;
		return new Quaternion(-x/quot, -y/quot, -z/quot, w/quot);
	}

	public Vector3d rotate(Vector3d v) {
		return this.mult(new Quaternion(v, 0)).mult(this.inverse()).v();
	}
	
	public Quaternion mult(Quaternion other) {
		return new Quaternion(
			other.v().scale(w).add(v().scale(other.w)).add(v().cross(other.v())),
			w*other.w - v().dot(other.v())
		);
	}
	
	public Vector3d v() {
		return new Vector3d(x, y, z);
	}

	public double angle() {
		return 2*Math.asin(Math.sqrt(x*x + y*y + z*z));
	}

	public Vector3d axis() {
		double quot = 1/Math.sin(0.5*angle());
		return new Vector3d(x/quot, y/quot, z/quot);
	}

	public String toString() {
		return "["+x+", "+y+", "+z+", "+w+"]";
	}
}

class Matrix44d {
	public double a0, a1, a2, a3, b0, b1, b2, b3, c0, c1, c2, c3, d0, d1, d2, d3;

	public Matrix44d(double a0, double a1, double a2, double a3,
		double b0, double b1, double b2, double b3,
		double c0, double c1, double c2, double c3,
		double d0, double d1, double d2, double d3
	) {
		this.a0 = a0;
		this.a1 = a1;
		this.a2 = a2;
		this.a3 = a3;
		this.b0 = b0;
		this.b1 = b1;
		this.b2 = b2;
		this.b3 = b3;
		this.c0 = c0;
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
		this.d0 = d0;
		this.d1 = d1;
		this.d2 = d2;
		this.d3 = d3;
	}

	public Vector3d mult(Vector3d v) {
		return new Vector3d(
			a0*v.x + a1*v.y + a2*v.z + a3,
			b0*v.x + b1*v.y + b2*v.z + b3,
			c0*v.x + c1*v.y + c2*v.z + c3
		).scale(1/(d0*v.x + d1*v.y + d2*v.z + d3));
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
		return "Ray { position "+position+", direction"+direction+" }";
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
