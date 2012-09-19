package display;

import display.*;
import event.*;
import processing.core.*;
import processing.opengl.*;
import traer.physics.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import synth.*;
import com.softsynth.jsyn.*;
import oscP5.*;
import netP5.*;

public class HMusic extends PApplet implements IntersectionEventListener {

    ///////////////////
    // OSC CONSTANTS //
    ///////////////////
    public static final int OSC_PORT_SEND = 12000;
    public static final int OSC_PORT_RCV = 13001;

    //////////////////////////////
    // APPLET-RELATED CONSTANTS //
    //////////////////////////////
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 800;
//     public static final int WIDTH = 1920;
//     public static final int HEIGHT = 1080;
    // public static final int WIDTH = 1920;
    // public static final int HEIGHT = 1200;

    ///////////////////////////////
    // PHYSICS-RELATED CONSTANTS //
    ///////////////////////////////
    // Velocity, elasticity, "environmental" physics
    public static final float ELASTICITY = 0.9f;
    public static final float MAX_VEL = 10.0f;
    public static final float REPEL_STRENGTH = -100.0f;	// Prevent like bodies from "clumping"

    // Dust constants and variables
    public static final float DUST_MASS = 0.2f;
    public static final float DUST_ATTRACTION_DIST = 1000f;
    public static final float DUST_ATTRACTION_STRENGTH = 1000f;
    public static final float DUST_SPRING_STRENGTH = 0.1f;
    public static final float DUST_SPRING_DAMPING = 0.1f;
    public static final float MAX_CAELI_VEL = 4.0f;
    public float CAELI_DENSITY = 0.1f;

    // Moon constants and variables
    public static final float MOON_MASS = 0.5f;
    public static float MOON_ATTRACTION_DIST = 200;
    public static float MOON_ATTRACTION_STRENGTH = 10000;

    // Planet constants and variables
    public static final float PLANET_MASS = 10.0f;
    public static float PLANET_ATTRACTION_DIST = 800;
    public static float PLANET_ATTRACTION_STRENGTH = 10000;

    // Solar constants
    public static final float SOLAR_MASS = 25.0f;

    // User particle constants
    public static final float USER_ATTRACTION_STRENGTH = 10000;
    public static final float USER_ATTRACTION_DIST = 50;


    ///////////////////////////////////////////////
    // NON-PHYSICS, SIMULATION-RELATED CONSTANTS //
    ///////////////////////////////////////////////
    // Simulation
    public static final int NUM_PLANETS = 10;
    public static final int NUM_MOONS = 0;
    public static int TRIANGLE_THRESH = 2000;		// Greatest allowable triangle area
    public static int TRIANGLE_DIST_THRESH = 500;	// Greatest allowable vrtx. dst.
    public static final int MIN_TRIANGLE_AGE = 10;	// Prevent "flashing triangles"
    public static int TRAIL_ERASE_ALPHA = 100;

    ///////////////////
    // CLASS MEMBERS //
    ///////////////////
    private OscP5 osc;
    private ParticleSystem ps;
    private Point traveler;				// Point representing the traveler's position
    private Particle userParticle;			// Particle that the user controls
    private ArrayList<Attraction> userAttractions;	// Attractions between the userParticle and planets
    private boolean userAttractionOn;
    private ArrayList<MoonParticle> moons;
    private CopyOnWriteArrayList<PlanetParticle> planets;	// Slightly larger particles than "dust"
    private SolarParticle sun;					// Wandering particle at the center of all
    private StellaCaeli caeli;					// "The Heavens"
    private Triangulation dustTriangulation;
    private LizSynth synth;	// The synthesizer
    private Timer noteTimer;	// Puts a minimum distance in-between notes
    Random rand;
    
    // External references
    private HMusicController controller;		// The control panel (ugly Java AWT thing)

    // GLOBAL VARS
    private boolean onoff = false;			// off by default
    private boolean noteGate = true;			// whether or not we will report an intersection
    private static boolean attractionOn;
    private long noteDelayTime = 10L;			// 10 ms between notes minimum!

