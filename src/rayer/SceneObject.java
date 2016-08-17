package rayer;

import rayermath.*;

public abstract class SceneObject {
	public boolean enabled = true;
	
	public String name;
	public String type;
	public Vector3d position;
	
	public Material material;

	public SceneObject(String name, Vector3d position) {
		this.name = name;
		this.position = position;
		
		type = "SceneObject";
		material = new Material();
	}
	
	// override
	public RayHit getRayHit(Ray ray) {
		return null;
	}

	public String toString() {
		return "SceneObject { type: "+type+", position: "+position+", material: "+material+" } ";
	}
}

