package de.tum.in.far.threedui.ex2;

import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.media.j3d.WakeupOnElapsedTime;

import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import de.tum.in.far.threedui.elsim.Simulation;

import ubitrack.SimplePose;
import ubitrack.SimplePoseReceiver;

class UpdateBehaviour extends Behavior
{

	private AtomPoserReceiver recv;
	
	public UpdateBehaviour(AtomPoserReceiver recv)
	{
		this.recv=recv;
	}
	
	@Override
	public void initialize() {
		this.setSchedulingBounds(new BoundingSphere(new Point3d(), 200.0) );
		wakeupOn(new WakeupOnElapsedFrames(0));
	}

	@Override
	public void processStimulus(Enumeration arg0) {
		recv.cancelPosition();	
		wakeupOn(new WakeupOnElapsedFrames(0));
	}
	
}

public class AtomPoserReceiver  extends SimplePoseReceiver {
	
	private Simulation sim;
	private int atom_id;
	private int null_id;
	private long last_update=System.currentTimeMillis();
	private UpdateBehaviour idle_timer;
	private String atom_name;
	
	private double change_length=0;
	private double dir_change_severity=0;
	private Vector3d last_pos=new Vector3d();
	private Vector3d last_dir=new Vector3d();
	private int curr_idle=0;

	AtomPoserReceiver(BranchGroup bg, Simulation sim, int atom_id, int null_id, String atom_name)
	{
		this.sim=sim;
		this.atom_id=atom_id;
		this.null_id=null_id;
		this.atom_name=atom_name;
		idle_timer=new UpdateBehaviour(this);
		bg.addChild(idle_timer);
	}
	
	public void receivePose(SimplePose pose) {
		
		double[] trans = new double[3];
		double[] rot = new double[4];
		trans[0] = pose.getTx();
		trans[1] = pose.getTy();
		trans[2] = pose.getTz();
		rot[0] = pose.getRx();
		rot[1] = pose.getRy();
		rot[2] = pose.getRz();
		rot[3] = pose.getRw();		
		
		Vector3d transVec = new Vector3d(pose.getTx(), pose.getTy(), pose.getTz());
		Quat4d rotQ = new Quat4d(pose.getRx(), pose.getRy(), pose.getRz(), pose.getRw());
		Vector3d currDir=new Vector3d(pose.getTx()-last_pos.x, pose.getTy()-last_pos.y, pose.getTz()-last_pos.z);

		if(curr_idle>1)
		{
			double d=currDir.dot(currDir);
			change_length=0.9*change_length+0.1*d;
			double a=Math.acos(currDir.dot(last_dir)/(currDir.length()*last_dir.length()));
			dir_change_severity=0.9*dir_change_severity+0.1*a;
			
			System.out.println("change_length="+change_length+" dir_change_severity="+dir_change_severity);
		}
		
		if(change_length>0.001 && dir_change_severity>1.5)
		{
			sim.removeMolecule(atom_id);
			atom_id=sim.addAtom(atom_name, 0, 0, 0);
			change_length=0;
			dir_change_severity=0;
		}
		
		Vector3d up=new Vector3d(0,0,0.07);
		Transform3D rot_trans=new Transform3D();
		rot_trans.set(rotQ);
		rot_trans.transform(up);
		
		transVec.add(up);
		
		sim.setAtomPosition(null_id, (float)pose.getTx()*Simulation.simposscale, (float)pose.getTy()*Simulation.simposscale, (float)pose.getTz()*Simulation.simposscale);
		sim.setAtomPosition(atom_id, (float)transVec.x*Simulation.simposscale, (float)transVec.y*Simulation.simposscale, (float)transVec.z*Simulation.simposscale);
		
		last_update=System.currentTimeMillis();	
		idle_timer.setEnable(true);
		
		
		if(curr_idle>0)
		{
			curr_idle=2;
			last_dir.x=pose.getTx()-last_pos.x;
			last_dir.y=pose.getTy()-last_pos.y;
			last_dir.z=pose.getTz()-last_pos.z;
		}
		else
		{
			curr_idle=1;
		}
		
		last_pos.x=pose.getTx();
		last_pos.y=pose.getTy();
		last_pos.z=pose.getTz();		
	}
	
	public void cancelPosition()
	{
		if(System.currentTimeMillis()-last_update>=500)
		{
			sim.freeAtomPosition(atom_id);
			idle_timer.setEnable(false);
		}
	}

}
