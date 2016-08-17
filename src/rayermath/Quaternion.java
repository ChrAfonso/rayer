package rayermath;

public class Quaternion {
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
	
	public static Quaternion fromAxisAngle(Vector3d axis, double angle) {
		return new Quaternion(axis.normalize().scale(Math.sin(0.5*angle)), Math.cos(0.5*angle));
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
		return 2*Math.acos(w);
	}

	public Vector3d axis() {
		return v().normalize(); 
	}

	public String toString() {
		return "["+x+", "+y+", "+z+", "+w+"]";
	}
}

