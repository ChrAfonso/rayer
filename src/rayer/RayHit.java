package rayer;

import rayermath.*;

public class RayHit implements Comparable<RayHit> {
	public Ray ray;
	public SceneObject object;
	public Vector3d position;
	public Vector3d normal;
	public double distance;

	public RayHit(Ray ray, SceneObject object, Vector3d position, Vector3d normal, double distance) {
		this.ray = ray;
		this.object = object;
		this.position = position;
		this.normal = normal;
		this.distance = distance;
	}

	public int compareTo(RayHit other) {
		return Double.compare(this.distance, other.distance);
	}
}

