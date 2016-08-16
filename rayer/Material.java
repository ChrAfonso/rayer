package rayer;

import java.awt.Color;

public class Material {
	public Color diffuse;
	public double reflectivity;

	public Material() {
		//default
		diffuse = Color.LIGHT_GRAY;
		reflectivity = 0;
	}

	public String toString() {
		return "Material { diffuse: "+diffuse+" }";
	}
}


