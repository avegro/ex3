package edu.cg.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.cg.Logger;
import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.*;
import edu.cg.scene.camera.PinholeCamera;
import edu.cg.scene.lightSources.Light;
import edu.cg.scene.objects.Surface;

public class Scene {
	private String name = "scene";
	private int maxRecursionLevel = 1;
	private int antiAliasingFactor = 1; // gets the values of 1, 2 and 3
	private boolean renderRefarctions = false;
	private boolean renderReflections = false;

	private PinholeCamera camera;
	private Vec ambient = new Vec(1, 1, 1); // white
	private Vec backgroundColor = new Vec(0, 0.5, 1); // blue sky
	private List<Light> lightSources = new LinkedList<>();
	private List<Surface> surfaces = new LinkedList<>();

	// MARK: initializers
	public Scene initCamera(Point eyePoistion, Vec towardsVec, Vec upVec, double distanceToPlain) {
		this.camera = new PinholeCamera(eyePoistion, towardsVec, upVec, distanceToPlain);
		return this;
	}

	public Scene initAmbient(Vec ambient) {
		this.ambient = ambient;
		return this;
	}

	public Scene initBackgroundColor(Vec backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public Scene addLightSource(Light lightSource) {
		lightSources.add(lightSource);
		return this;
	}

	public Scene addSurface(Surface surface) {
		surfaces.add(surface);
		return this;
	}

	public Scene initMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
		return this;
	}

	public Scene initAntiAliasingFactor(int antiAliasingFactor) {
		this.antiAliasingFactor = antiAliasingFactor;
		return this;
	}

	public Scene initName(String name) {
		this.name = name;
		return this;
	}

	public Scene initRenderRefarctions(boolean renderRefarctions) {
		this.renderRefarctions = renderRefarctions;
		return this;
	}

	public Scene initRenderReflections(boolean renderReflections) {
		this.renderReflections = renderReflections;
		return this;
	}

	// MARK: getters
	public String getName() {
		return name;
	}

	public int getFactor() {
		return antiAliasingFactor;
	}

	public int getMaxRecursionLevel() {
		return maxRecursionLevel;
	}

	public boolean getRenderRefarctions() {
		return renderRefarctions;
	}

	public boolean getRenderReflections() {
		return renderReflections;
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Camera: " + camera + endl + "Ambient: " + ambient + endl + "Background Color: " + backgroundColor + endl
				+ "Max recursion level: " + maxRecursionLevel + endl + "Anti aliasing factor: " + antiAliasingFactor
				+ endl + "Light sources:" + endl + lightSources + endl + "Surfaces:" + endl + surfaces;
	}

	private transient ExecutorService executor = null;
	private transient Logger logger = null;

	private void initSomeFields(int imgWidth, int imgHeight, Logger logger) {
		this.logger = logger;
			// TODO: initialize your additional field here.
	}

	public BufferedImage render(int imgWidth, int imgHeight, double viewAngle, Logger logger)
			throws InterruptedException, ExecutionException, IllegalArgumentException {

		initSomeFields(imgWidth, imgHeight, logger);

		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		camera.initResolution(imgHeight, imgWidth, viewAngle);
		int nThreads = Runtime.getRuntime().availableProcessors();
		nThreads = nThreads < 2 ? 2 : nThreads;
		this.logger.log("Intitialize executor. Using " + nThreads + " threads to render " + name);
		executor = Executors.newFixedThreadPool(nThreads);

		@SuppressWarnings("unchecked")
		Future<Color>[][] futures = (Future<Color>[][]) (new Future[imgHeight][imgWidth]);

		this.logger.log("Starting to shoot " + (imgHeight * imgWidth * antiAliasingFactor * antiAliasingFactor)
				+ " rays over " + name);

		for (int y = 0; y < imgHeight; ++y)
			for (int x = 0; x < imgWidth; ++x) {
				futures[y][x] = calcColor(x, y);
				System.out.println();
			}

		this.logger.log("Done shooting rays.");
		this.logger.log("Wating for results...");

		for (int y = 0; y < imgHeight; ++y)
			for (int x = 0; x < imgWidth; ++x) {
				Color color = futures[y][x].get();
				img.setRGB(x, y, color.getRGB());
			}

		executor.shutdown();

		this.logger.log("Ray tracing of " + name + " has been completed.");

		executor = null;
		this.logger = null;

		return img;
	}

