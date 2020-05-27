package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.*;

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
		Vec normalizeDir = ray.direction().normalize();
		Vec L = this.center.sub(ray.source());
		// check direction for relevance/ if we can disregard because it doesn't intersect for sure
		if (L.dot(normalizeDir) < 0) {
			return null;
		}
		double d = Math.sqrt(Math.pow(L.length(),2) - (Math.pow(L.dot(normalizeDir), 2)));
		if (d > this.radius) {
			return null;

		}
		// if it intersects, we use trigonometric functions to calculate intersection points with the sphere
		double adj = L.dot(normalizeDir);
		double hyp = Math.sqrt((Math.pow(this.radius, 2)) - (Math.pow(d,2)));
		double t0 = adj - hyp, t1 = adj + hyp;
		// now that we've calculated the two t values, we find the points on the sphere itself that are intersected with:
		Point p = ray.source().add(normalizeDir.mult(t0));
		Point p1 = ray.source().add(normalizeDir.mult(t1));
		Vec norm2 = p1.sub(this.center).normalize();
		Vec norm1 = p.sub(this.center).normalize();
		// we calculate the nearest intersection by intersection distances
		double pDist = p.sub(ray.source()).length();
		double p1Dist = p1.sub(ray.source()).length();
		// we check to avoid double calculation/errors
		if (pDist > p1Dist) {
			if (p1Dist > Ops.epsilon & p1Dist < Ops.infinity){
				return new Hit(p1Dist, norm1);
			}
			return null;
		} else{
			if (pDist > Ops.epsilon & pDist < Ops.infinity){
				return new Hit(pDist, norm2);
			}
			return null;
		}


	}
}