    public void setup() {
	size( screen.width, screen.height, OPENGL );
	// size( screen.width, screen.height, JAVA2D );
	// smooth();

	ps = new ParticleSystem();
	ps.setIntegrator(ParticleSystem.MODIFIED_EULER);
	ps.setGravity(0.0f);

	userParticle = ps.makeParticle();
	userParticle.makeFixed();

	rand = new Random();

	planets = new CopyOnWriteArrayList<PlanetParticle>();
	moons = new ArrayList<MoonParticle>();
	sun = new SolarParticle ( this, ps );

	// Add planets to the sun
	for (int i=0; i<NUM_PLANETS; i++)
	    planets.add( sun.addPlanet() );

	// Add moons to each of the planets
	for ( PlanetParticle planet : planets )
	    for (int i=0; i<NUM_MOONS; i++)
		moons.add(planet.addMoon());

	// Add user attractions
	userAttractions = new ArrayList<Attraction>();
	for ( PlanetParticle planet : planets )
	    userAttractions.add( createUserAttraction( planet.getParticle() ) );
	userAttractionOn = false;

	// Make "the heavens"
	caeli = new StellaCaeli( this, CAELI_DENSITY, ps );

	// Add triangulation effects
	dustTriangulation = new Triangulation( this,
					       ps,
					       caeli.getParticles(),
					       new Color( 0, 0, 0 ),
					       new Color( 0, 0, 0 ) );

	// Add this as a IntersectionEventListener to all Triangulations
	dustTriangulation.addIntersectionEventListener( this );
					       
	attractionOn = true;

	// Initialize and display controller window.
	controller = new HMusicController( this );
	// Don't use the controller window. Instead, use the iPad!
	// controller.setVisible( true );

	// Initialize the timer
	noteTimer = new Timer();
	noteTimer.schedule( new TimerTask() {
		public void run() {
		    // Reset the note gate
		    noteGate = true;
		}
	    },
	    0L,
	    noteDelayTime );

	// Initialize OscP5 object
	osc = new OscP5( this, OSC_PORT_RCV );

	// Tell Max we're ready
	osc.send( new OscMessage("/onoff", new Object[]{ 1 }),
		  new NetAddress("127.0.0.1", OSC_PORT_SEND) );
    }

    public void turnon() {
	onoff = true;
    }

    public void turnoff() {
	onoff = false;
    }

    public int getWidth() {
	return WIDTH;
    }

    public int getHeight() {
	return HEIGHT;
    }



   /**
     * updateUserPosition()
     *
     * This method should contain whatever code is necessary to update the
     * `userParticle` object to the position where the user's controller
     * specifies. If that means a mouse, update the particle to the mouse
     * position. If that means IR-tracking, then update the particle to wherever
     * the IR-camera sees the user.
     * */
    private void updateUserPosition() {
	userParticle.position().set( mouseX, mouseY, 0);
    }

    /**
     * createUserAttraction( Particle p )
     *
     * Create an Attraction suitable for usage between the user and a Particle p.
     *
     * @param p - The particle that is to be attracted to the user.
     * @return - An Attraction that can be added to a List of the users' attractions.
     * */
    private Attraction createUserAttraction( Particle p ) {
	return ps.makeAttraction( p, userParticle,
				  USER_ATTRACTION_STRENGTH,
				  USER_ATTRACTION_DIST );
    }

    /**
     * setUserAttraction( onoff )
     *
     * Sets whether or not planet particles are attracted to the user.
     * @param onoff - true if planets should be attracted, false otherwise.
     * */
    public boolean setUserAttraction( boolean onoff ) {
	if ( onoff ) {
	    for ( Attraction att : userAttractions )
		att.turnOn();
	    System.out.println("set user attraction on.");
	}
	else {
	    for ( Attraction att : userAttractions )
		att.turnOff();
	    System.out.println("set user attraction off.");
	}

	return onoff;
    }

   
    ///////////////
    // DRAW LOOP //
    ///////////////

