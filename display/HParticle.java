package display;

import display.*;
import processing.core.*;
import processing.opengl.*;
import traer.physics.*;
import java.awt.*;

public abstract class HParticle {
    protected Particle p;
    protected PApplet parent;
 
    public Particle getParticle() {
	return p;
    }

    public int getX() {
	return (int)(p.position().x());
    }

    public int getY() {
	return (int)(p.position().y());
    }

    public void checkBoundaries() {
	if (p.position().x() < 0 || p.position().x() > parent.width)
	    p.velocity().set( -HMusic.ELASTICITY* p.velocity().x(), p.velocity().y(), 0);
	if (p.position().y() < 0 || p.position().y() > parent.height)
	    p.velocity().set( p.velocity().x(), -HMusic.ELASTICITY* p.velocity().y(), 0);

	p.position().set( parent.constrain(p.position().x(), 0, parent.width),
			  parent.constrain(p.position().y(), 0, parent.height),
			  0);
	    
	if (p.velocity().length() > HMusic.MAX_VEL) {
	    p.velocity().normalize();
	    p.velocity().multiplyBy(HMusic.MAX_VEL);
	}
    }

    /** This needs to be overridden in any child classes **/
    public abstract void render();

}