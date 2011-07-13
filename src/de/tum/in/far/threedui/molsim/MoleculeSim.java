package de.tum.in.far.threedui.molsim;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import org.jdesktop.j3d.loaders.vrml97.VrmlLoader;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;

import de.tum.in.far.threedui.elsim.NodeInfo;
import de.tum.in.far.threedui.elsim.Simulation;
import de.tum.in.far.threedui.molsim.common.BlueAppearance;


class UpdateSimBehaviour extends Behavior
{
	private MoleculeSim ex;
	
	public UpdateSimBehaviour(MoleculeSim ex)
	{
		this.ex=ex;
	}
	
	@Override
	public void initialize() {
		this.setSchedulingBounds(new BoundingSphere(new Point3d(), 200.0) );
		wakeupOn(new WakeupOnElapsedFrames(0));
	}

	@Override
	public void processStimulus(Enumeration arg0) {
		ex.handleCollision();
		wakeupOn(new WakeupOnElapsedFrames(0));
	}
}

public class MoleculeSim {

	public static final String EXERCISE = "MoleculeSim";
	
	public final static String COMPONENT_DIRECTORY = System.getProperty("user.dir") + File.separator + "libs" + File.separator + "ubitrack" + File.separator + "bin" + File.separator + "ubitrack";
	public final static String DATAFLOW_PATH = System.getProperty("user.dir") + File.separator + "dataflow" + File.separator + "3D-UI-SS-2011-Markertracker_Direct.dfg";
	
	private Viewer viewer;
	
	private UbitrackFacade ubitrackFacade;

	private ImageReceiver imageReceiver;
	private List<AtomPoseReceiver> atomPoseReceivers=new ArrayList<AtomPoseReceiver>();
	
	private Simulation sim;
	
	public MoleculeSim() {
		ubitrackFacade = new UbitrackFacade();
		
	}

	public static void main(String[] args) {
		MoleculeSim exercise2 = new MoleculeSim();
		
		exercise2.initializeJava3D();
		exercise2.initializeUbitrack();
		exercise2.linkUbitrackToViewer();
	}
	
	private void addAtomReceiver(String name, String posesink)
	{
		int null_id=sim.SimAddAtom("Null", 5.f, 0.f, 0.f);
		int catom_id=sim.SimAddAtom(name, 0.f, 0.f, 0.f);
		BranchGroup tmp=new BranchGroup();
		AtomPoseReceiver atomPoseReceiver = new AtomPoseReceiver(tmp, sim, catom_id, null_id, name);
		viewer.addObject(tmp);
		if (!ubitrackFacade.setPoseCallback(posesink, atomPoseReceiver)) {
			return;
		}
		atomPoseReceivers.add(atomPoseReceiver);
	}
	
	private void initializeUbitrack() {
		ubitrackFacade.initUbitrack();
		
		
		addAtomReceiver("Kohlenstoff", "posesink");
		addAtomReceiver("Wasserstoff", "posesink2");
		addAtomReceiver("Kohlenstoff", "posesink3");
		addAtomReceiver("Sauerstoff", "posesink4");
		
		
		imageReceiver = new ImageReceiver();
		if (!ubitrackFacade.setImageCallback("imgsink", imageReceiver)) {
			return;
		}
		
		BranchGroup bgt=new BranchGroup();
		bgt.addChild(new UpdateSimBehaviour(this));
		viewer.addObject(bgt);
		
		ubitrackFacade.startDataflow();

	}
	
	private void linkUbitrackToViewer() {
		BackgroundObject backgroundObject = new BackgroundObject();
		viewer.addObject(backgroundObject);
		
		imageReceiver.setBackground(backgroundObject.getBackground());
	}
	