    public void draw() {
	if ( onoff ) {
	    // Perform physics-related operations
	    // updateUserPosition();
	    ps.tick();

	    // ==================================================
	    // this all for the background
	    strokeWeight(0);
	    if (mousePressed)
		TRAIL_ERASE_ALPHA = 5;
	    else
		TRAIL_ERASE_ALPHA = 120;

	    fill(255, 255, 255, TRAIL_ERASE_ALPHA);

	    rect(0, 0, WIDTH, HEIGHT);
	    // ==================================================

	    // Update triangulation effects
	    dustTriangulation.update();
	    for ( PlanetParticle pl : planets )
		dustTriangulation.highlightAtPoint( new Point( pl.getX(), pl.getY() ) );

	    // Render the heavens
	    caeli.render();

	    // Render triangulation effects
	    dustTriangulation.render();
	
	    // draw stuff
	    sun.render();

	    for ( PlanetParticle planet : planets )
		planet.render();


	}
	else {
	    background( 0 );
	}
	noCursor();
    }


    ///////////////
    // LISTENERS //
    ///////////////
 
    public void keyPressed() {
	switch (keyCode) {
	case 32:	// Spacebar
	    setAttraction( PLANET_ATTRACTION_DIST, 0 );
	    break;
	case 9:		// Tab key
	    for ( PlanetParticle planet : planets )
		moons.add( planet.addMoon() );
	    break;
	case 192:	// Tilde
	    Color c1 = new Color( (int)(255 * Math.random() ),
				  (int)(255 * Math.random() ),
				  (int)(255 * Math.random() ) );
	    // Haha.
	    Color c2 = c1.brighter().brighter().brighter().brighter();
	    setTriangulationColor( c1, c2 );
	    break;
	case 80:
	    controller.printPreset();
	    break;
	default:
	    System.out.println("The key was: "+keyCode);
	    break;
	}
    }

    public void mouseClicked() {
	userAttractionOn = setUserAttraction( ! userAttractionOn );
    }

    /**
     * intersectionEventReceived( IntersectionEvent e )
     *
     * This method gets called by a Triangulation when an intersection occurs.
     * 
     * @param e - The IntersectionEvent, complete with a reference to HTriangle and
     * a Point. See documentation for HTriangle within the Triangulation class.
     *
     * */
    public void intersectionEventReceived( IntersectionEvent e ) {
	// Check the note gate.
	if ( ! noteGate )
	    return;
	else
	    noteGate = false;

	// Which edge was crossed?
	ArrayList<Particle> pts = e.getTriangle().getPoints();
	// I really wish I knew this function existed sooner....
	double e1 = java.awt.geom.Line2D.ptLineDist( pts.get( 0 ).position().x(),
						     pts.get( 0 ).position().y(),
						     pts.get( 1 ).position().x(),
						     pts.get( 1 ).position().y(),
						     e.getPoint().getX(),
						     e.getPoint().getY() );
	double e2 = java.awt.geom.Line2D.ptLineDist( pts.get( 1 ).position().x(),
						     pts.get( 1 ).position().y(),
						     pts.get( 2 ).position().x(),
						     pts.get( 2 ).position().y(),
						     e.getPoint().getX(),
						     e.getPoint().getY() );
	double e3 = java.awt.geom.Line2D.ptLineDist( pts.get( 2 ).position().x(),
						     pts.get( 2 ).position().y(),
						     pts.get( 0 ).position().x(),
						     pts.get( 0 ).position().y(),
						     e.getPoint().getX(),
						     e.getPoint().getY() );

	double closest = Math.min( e1, Math.max( e2, e3 ) );
	int lineLength = 0;
	if ( closest == e1 )
	    lineLength = (int)Math.hypot( pts.get( 0 ).position().x() - pts.get( 1 ).position().x(),
					  pts.get( 0 ).position().y() - pts.get( 1 ).position().y() );
	else if ( closest == e2 )
	    lineLength = (int)Math.hypot( pts.get( 1 ).position().x() - pts.get( 2 ).position().x(),
					  pts.get( 1 ).position().y() - pts.get( 2 ).position().y() );
	else if ( closest == e3 )
	    lineLength = (int)Math.hypot( pts.get( 2 ).position().x() - pts.get( 0 ).position().x(),
					  pts.get( 2 ).position().y() - pts.get( 0 ).position().y() );
	    
	// Send information to MaxMSP in an OscMessage.
	OscMessage msg = new OscMessage( "/intersect", new Object[] { (int)e.getTriangle().size(),
								      lineLength,
								      (int)e.getPoint().getX(),
								      (int)e.getPoint().getY() } );

	sendToMax( msg );

    }

