package rayer;

import rayermath.*;

//TODO: repositioning should reposition all triangles
public class Mesh extends SceneObject {
	public int numTriangles = 0;
	protected Triangle[] triangles;

	public Mesh(String name, Vector3d position, Triangle[] triangles) {
		super(name, position);

		type = "Mesh";

		if(triangles != null) {
			this.triangles = triangles;
			numTriangles = triangles.length;
		} else System.out.println("WARNING: Creating empty Mesh!");
	}
	
	// NOTE: getRayHit undelegated to Rayer, because of sorting
	
	public void rotate(Quaternion quat) {
		for(Triangle tri: triangles) {
			tri.rotate(quat, this.position);
		}
	}
	
	public Triangle[] getTriangles() {
		return triangles;
	}

	public String toString() {
		return "Mesh { name: "+name+", triangles: "+numTriangles+"}";
	}
}
