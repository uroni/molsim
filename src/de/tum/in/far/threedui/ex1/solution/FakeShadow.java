package de.tum.in.far.threedui.ex1.solution;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

public class FakeShadow extends BranchGroup {

	private float height = 0.0f;
	
	private final TransformGroup tg;
	
	public FakeShadow(GeometryArray geom, Color3f col) {
		tg = new TransformGroup();
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		FlatShape flatShape = new FlatShape(geom, col);
		tg.addChild(flatShape);
	    addChild(tg);
	}

	public TransformGroup getTransformGroup() {
		return tg;
	}

	// ******************
	
	private class FlatShape extends Shape3D {
		FlatShape(GeometryArray geom, Color3f col) {
			// Y-component of direction must not be 0.0!
			//Vector3f direction = new Vector3f(0.0f, -1.0f, 0.0f);

    		int vCount = geom.getVertexCount();
//    		QuadArray poly = new QuadArray(vCount, GeometryArray.COORDINATES | GeometryArray.COLOR_3 | GeometryArray.NORMALS);
    		int[] stripVertexArray = new int[((TriangleStripArray)geom).getNumStrips()];
    		((TriangleStripArray)geom).getStripVertexCounts(stripVertexArray);
    		TriangleStripArray poly = new TriangleStripArray(vCount, GeometryArray.COORDINATES | GeometryArray.COLOR_3 | GeometryArray.NORMALS, stripVertexArray);

    		Point3f vertex = new Point3f();
    		Point3f shadow = new Point3f();
    		for (int v = 0; v < vCount; v++) {
    			geom.getCoordinate(v, vertex);
    			shadow.set(vertex.x, height + 0.0001f, vertex.z);
//    			shadow.set(vertex.x - ((direction.x / direction.y) * (vertex.y - height)),
//    					   height + 0.0001f,
//    					   vertex.z - ((direction.z / direction.y) * (vertex.y - height)));
    			
    			poly.setCoordinate(v, shadow);
//    			poly.setNormal(v, direction);
    			poly.setColor(v, col);
    		}

    		this.setGeometry(poly);
		}
	}
}
