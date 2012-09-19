package display;

import display.*;
import event.*;
import processing.core.*;
import processing.opengl.*;
import traer.physics.*;
import java.util.*;
import java.awt.*;

public class Triangulation {

    // Triangle attributes/behavior, modifiable by controller
    private int TRIANGLE_THRESH;
    private int TRIANGLE_DIST_THRESH;

    private ArrayList<IntersectionEventListener> listeners;

    private java.util.List<Particle> plist;
    private ArrayList<HTriangle> triangles;
    private PApplet parent;
    private ParticleSystem ps;
    private HashMap<Point,Integer> intersectionMap;

    // Colors
    private int r1, g1, b1;
    private int r2, g2, b2;

    // Random # generator
    Random rand;

    public Triangulation( PApplet parent ) {
	this.parent = parent;
	triangles = new ArrayList<HTriangle>();
	rand = new Random();
    }

    public Triangulation( PApplet parent, ParticleSystem ps, Object[] parts,
			  Color c1, Color c2 ) {
	this.parent = parent;
	this.ps = ps;
	triangles = new ArrayList<HTriangle>();
	plist = new ArrayList<Particle>();
	listeners = new ArrayList<IntersectionEventListener>();

	for ( Object o : parts ) {
	    if ( o instanceof Particle )
		plist.add( (Particle)o );
	    else if ( o instanceof HParticle )
		plist.add( ((HParticle)o).getParticle() );
	}
	setColors( c1, c2 );

	rand = new Random();
    }

    public Triangulation( PApplet parent, ParticleSystem ps,
			  java.util.List<Particle> parts,
			  Color c1, Color c2 ) {
	this.parent = parent;
	this.ps = ps;
	triangles = new ArrayList<HTriangle>();
	plist = parts;
	listeners = new ArrayList<IntersectionEventListener>();

	setColors( c1, c2 );

	rand = new Random();
    }

    public void addIntersectionEventListener( IntersectionEventListener listener ) {
	listeners.add( listener );
    }

    public boolean add( HParticle p ) {
	return plist.add( p.getParticle() );
    }

    public boolean add( Particle p ) {
	return plist.add( p );
    }

    public boolean add() {
	return add( ps.makeParticle( HMusic.DUST_MASS,
				     rand.nextFloat() * HMusic.WIDTH,
				     rand.nextFloat() * HMusic.HEIGHT,
				     0 ) );
    }

    public boolean remove( HParticle p ) {
	return plist.remove( p.getParticle() );
    }

    public boolean remove( Particle p ) {
	return plist.remove( p );
    }

    public boolean remove() {
	if ( plist.size() > 0 ) {
	    plist.remove( plist.size() - 1 );
	    return true;
	}
	return false;
    }

    public Iterator<HTriangle> iterator() {
	return triangles.iterator();
    }

    public int size() {
	return triangles.size();
    }

    public void setColors( Color c1, Color c2 ) {
	r1 = (c1.getRed() < c2.getRed()? c1.getRed() : c2.getRed());
	g1 = (c1.getGreen() < c2.getGreen()? c1.getGreen() : c2.getGreen());
	b1 = (c1.getBlue() < c2.getBlue()? c1.getBlue() : c2.getBlue());
	r2 = (c1.getRed() >= c2.getRed()? c1.getRed() : c2.getRed());
	g2 = (c1.getGreen() >= c2.getGreen()? c1.getGreen() : c2.getGreen());
	b2 = (c1.getBlue() >= c2.getBlue()? c1.getBlue() : c2.getBlue());
    }

    /**
     * highlightAtPoint( p )
     *
     * Highlights or un-highlights the triangles that contains a Point p.
     * A triangle will highlight if it contains p, otherwise it will be off.
     *
     * @param p - The point to find within a triangle.
     * */
    public void highlightAtPoint( Point p ) {
	for ( HTriangle t : triangles ) {
	    if ( t.containsPoint( p ) )
		t.registerPoint( p );
	}
    }

    public void update() {
	// Create triangles
    	buildTriangles();
    }

    public void render() {
	for ( HTriangle t : triangles )
	    t.render();
	// Generate events for intersections
	for ( HTriangle tri : triangles ) {
	    if ( tri.intersected() ) {
		HashSet<Point> ips = tri.getIntersectingPoints();
		for ( Point p : ips ) {
		    for ( IntersectionEventListener l : listeners )
			l.intersectionEventReceived( new IntersectionEvent(tri, p) );
		}
	    }
	}
    }

