package de.tum.in.far.threedui.ex2;

import javax.media.j3d.Appearance;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Sphere;

public class CubeObject extends TransformableObject {
	
	public CubeObject(Appearance app) {
		Box box=new Box(0.023f, 0.023f, 0.023f, app);
		TransformGroup tg=new TransformGroup();
		Transform3D trans=new Transform3D();
		trans.setTranslation(new Vector3d(0,0,0.023));
		tg.setTransform(trans);
		tg.addChild(box);
		transGroup.addChild(tg);
	}

	
}
