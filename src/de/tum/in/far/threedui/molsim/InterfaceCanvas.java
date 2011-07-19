/**
 * 
 */
package de.tum.in.far.threedui.molsim;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Stroke;

import javax.media.j3d.Canvas3D;

/**
 * @author Martin
 *
 */
public class InterfaceCanvas extends Canvas3D
{

	public InterfaceCanvas(GraphicsConfiguration arg0) {
		super(arg0);
		this.setSize(800,600);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void postRender()
    {
        this.getGraphics2D().setColor(Color.white);
        this.getGraphics2D().drawRect(10,10,780, 20);
        this.getGraphics2D().setColor(Color.blue);
        this.getGraphics2D().fillRect(11, 11, Math.round(778*(pc_done/100.f)), 18);
        this.getGraphics2D().flush(false);
    }
	
	
	public void setPc_done(int pc_done) {
		if(pc_done<=100)
		{
			this.pc_done = pc_done;
		}
		else
		{
			this.pc_done=100;
		}
	}


	public int getPc_done() {
		return pc_done;
	}

	public void setOwns_id(int owns_id) {
		this.owns_id = owns_id;
	}


	public int getOwns_id() {
		return owns_id;
	}

	private int pc_done=0;
	private int owns_id=0;

}
