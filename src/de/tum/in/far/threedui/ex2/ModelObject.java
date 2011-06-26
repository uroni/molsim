package de.tum.in.far.threedui.ex2;

import javax.media.j3d.BranchGroup;

public class ModelObject extends TransformableObject {
	
	public ModelObject(BranchGroup model) {
		super.transGroup.addChild(model);
	}
	
}
