package rayer;

import rayermath.*;

public class Ray {
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