	private Future<Color> calcColor(int x, int y) {
		return executor.submit(() -> {
			// TODO: You need to re-implement this method if you want to handle
			// super-sampling. You're also free to change the given implementation if you
			// want.
			Point centerPoint = camera.transform(x, y);
			Ray ray = new Ray(camera.getCameraPosition(), centerPoint);
			Vec color = calcColor(ray, 0);
			return color.toColor();
		});
	}

	private Vec calcColor(Ray ray, int recursionLevel) {
		Vec recReflect = new  Vec(0, 0, 0);
		Hit surfHit;
		Hit hit = null;
		Surface resultSurface;
		Vec pNormal, lSum;
		Point p;
		// First, we find the surface (location where the ray hits the nearest object)
		// so that we can determine color based on the material, location etc.
		for(Surface surf: this.surfaces){
			surfHit = surf.intersect(ray);
			if(surfHit != null){
				surfHit.setSurface(surf);
			}
			if(hit == null) hit = surfHit;
			if(surfHit != null && surfHit.compareTo(hit) < 0) hit = surfHit;
		}
		if(hit == null) return backgroundColor;
		// Now we know what point and surface we're working with,
		// so we define variables for the point we are getting the color from,
		// the vector which is normal to the surface, and of course the surface we are working with.
		// We also start the sum for the light intensity formula.
		resultSurface = hit.getSurface();
		pNormal = hit.getNormalToSurface();
		p = ray.getHittingPoint(hit);
		Vec normToSurf = hit.getNormalToSurface();
		lSum = ambient.mult(resultSurface.Ka());
		//diffuse and specular calculations, shadows considered (using sj scalar).
		double sj = 1;
		// Iterate over every light source and check if it's blocked. If it is, we will
		// multiply the relevant diffuse/specular term by 0 as the light is not getting through.
		for(Light light: this.lightSources){
			Ray toLight = light.rayToLight(p);
			Vec lHat = Ops.reflect(toLight.direction(), normToSurf);
			for(Surface s : this.surfaces){
				sj = (light.isOccludedBy(s,toLight)) ? 0: 1;
				if(sj == 0) break;
			}
			// Follow intensity formula
			Vec intense = light.intensity(p,toLight);
			double vTimesLHat = ray.direction().neg().dot(lHat);
			vTimesLHat = Math.max(vTimesLHat,0);
			double shiny = Math.pow(vTimesLHat, resultSurface.shininess());
			double NdotLj = normToSurf.dot(toLight.direction());
			NdotLj = Math.max(NdotLj, 0);
			// diffuse aspect
			Vec diff = (resultSurface.Kd().mult(intense)).mult(NdotLj);
			// specular aspect
			Vec spec = (resultSurface.Ks().mult(intense)).mult(shiny);

			Vec diffPlusSpec = diff.add(spec);

			lSum = lSum.add(diffPlusSpec.mult(sj));

		}
		// Check if we have reached maximum depth.
		// If not, move the ray forward by an extremely small amount and continue with the recursion.
		if(++recursionLevel < this.maxRecursionLevel && this.renderReflections){
			Ray nextRay = new Ray(p, Ops.reflect(ray.direction(),pNormal));
			recReflect = calcColor(nextRay, recursionLevel);
		}
		// Calculate sum and return appropriate color.
		lSum = lSum.add(recReflect.mult(hit.getSurface().reflectionIntensity()));
		return lSum;
	}
}