    private void buildTriangles() {
	// Step 1: Loop through the triangles and remove ones that are no longer needed
	Iterator<HTriangle> ti = iterator();
	while (ti.hasNext()) {
	    HTriangle t_curr = ti.next();
	    // Reset the count for number of agents occupying the triangle
	    t_curr.clearPointCount();

	    // Make sure the Triangle is still intact (from controller)
	    if ( ! t_curr.isSound() ) {
		ti.remove();
		continue;
	    }

	    // Remove triangles that are too large. Give some time to adjust in size.
	    if (t_curr.size() > TRIANGLE_THRESH && t_curr.getAge() > HMusic.MIN_TRIANGLE_AGE)
		ti.remove();
	    else if ( t_curr.getLongestEdge() > TRIANGLE_DIST_THRESH && t_curr.getAge() > HMusic.MIN_TRIANGLE_AGE )
		ti.remove();
	}

	// Step 2: Find places to build triangles.
	for (int i = 0; i<plist.size(); i++) {
	    Particle point1 = plist.get(i);
	    ArrayList<Particle> otherParticles = new ArrayList<Particle>();

	    // Find other close moons
	    for (int j = 0; j<plist.size() && otherParticles.size() < 2; j++) {
		if (j == i) continue;
		Particle point2 = plist.get(j);
		if (Math.hypot((point1.position().x() - point2.position().x()),
			       (point1.position().y() - point2.position().y()))
		    <= Math.sqrt(TRIANGLE_THRESH*2))

		    otherParticles.add(point2);
	    }

	    if (otherParticles.size() == 2) {
		HTriangle tt = new HTriangle(parent, point1,
					     otherParticles.get(0),
					     otherParticles.get(1) );

		// See if the triangle already exists.
		boolean skip = false;
		for (HTriangle tri : triangles)
		    if (tri.equals(tt))
			skip = true;
		if (skip) continue;

		int theRed, theGreen, theBlue;
		if ( r1 == r2 )
		    theRed = r1;
		else
		    theRed = rand.nextInt( r2 - r1 )+r1;
		if ( g1 == g2 )
		    theGreen = g1;
		else
		    theGreen = rand.nextInt( g2 - g1 )+g1;
		if ( b1 == b2 )
		    theBlue = b1;
		else
		    theBlue = rand.nextInt( b2 - b1 )+b1;
		tt.setColor( theRed, theGreen, theBlue );
		triangles.add(tt);
	    }
	}
    }


    ////////////////////////////////
    // METHODS FOR THE CONTROLLER //
    ////////////////////////////////

    public void setTrianglesThreshold( int threshold ) {
	TRIANGLE_THRESH = threshold;
	TRIANGLE_DIST_THRESH = threshold / 4;
    }

    public int getTrianglesThreshold() {
	return TRIANGLE_THRESH;
    }


    /////////////////////
    // HTriangle Class //
    /////////////////////

    public class HTriangle {

	// Graphical properties
	private Color baseColor;
	private Color lightColor;

	private PApplet parent;		// Used to render the triangle and get convenience funcs.

	// MISCH. properties
	private int age;		// Used to prevent "triangle flashing"
	private HashSet<Point> containedPoints;
	private HashSet<Point> lastContainedPoints;
	private int lastOccupation;	// Last occupation count
	private Particle p1, p2, p3;	// Vertices

	/**
	 * CONSTRUCTOR
	 * */
	public HTriangle(PApplet parent, Particle p1, Particle p2, Particle p3) {
	    this.p1 = p1;
	    this.p2 = p2;
	    this.p3 = p3;

	    this.parent = parent;
	    this.containedPoints = new HashSet<Point>();
	    this.lastContainedPoints = new HashSet<Point>();
	}

	/**
	 * registerPoint()
	 *
	 * acknowledge the existence of a point within this triangle.
	 * */
	public void registerPoint( Point p ) {
	    this.containedPoints.add( p );
	}

	/**
	 * clearPointCount()
	 *
	 * unregister all points within this triangle. This does not happen
	 * as a result of Particles leaving the Triangle, but because the
	 * count is cleared during buildTriangles.
	 * */
	public void clearPointCount() {
	    this.containedPoints = new HashSet<Point>();
	}

	/**
	 * isSound()
	 *
	 * @return - true if all Particles in this HTriangle are intact,
	 * false otherwise.
	 * */
	public boolean isSound() {
	    return (! (p1.isMarked() || p2.isMarked() || p3.isMarked() ) );
	}