    /**
     * oscEvent( OscMessage msg )
     *
     * This method gets called whenever we receive an OSC packet on OSC_PORT_RCV.
     * See documentation for the OscP5 library at:
     * http://www.sojamo.de/libraries/oscP5/reference/index.html
     * */
    public void oscEvent( OscMessage maxMsg ) {
	if ( maxMsg.checkAddrPattern( "/toipad" ) ) {
	    Float[] ipadMsgArgs = new Float[ maxMsg.arguments().length ];
	    for ( int i=0; i<maxMsg.arguments().length; i++ ) {
		ipadMsgArgs[i] = new Float( (Integer)maxMsg.arguments()[i] / 1000.0f );
	    }
	    
	    OscMessage msg = new OscMessage( "/toipad", (Object[])ipadMsgArgs );
	    controller.sendToIpad( msg );
	}
    }

    public void sendToMax( OscMessage msg ) {
	osc.send( msg, new NetAddress( "127.0.0.1", OSC_PORT_SEND ) );
    }


    //////////////////////////////////////
    // FUNCTIONS USED BY THE CONTROLLER //
    //////////////////////////////////////

    public void setNoteSpace( long howlong ) {
	noteGate = false;
	noteDelayTime = howlong < 2L? 2L : howlong;	// This value can't be zero.

	noteTimer.cancel();
	noteTimer = new Timer();
	noteTimer.schedule( new TimerTask() {
		public void run() {
		    noteGate = true;
		}
	    },
	    0L,
	    noteDelayTime );

	System.out.println("Note space is now "+noteDelayTime);
    }

    public long getNoteDelayTime() {
	return noteDelayTime;
    }
    
    public void setTriangulationColor( Color c1, Color c2 ) {
	int trianglesThreshold = dustTriangulation.getTrianglesThreshold();
	
	// We build a whole new Triangulation instead of calling setColors() on it,
	// because we want ALL triangles to change their colors ALL AT ONCE.
	dustTriangulation = new Triangulation( this,
					       ps,
					       caeli.getParticles(),
					       c1, c2 );

	// Add this as a IntersectionEventListener to all Triangulations
	dustTriangulation.addIntersectionEventListener( this );

	// Restore size of Triangulation
	setDustTrianglesThreshold( trianglesThreshold );
    }

    public void setCaeliDensity( double density ) {
	caeli.setDensity( density );
    }

    public void setPlanetCount( int count ) {
	if ( count < 0 ) count = 0;	// shouldn't ever happen

	if ( count > planets.size() ) {
	    // Add planets
	    for ( int i = planets.size(); i <= count; i++ ) {
		PlanetParticle newPlanet = sun.addPlanet();
		userAttractions.add( createUserAttraction( newPlanet.getParticle() ) );
		planets.add( newPlanet );
	    }

	} else if ( count < planets.size() ) {
	    // Remove planets
	    while ( count < planets.size() ) {
		int which = rand.nextInt( planets.size() );
		planets.remove( which );
	    }
	} // Else, no change necessary
    }

    public void setDustTrianglesThreshold( int threshold ) {
	dustTriangulation.setTrianglesThreshold( threshold );
    }

    public void setWandererLocation( int x, int y ) {
	userParticle.position().set( x, y, 0 );
    }

    public void setAttraction( float distance, float strength ) {
	float[] attProps = sun.setAttractionProperties( distance, strength );
	PLANET_ATTRACTION_DIST = attProps[0];
	PLANET_ATTRACTION_STRENGTH = attProps[1];

	if ( planets.size() > 0 ) {
	    attProps = planets.get(0).setAttractionProperties( distance, strength );
	    MOON_ATTRACTION_DIST = attProps[0];
	    MOON_ATTRACTION_STRENGTH = attProps[1];
	}
	for ( PlanetParticle plan : planets )
	    plan.setAttractionProperties( distance, strength );
    }

    
    /////////////////////////////////////
    // UTILITY / CONVENIENCE FUNCTIONS //
    /////////////////////////////////////

    public void debug(String message) {
	System.err.println(message);
    }

}
