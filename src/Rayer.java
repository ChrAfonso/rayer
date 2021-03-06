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

import rayer.*;
import rayermath.*;

public class Rayer {
	private double MAX_DISTANCE = 5; // TODO HACK
	
	private JFrame frame;
	private RayerPanel panel;
	private BufferedImage image;
	private BufferedImage zBuffer;

	private Scene scene; 
	
	//test
	private static int t = 0;
	private boolean linebyline = true;

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

					case KeyEvent.VK_PLUS:
						t += 10;
						initDummyScene(t);
						renderScene();
						saveImage("testrender_"+t+".png");
						break;
					
					case KeyEvent.VK_MINUS:
						t -= 10;
						initDummyScene(t);
						renderScene();
						saveImage("testrender_"+t+".png");
						break;
					
					case KeyEvent.VK_F12:
						// animate
						for(int i = 0; i < 20; i++) {
							initDummyScene(t);
							renderScene();
							saveImage("animation_"+t+".png");
							t++;
						}
						break;
						
					case KeyEvent.VK_1:
						Light light = scene.getLightByName("light1");
						if(light != null) light.enabled = !light.enabled;
						renderScene();
						break;
					

					case KeyEvent.VK_2:
						light = scene.getLightByName("light2");
						if(light != null) light.enabled = !light.enabled;
						renderScene();
						break;
					
					case KeyEvent.VK_3:
						light = scene.getLightByName("light3");
						if(light != null) light.enabled = !light.enabled;
						renderScene();
						break;
					

					case KeyEvent.VK_R:
						SceneObject sphere = scene.getObjectByName("sphere");
						if(sphere != null) sphere.material.reflectivity += 0.1;
						renderScene();
						break;
					
					case KeyEvent.VK_F:
						sphere = scene.getObjectByName("sphere");
						if(sphere != null) sphere.material.reflectivity -= 0.1;
						renderScene();
						break;
					
					case KeyEvent.VK_T:
						sphere = scene.getObjectByName("sphere3");
						if(sphere != null) sphere.material.reflectivity += 0.1;
						renderScene();
						break;
					
					case KeyEvent.VK_G:
						sphere = scene.getObjectByName("sphere3");
						if(sphere != null) sphere.material.reflectivity -= 0.1;
						renderScene();
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
	
	private Mesh readMeshFromDAE(String daeFile) {
		DAEReader reader = new DAEReader(daeFile);
		Triangle[] triangles = reader.getTriangles();
		Mesh mesh = new Mesh(daeFile, new Vector3d(), triangles);
		return mesh;
	}

