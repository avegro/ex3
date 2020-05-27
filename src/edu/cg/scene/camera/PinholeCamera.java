package edu.cg.scene.camera;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class PinholeCamera {
	private int height;
	private int width;
	private double viewAngle;
	private Point cameraPosition;
	private Vec towardsVec;
	private Vec upVec;
	private Vec rightVec;
	private double distanceToPlain;
	private double pixelWidth;
	private double plainWidth;


	/**
	 * Initializes a pinhole camera model with default resolution 200X200 (RxXRy)
	 * and View Angle 90.
	 * 
	 * @param cameraPosition  - The position of the camera.
	 * @param towardsVec      - The towards vector of the camera (not necessarily
	 *                        normalized).
	 * @param upVec           - The up vector of the camera.
	 * @param distanceToPlain - The distance of the camera (position) to the center
	 *                        point of the image-plain.
	 * 
	 */
	public PinholeCamera(Point cameraPosition, Vec towardsVec, Vec upVec, double distanceToPlain) {
		this.cameraPosition = cameraPosition;
		this.towardsVec = towardsVec.normalize();
		this.upVec = upVec.normalize();
		this.distanceToPlain = distanceToPlain;
		initResolution(200,200,90);

	}

	/**
	 * Initializes the resolution and width of the image.
	 * 
	 * @param height    - the number of pixels in the y direction.
	 * @param width     - the number of pixels in the x direction.
	 * @param viewAngle - the view Angle.
	 */
	public void initResolution(int height, int width, double viewAngle) {
		this.height = height;
		this.width = width;
		this.viewAngle = viewAngle;
		this.plainWidth = Math.tan(Math.toRadians(viewAngle/2)) * distanceToPlain * 2;
		this.pixelWidth = plainWidth/width;
	}

	/**
	 * Transforms from pixel coordinates to the center point of the corresponding
	 * pixel in model coordinates.
	 * 
	 * @param x - the pixel index in the x direction.
	 * @param y - the pixel index in the y direction.
	 * @return the middle point of the pixel (x,y) in the model coordinates.
	 */
	public Point transform(int x, int y) {
		// We add 0.5 because we will pass the ray through the center of each pixel.
		try{
			double finalX = ((double)x+0.5)/width;
			double finalY = ((double)y+0.5)/height;
			double newX = 2 * finalX - 1;
			double newY = 1 - 2 * finalY;
			double aspectRatio = width/height;
			finalX = newX * aspectRatio;
			finalY = newY;
			Ray initalRay = new Ray(cameraPosition, towardsVec);
			Point centerPoint = initalRay.add(distanceToPlain);
			double z = centerPoint.z;
			return new Point(finalX, finalY, z);
		}
		catch(Exception e){
			throw new RuntimeException("Function failed: PinholeCamera/transform  ");
		}
	}



	/**
	 * Returns the camera position
	 * 
	 * @return a new point representing the camera position.
	 */
	public Point getCameraPosition() {

		try {return new Point(0,0,0); }
		catch(Exception e){
			throw new RuntimeException("camera position failed");
		}
	}
}
