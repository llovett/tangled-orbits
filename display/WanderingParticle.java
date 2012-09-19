package display;

import display.*;
import processing.core.*;
import processing.opengl.*;
import traer.physics.*;
import java.awt.*;
import java.util.Random;

public class WanderingParticle extends HParticle {

    // How close do we have to be to out destination before generating a new one?
    private static final float DISTANCE_THRESHOLD = 100.0f;
    private static final float WANDERING_ATT = 2000.0f;
    private static final float WANDERING_DIST = 500.0f;
    private static final float MAX_WAND_VEL = 40.0f;

    private static final int PARTICLE_WIDTH = 75;

    // Pulse effect
    private double w_counter;
    private static final double PULSE_INC = 0.02;
    public static final int PULSE_AMOUNT = 15;


    Random rand;
    private Particle destination;

    private Point TL, BR;

    /**
     * WanderingParcile --- CONSTRUCTOR
     *
     * parent	= controlling PApplet instance
     * ps	= TraerPhysics ParticleSystem
     * RL	= Point giving top-left boundary of "wandering zone"
     * BR	= Point giving bottom-right boundary of "wandering zone"
     * */
    public WanderingParticle ( PApplet parent, ParticleSystem ps, Point TL, Point BR ) {
	this.parent = parent;

	// Set boundaries
	this.TL = TL;
	this.BR = BR;

	// Do some input checking
	if (TL.getY() >= BR.getY() || TL.getX() >= BR.getX()) {
	    System.err.println("Invalid boundary points: "+TL.toString()+", "+BR.toString());
	    System.exit(1);
	}

	rand = new Random();

	// make the destination fixed
	destination = ps.makeParticle();
	destination.makeFixed();
	p = ps.makeParticle(HMusic.SOLAR_MASS,
			    (int)(rand.nextInt((int)(BR.getX() - TL.getX())) + TL.getX()),
			    (int)(rand.nextInt((int)(BR.getY() - TL.getY())) + TL.getY()),
			    0.0f);

	rand = new Random();
	newDestination();

	// set the attraction
	ps.makeAttraction(destination,
			  p,
			  WANDERING_ATT,
			  WANDERING_DIST);

	// set the width counter
	w_counter = 0.0;
    }

    /**
     * update()
     *
     * Called to update this particle's position automatically.
     * */
    public void update() {
	if (! checkPosition())
	    newDestination();
	checkBoundaries();
    }

    /**
     * checkPosition ()
     *
     * Checks to see if we need to make another random destination to wander to.
     *
     * returns:
     *	true == still far enough away from destination to keep wandering
     *	false == time to make a new destination
     * */
    private boolean checkPosition () {
	return Math.hypot( getX() - destination.position().x(),
			   getY() - destination.position().y()) >= DISTANCE_THRESHOLD;
    }

    /** overridden from HParticle **/
    public void checkBoundaries() {
	if (p.velocity().length() > MAX_WAND_VEL ) {
	    p.velocity().normalize();
	    p.velocity().multiplyBy(MAX_WAND_VEL);
	}
    }

    /**
     * newDestination()
     *
     * Creates another randomized destination in our range to use as a fixed
     * "destination point".
     * */
    private void newDestination () {
	// System.out.println("Making a new destination... (was "+destination.position()+")");
	destination.position().set((int)(rand.nextInt((int)(BR.getX() - TL.getX())) + TL.getX()),
				   (int)(rand.nextInt((int)(BR.getY() - TL.getY())) + TL.getY()),
				   0);
	// System.out.println("Curr part pos: "+p.position());
	// System.out.println("Destination position: "+destination.position());
    }

    public void render () {
	w_counter += PULSE_INC;
	if (w_counter >= 2*Math.PI) w_counter = 0.0;

	parent.pushMatrix();
	parent.translate(getX(), getY());

	parent.fill( 255, 255, 0 );
	int renderWidth = (int)(PARTICLE_WIDTH + (Math.sin(w_counter) * PULSE_AMOUNT));
	parent.ellipse( 0, 0, renderWidth, renderWidth);
	parent.popMatrix();
    }

}