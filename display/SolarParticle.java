package display;

import display.*;
import processing.core.*;
import processing.opengl.*;
import traer.physics.*;
import java.util.ArrayList;
import java.util.Random;

public class SolarParticle extends HParticle {

    private int w;		// The width
    private double w_counter;

    private ArrayList<PlanetParticle> planets;
    private ArrayList<Attraction> planetAttractions;
    private Random rand;
    private ParticleSystem ps;

    private static final double PULSE_INC = 0.01;
    public static final int PULSE_AMOUNT = 50;

    /**
     * CONSTRUCTOR
     **/
    public SolarParticle ( PApplet parent, ParticleSystem ps, int w ) {
	this.parent = parent;
	this.ps = ps;
	
	rand = new Random();

	this.p = ps.makeParticle(HMusic.SOLAR_MASS,
				 rand.nextFloat()*HMusic.WIDTH,
				 rand.nextFloat()*HMusic.HEIGHT,
				 0.0f);
	this.w = w;

	w_counter = 0.0;

	planets = new ArrayList<PlanetParticle>();
	planetAttractions = new ArrayList<Attraction>();
    }

    public SolarParticle ( PApplet parent, ParticleSystem ps ) {
	this( parent, ps, 75 );
    }

    public void attractionOn( boolean attraction ) {
	if ( attraction )
	    for ( PlanetParticle planet : planets )
		setAttraction( planet );
	else
	    for ( Attraction att : planetAttractions )
		ps.removeAttraction( att );
    }

    public void setAttraction( PlanetParticle planet ) {
// 	planetAttractions.add(ps.makeAttraction(getParticle(),
// 						planet.getParticle(),
// 						HMusic.PLANET_ATTRACTION_STRENGTH,
// 						HMusic.PLANET_ATTRACTION_DIST));
    }

    public float[] setAttractionProperties( float distance, float strength ) {
	float[] ret = new float[2];
	if ( planetAttractions.size() > 0 ) {
	    ret[0] = planetAttractions.get(0).getMinimumDistance();
	    ret[1] = planetAttractions.get(0).getStrength();
	}
	for ( Attraction att : planetAttractions ) {
	    att.setMinimumDistance( distance );
	    att.setStrength( strength );
	}
	return ret;
    }

    public PlanetParticle addPlanet() {
	PlanetParticle planet = new PlanetParticle(parent, ps);

	setAttraction( planet );
	planets.add( planet );

	return planet;
    }

    public void render () {
	// checkBoundaries();

	// w_counter += PULSE_INC;
	// if (w_counter >= 2*Math.PI) w_counter = 0.0;

	// parent.pushMatrix();
	// parent.translate( p.position().x(), p.position().y() );
	// parent.ellipseMode( parent.CENTER );
	
	// parent.fill( 255, 200, 100, 50 + (int)(100.0 * Math.abs(Math.sin(w_counter))) );
	// parent.ellipse( 0, 0, (int)(w + 10*Math.sin(w_counter)), (int)(w + 10*Math.sin(w_counter)));
	// parent.popMatrix();
    }
   
}