	public int getAge() {
	    return age;
	}

	public void setColor(int r, int g, int b) {
	    baseColor = new Color(r, g, b);
	    lightColor = baseColor.brighter().brighter().brighter();	// This method exists?!
	}

	/**
	 * test2
	 *
	 * */
	private int test2( double px, double py, double m, double b ) {
	    if (py < m * px + b ) {
		return -1; // point is under line
	    }else if ( py == m * px + b ){
		return 0; // point is on line
	    } else {
		return 1; // point is over line
	    }
	}

	/**
	 * test1
	 *
	 * */
	private boolean test1(double px, double py, double m,double b, double lx,double ly) {
	    return (test2(px,py, m,b) == test2(lx,ly,m,b));
	}

	/**
	 * containsPoint( p )
	 *
	 * Returns whether or not the Point p is contained within this triangle.
	 * */
	public boolean containsPoint (Point p) {
	    double x0 = p1.position().x();
	    double y0 = p1.position().y();
	    double x1 = p2.position().x();
	    double y1 = p2.position().y();
	    double x2 = p3.position().x();
	    double y2 = p3.position().y();

	    double px = p.getX();
	    double py = p.getY();

	    boolean line1, line2, line3;
	    // Getting y-intercepts and slopes for triangle sides
	    double m01 = (y1-y0)/(x1-x0);
	    double b01 = m01 * -x1 + y1;
	    double m02, m12, b02, b12;
	    m02 = (y2-y0)/(x2-x0);
	    m12 = (y2-y1)/(x2-x1);
	    b02 = m02 * -x2 + y2;
	    b12 = m12 * -x2 + y2;

	    // Check for a vertical line. Point must be on same side of the vertical
	    // line as the third point.
	    if( x1 == x0 ) {
		line1 = ( (px <= x0) == (x2 <= x0) );
	    } else {
		// Not vertical line. Make sure point is on the same side of the line with
		// specified slope and y-intercept as the given (x,y) coordinate (other point
		// on the triangle)
		line1 = test1( px, py, m01, b01, x2, y2 );
	    }

	    if( x1 == x2 ) {
		line2 = ( (px <= x2) == (x0 <= x2) );
	    } else {
		line2 = test1( px, py, m12, b12, x0, y0 );
	    }

	    if( x2 == x0 ) {
		line3 = ( (px <= x0 ) == (x1 <= x0) );
	    } else {
		line3 = test1( px, py, m02, b02, x1, y1);
	    }

	    return line1 && line2 && line3;
	}

	/**
	 * intersected()
	 *
	 * @return - true if there was recently an intersection on this triangle.
	 * */
	public boolean intersected() {
	    boolean ret = ! lastContainedPoints.equals( containedPoints );
	    return ret;
	}

	/**
	 * getContainedPoints()
	 *
	 * @return - The set of Points that give the positions of each of the Particles
	 * contained within this triangle.
	 * */
	public HashSet<Point> getContainedPoints() {
	    return containedPoints;
	}

	/**
	 * getIntersectingPoints()
	 *
	 * @return - A set giving all the points that have recently (since the last call)
	 * intersected this Triangle.
	 * */
	public HashSet<Point> getIntersectingPoints() {
	    HashSet<Point> intersectingPoints = null;

	    if ( containedPoints.size() > lastContainedPoints.size() ) {
		intersectingPoints = new HashSet<Point>( containedPoints );
		intersectingPoints.removeAll( lastContainedPoints );
	    } else {
		intersectingPoints = new HashSet<Point>( lastContainedPoints );
		intersectingPoints.removeAll( containedPoints );
	    }

	    lastContainedPoints = new HashSet<Point>( containedPoints );
	    return intersectingPoints;
	}

	/**
	 * getLongestEdge()
	 *
	 * Returns the length of the longest edge of this HTriangle.
	 * */
	public double getLongestEdge() {
	    double e1 = Math.hypot(p1.position().x() - p2.position().x(), p1.position().y() - p2.position().y());
	    double e2 = Math.hypot(p2.position().x() - p3.position().x(), p2.position().y() - p3.position().y());
	    double e3 = Math.hypot(p3.position().x() - p1.position().x(), p3.position().y() - p1.position().y());

	    return Math.max(Math.max(e1, e2), e3);
	}

	public ArrayList<Particle> getPoints() {
	    ArrayList<Particle> ptList = new ArrayList<Particle>();
	    ptList.add(p1);
	    ptList.add(p2);
	    ptList.add(p3);
	    return ptList;
	}

