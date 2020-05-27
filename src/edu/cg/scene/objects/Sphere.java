package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Sphere extends Shape {
	private Point center;
	private double radius;
	
	public Sphere(Point center, double radius) {
		this.center = center;
		this.radius = radius;
	}
	
	public Sphere() {
		this(new Point(0, -0.5, -6), 0.5);
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Sphere:" + endl + 
				"Center: " + center + endl +
				"Radius: " + radius + endl;
	}
	
	public Sphere initCenter(Point center) {
		this.center = center;
		return this;
	}
	
	public Sphere initRadius(double radius) {
		this.radius = radius;
		return this;
	}
	
	@Override
	public Hit intersect(Ray ray) {
		double a = ray.direction().dot(ray.direction());
		double b = 2 * (ray.direction().dot(ray.source().sub(center)));
		double c = (ray.source().sub(center).dot(ray.source().sub(center))) - Math.pow(radius,2);
		double tempT, t;
		double discriminant = Math.pow(b,2) -(4*a*c);
		if( discriminant < 0){
			return null;
		}
		else{
			tempT = (-b - Math.sqrt(discriminant))/ 2*a;
			t = (tempT < 0)? (-b + Math.sqrt(discriminant))/2*a : tempT;
			Point intersectionPoint = ray.add(t);
			Vec normal = intersectionPoint.sub(center).normalize();
			return new Hit(t,normal);
		}


	}
}