	private void initDummyScene(int t) {
		scene = new Scene();
		
		Camera camera = new Camera("camera", new Vector3d(Math.sin((double)t/100)*2, 0, -Math.cos((double)t/100)*5));
		camera.setDirection(new Vector3d(0, 0, 0).sub(camera.position).normalize());
		camera.setFOV(80, 80);
		scene.setCamera(camera);


		Light light = new Light("light1", new Vector3d(-20, 20, -50));
		light.color = new Color(255, 255, 0);
		scene.addLight(light);

		Light light2 = new Light("light2", new Vector3d(20, -20, -20));
		light2.color = new Color(255, 0, 0);
		scene.addLight(light2);

		Light light3 = new Light("light3", new Vector3d(0, 50, -20));
		light3.color = new Color(0, 255, 0);
		scene.addLight(light3);

		Light light4 = new Light("light3", new Vector3d(0, 100, -20));
		light4.color = new Color(255, 255, 255);
		scene.addLight(light4);

/*
		Sphere sphere = new Sphere("sphere", new Vector3d(3, 0, 0), 2);
//		sphere.material.diffuse = Color.RED;
		sphere.material.reflectivity = 0.5;
		scene.addObject(sphere);

		Sphere sphere2 = new Sphere("sphere2", new Vector3d(-0.5, 1, 1), 1);
		sphere2.material.diffuse = Color.GREEN;
		scene.addObject(sphere2);
		
		Sphere sphere3 = new Sphere("sphere3", new Vector3d(-2, 1, -3), 1);
		sphere3.material.diffuse = Color.BLUE;
		scene.addObject(sphere3);
		
		Sphere sphere4 = new Sphere("sphere4", new Vector3d(-2.7, 0.5, -3), 0.9);
		sphere4.material.diffuse = Color.YELLOW;
		scene.addObject(sphere4);

		Plane[] planes = new Plane[5];
		for(int i = 0; i < 5; i++) {
			planes[i] = new Plane("plane"+i, new Vector3d(-2 + i, -1, 0), new Vector3d(0, 2, -2 + i));
			planes[i].material.diffuse = Color.WHITE;
			planes[i].doublesided = true;
			planes[i].fixedWidth = 0.5;
			scene.addObject(planes[i]);
		}
		//TEST:
		RayHit rh = planes[0].getRayHit(new Ray(new Vector3d(0, 1, 0), new Vector3d(0, -1, 0)));
		System.out.println(rh != null ? rh.position : "no hit.");
		rh = planes[0].getRayHit(new Ray(new Vector3d(0, 4, 0), new Vector3d(0, -2, 1)));
		System.out.println(rh != null ? rh.position : "no hit.");

		Vector3d[] vertices = new Vector3d[3];
		vertices[0] = new Vector3d(-1, 1, 0);
		vertices[1] = new Vector3d(0, 2, t);
		vertices[2] = new Vector3d(1, 1, 0);
		Triangle tri = new Triangle("tri", vertices);
		tri.material.diffuse = Color.RED;
		scene.addObject(tri);
		
		vertices = new Vector3d[3];
		vertices[0] = new Vector3d(-2, -2, 0);
		vertices[1] = new Vector3d(-2, 1, 4);
		vertices[2] = new Vector3d(2, -2, 0);
		Triangle tri1 = new Triangle("tri1", vertices);
		tri1.material.diffuse = Color.WHITE;
		scene.addObject(tri1);
		
		vertices = new Vector3d[3];
		vertices[0] = new Vector3d(-2, 1, 4);
		vertices[1] = new Vector3d(2, 1, 4);
		vertices[2] = new Vector3d(2, -1, 0);
		Triangle tri2 = new Triangle("tri2", vertices);
		tri2.material.diffuse = Color.GREEN;
		scene.addObject(tri2);
*/
		Mesh monkey = readMeshFromDAE("models/monkey_old.dae");
		Quaternion xrot = Quaternion.fromAxisAngle(new Vector3d(1, 0, 0), Math.PI*(0.05*t));
		monkey.rotate(xrot);
		monkey.material.diffuse = new Color(128, 88, 0);
		scene.addObject(monkey);
	}

	public void logSceneContents() {
		System.out.println("Scene contents: -------");
		System.out.println(scene.toString());
	}
	
	private Vector<RayHit> getRayHits(Scene scene, Ray ray) {
		Vector<RayHit> hits = new Vector<RayHit>();
		
		for(SceneObject object: scene.getObjects()) {
			if(!object.enabled) continue;
			
			if(object.type == "Mesh" && ((Mesh)object).numTriangles > 0) {
				// TEMP HACK distribute to triangles
				Triangle[] triangles = ((Mesh)object).getTriangles();
				for(int i = 0; i < triangles.length; i++) {
					RayHit hit = triangles[i].getRayHit(ray);
					if(hit != null) {
						hits.add(hit);
					}
				}
			} else {
				RayHit hit = object.getRayHit(ray);
				if(hit != null) {
					hits.add(hit);
				}
			}
		}
		
		Collections.sort(hits);

		return hits;
	}

