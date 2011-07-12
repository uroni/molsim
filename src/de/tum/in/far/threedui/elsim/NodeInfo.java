package de.tum.in.far.threedui.elsim;

import javax.vecmath.Vector3f;

public class NodeInfo implements Cloneable
{
	public Vector3f position;
	public Vector3f rotation;
	public Vector3f scale;
	public Vector3f size;
	public String material;
	public boolean visible;
	public float opacy;
	public int type;
	public int id;
	
	public String toString() {
		return "id: "+id+" material: "+material;
	}
	
	public NodeInfo clone()
	{
		NodeInfo n=new NodeInfo();
		n.position=(Vector3f)position.clone();
		n.rotation=(Vector3f)rotation.clone();
		n.scale=(Vector3f)scale.clone();
		n.size=(Vector3f)size.clone();
		n.material=new String(material);
		n.visible=visible;
		n.opacy=opacy;
		n.type=type;
		n.id=id;
		return n;
	}
}