	private void initializeJava3D() {
		System.out.println("Creating Viewer - " + EXERCISE);
		viewer = new Viewer(EXERCISE, ubitrackFacade);
		
		BranchGroup tmp=new BranchGroup();
		
		
		TransformGroup tg=new TransformGroup();
		tg.setBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000000000000.0));
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		
		sim=new Simulation(tg, viewer);

	
		sim.setSchedulingBounds(new BoundingSphere(new Point3d(), 200.0));
		tg.addChild(sim);
		
		tmp.addChild(tg);
		
		viewer.addObject(tmp);
		
		
		System.out.println("Done");
	}
	
	static double THRESHOLD = 0.12;
	
	public void handleCollision()
	{
		NodeInfo[] nInfo = sim.getCached_nodes();
		
		for(AtomPoseReceiver a1: atomPoseReceivers)
		{
			if(a1.getCurr_idle()==0)
				continue;
			
			for( AtomPoseReceiver a2: atomPoseReceivers)
			{
				if(a2.getCurr_idle()==0)
					continue;
				
				if(a1==a2)
					continue;
				
				if(a1.isConnected() && a1.getConnected_with()==a2 && a1.isIdle() )
				{
					sim.SimOnlyConnect(a1.getAtomID(), a2.getAtomID());
					int atom_id=sim.SimAddAtom(a1.getAtom_name(), 0, 0, 0);
					a1.setConnected(false);
					a1.setAtomID(atom_id);
					a2.setConnected(false);
				}
				
			
				double dist, absDist;
				NodeInfo node1, node2;
				
				// check if the nodes are still close enough to each other
				if(a1.isConnected()) {
					NodeInfo connectedNode1 = null, connectedNode2 = null;
					for(NodeInfo n : nInfo) {
						if(n.id == a1.getConnectedId1())
							connectedNode1 = n;
						if(n.id == a1.getConnectedId2())
							connectedNode2 = n;
					}
					if(connectedNode1 == null || connectedNode2 == null) {
						System.out.println("one node is null");
						return;
					}
					dist = distance(connectedNode1, connectedNode2);
					Vector3f realSize1 = new Vector3f(connectedNode1.scale);
					realSize1.x /= Simulation.simscale;
					Vector3f realSize2 = new Vector3f(connectedNode2.scale);
					realSize2.x /= Simulation.simscale;
					absDist = Math.abs(dist - realSize1.x - realSize2.x);

					//System.out.println("absDist: "+absDist);
					if(absDist > THRESHOLD * 1.2) {
						System.out.println("disconnecting...");
						sim.SimPretendDisconnect(a1.getConnectedId1(), a1.getConnectedId2());
						a1.setConnected(false);
						a1.getConnected_with().setConnected(false);
					}
				}
				
				NodeInfo collNode1 = null, collNode2 = null;
				double minDist = 0.1;
				for(int i = 0; i < nInfo.length; i++) {
					node1 = nInfo[i];
					if(!sim.isConnected(a1.getAtomID(), node1.id))
						continue;
					if(node1.material.equals(""))
						continue;
					
					for(int j = 0; j < nInfo.length; j++) {
						node2 = nInfo[j];
						if(j == i)
							continue;						
						if(!(node1.type == 0 && node2.type == 0)) 
							continue;									
						if( node2.material.equals("")){
							continue;
						}
						if(!sim.SimCanConnect(node1.id, node2.id))
							continue;
						if(sim.isConnected(a1.getAtomID(), node2.id))
							continue;
						if(!sim.isConnected(a2.getAtomID(), node2.id))
							continue;
						
						dist = distance(node1, node2);
						Vector3f realSize1 = new Vector3f(node1.scale);
						realSize1.x /= Simulation.simscale;
						Vector3f realSize2 = new Vector3f(node2.scale);
						realSize2.x /= Simulation.simscale;
						absDist = dist - realSize1.x - realSize2.x;

						if(absDist < THRESHOLD) {
							if(absDist < minDist) {
								collNode1 = node1;
								collNode2 = node2;
								minDist = absDist;
							}
						}
					}
				}
				if(collNode1 != null && collNode2 != null) {
					
					if( (!a1.isConnected() || 
						 
						!(   (collNode1.id==a1.getConnectedId1() && collNode2.id==a1.getConnectedId2()) || 
							 (collNode1.id==a1.getConnectedId2() && collNode2.id==a1.getConnectedId1())   
						) ) && 
							sim.SimCanConnect(collNode1.id, collNode2.id)) {
						System.out.println("COLLISION between node "+ collNode1.toString()+" and "+ collNode2.toString() +" distance: "+minDist);
						if(a1.isConnected())
						{
							System.out.println("disconnecting...");
							sim.SimPretendDisconnect(a1.getConnectedId1(), a1.getConnectedId2());
							a1.setConnected(false);
							a1.getConnected_with().setConnected(false);
						}
						sim.SimPretendConnect(collNode1.id, collNode2.id);
						a1.setConnectedId1(collNode1.id);
						a1.setConnectedId2(collNode2.id);
						a1.setConnected_with(a2);
						a1.setConnected(true);
						a2.setConnectedId1(collNode1.id);
						a2.setConnectedId2(collNode2.id);
						a2.setConnected_with(a1);
						a2.setConnected(true);
					}
				}
			}
		}
	}
	
	/**
	 * Computes the euclidean distance between two nodes (specifically 
	 * between the centers of two spheres).
	 * @param n1
	 * @param n2
	 * @return
	 */
	private double distance(NodeInfo n1, NodeInfo n2) {
		Vector3f realPosition1 = new Vector3f(n1.position);
		realPosition1.x /= Simulation.simposscale;
		realPosition1.y /= Simulation.simposscale;
		realPosition1.z /= Simulation.simposscale;
		Vector3f realPosition2 = new Vector3f(n2.position);
		realPosition2.x /= Simulation.simposscale;
		realPosition2.y /= Simulation.simposscale;
		realPosition2.z /= Simulation.simposscale;
		double dist = Math.sqrt(Math.pow((realPosition1.x - realPosition2.x),2) 
				+ Math.pow((realPosition1.y - realPosition2.y),2)
				+ Math.pow((realPosition1.z - realPosition2.z),2));
//		System.out.println("distance: "+dist);
		return dist;
		
	}
}
