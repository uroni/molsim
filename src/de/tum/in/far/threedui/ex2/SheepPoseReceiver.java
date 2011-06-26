package de.tum.in.far.threedui.ex2;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import ubitrack.SimplePose;
import ubitrack.SimplePoseReceiver;

public class SheepPoseReceiver extends SimplePoseReceiver {

	protected TransformGroup markerTransGroup = null;
	
	public void setTransformGroup(TransformGroup markerTransGroup) {
		this.markerTransGroup = markerTransGroup;
	}
	
	public void receivePose(SimplePose pose) {
		if (markerTransGroup == null){
			return;
		}

		double[] trans = new double[3];
		double[] rot = new double[4];
		trans[0] = pose.getTx();
		trans[1] = pose.getTy();
		trans[2] = pose.getTz();
		rot[0] = pose.getRx();
		rot[1] = pose.getRy();
		rot[2] = pose.getRz();
		rot[3] = pose.getRw();
		
		Vector3d up=new Vector3d(0,0,0.02);

		Vector3d transVec = new Vector3d(pose.getTx(), pose.getTy(), pose.getTz());
		Quat4d rotQ = new Quat4d(pose.getRx(), pose.getRy(), pose.getRz(), pose.getRw());
		Transform3D rotTransform=new Transform3D();
		rotTransform.set(rotQ);
		rotTransform.transform(up);
		Transform3D markerTransform = new Transform3D();
		transVec.add(up);
		markerTransform.set(rotQ, transVec, 1);
		markerTransGroup.setTransform(markerTransform);
		
//		System.out.println("Pos: " + pose.getTx() + ", " + pose.getTy() + ", " + pose.getTz());
	}

}
