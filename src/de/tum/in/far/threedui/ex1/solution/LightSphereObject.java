package de.tum.in.far.threedui.ex1.solution;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.PointLight;

public class LightSphereObject extends SphereObject {

	public LightSphereObject(float radius) {
		this(radius, null);
	}

	public LightSphereObject(float radius, Appearance app) {
		super(radius, app);
		
		// Create Point Light
		PointLight pointLight = new PointLight();
		BoundingSphere boundingSphere = new BoundingSphere();
		boundingSphere.setRadius(40.0);
		pointLight.setInfluencingBounds(boundingSphere);
		transGroup.addChild(pointLight);
	}

}
