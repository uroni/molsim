package de.tum.in.far.threedui.elsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.media.j3d.Appearance;
import javax.media.j3d.Behavior;
import javax.media.j3d.Billboard;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.Material;
import javax.media.j3d.OrientedShape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.media.j3d.WakeupOnElapsedTime;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;

import de.tum.in.far.threedui.molsim.Viewer;
import de.tum.in.far.threedui.molsim.common.BlueAppearance;


class SimAtom
{
	public SimAtom(int aid, String name)
	{
		atom_id=aid;
		this.setName(name);
		if(name.equals("Kohlenstoff"))
			setEls(4);
		else if(name.equals("Wasserstoff"))
			setEls(1);
		else if(name.equals("Sauerstoff"))
			setEls(6);
	}
	
	public boolean ConnectedTo(int aid)
	{
		if( atom_id==aid)
			return true;
		
		if(marked)
			return false;
		else
			marked=true;
		
		for(SimAtom a: connectedAtoms)
		{
			if( a.ConnectedTo(aid) )
				return true;
		}
		return false;
	}
	
	public List<SimAtom> getConnectedAtoms()
	{
		List<SimAtom> ret=new ArrayList<SimAtom>();
		
		if(marked)
			return ret;
		else
			marked=true;
		
		
		for(SimAtom a: connectedAtoms)
		{
			List<SimAtom> t=a.getConnectedAtoms();
			ret.addAll(t);
		}
		return ret;
	}
	
	public void Connect(SimAtom a)
	{
		connectedAtoms.add(a);
	}
	
	public void Disconnect(SimAtom a)
	{
		connectedAtoms.remove(a);
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setEls(int els) {
		this.els = els;
	}

	public int getEls() {
		return els;
	}

	private int atom_id;
	private Set<SimAtom> connectedAtoms=new HashSet<SimAtom>();
	public boolean marked;
	private String name;
	
	private int els=0;
}


public class Simulation extends Behavior
{
	static {
        System.loadLibrary("elsimlib");
    }
	
	public Simulation(TransformGroup parent, Viewer viewer)  {
		super();
        simptr=init();
        this.parent=parent;
        this.viewer=viewer;
    }
	
	public native NodeInfo[] getNodes();
	private native int addAtom(String name, float x, float y, float z);
	public native void removeAtom(int id);
	public native boolean CanConnect(int a1, int a2);
	public native boolean Connect(int a1, int a2);
	public native boolean Disconnect(int a1, int a2);
	private native boolean removeMolecule(int id);
	public native void setAtomPosition(int id, float x, float y, float z);
	public native void freeAtomPosition(int id);
	
    private native long init();
    private long simptr;
    
    private NodeInfo[] cached_nodes=new NodeInfo[1];
    
    public static native Vector3f getRotationToTarget(Vector3f position, Vector3f target);
    
    public static float simscale=2000.f;
    public static float simposscale=20000.f;
    
    private TransformGroup parent;
    private Viewer viewer;
    
    private Map<Integer, SimAtom> sim_atoms=new HashMap<Integer, SimAtom>();
    
    public int SimAddAtom(String name, float x, float y, float z)
    {
    	int id=addAtom(name, x, y, z);
    	sim_atoms.put(id, new SimAtom(id, name));
    	return id;
    }
    
    public boolean SimConnect(int a1, int a2)
    {
    	boolean b=Connect(a1, a2);
    	if(b)
    	{
    		SimOnlyConnect(a1, a2);
    	}
    	return b; 
    }
    
    public boolean SimPretendConnect(int a1, int a2)
    {
    	boolean b=Connect(a1, a2);
    	if(b)
    	{
    		SimAtom sa1=sim_atoms.get(a1);
        	SimAtom sa2=sim_atoms.get(a2);
        	if(sa1!=null && sa2!=null)
        	{
        		sa1.setEls(sa1.getEls()-1);
        		sa2.setEls(sa2.getEls()-1);
        	}
    	}
    	return b; 
    }
    
    public boolean SimOnlyConnect(int a1, int a2)
    {
    	SimAtom sa1=sim_atoms.get(a1);
    	SimAtom sa2=sim_atoms.get(a2);
    	if(sa1!=null && sa2!=null)
    	{
    		sa1.Connect(sa2);
    		sa2.Connect(sa1);
    		return true;
    	}
    	return false;
    }
    
