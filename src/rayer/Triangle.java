package rayer;

import rayermath.*;

/** vertices clockwise towards viewer */
public class Triangle extends Plane {
	private Vector3d[] vertices;
	private Vector3d[] normals;
	private Vector3d edge1, edge2, edge3;

	public Triangle(String name, Vector3d[] vertices, Vector3d[] normals) {
		super(name, new Vector3d(), new Vector3d()); // temp position/normal, recalculated later

		type = "Triangle";
		doublesided = true;

		if(vertices.length != 3) {
			System.out.println("ERROR: Triangle must be initialized with 3 vertices!");
			return;
		}
		
		this.vertices = vertices;
		
		this.position = vertices[0];

		// compute edges and normal normal
		edge1 = vertices[1].sub(vertices[0]);
		edge2 = vertices[2].sub(vertices[1]);
		edge3 = vertices[0].sub(vertices[2]);
		this.normal = edge1.cross(edge3.scale(-1)).normalize();

		// detailed normals?
		if(normals != null) {
			this.normals = normals;
			this.normal = normals[0].add(normals[1]).add(normals[2]).normalize();
		}
	}
	
	public Triangle(String name, Vector3d[] vertices) {
		this(name, vertices, null);
	}

	public RayHit getRayHit(Ray ray) {
		RayHit planeHit = super.getRayHit(ray);
		if(planeHit != null) {
			// check bounds - source: http://geomalgorithms.com/a06-_intersect-2.html
			Vector3d u = edge1;
			Vector3d v = edge3.scale(-1);
			double uu = u.dot(u);
			double uv = u.dot(v);
			double vv = v.dot(v); 
			Vector3d w = planeHit.position.sub(vertices[0]);
			double wu = w.dot(u);
			double wv = w.dot(v);
			double denom = uv*uv - uu*vv;

			double s = (uv*wv - vv*wu) / denom;
			double t = (uv*wu - uu*wv) / denom;
			
			if(s < 0 || s > 1) return null;
			if(t < 0 || t > 1) return null;
			if(s + t > 1) return null;
			
			// TODO: if normals, interpolate normal

			return planeHit;
		} else return null;
	}
	
	public String toString() {
		return "Triangle { vertices: "+vertices[0].toString()+", "+vertices[1].toString()+", "+vertices[2].toString()+(normals == null ? "" : ", normals: "+normals[0].toString()+", "+normals[1].toString()+", "+normals[2].toString())+"}";
	}
}


