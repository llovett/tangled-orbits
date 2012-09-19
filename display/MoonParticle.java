package display;

import display.*;
import processing.core.*;
import processing.opengl.*;
import traer.physics.*;
import java.util.Random;

class MoonParticle extends HParticle {
    private int w;
    private Random rand;
	
    public MoonParticle(PApplet parent, ParticleSystem ps, int w) {
	this.parent = parent;
	
	rand = new Random();

	this.p = ps.makeParticle(HMusic.MOON_MASS,
				 rand.nextFloat()*HMusic.WIDTH,
				 rand.nextFloat()*HMusic.HEIGHT,
				 0.0f);
	this.w = w;
    }

    public MoonParticle(PApplet parent, ParticleSystem ps) {
	this(parent, ps, 10);
    }

    public void setWidth(int w) {
	this.w = w;
    }

    public void render() {
	checkBoundaries();

	parent.pushMatrix();
	parent.translate(getX(), getY());
	parent.ellipseMode(parent.CENTER);
	parent.fill(255);
	parent.ellipse(0, 0, w, w);
	parent.popMatrix();
    }
}