    public boolean SimDisconnect(int a1, int a2)
    {
    	boolean b=Disconnect(a1, a2);
    	if(b)
    	{
	    	SimAtom sa1=sim_atoms.get(a1);
	    	SimAtom sa2=sim_atoms.get(a2);
	    	if(sa1!=null && sa2!=null)
	    	{
	    		sa1.Disconnect(sa2);
	    		sa2.Disconnect(sa1);
	    		sa1.setEls(sa1.getEls()+1);
        		sa2.setEls(sa2.getEls()+1);
	    	}
    	}
    	return b; 
    }
    
    public boolean SimPretendDisconnect(int a1, int a2)
    {
    	boolean b=Disconnect(a1, a2);
    	if(b)
    	{
	    	SimAtom sa1=sim_atoms.get(a1);
	    	SimAtom sa2=sim_atoms.get(a2);
	    	if(sa1!=null && sa2!=null)
	    	{
	    		sa1.setEls(sa1.getEls()+1);
        		sa2.setEls(sa2.getEls()+1);
	    	}
    	}
    	return b; 
    }
    
    private void unmarkAtoms()
    {
    	for(Entry<Integer, SimAtom> e: sim_atoms.entrySet() )
    	{
    		e.getValue().marked=false;
    	}
    }
    
    public boolean SimRemoveMolecule(int id)
    {
    	boolean b=removeMolecule(id);
    	if(b)
    	{
	    	SimAtom a=sim_atoms.get(id);
	    	if(a!=null)
	    	{
	    		unmarkAtoms();
		    	List<SimAtom> l=a.getConnectedAtoms();
		    	for(SimAtom c: l)
		    	{
		    		sim_atoms.remove(c);
		    	}
	    	}
    	}
    	return b;
    }
    
    public boolean isConnected(int a1, int a2)
    {
    	SimAtom a=sim_atoms.get(a1);
    	if(a!=null)
    	{
    		unmarkAtoms();
    		return a.ConnectedTo(a2);
    	}
    	return false;
    }
    
    public boolean SimCanConnect(int a1, int a2)
    {
    	SimAtom sa1=sim_atoms.get(a1);
    	SimAtom sa2=sim_atoms.get(a2);
    	if(sa1!=null && sa2!=null)
    	{
    		if(sa1.getEls()>0 && sa2.getEls()>0)
    			return true;
    		else
    			return false;
    	}
    	else
    	{
    		return false;
    	}
    }
        
    public NodeInfo[] getCached_nodes() {
		return cached_nodes;
	}

	public void setCached_nodes(NodeInfo[] cached_nodes) {
		this.cached_nodes = cached_nodes;
	}

