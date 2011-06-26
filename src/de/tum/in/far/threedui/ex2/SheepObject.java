package de.tum.in.far.threedui.ex2;

import java.io.FileNotFoundException;

import javax.media.j3d.Appearance;

import org.jdesktop.j3d.loaders.vrml97.VrmlLoader;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;

import com.sun.j3d.utils.geometry.Box;

public class SheepObject extends TransformableObject {
	
	public SheepObject(Appearance app) {
		VrmlLoader loader=new VrmlLoader();
		Scene scene = null;
		try {
			scene = loader.load("models/sheep.wrl");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IncorrectFormatException e) {
			e.printStackTrace();
		} catch (ParsingErrorException e) {
			e.printStackTrace();
		}
		ModelObject sheep=new ModelObject(scene.getSceneGroup());
	}	
}
