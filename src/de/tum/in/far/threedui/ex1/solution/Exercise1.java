package de.tum.in.far.threedui.ex1.solution;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

public class Exercise1 {

	public static final String EXERCISE = "Exercise 1 Solution";
	
	public static void main(String[] args) {
		Exercise1 exercise1 = new Exercise1();
		exercise1.initializeJava3D();
	}
	
	private void initializeJava3D() {
		System.out.println("Creating Viewer - " + EXERCISE);
		Viewer viewer = new Viewer(EXERCISE);

		System.out.println("Create Sphere Object");
		SphereObject sphereObject = new SphereObject(0.1f);
		viewer.addObject(sphereObject);
		
		Transform3D sphereT3D = new Transform3D();
		sphereT3D.setTranslation(new Vector3d(0.0, 0.2, 0.0));
		sphereObject.getTransformGroup().setTransform(sphereT3D);
		
		System.out.println("Move Camera backwards and a little bit up");
		Transform3D cameraTransform = new Transform3D();
		cameraTransform.setTranslation(new Vector3d(0.0, 0.15, 1.0));
		TransformGroup cameraTG = viewer.getCameraTransformGroup();
		cameraTG.setTransform(cameraTransform);
		
		System.out.println("Create Blue Sphere Object");
		BlueAppearance blueAppearance = new BlueAppearance();
		LightSphereObject blueSphereObject = new LightSphereObject(0.04f, blueAppearance);
//		viewer.addObject(blueSphereObject);
		
		Transform3D blueSphereT3D = new Transform3D();
		blueSphereT3D.setTranslation(new Vector3d(0.2, 0.2, 0.0));
		blueSphereObject.getTransformGroup().setTransform(blueSphereT3D);
		
		System.out.println("Create Ground Object");
		Ground ground = new Ground();
		viewer.addObject(ground);
		
		System.out.println("Create Shadow for Sphere Object");
		FakeShadow sphereObjectShadow = new FakeShadow((GeometryArray) sphereObject.getGeometry(), new Color3f(0.2f, 0.2f, 0.2f));
		sphereObject.getTransformGroup().addChild(sphereObjectShadow);
		
		Transform3D sphereObjectShadowT3D = new Transform3D();
		sphereObjectShadowT3D.setTranslation(new Vector3d(0.0, -0.2, 0.0));
		sphereObjectShadow.getTransformGroup().setTransform(sphereObjectShadowT3D);
		
		System.out.println("Create Shadow for blue Object");
   	    FakeShadow blueObjectShadow = new FakeShadow((GeometryArray) blueSphereObject.getGeometry(), new Color3f(0.2f, 0.2f, 0.2f));
   	    blueSphereObject.getTransformGroup().addChild(blueObjectShadow);
   	    
	    Transform3D blueObjectShadowT3D = new Transform3D();
	    blueObjectShadowT3D.setTranslation(new Vector3d(0.0, -0.2, 0.0));
		blueObjectShadow.getTransformGroup().setTransform(blueObjectShadowT3D);
//	    viewer.addObject(shadow);
	    
		System.out.println("Animation");
		AnimationRotation animationRotation = new AnimationRotation(blueSphereObject);
		viewer.addObject(animationRotation);
		
		System.out.println("Done - Enjoy");
	}
}
