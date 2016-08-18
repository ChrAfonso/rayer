package rayer;

import rayermath.*;

public class Plane extends SceneObject {
	protected Vector3d normal;
	private double planeOriginDist;
	
	public boolean doublesided = false;
	public double fixedWidth = -1;
	
	public Plane(String name, Vector3d position, Vector3d normal) {
		super(name, position);

		this.type = "Plane";
		
		this.normal = normal.normalize();
		this.planeOriginDist = -(this.position.dot(this.normal));
	}
	
	public RayHit getRayHit(Ray ray) {
		// check direction, no back rays!
		double epsilon = 0.001; // HACK
		if(!doublesided && ray.direction.normalize().dot(this.normal) >= epsilon) {
			return null;
		}

		double distance = -(ray.position.dot(this.normal) + planeOriginDist) / ray.direction.normalize().dot(this.normal);
		Vector3d hitPoint = ray.position.add(ray.direction.normalize().scale(distance));
		
		// HACK TEST - only segment
		if(this.type == "Plane" && fixedWidth > -1) {
			Vector3d hitOriginDist = hitPoint.sub(position);
			if(Math.abs(hitOriginDist.x) > fixedWidth || Math.abs(hitOriginDist.z) > fixedWidth) return null;
		}
		
		return new RayHit(ray, this, hitPoint, this.normal, distance);
	}
	
	public void update() {
		if(this.normal != null) {
			this.normal = this.normal.normalize();
			this.planeOriginDist = -(this.position.dot(this.normal));
		}
	}
	
	public String toString() {
		return "Plane { origin: "+position.toString()+", normal: "+normal.toString()+" }";
	}
}


