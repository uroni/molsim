package de.tum.in.far.threedui.ex2;

import java.io.File;
import java.io.FileNotFoundException;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;

import org.jdesktop.j3d.loaders.vrml97.VrmlLoader;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;

import de.tum.in.far.threedui.elsim.NodeInfo;
import de.tum.in.far.threedui.elsim.Simulation;
import de.tum.in.far.threedui.ex1.solution.BlueAppearance;


public class Exercise2 {

	public static final String EXERCISE = "Exercise 2";
	
	public final static String COMPONENT_DIRECTORY = System.getProperty("user.dir") + File.separator + "libs" + File.separator + "ubitrack" + File.separator + "bin" + File.separator + "ubitrack";
	public final static String DATAFLOW_PATH = System.getProperty("user.dir") + File.separator + "dataflow" + File.separator + "3D-UI-SS-2011-Markertracker_Direct.dfg";
	
	private Viewer viewer;
	private CubeObject cubeObject;
	private ModelObject sheepObject;
	
	private UbitrackFacade ubitrackFacade;
	private PoseReceiver poseReceiver;
	private PoseReceiver poseReceiver2;
	private ImageReceiver imageReceiver;
	private AtomPoserReceiver atomPoseReceiver, atomPoseReceiver2;
	
	private Simulation sim;
	
	public Exercise2() {
		ubitrackFacade = new UbitrackFacade();
		
	}

	public static void main(String[] args) {
		Exercise2 exercise2 = new Exercise2();
		
		exercise2.initializeJava3D();
		exercise2.initializeUbitrack();
//		exercise2.loadSheep();
		exercise2.linkUbitrackToViewer();
	}
	
	private void initializeUbitrack() {
		ubitrackFacade.initUbitrack();
		int null_id=sim.addAtom("Null", 5.f, 0.f, 0.f);
		int catom_id=sim.addAtom("Kohlenstoff", 0.f, 0.f, 0.f);
//		int catom2_id=sim.addAtom("Kohlenstoff", 0.f, 0.f, 0.f);
		int hatom1_id=sim.addAtom("Wasserstoff", 1.f, 1.f, 1.f);
//		int hatom2_id=sim.addAtom("Wasserstoff", 2.f, 1.f, 1.f);
//		int hatom3_id=sim.addAtom("Wasserstoff", 3.f, 1.f, 1.f);
//		int hatom4_id=sim.addAtom("Wasserstoff", 4.f, 1.f, 1.f);
//		int hatom5_id=sim.addAtom("Wasserstoff", 7.f, 1.f, 1.f);
//		int hatom6_id=sim.addAtom("Wasserstoff", 8.f, 1.f, 1.f);
		sim.ConnectAndStore(catom_id, hatom1_id);
//		sim.ConnectAndStore(catom_id, hatom2_id);
//		sim.ConnectAndStore(catom_id, hatom3_id);
//		sim.ConnectAndStore(catom_id, catom2_id);
//		sim.ConnectAndStore(catom2_id, hatom4_id);
//		sim.ConnectAndStore(catom2_id, hatom5_id);
//		sim.ConnectAndStore(catom2_id, hatom6_id);
		BranchGroup tmp=new BranchGroup();
		atomPoseReceiver = new AtomPoserReceiver(tmp, sim, catom_id, null_id, "Kohlenstoff");
		viewer.addObject(tmp);
		if (!ubitrackFacade.setPoseCallback("posesink", atomPoseReceiver)) {
			return;
		}
		
		null_id=sim.addAtom("Null", 5.f, 0.f, 0.f);
		catom_id=sim.addAtom("Kohlenstoff", 0.f, 0.f, 0.f);
//		catom2_id=sim.addAtom("Kohlenstoff", 0.f, 0.f, 0.f);
		hatom1_id=sim.addAtom("Wasserstoff", 1.f, 1.f, 1.f);
//		hatom2_id=sim.addAtom("Wasserstoff", 2.f, 1.f, 1.f);
//		hatom3_id=sim.addAtom("Wasserstoff", 3.f, 1.f, 1.f);
//		hatom4_id=sim.addAtom("Wasserstoff", 4.f, 1.f, 1.f);
//		hatom5_id=sim.addAtom("Wasserstoff", 7.f, 1.f, 1.f);
//		hatom6_id=sim.addAtom("Wasserstoff", 8.f, 1.f, 1.f);
		sim.ConnectAndStore(catom_id, hatom1_id);
//		sim.ConnectAndStore(catom_id, hatom2_id);
//		sim.ConnectAndStore(catom_id, hatom3_id);
//		sim.ConnectAndStore(catom_id, catom2_id);
//		sim.ConnectAndStore(catom2_id, hatom4_id);
//		sim.ConnectAndStore(catom2_id, hatom5_id);
//		sim.ConnectAndStore(catom2_id, hatom6_id);
		
		sim.storeNodes();
		BranchGroup tmp2=new BranchGroup();
		atomPoseReceiver2 = new AtomPoserReceiver(tmp2, sim, catom_id, null_id, "Kohlenstoff");
		viewer.addObject(tmp2);
		if (!ubitrackFacade.setPoseCallback("posesink2", atomPoseReceiver2)) {
			return;
		}
		imageReceiver = new ImageReceiver();
		if (!ubitrackFacade.setImageCallback("imgsink", imageReceiver)) {
			return;
		}
		ubitrackFacade.startDataflow();
//		NodeInfo[] nInfo = sim.getNodes();
//		System.out.println("Position x: "+nInfo[0].position.x);
//		System.out.println("Position y: "+nInfo[0].position.y);
//		System.out.println("Position z: "+nInfo[0].position.z);
	}
	
	private void linkUbitrackToViewer() {
		BackgroundObject backgroundObject = new BackgroundObject();
		viewer.addObject(backgroundObject);
		
		imageReceiver.setBackground(backgroundObject.getBackground());
		
		//poseReceiver.setTransformGroup(cubeObject.getTransformGroup());
//		poseReceiver2.setTransformGroup(sheepObject.getTransformGroup());
	}
	
	private void loadSheep() {
		VrmlLoader loader = new VrmlLoader();
		Scene myScene = null;
		try {
			myScene = loader.load( "models" + File.separator + "Sheep.wrl");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IncorrectFormatException e) {
			e.printStackTrace();
		} catch (ParsingErrorException e) {
			e.printStackTrace();
		}

		// Maybe some transformation here
		
		sheepObject = new ModelObject(myScene.getSceneGroup());
		viewer.addObject(sheepObject);
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

		BlueAppearance blueAppearance = new BlueAppearance();
		
		cubeObject = new CubeObject(blueAppearance);
		
		tg.addChild(cubeObject);
		sim.setSchedulingBounds(new BoundingSphere(new Point3d(), 200.0));
		tg.addChild(sim);
		
		tmp.addChild(tg);
		
		viewer.addObject(tmp);
		
		
		System.out.println("Done");
	}
}
