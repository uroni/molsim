/**
 * 
 */
package de.tum.in.far.threedui.elsim;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;

/**
 * @author Martin
 *
 */
public class ConnectionAppearance extends Appearance
{
	public ConnectionAppearance(float opacy) {
		// Create the blue appearance node
		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
		Color3f blue = new Color3f(0.3f, 0.3f, 1.f);
		Color3f specular = new Color3f(0.0f, 0.0f, 0.0f);
		
		// Ambient,emissive,diffuse,specular,shininess
		Material blueMat = new Material
		(blue, black, blue, specular,0.0f);
		
		//Switch on light
		blueMat.setLightingEnable(false);
		
		setMaterial(blueMat);
		TransparencyAttributes attr=new TransparencyAttributes(TransparencyAttributes.BLENDED, 1.f-opacy);
		this.setTransparencyAttributes(attr);
		this.setColoringAttributes(new ColoringAttributes (new Color3f (132/255, 112/266, 1.0f),1));
		this.setCapability(ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE );
	}

}
