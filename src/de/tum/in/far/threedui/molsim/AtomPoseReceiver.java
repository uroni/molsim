package de.tum.in.far.threedui.molsim;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.vecmath.Vector3f;

import de.tum.in.far.threedui.elsim.NodeInfo;
import de.tum.in.far.threedui.elsim.Simulation;

import ubitrack.SimplePose;
import ubitrack.SimplePoseReceiver;

class UpdateBehaviour extends Behavior
{

	private AtomPoseReceiver recv;
	
	public UpdateBehaviour(AtomPoseReceiver recv)
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

class LastPoint
{
	public LastPoint(long timeAdded, Vector3d point)
	{
		this.timeAdded=timeAdded;
		this.point=point;
	}
	public long timeAdded;
	public Vector3d point;
}

public class AtomPoseReceiver  extends SimplePoseReceiver {
	
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
	
	public int getCurr_idle() {
		return curr_idle;
	}

	private long last_shake_time=System.currentTimeMillis();
	private List<LastPoint> last_points=new ArrayList<LastPoint>();
	private boolean idle;
	
	private AtomPoseReceiver connected_with;
	private boolean connected=false;
	private int connectedId1=-1;
	private int connectedId2=-1;

	AtomPoseReceiver(BranchGroup bg, Simulation sim, int atom_id, int null_id, String atom_name)
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

		setIdle(false);
		
		if(curr_idle>1)
		{
			double d=currDir.dot(currDir);
			change_length=0.99*change_length+0.01*d;
			double a=Math.acos(currDir.dot(last_dir)/(currDir.length()*last_dir.length()));
			dir_change_severity=0.9*dir_change_severity+0.1*change_length*a;
			
			//System.out.println("change_length="+change_length+" dir_change_severity="+dir_change_severity);
		}
		
		/*if(change_length>1e-5 && dir_change_severity>1e-5 && System.currentTimeMillis()-last_shake_time>2000)
		{
			sim.removeMolecule(atom_id);
			atom_id=sim.addAtom(atom_name, 0, 0, 0);
			change_length=0;
			dir_change_severity=0;
			last_shake_time=System.currentTimeMillis();
		}*/
		
		last_points.add(new LastPoint(System.currentTimeMillis(), transVec));
		while(last_points.size()>0 && (System.currentTimeMillis()-last_points.get(0).timeAdded)>1500)
		{
			last_points.remove(0);
		}
		
		Vector3d mean_p=new Vector3d(0,0,0);
		for(LastPoint p: last_points)
		{
			mean_p.add(p.point);
		}
		mean_p.scale(1.0/last_points.size());
		
		int i=0;
		double mean_dist=0.;
		double travel_dist=0.;
		Vector3d last=null;
		for(LastPoint p: last_points)
		{
			Vector3d ldist=new Vector3d(p.point);
			if(i>0)
			{
				ldist.sub(last);
				travel_dist+=ldist.dot(ldist);
			}
			Vector3d distVec=new Vector3d(p.point);
			distVec.sub(mean_p);
			mean_dist+=distVec.dot(distVec);
			++i;
			last=new Vector3d(p.point);
		}
		mean_dist/=last_points.size();
		travel_dist/=(last_points.size()-1);
		
		//System.out.println("mean_dist="+mean_dist+" travel_dist="+travel_dist+" last_points.size()="+last_points.size());
		if(last_points.size()>=10 && travel_dist*0.3>mean_dist && mean_dist<8e-4 && System.currentTimeMillis()-last_shake_time>2000)
		{
			sim.SimRemoveMolecule(atom_id);
			atom_id=sim.SimAddAtom(atom_name, 0, 0, 0);
			last_shake_time=System.currentTimeMillis();
			System.out.println("DETECTED SHAKING");
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
//		System.out.println("last Position: "+pose.getTx()+" - "+pose.getTy()+" - "+pose.getTz());
	}
	
	
	
	
	
	public void cancelPosition()
	{
		if(System.currentTimeMillis()-last_update>=1000)
		{
			sim.freeAtomPosition(atom_id);
			idle_timer.setEnable(false);
			setIdle(true);
		}
	}

	public void setIdle(boolean idle) {
		this.idle = idle;
	}

	public boolean isIdle() {
		return idle;
	}

	public void setConnected_with(AtomPoseReceiver connected_with) {
		this.connected_with = connected_with;
	}

	public AtomPoseReceiver getConnected_with() {
		return connected_with;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public boolean isConnected() {
		return connected;
	}
	
	public int getAtomID()
	{
		return atom_id;
	}

	public void setAtomID(int aid)
	{
		atom_id=aid;
	}

	public String getAtom_name() {
		return atom_name;
	}

	public void setAtom_name(String atom_name) {
		this.atom_name = atom_name;
	}

	public void setConnectedId1(int connectedId1) {
		this.connectedId1 = connectedId1;
	}

	public int getConnectedId1() {
		return connectedId1;
	}

	public void setConnectedId2(int connectedId2) {
		this.connectedId2 = connectedId2;
	}

	public int getConnectedId2() {
		return connectedId2;
	}
}
