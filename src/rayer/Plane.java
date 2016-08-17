package rayer;

import rayermath.*;

public class Plane extends SceneObject {
	protected Vector3d normal;
	private double planeOriginDist;
	
	public boolean doublesided = false;

	public Plane(String name, Vector3d position, Vector3d normal) {
		super(name, position);

		this.normal = normal.normalize();
		double planeOriginDist = -(this.position.dot(this.normal));
	}
	
	public RayHit getRayHit(Ray ray) {
		// check direction, no back rays!
		double epsilon = 0.001; // HACK
		if(ray.direction.normalize().dot(this.normal) >= epsilon) {
			return null;
		}

		double distance = -(ray.position.dot(this.normal) + planeOriginDist) / ray.direction.normalize().dot(this.normal);
		Vector3d hitPoint = ray.position.add(ray.direction.normalize().scale(distance));
		
		// HACK TEST
		if(Math.abs(hitPoint.x) > 1 || Math.abs(hitPoint.z) < 1) return null;
		
		return new RayHit(ray, this, hitPoint, this.normal, distance);
	}

	public String toString() {
		return "Plane { origin: "+position.toString()+", normal: "+normal.toString()+" }";
	}
}


