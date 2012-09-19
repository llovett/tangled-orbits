package display;

import display.*;
import processing.core.*;
import processing.opengl.*;
import traer.physics.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.awt.Point;

public class StellaCaeli {

    private ParticleSystem ps;
    private PApplet parent;
    private Random rand;
    private CopyOnWriteArrayList<Particle> particles;
    private ArrayList<Attraction> attrs;
    private ArrayList<Spring> springs;

    private static final int MAX_STARS = 2000;
    private static final int CAELI_BUFFER = 400;	// Extra space outside of window

    public StellaCaeli( PApplet parent, double density, ParticleSystem ps ) {
	this.ps = ps;
	this.parent = parent;

	rand = new Random();

	int numStars = (int)(MAX_STARS * density);
	particles = new CopyOnWriteArrayList<Particle>();
	
	for (int i = 0; i < numStars; i++) {
	    Particle p = ps.makeParticle( HMusic.DUST_MASS,
					  rand.nextFloat() * HMusic.WIDTH,
					  rand.nextFloat() * HMusic.HEIGHT,
					  0 );
	    for ( int j = 1; j < i; j++ ) {
		ps.makeAttraction( p, particles.get(j),
				   HMusic.REPEL_STRENGTH,
				   100 );
	    }
	    particles.add( p );
	}
    }

    /**
     * setDensity()
     *
     * Change the number of Particles used by StellaCaeli.
     * */
    public void setDensity( double density ) {
	int numStars = (int)(MAX_STARS * density);

	if ( numStars > particles.size() ) {
	    // Add more Particles
	    for ( int i = particles.size(); i <= numStars; i++ ) {
		Particle p = ps.makeParticle( HMusic.DUST_MASS,
					      rand.nextFloat() * HMusic.WIDTH,
					      rand.nextFloat() * HMusic.HEIGHT,
					      0 );
		// Add attractions
		for ( int j = 1; j < i; j++ ) {
		    ps.makeAttraction( p, particles.get(j),
				       HMusic.REPEL_STRENGTH,
				       100 );
		}
		particles.add( p );
	    }

	} else if ( numStars < particles.size() ) {
	    while ( numStars < particles.size() ) {
		// Remove some Particles
		int which = rand.nextInt( particles.size() );
		Particle p = particles.get( which );
		p.setMark( true );	// Mark for deletion
		particles.remove( which );
	    }
	}	// else, don't do anything!
    }

    /**
     * getParticles()
     *
     * returns:
     * An ArrayList of this StellaCaeli's Particles.
     * */
    public java.util.List<Particle> getParticles() {
	return particles;
    }

    /**
     * checkParticles()
     *
     * Apply any kind of physical restrictions to particles
     * (e.g., make sure they are under a maximum velocity)
     * */
    private void checkParticles() {
	for ( Particle p : particles ) {
	    // Keep velocities in check.
	    if ( p.velocity().length() > HMusic.MAX_CAELI_VEL ) {
		p.velocity().normalize();
		p.velocity().multiplyBy(HMusic.MAX_CAELI_VEL);
	    }

	    // Wrap on leaving bounds.
	    boolean wrapped = false;
	    if ( p.position().x() < -CAELI_BUFFER ) {
		p.position().set( HMusic.WIDTH,
				  p.position().y(),
				  0 );
		wrapped = true;
	    }
	    if ( p.position().x() > HMusic.WIDTH + CAELI_BUFFER ) {
		p.position().set( 0,
				  p.position().y(),
				  0 );
		wrapped = true;
	    }
	    if ( p.position().y() < -CAELI_BUFFER ) {
		p.position().set( p.position().x(),
				  HMusic.HEIGHT,
				  0 );
		wrapped = true;
	    }
	    if ( p.position().y() > HMusic.HEIGHT + CAELI_BUFFER ) {
		p.position().set( p.position().x(),
				  0,
				  0 );
		wrapped = true;
	    }

	    if ( wrapped )
		p.velocity().multiplyBy(2.0f);
	}
    }

    /**
     * update()
     *
     * Do whatever movement on the particles is necessary before rendering.
     * */
    private void update() {
	for ( Particle p : particles ) {
	    float x = p.position().x();
	    float y = p.position().y();

	    p.position().set(x, y-1f, 0);
	}
	checkParticles();
    }

    public void render() {
	update();

	Iterator<Particle> partIt = particles.iterator();
	for ( Particle p : particles ) {
	    parent.pushMatrix();

	    parent.translate( p.position().x(), p.position().y() );
	    parent.fill( 100 );
	    parent.ellipse(0, 0, 3, 3);

	    parent.popMatrix();
	}
    }

}