import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.*;
import java.util.Scanner;
import java.nio.file.Paths;

import rayer.*;
import rayermath.*;

public class DAEReader {
	Document document;
	Vector3d[] positions;
	Vector3d[] normals;
	Triangle[] triangles;

	private Element getChildByName(Element parent, String name) {
		NodeList elements = parent.getElementsByTagName(name);
		if(elements.getLength() > 0 && elements.item(0).getNodeType() == Node.ELEMENT_NODE) {
			return (Element)(elements.item(0));
		} else return null;
	}
	
	private Element getChildWithAttribute(Element parent, String name, String attrName, String attrValue) {
		NodeList elements = parent.getElementsByTagName(name);
		for(int i = 0; i < elements.getLength(); i++) {
			Node node = elements.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element)node;
				if(element.getAttribute(attrName).equals(attrValue)) {
					return element;
				}
			}
		}
		
		return null;
	}
	
	public DAEReader(String filename) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			System.out.println("Reading document...");
			if(filename.indexOf("://") == -1) {
				String pwd = Paths.get(".").toAbsolutePath().normalize().toString();
				filename = pwd + File.separatorChar + filename;
			}
			File file = new File(filename);
			document = builder.parse(file);
			document.normalizeDocument();
			
			NodeList geometries = document.getElementsByTagName("geometry");

			System.out.println("Looking for elements...");
			// TODO: Error-check, extend to multiple
			Element geo = (Element)(geometries.item(0));
			Element mesh = getChildByName(geo, "mesh");
			Element polylist = getChildByName(mesh, "polylist");
			//Element vertexInfo = getChildWithAttribute(polylist, "input", "semantic", "VERTEX");
			Element vertexInfo = getChildWithAttribute(getChildByName(mesh, "vertices"), "input", "semantic", "POSITION");
			Element normalInfo = getChildWithAttribute(polylist, "input", "semantic", "NORMAL");
			
			System.out.println("Reading vertices...");
			// read in vertices
			String vertexSource = vertexInfo.getAttribute("source").replace("#", "");
			System.out.println(vertexSource);
			Element vertexNode = getChildWithAttribute(mesh, "source", "id", vertexSource);
			Element vertexFloats = getChildByName(vertexNode, "float_array");
			int numPositions = Integer.parseInt(vertexFloats.getAttribute("count")) / 3; // TODO range check
			String[] floats = vertexFloats.getTextContent().split(" ");
			
			positions = new Vector3d[numPositions];
			for(int i = 0; i < numPositions; i++) {
				double x = Double.parseDouble(floats[i*3]);
				double y = Double.parseDouble(floats[i*3 + 1]);
				double z = Double.parseDouble(floats[i*3 + 2]);
				positions[i] = new Vector3d(x, y, z);
			}
			
			System.out.println("Reading normals...");
			// read in normals
			String normalSource = normalInfo.getAttribute("source").replace("#", "");
			System.out.println(normalSource);
			Element normalNode = getChildWithAttribute(mesh, "source", "id", normalSource);
			Element normalFloats = getChildByName(normalNode, "float_array");
			int numNormals = Integer.parseInt(normalFloats.getAttribute("count")) / 3; // TODO range check
			floats = normalFloats.getTextContent().split(" ");
			
			normals = new Vector3d[numNormals];
			for(int i = 0; i < numNormals; i++) {
				double x = Double.parseDouble(floats[i*3]);
				double y = Double.parseDouble(floats[i*3 + 1]);
				double z = Double.parseDouble(floats[i*3 + 2]);
				normals[i] = new Vector3d(x, y, z);
			}
			
			Element vcount = getChildByName(polylist, "vcount");
			String[] vcounts = vcount.getTextContent().split(" "); // should be 3 3 3 3...
			for(String vc: vcounts) { if(!vc.equals("3")) { throw new Exception("All polys must be triangles!"); } }
			
			System.out.println("Building triangles...");
			// build triangles
			int polycount = Integer.parseInt(polylist.getAttribute("count"));
			triangles = new Triangle[polycount];
			Element polyDefs = getChildByName(polylist, "p");
			String[] ps = polyDefs.getTextContent().split(" ");
			for(int i = 0; i < polycount; i++) {
				// positions
				int vi1 = Integer.parseInt(ps[i*6]); // 6: 3 times position,normal
				int vi2 = Integer.parseInt(ps[i*6 + 2]);
				int vi3 = Integer.parseInt(ps[i*6 + 4]);

				// normals
				int ni1 = Integer.parseInt(ps[i*6 + 1]);
				int ni2 = Integer.parseInt(ps[i*6 + 3]);
				int ni3 = Integer.parseInt(ps[i*6 + 5]);
				
				// DAE triangles are counterclockwise...
				triangles[i] = new Triangle("tri"+i, 
					new Vector3d[] {positions[vi1], positions[vi3], positions[vi2]},
					new Vector3d[] {normals[ni1], normals[ni3], normals[ni2]}
				); // TODO range-checks
			}
			System.out.println("Done!");
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public Triangle[] getTriangles() {
		return triangles;
	}

	public static void main(String[] args) {
		DAEReader reader = new DAEReader("models/monkey.dae");
	}
}
