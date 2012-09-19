package display;

import display.*;
import processing.core.*;
import processing.opengl.*;
import traer.physics.*;
import java.util.ArrayList;
import java.util.Random;

class PlanetParticle extends HParticle {
    private int w;
    private ArrayList<MoonParticle> moons;
    private ArrayList<Attraction> moonAttractions;
    private Random rand;
    private ParticleSystem ps;

    /**
     * CONSTRUCTOR
     * */
    public PlanetParticle(PApplet parent, ParticleSystem ps, int w) {
	this.parent = parent;
	this.ps = ps;

	rand = new Random();

	this.p = ps.makeParticle(HMusic.PLANET_MASS,
				 rand.nextFloat()*HMusic.WIDTH,
				 rand.nextFloat()*HMusic.HEIGHT,
				 0.0f);
	this.w = w;

	moons = new ArrayList<MoonParticle>();
	moonAttractions = new ArrayList<Attraction>();
    }

    public PlanetParticle(PApplet parent, ParticleSystem ps) {
	this(parent, ps, 20);
    }

    public void attractionOn( boolean attraction ) {
	if ( attraction )
	    for ( MoonParticle mp : moons )
		setAttraction( mp );
	else
	    for ( Attraction att : moonAttractions )
		ps.removeAttraction( att );
    }

    public void setAttraction( MoonParticle moon ) {
	moonAttractions.add(ps.makeAttraction(getParticle(),
					      moon.getParticle(),
					      HMusic.MOON_ATTRACTION_STRENGTH,
					      HMusic.MOON_ATTRACTION_DIST));
    }

    public float[] setAttractionProperties( float distance, float strength ) {
	float[] ret = new float[2];
	if ( moonAttractions.size() > 0 ) {
	    ret[0] = moonAttractions.get(0).getMinimumDistance();
	    ret[1] = moonAttractions.get(0).getStrength();
	}
	for ( Attraction att : moonAttractions ) {
	    att.setMinimumDistance( distance );
	    att.setStrength( strength );
	}
	return ret;
    }

    public void setWidth(int w) {
	this.w = w;
    }

    public MoonParticle addMoon( ) {
	MoonParticle tp = new MoonParticle(parent, ps);

	tp.setWidth(rand.nextInt(10)+1);
	setAttraction( tp );
    
	moons.add(tp);

	return tp;
    }

    public void render() {
	checkBoundaries();

	parent.pushMatrix();
	parent.translate(getX(), getY());
	parent.ellipseMode(parent.CENTER);
	parent.fill(255,200,50);
	parent.ellipse(0, 0, w, w);
	parent.popMatrix();

	// Render all the moons
	for ( MoonParticle moon : moons )
	    moon.render();

    }
}