package de.tum.in.far.threedui.molsim.common;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Material;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.geometry.Box;

public class Ground extends BranchGroup {

	private static final float HEIGHT_HALF = 0.001f;
	
	public Ground() {
		
		// Create the blue appearance node
		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
		Color3f green = new Color3f(0.3f, 0.8f, 0.3f);
		Color3f specular = new Color3f(0.9f, 0.9f, 0.9f);
		
		// Ambient,emissive,diffuse,specular,shininess
		Material blueMat = new Material
		(green, black, green, specular,25.0f);
		
		//Switch on light
		blueMat.setLightingEnable(true);
		
		Appearance app = new Appearance();
		app.setMaterial(blueMat);
		
		Box ground = new Box(1.0f, HEIGHT_HALF, 1.0f, app);
		
		// Move down, so that the surface is at Y = 0.0
		TransformGroup tg = new TransformGroup();
		Transform3D t3d = new Transform3D();
		t3d.setTranslation(new Vector3d(0.0, -HEIGHT_HALF, 0.0));
		tg.setTransform(t3d);
		
		tg.addChild(ground);
		addChild(tg);
	}
	
}
