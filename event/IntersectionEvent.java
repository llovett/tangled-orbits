package event;

import java.awt.Point;
import display.Triangulation;

public class IntersectionEvent {
    
    private Point p;
    private Triangulation.HTriangle tri;

    public IntersectionEvent( Triangulation.HTriangle tri, int x, int y ) {
	this.tri = tri;
	p = new Point( x, y );
    }

    public IntersectionEvent( Triangulation.HTriangle tri, Point p ) {
	this.tri = tri;
	this.p = p;
    }

    public Point getPoint() {
	return p;
    }

    public Triangulation.HTriangle getTriangle() {
	return tri;
    }

    public String toString() {
	return p.toString();
    }

}