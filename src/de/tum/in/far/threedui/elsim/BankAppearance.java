package de.tum.in.far.threedui.elsim;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;

public class BankAppearance extends Appearance
{
	public BankAppearance(float opacy) {
		// Create the blue appearance node
		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
		Color3f blue = new Color3f(0.0f, 0.0f, 1.f);
		Color3f specular = new Color3f(0.0f, 0.0f, 0.0f);
		
		// Ambient,emissive,diffuse,specular,shininess
		Material blueMat = new Material
		(blue, blue, blue, blue, 1.0f);
		blueMat.setColorTarget(Material.EMISSIVE);
		this.setColoringAttributes (new ColoringAttributes (new Color3f (0.0f, 0.0f, 1.0f),1));
		
		//Switch on light
		blueMat.setLightingEnable(false);
		
		setMaterial(blueMat);
		TransparencyAttributes attr=new TransparencyAttributes(TransparencyAttributes.BLENDED, 1.f-opacy);
		this.setTransparencyAttributes(attr);
	}

}
