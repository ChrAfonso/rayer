package rayermath;

public class Vector3d {
	public double x, y, z;
	
	public Vector3d() {
		this(0, 0, 0);
	}

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
		Quaternion doubleRot = new Quaternion(this.normalize().cross(other.normalize()), 1 + this.normalize().dot(other.normalize()));
		return Quaternion.fromAxisAngle(doubleRot.axis(), doubleRot.angle()/2);
	}
	
	public Vector3d rotate(Quaternion q) {
		return q.rotate(this);
	}

	public String toString() {
		return "["+x+", "+y+", "+z+"]";
	}
}