	private int maxBounceLevel = 5;
	private RayResult getColorForRay(Ray ray, int bounceLevel) {
		Color color;
		double distance = 0;

		Vector<RayHit> hits = getRayHits(scene, ray);
		if(hits.size() > 0 && hits.get(0).distance > 0.001) {
			// TODO order front to back, use first
			RayHit hit = hits.get(0);
			
			Color diffuse = hit.object.material.diffuse;
			
			double red = 0;
			double green = 0;
			double blue = 0;
			for(Light light: scene.getLights()) {
				if(!light.enabled) continue;
				
				boolean shadowed = false;
				Ray lightRay = new Ray(light.position, hit.position.sub(light.position).normalize());
				Vector<RayHit> lightHits = getRayHits(scene, lightRay);
				if(lightHits.size() > 0 && lightHits.get(0).object != hit.object) {
					shadowed = true;
				}
				
				if(!shadowed) {
					float factor = (float) light.position.sub(hit.position).normalize().dot(hit.normal);
					factor = Math.max(0, Math.min(1, factor));

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

			// TEST reflections
			if(hit.object.material.reflectivity > 0 && bounceLevel < maxBounceLevel) {
				// shoot reflected ray, get diffuse lighted color at reflection point (recursive?)
				Vector3d rayFromSurface  = ray.direction.scale(-1).normalize();
				Quaternion angleToNormal = rayFromSurface.rotationTo(hit.normal);
				Vector3d reflectDir = rayFromSurface.rotate(angleToNormal).rotate(angleToNormal);
				Ray reflectRay = new Ray(hit.position, reflectDir);
				
				Color reflectColor = getColorForRay(reflectRay, bounceLevel+1).color;
				
				// mix
				double r = Math.min(1, Math.max(0, hit.object.material.reflectivity));
				color = new Color(
					(int) (color.getRed()*(1-r) + reflectColor.getRed()*r),
					(int) (color.getGreen()*(1-r) + reflectColor.getGreen()*r),
					(int) (color.getBlue()*(1-r) + reflectColor.getBlue()*r)
				);
			}

			distance = hit.distance;
		} else {
			// TODO background?
			color = Color.BLACK;
		}
		
		return new RayResult(color, distance);
	}

	private void renderPixel(int i, int j) {
		Ray camRay = scene.getCamera().getCamRayForScreenPosition(i, j);
		RayResult pixel = getColorForRay(camRay, 0);

		image.setRGB(i, j, pixel.color.getRGB());
		
		float zValue = (float)(1 - (Math.min(1, Math.max(0, pixel.distance/MAX_DISTANCE))));
		zBuffer.setRGB(i, j, new Color(zValue, zValue, zValue).getRGB());
	}
	
	public void renderScene() {
		logSceneContents();
		
		panel.clear();
		image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
		zBuffer = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

		scene.getCamera().setScreenSize(image.getWidth(), image.getHeight());
		
		MAX_DISTANCE = 2*scene.getCamera().position.sub(new Vector3d(0, 0, 0)).getLength();
		for(int j = 0; j < image.getHeight(); j++) {
			for(int i = 0; i < image.getWidth(); i++) {
				renderPixel(i, j);
			}

			if(linebyline) {
				panel.setImage(image);

				panel.invalidate();
				panel.repaint();
			}
		}

		image = ImagePost.blurGauss(image, 1, zBuffer);
		
		//TEST
		boolean useDoF = false; // not working correctly
		if(useDoF) {
			image = ImagePost.blurGauss(image, 5, zBuffer);
			System.out.println("MAX_DISTANCE: "+MAX_DISTANCE);
			int zMin = 255;
			int zMax = 0;
			for(int j = 0; j < zBuffer.getHeight(); j++) {
				for(int i = 0; i < zBuffer.getWidth(); i++) {
					zMin = Math.min(zMin, new Color(zBuffer.getRGB(i, j)).getRed());
					zMax = Math.max(zMax, new Color(zBuffer.getRGB(i, j)).getRed());
				}
			}
			System.out.println("zMin: "+zMin);
			System.out.println("zMax: "+zMax);
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
	
	public SceneObject getObjectByName(String name) {
		for(SceneObject object: objects) {
			if(object.name == name) {
				return object;
			}
		}

		return null;
	}

	public Light getLightByName(String name) {
		for(Light light: lights) {
			if(light.name == name) {
				return light;
			}
		}

		return null;
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

class Sphere extends SceneObject {
	public double radius = 1;
	
	public Sphere(String name, Vector3d position) {
		super(name, position);
	
		this.type = "Sphere";
	}
	
	public Sphere(String name, Vector3d position, double radius) {
		this(name, position);
		
		this.radius = radius;
	}
	
	public RayHit getRayHit(Ray ray) {
		double dist = ray.direction.normalize().cross(this.position.sub(ray.position)).getLength();
		if(dist < this.radius) { 

			Vector3d minDistPoint = ray.position.add(ray.direction.normalize().scale(ray.direction.normalize().dot(this.position.sub(ray.position))));
			Vector3d hitPoint = minDistPoint.sub(ray.direction.normalize().scale(Math.sin(Math.acos(dist/this.radius))*this.radius));
			Vector3d normal = hitPoint.sub(this.position).normalize();
			
			double distance = ray.position.sub(hitPoint).getLength();

			// check direction, no back rays!
			if(ray.direction.normalize().dot(hitPoint.sub(ray.position)) < 0) return null;

			return new RayHit(ray, this, hitPoint, normal, distance);
		} else {
			return null;
		}
	}

	public String toString() {
		return "Sphere { center: "+position.toString()+", radius: "+radius+" }";
	}
}

class Camera extends SceneObject {
	private Vector3d direction;
	private Quaternion rotationToDirection;

	private double fovH = 80;
	private double fovV = 80;
	
	private int screenWidth, screenHeight;
	private double fovRatioX, fovRatioY;

	public Camera(String name, Vector3d position, Vector3d direction, int screenWidth, int screenHeight) {
		super(name, position);

		setDirection(direction);
		this.type = "Camera";

		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		calculateFOVRatio();
	}

	public Camera(String name, Vector3d position, int screenWidth, int screenHeight) {
		this(name, position, new Vector3d(0, 0, 1), screenWidth, screenHeight);
	}

	public Camera(String name, Vector3d position) {
		this(name, position, 1, 1);
	}
	
	public Camera() {
		this("default_camera", new Vector3d(0, 0, 0));
	}

	public void setFOV(double fovX, double fovY) {
		fovH = fovX;
		fovV = fovY;

		calculateFOVRatio();
	}
	
	public void setDirection(Vector3d direction) {
		rotationToDirection = new Vector3d(0, 0, 1).rotationTo(direction.normalize());
		this.direction = direction;
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

	public Light(String name, Vector3d position) {
		super(name, position);
		this.type = "Light";
		color = Color.WHITE; // default
	}

	public String toString() {
		return "Light { position: "+position+", color: "+color+", intensity: "+intensity+" }";
	}
}

/*
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
*/

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

class RayResult {
	public Color color;
	public double distance;

	public RayResult(Color color, double distance) {
		this.color = color;
		this.distance = distance;
	}
}

class ImagePost {
	public static BufferedImage blurGauss(BufferedImage input, int radius, BufferedImage zBuffer) {
		BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
		GaussKernel kernel = new GaussKernel(radius);
		for(int j = radius; j < output.getHeight() - radius; j++) {
			for(int i = radius; i < output.getWidth() - radius; i++) {
				double r = 0;
				double g = 0;
				double b = 0;
				double factor;
				for(int y = -radius; y <= radius; y++) {
					for(int x = -radius; x <= radius; x++) {
						double rad;
						if(zBuffer != null) {
							rad = Math.abs(new Color(zBuffer.getRGB(i, j)).getRed()/255 - 0.5) * radius; 
						} else {
							rad = radius;
						}
						factor = kernel.getValue(x, y, (int)rad);
						double quot = Math.pow(radius*2 + 1, 2);

						Color pixel = new Color(input.getRGB(i+x, j+y));
						r += factor*pixel.getRed()/quot;
						g += factor*pixel.getGreen()/quot;
						b += factor*pixel.getBlue()/quot;
					}
				}
				
				output.setRGB(i, j, new Color((int)(Math.min(r, 255)), (int)(Math.min(g, 255)), (int)(Math.min(b, 255))).getRGB());
			}
		}
		System.out.println("Finished blur!");
		return output;
	}
}

class ConvoKernel {
	protected int radius = 3;

	public ConvoKernel() {}
	public ConvoKernel(int radius) {
		this.radius = radius;
	}

	public int getRadius() { return radius; }
	public void setRadius(int radius) { 
		this.radius = radius;
	}

	public double getValue(int offX, int offY) {
		return (Math.abs(offX) <= radius && Math.abs(offY) <= radius ? 1 : 0);
	}
}

class GaussKernel extends ConvoKernel {
	
	public GaussKernel() {
		super();
	}

	public GaussKernel(int radius) {
		super(radius);
	}

	@Override
	public double getValue(int offX, int offY) {
		return Math.exp(-((offX*offX + offY*offY)/(2*radius*radius)));
	}

	public double getValue(int offX, int offY, int radius) {
		if(radius < 1) radius = 1;
		return Math.exp(-((offX*offX + offY*offY)/(2*radius*radius)));
	}

}