	public void update(TransformGroup parent)
    {
    	NodeInfo[] new_nodes=getNodes();
    	if(cached_nodes.length!=new_nodes.length)
    		cached_nodes=new NodeInfo[new_nodes.length];
    	
    	HashMap<TransformGroup, Boolean> found=new HashMap<TransformGroup, Boolean>();
    	for(int i=0;i<new_nodes.length;++i)
    	{    		
    		NodeInfo n=new_nodes[i];
    		
    		cached_nodes[i]=new_nodes[i].clone();
    		
    		n.position.x/=simposscale;
    		n.position.y/=simposscale;
    		n.position.z/=simposscale;
    		
    		TransformGroup tg=nodes.get(n.id);
    		if(tg==null && (n.type!=0 || n.material.length()>0))
    		{
    			BranchGroup bg=new BranchGroup();
    			bg.setCapability(BranchGroup.ALLOW_DETACH);
    			tg=new TransformGroup();
    			tg.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
    			tg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
    			tg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
    			tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    			tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    			tg.setBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000000000000.0));
    			
    			if(n.type==0)
    			{
    				Appearance app=new Appearance();
    				if(n.material.length()>0)
    				{
	    				TextureLoader tl=new TextureLoader("resources2\\"+n.material, null);
	    				Texture tex=tl.getTexture();
	    				tex.setBoundaryModeS(Texture.WRAP);
	    				tex.setBoundaryModeT(Texture.WRAP);
	    				
	    				TextureAttributes texAttr = new TextureAttributes();
	    			    texAttr.setTextureMode(TextureAttributes.MODULATE);
	    			    
	    				Material mat=new Material();
	    				mat.setLightingEnable(false);
	    				app.setMaterial(mat);
	    				app.setTexture(tex);
	    				app.setTextureAttributes(texAttr);
    				}
    				Sphere s=new Sphere(n.scale.x/simscale, Primitive.GENERATE_NORMALS|Primitive.GENERATE_TEXTURE_COORDS, 64, app);
    				
    				TransformGroup sg=new TransformGroup();
    				TransformGroup sg2=new TransformGroup();
    				sg2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        			sg2.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    				Transform3D ts=new Transform3D();
    				ts.setEuler(new Vector3d(0,Math.PI/2,0));
    				sg.setTransform(ts);
    				sg.addChild(s);
    				sg2.addChild(sg);
    				tg.addChild(sg2);
    			}
    			else if(n.type==3)
    			{
    				if(n.opacy==0)
    				{
    					Sphere s=new Sphere(0.005f, new BankAppearance(0.5f));
        				tg.addChild(s);
    				}
    				else
    				{
    					Box box=new Box(0.01f, 0.01f, 0.001f, new BankAppearance(0.5f));
        				tg.addChild(box);
    				}
    			}
    			else if(n.type==1)
    			{   				
    				Box box=new Box(2.f, 2.f, 1.f, new ConnectionAppearance(0.7f));
    				TransformGroup tg2=new TransformGroup();
    				Transform3D offset=new Transform3D();
    				offset.setTranslation(new Vector3d(0,0,0.0f));
    				tg2.setTransform(offset);
    				tg2.addChild(box);
    				tg.addChild(tg2);
    			}
    			
    			nodes.put(n.id, tg);
    			bg.addChild(tg);
    			parent.addChild(bg);
    			found.put(tg, true);
    		}
    		else if(tg!=null)
    		{
    			found.put(tg, true);
    			Transform3D t=new Transform3D();
    			
    			if(n.type!=0)
    			{
    				Vector3d rot=new Vector3d(n.rotation);
    				rot.x*=(float)Math.PI/180.f;
    				rot.y*=(float)Math.PI/180.f;
    				rot.z*=(float)Math.PI/180.f;
    				t.setEuler(rot);
    			}
    			else
    			{
    				Transform3D ct=new Transform3D();
    				viewer.getCameraTransformGroup().getTransform(ct);
    				Vector3d campos=new Vector3d();
    				ct.get(campos);
    				Vector3f rot_f=getRotationToTarget(new Vector3f(campos), n.position);
    				Vector3d rot=new Vector3d(rot_f);
    				rot.x*=((float)Math.PI/180.f);
    				rot.y*=((float)Math.PI/180.f);
    				rot.z*=((float)Math.PI/180.f);
    				t.setEuler(rot);
    			}
    			
    			t.setTranslation(n.position);
    			
    			if(n.type==1)
    			{
    				Vector3d scale=new Vector3d(n.scale);
    				scale.x/=simposscale;
    				scale.y/=simposscale;
    				scale.z/=simposscale;
    				t.setScale(scale);
    				
    				Box box=(Box)( (TransformGroup)(tg.getChild(0))).getChild(0);
    				float o=1.f-(n.opacy/100.f)*0.8f;
    				box.getAppearance().setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED, o));
    			}
    			
   				try {
					tg.setTransform(t);
				} catch (Exception e1) {
					e1.printStackTrace();
				}		
    		}
    	}
    	
    	boolean c=true;
    	while(c)
    	{
    		c=false;
	    	for(Entry<Integer, TransformGroup> e: nodes.entrySet())
	    	{
	    		if(!found.containsKey(e.getValue()))
	    		{
	    			parent.removeChild(e.getValue().getParent());
	    			nodes.remove(e.getKey());
	    			c=true;
	    			break;
	    		}
	    	}
    	}
    }
    
    private HashMap<Integer, TransformGroup> nodes = new HashMap<Integer, TransformGroup>();

	@Override
	public void initialize()
	{
		wakeupOn(new WakeupOnElapsedFrames(0, false));
	}

	@Override
	public void processStimulus(Enumeration arg0) {
		update(parent);
		wakeupOn(new WakeupOnElapsedFrames(0, false));
	}
}