	// Returns the area of the triangle
	public double size() {
	    // find longest edge
	    double e1 = Math.hypot(p1.position().x() - p2.position().x(), p1.position().y() - p2.position().y());
	    double e2 = Math.hypot(p2.position().x() - p3.position().x(), p2.position().y() - p3.position().y());
	    double e3 = Math.hypot(p3.position().x() - p1.position().x(), p3.position().y() - p1.position().y());

	    double theEdge = 0;
	    Particle thePoint = null;

	    Particle ep1, ep2;
	    ep1 = ep2 = null;

	    if (Math.max(Math.max(e1, e2), e3) == e3) {
		theEdge = e1;
		ep1 = (p1.position().x() < p2.position().x()? p1 : p2);
		ep2 = (p1.position().x() >= p2.position().x()? p1 : p2);
		thePoint = p3;
	    }
	    else if (Math.max(Math.max(e1, e2), e3) == e2) {
		theEdge = e2;
		ep1 = (p3.position().x() < p2.position().x()? p3 : p2);
		ep2 = (p3.position().x() >= p2.position().x()? p3 : p2);
		thePoint = p1;
	    }
	    else if (Math.max(Math.max(e1, e2), e3) == e1) {
		theEdge = e3;
		ep1 = (p1.position().x() < p3.position().x()? p1 : p3);
		ep2 = (p1.position().x() >= p3.position().x()? p1 : p3);
		thePoint = p2;
	    }

	    // get slope
	    if (ep1.position().x() == ep2.position().x())
		return Math.abs(thePoint.position().y() - ep2.position().y());
	    if (ep1.position().y() == ep2.position().y())
		return Math.abs(thePoint.position().x() - ep2.position().x());

	    double slope = ((double)(ep1.position().y() - ep2.position().y()))/(ep1.position().x() - ep2.position().x());
	    double perpSlope = -1.0 / slope;

	    double eyi = ep1.position().y() - slope*ep1.position().y();
	    double myi = thePoint.position().y() - perpSlope*thePoint.position().x();

	    double xi = (eyi - myi)/((1 - (slope/perpSlope))*perpSlope);
	    double yi = slope*xi + eyi;

	    // distance from thePoint to theEdge:
	    double perpDist = Math.hypot(xi - thePoint.position().x(), yi - thePoint.position().y());
	    double area = (theEdge * perpDist)/2.0;
	    return area;
	}

	public boolean equals(HTriangle other) {
	    ArrayList<Particle> otherPts = other.getPoints();
	    ArrayList<Particle> ptList = getPoints();

	    Iterator<Particle> oi = otherPts.iterator();
	    while (oi.hasNext()) {
		Iterator<Particle> ti = ptList.iterator();
		while (ti.hasNext()) {
		    Particle p1 = oi.next();
		    Particle p2 = ti.next();
		    if (p1 == p2) {
			oi.remove();
			ti.remove();
		    }
		}
	    }

	    return ptList.isEmpty() && otherPts.isEmpty();
	}

	public void render() {
	    // increment age
	    age++;

	    int alpha = parent.constrain(100 - (int)((255.0/TRIANGLE_THRESH) * size()),
					 0,
					 255);
	    if (alpha < 0) alpha = 0;
	    if (alpha > 255) alpha = 255;

	    parent.stroke(233, 100);

	    if ( containedPoints.size() > 0 ) {
		parent.fill( lightColor.getRed(),
			     lightColor.getGreen(),
			     lightColor.getBlue(),
			     alpha );

		parent.colorMode( PApplet.HSB );
		// parent.stroke( 255, 255, 100 );
		parent.stroke( (containedPoints.size() * 16 + 32) % 256, 255, 255 );
		parent.strokeWeight( 2 );
		parent.colorMode( PApplet.RGB );
	    }
	    else {
		parent.fill( baseColor.getRed(),
			     baseColor.getGreen(),
			     baseColor.getBlue(),
			     alpha );
		parent.strokeWeight( 1 );
	    }

	    parent.triangle(p1.position().x(), p1.position().y(),
			    p2.position().x(), p2.position().y(),
			    p3.position().x(), p3.position().y());
	}

	public String toString() {
	    return "< ("+p1.position().x()+","+p1.position().y()+"), "+
		"("+p2.position().x()+","+p2.position().y()+"), "+
		"("+p3.position().x()+","+p3.position().y()+") >";
	}

    }

}