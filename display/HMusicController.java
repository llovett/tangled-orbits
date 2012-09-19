package display;

import java.awt.*;
import java.awt.event.*;
import javax.sound.midi.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import oscP5.*;
import netP5.*;

public class HMusicController extends Frame implements ItemListener, AdjustmentListener, Receiver {

    private HMusic parent;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 500;
    private static final int SCROLL_HEIGHT = 400;

    // OSC stuff
    OscP5 osc;
    public static final int OSC_PORT_RCV = 13000;
    public static final int OSC_PORT_SEND = 12500;	// Should be same as iPad
    public String ipadAddress = "10.0.1.8";	// So should this.


    // Presets
    private ArrayList<Float[]> presets;
    private int curPreset;

    ////////////////////
    // GUI COMPONENTS //
    ////////////////////
    private GridBagConstraints constraints;


    // Adjustment for StellaCaeli
    private Scrollbar caeliScroll;
    private Label caeliLabel;
    private Label caeliValueLabel;

    // Adjustment for Planet count
    private Scrollbar planetScroll;
    private Label planetLabel;
    private Label planetValueLabel;

    // Adjustment for Triangle size
    private Scrollbar triSizeScroll;
    private Label triSizeLabel;
    private Label triSizeValueLabel;

    private Scrollbar triFineSizeScroll;
    private Label triFineSizeLabel;
    private Label triFineSizeValueLabel;

    // Adjustment for particle attractions
    private Scrollbar attDistanceScroll;
    private Scrollbar attStrengthScroll;
    private Label attDistLabel;
    private Label attStrnLabel;
    private Label attDistValueLabel;
    private Label attStrnValueLabel;

    // MIDI STUFF GOES HERE //
    // Combo box for selecting a MIDI device to receive from
    Choice midiDevices;
    ArrayList<MidiDevice.Info> mdis;
    // Default MIDI device
    String defaultMidiDevice = "Akai MPD24 - ";	// will probably be something different
    // The MIDI device itself
    MidiDevice midi;
    // Things to be controlled by CC messages, in order of controler number [0 - ...]
    Scrollbar[] controllees;

    public HMusicController( HMusic parent ) {
	this.parent = parent;

	// Construct preset list
	presets = new ArrayList<Float[]>();
	curPreset = 0;

	// Get MIDI devices
	midiDevices = new Choice();
	mdis = new ArrayList<MidiDevice.Info>();
	for ( MidiDevice.Info mdi : MidiSystem.getMidiDeviceInfo() ) {
	    try {
		MidiDevice device = MidiSystem.getMidiDevice( mdi );
		if ( device.getMaxTransmitters() != 0 ) {
		    mdis.add( mdi );
		    midiDevices.add( mdi.getName() );
		}
	    } catch ( MidiUnavailableException e ) {
		// Nothing to do here. Just don't use the MIDI device!
	    }
	}
	midiDevices.select( defaultMidiDevice );
	midiDevices.addItemListener( this );

	// Try to grab a MIDI device
	try {
	    midi = MidiSystem.getMidiDevice( mdis.get( midiDevices.getSelectedIndex() ) );
	    midi.open();
	    midi.getTransmitter().setReceiver( this );
	} catch ( MidiUnavailableException e ) {
	    System.err.println( "Could not get MIDI device: "+midiDevices.getSelectedItem() );
	} catch ( SecurityException se ) {
	    System.err.println( "Could not get MIDI device: "+midiDevices.getSelectedItem() );
	}

	constructComponents();
	setDefaultValues();

	controllees = new Scrollbar[] { caeliScroll, planetScroll, triSizeScroll,
					triFineSizeScroll, attDistanceScroll, attStrengthScroll };

	// Initialize OSC
	osc = new OscP5( this, OSC_PORT_RCV );
    }

    public void constructComponents() {
	/////////////////////////////////
        // taking care of window setup //
        /////////////////////////////////

	setLayout(new GridBagLayout());
	setTitle("HMusicController");

	setResizable(false);

	setMinimumSize( new Dimension( WIDTH, HEIGHT ) );

	constraints = new GridBagConstraints();

	
	////////////////////////////////
        // adjustment for stellacaeli //
        ////////////////////////////////

	caeliLabel = new Label( "star density" );
	setConstraints( 0, 0,  1, 1 );
	add( caeliLabel, constraints );

	caeliScroll = new Scrollbar( Scrollbar.VERTICAL, 9, 1, 0, 10 );
	caeliScroll.setMinimumSize( new Dimension( 30, SCROLL_HEIGHT ) );
	caeliScroll.setPreferredSize( new Dimension( 30, SCROLL_HEIGHT ) );
	caeliScroll.addAdjustmentListener( this );
	setConstraints( 0, 1, 1, 1 );
	add( caeliScroll, constraints );

	caeliValueLabel = new Label( ""+((caeliScroll.getMaximum() - caeliScroll.getValue())/100.0) );
	setConstraints( 0, 2, 1, 1 );
	add( caeliValueLabel, constraints );

	
	/////////////////////////////////
        // adjustment for planet count //
        /////////////////////////////////

	planetLabel = new Label( "planet count" );
	setConstraints( 1, 0,  1, 1 );
	add( planetLabel, constraints );

	planetScroll = new Scrollbar( Scrollbar.VERTICAL, 9, 1, 0, 10 );
	planetScroll.setMinimumSize( new Dimension( 30, SCROLL_HEIGHT ) );
	planetScroll.setPreferredSize( new Dimension( 30, SCROLL_HEIGHT ) );
	planetScroll.addAdjustmentListener( this );
	setConstraints( 1, 1, 1, 1 );
	add( planetScroll, constraints );

	planetValueLabel = new Label( ""+(planetScroll.getMaximum() - planetScroll.getValue()));
	setConstraints( 1, 2, 1, 1 );
	add( planetValueLabel, constraints );


	////////////////////////////////////////
        // adjustment for triangle thresholds //
        ////////////////////////////////////////

	triSizeLabel = new Label( "coarse T size" );
	setConstraints( 2, 0, 1, 1 );
	add( triSizeLabel, constraints );

	triSizeScroll = new Scrollbar( Scrollbar.VERTICAL, 99000, 100, 100, 100000 );
	triSizeScroll.setMinimumSize( new Dimension( 30, SCROLL_HEIGHT ) );
	triSizeScroll.setPreferredSize( new Dimension( 30, SCROLL_HEIGHT ) );
	triSizeScroll.addAdjustmentListener( this );
	setConstraints( 2, 1, 1, 1 );
	add( triSizeScroll, constraints );

	triSizeValueLabel = new Label( ""+(triSizeScroll.getMaximum() - triSizeScroll.getValue()));
	setConstraints( 2, 2, 1, 1 );
	add( triSizeValueLabel, constraints );

	// ---------------------------------------- //

	triFineSizeLabel = new Label( "fine T size" );
	setConstraints( 3, 0,  1, 1 );
	add( triFineSizeLabel, constraints );

	triFineSizeScroll = new Scrollbar( Scrollbar.VERTICAL, 5000, 100, 0, 10000 );
	triFineSizeScroll.setMinimumSize( new Dimension( 30, SCROLL_HEIGHT ) );
	triFineSizeScroll.setPreferredSize( new Dimension( 30, SCROLL_HEIGHT ) );
	triFineSizeScroll.addAdjustmentListener( this );
	setConstraints( 3, 1, 1, 1 );
	add( triFineSizeScroll, constraints );

	triFineSizeValueLabel = new Label( ""+(triFineSizeScroll.getMaximum() - triFineSizeScroll.getValue()));
	setConstraints( 3, 2, 1, 1 );
	add( triFineSizeValueLabel, constraints );


	////////////////////////////////////////
        // adjustment for triangle thresholds //
        ////////////////////////////////////////

	attDistLabel = new Label( "att. dist." );
	setConstraints( 4, 0, 1, 1 );
	add( attDistLabel, constraints );

	attDistanceScroll = new Scrollbar( Scrollbar.VERTICAL, 3200, 100, 100, 4000 );
	attDistanceScroll.setMinimumSize( new Dimension( 30, SCROLL_HEIGHT ) );
	attDistanceScroll.setPreferredSize( new Dimension( 30, SCROLL_HEIGHT ) );
	attDistanceScroll.addAdjustmentListener( this );
	setConstraints( 4, 1, 1, 1 );
	add( attDistanceScroll, constraints );

	attDistValueLabel = new Label( ""+(attDistanceScroll.getMaximum() - attDistanceScroll.getValue()));
	setConstraints( 4, 2, 1, 1 );
	add( attDistValueLabel, constraints );

	// ---------------------------------------- //

	attStrnLabel = new Label( "att. strn." );
	setConstraints( 5, 0,  1, 1 );
	add( attStrnLabel, constraints );

	attStrengthScroll = new Scrollbar( Scrollbar.VERTICAL, 90000, 1000, 1000, 100000 );
	attStrengthScroll.setMinimumSize( new Dimension( 30, SCROLL_HEIGHT ) );
	attStrengthScroll.setPreferredSize( new Dimension( 30, SCROLL_HEIGHT ) );
	attStrengthScroll.addAdjustmentListener( this );
	setConstraints( 5, 1, 1, 1 );
	add( attStrengthScroll, constraints );

	attStrnValueLabel = new Label( ""+(attStrengthScroll.getMaximum() - attStrengthScroll.getValue()));
	setConstraints( 5, 2, 1, 1 );
	add( attStrnValueLabel, constraints );

	// ---------------------------------------- //

	// Add MIDI choice
	Label midiLabel = new Label("Select MIDI device");
	setConstraints( 0, 3, 2, 1 );
	add( midiLabel, constraints );
	setConstraints( 2, 3, 3, 1 );
	add( midiDevices, constraints );

	pack();
    }
    
    /**
     * setConstraints( x, y, width, height )
     *
     * A convenient private method for adding components to this Frame.
     * */
    private void setConstraints( int gx, int gy, int width, int height ) {
	constraints.gridx = gx;
	constraints.gridy = gy;
	constraints.gridwidth = width;
	constraints.gridheight = height;
	constraints.insets = new Insets( 10, 10, 10, 10 );
	constraints.fill = GridBagConstraints.HORIZONTAL;
    }

    /**
     * setDefaultValues()
     *
     * Takes the default values for the GUI components, and activates
     * them so that the values displayed initially actually correspond
     * to the real default values that HMusic is working with.
     * */
    private void setDefaultValues() {
	// Caeli scroll bar
	int scrollValue = caeliScroll.getMaximum() - caeliScroll.getValue();
	parent.setCaeliDensity( scrollValue / 100.0 );

	// Planet count scroll bar
	scrollValue = planetScroll.getMaximum() - planetScroll.getValue();
	parent.setPlanetCount( scrollValue );

	// Triangle size
	scrollValue = triSizeScroll.getMaximum() - triSizeScroll.getValue()
	    + triFineSizeScroll.getMaximum() - triFineSizeScroll.getValue();
	parent.setDustTrianglesThreshold( scrollValue );

	// Particle attractions
	int strnScrollValue = attStrengthScroll.getMaximum() - attStrengthScroll.getValue();
	int distScrollValue = attDistanceScroll.getMaximum() - attDistanceScroll.getValue();
	parent.setAttraction( distScrollValue, strnScrollValue );
    }
    
    public void openPresets( String presetFile ) {
	File f = null;
	Scanner s = null;
	try {
	    f = new File( presetFile );
	    s = new Scanner( f );
	} catch ( IOException e ) {
	    System.err.println( "Could not open file: "+presetFile );
	    return;
	}

	while ( s.hasNextLine() ) {
	    Scanner lineScanner = new Scanner( s.nextLine() );
	    Float[] presetVals = new Float[6];
	    for ( int i=0; i<presetVals.length; i++ ) {
		float val = 0.0f;
		try {
		    val = lineScanner.nextFloat();
		} catch ( Exception e ) {
		    System.err.println( "Invalid presets file: "+presetFile );
		    return;
		}
		presetVals[i] = val;
	    }
	    presets.add( presetVals );
	}
    }

    public void loadPreset( int which ) {
	if ( which < presets.size() && which >= 0 ) {
	    Float[] presetVals = presets.get( which );

	    adjustSlider( caeliScroll, presetVals[0] );
	    adjustSlider( planetScroll, presetVals[1] );
	    adjustSlider( triSizeScroll, presetVals[2] );
	    adjustSlider( triFineSizeScroll, presetVals[3] );
	    adjustSlider( attDistanceScroll, presetVals[4] );
	    adjustSlider( attStrengthScroll, presetVals[5] );

	}
	else if ( which < 0 )
	    curPreset = 0;
	else
	    curPreset = presets.size();
    }

    public void writePreset() {
	Float[] presetVals = new Float[6];
	presetVals[0] = sliderFloatValue( caeliScroll );
	presetVals[1] = sliderFloatValue( planetScroll );
	presetVals[2] = sliderFloatValue( triSizeScroll );
	presetVals[3] = sliderFloatValue( triFineSizeScroll );
	presetVals[4] = sliderFloatValue( attDistanceScroll );
	presetVals[5] = sliderFloatValue( attStrengthScroll );

	Float[] currPreset = presets.get( curPreset );
	currPreset = presetVals;
    }

    /**
     * handleControlMessage
     *
     * Handles a CC message by adjusting the appropriate slider on the interface.
     * */
    private void handleControlMessage( int controller, int value ) {
	if ( controller > controllees.length ) return;

	// Control numbers start at 1 on Akai MPD24?
	Scrollbar adjusted = controllees[ controller-1 ];
	// Scale the value
	int scaled = adjusted.getMaximum() -
	    (int)((double)( value * ( adjusted.getMaximum() - adjusted.getMinimum() ) )/127.0) +
	    adjusted.getMinimum();

	adjusted.setValue( scaled );

	refreshControls();
    }

    /**
     * refreshControls()
     *
     * Refreshes settings based on current values of the controls.
     * Gets around hairy coding of trying to send ActionEvents artifically when
     * we get MIDI messages.
     * */
    private void refreshControls() {
	// Scrollbars are upside-down from real sliders.
	int scrollValue = caeliScroll.getMaximum() - caeliScroll.getValue();
	caeliValueLabel.setText( ""+(scrollValue / 100.0 ) );
	parent.setCaeliDensity( scrollValue / 100.0 );

	// Scrollbars are upside-down from real sliders.
	scrollValue = planetScroll.getMaximum() - planetScroll.getValue();
	planetValueLabel.setText( ""+scrollValue );
	parent.setPlanetCount( scrollValue );

	// Scrollbars are upside-down from real sliders.
	scrollValue = triSizeScroll.getMaximum() - triSizeScroll.getValue()
	    + triFineSizeScroll.getMaximum() - triFineSizeScroll.getValue();
	    
	triSizeValueLabel.setText( ""+(triSizeScroll.getMaximum() - triSizeScroll.getValue()) );
	triFineSizeValueLabel.setText( ""+(triFineSizeScroll.getMaximum() - triFineSizeScroll.getValue()) );
	parent.setDustTrianglesThreshold( scrollValue );

	// Scrollbars are upside-down from real sliders.
	int strnScrollValue = attStrengthScroll.getMaximum() - attStrengthScroll.getValue();
	int distScrollValue = attDistanceScroll.getMaximum() - attDistanceScroll.getValue();

	attStrnValueLabel.setText( ""+strnScrollValue );
	attDistValueLabel.setText( ""+distScrollValue );

	parent.setAttraction( distScrollValue, strnScrollValue );


	// Refresh iPad display values
	osc.send( new OscMessage( "/faders", new Object[] { sliderFloatValue( caeliScroll ),
							    sliderFloatValue( planetScroll ),
							    sliderFloatValue( triSizeScroll ),
							    sliderFloatValue( triFineSizeScroll ),
							    sliderFloatValue( attDistanceScroll ),
							    sliderFloatValue( attStrengthScroll ) } ),
	    new NetAddress( ipadAddress, OSC_PORT_SEND ) );
    }

    ///////////////////////
    // LISTENER HANDLERS //
    ///////////////////////

    public void adjustmentValueChanged( AdjustmentEvent e ) {
	refreshControls();
    }

    /**
     * This is called when a new MidiDevice is selected.
     * */
    public void itemStateChanged( ItemEvent e ) {
	// Try to grab a MIDI device
	try {
	    midi = MidiSystem.getMidiDevice( mdis.get( midiDevices.getSelectedIndex() ) );
	    midi.open();
	    midi.getTransmitter().setReceiver( this );
	} catch ( MidiUnavailableException mue ) {
	    System.err.println( "Could not get MIDI device: "+midiDevices.getSelectedItem() );
	} catch ( SecurityException se ) {
	    System.err.println( "Could not get MIDI device: "+midiDevices.getSelectedItem() );
	}

    }

    private void adjustSlider( Scrollbar scroll, float value ) {
	scroll.setValue( (int)(scroll.getMaximum() - (value *
						      (scroll.getMaximum() - scroll.getMinimum()) +
						      scroll.getMinimum())) );
    }

    private float sliderFloatValue( Scrollbar scroll ) {
	return (float)(scroll.getMaximum() - scroll.getValue()) /
	    (float)(scroll.getMaximum() - scroll.getMinimum());
    }

    /**
     * oscEvent( OscMessage msg )
     *
     * This method gets called whenever we receive an OSC packet on OSC_PORT_RCV.
     * See documentation for the OscP5 library at:
     * http://www.sojamo.de/libraries/oscP5/reference/index.html
     * */
    public void oscEvent( OscMessage msg ) {
	if ( msg.checkAddrPattern( "/hmusicon" ) ) {
	    parent.turnon();
	}
	else if ( msg.checkAddrPattern( "/hmusicoff" ) ) {
	    parent.turnoff();
	}
	else if ( msg.checkAddrPattern( "/controlpos" ) ) {
	    int x = (int)((Float)msg.arguments()[0] * HMusic.WIDTH);
	    int y = (int)((Float)msg.arguments()[1] * HMusic.HEIGHT);
	    parent.setWandererLocation( x, y );
	}
	else if ( msg.addrPattern().startsWith( "/faders/" ) ) {
	    switch ( msg.addrPattern().substring(8).charAt(0) ) {
	    case '1':	// Star density
		{
		    float val = (Float)msg.arguments()[0];
		    adjustSlider( caeliScroll, val );
		    refreshControls();
		}
		break;
	    case '2':	// Num planets
		{
		    float val = (Float)msg.arguments()[0];
		    adjustSlider( planetScroll, val );
		    refreshControls();
		}
		break;
	    case '3':	// Coarse triangle size
		{
		    float val = (Float)msg.arguments()[0];
		    adjustSlider( triSizeScroll, val );
		    refreshControls();
		}
		break;
	    case '4':	// Fine triangle size
		{
		    float val = (Float)msg.arguments()[0];
		    adjustSlider( triFineSizeScroll, val );
		    refreshControls();
		}
		break;
	    case '5':	// Attraction distance
		{
		    float val = (Float)msg.arguments()[0];
		    adjustSlider( attDistanceScroll, val );
		    refreshControls();
		}
		break;
	    case '6':	// Attraction strength
		{
		    float val = (Float)msg.arguments()[0];
		    adjustSlider( attStrengthScroll, val );
		    refreshControls();
		}
		break;
	    default:
		break;
	    }
	}
	else if ( msg.checkAddrPattern( "/active" ) ) {
	    parent.setUserAttraction( ((Float)msg.arguments()[0]) == 1 );
	}
	else if ( msg.addrPattern().startsWith("/ipad") ) {
	    int conversion = (int)(1000 * (Float)msg.arguments()[0]);

	    // Check for preset
	    if ( msg.addrPattern().startsWith( "/ipad/presets" ) ) {
		switch ( msg.addrPattern().charAt( 14 ) ) {
		case '1':
		    /*
		     * The beginning: black triangles, sparsely populating the screen.
		     * Only a single planet, barely making any sound at all.
		     *
		     *
		     *
		     *
		     **/ 
		    /* old, but pretty and sonically pleasing, preset */
		    // adjustSlider( caeliScroll, 0.6f );
		    // adjustSlider( planetScroll, 0.0f );
		    // adjustSlider( triSizeScroll, 0.0f );
		    // adjustSlider( triFineSizeScroll, 0.2f );
		    // adjustSlider( attDistanceScroll, 0.2f );
		    // adjustSlider( attStrengthScroll, 0.2f );

		    adjustSlider( caeliScroll, 0.480000f );
		    adjustSlider( planetScroll, 0.050000f );
		    adjustSlider( triSizeScroll, 0.000500f );
		    adjustSlider( triFineSizeScroll, 0.200000f );
		    adjustSlider( attDistanceScroll, 0.225641f );
		    adjustSlider( attStrengthScroll, 0.210101f );


		    parent.setTriangulationColor( new Color( 0, 0, 0 ),
						  new Color( 10, 10, 10 ) );

		    refreshControls();
		    break;
		case '2':
		    /*
		     * Part 2 ------
		     * Red triangles, lower tones.
		     *
		     *
		     *
		     *
		     **/ 
		    adjustSlider( caeliScroll, 0.2f );
		    adjustSlider( planetScroll, 0.1f );
		    adjustSlider( triSizeScroll, 0.25f );
		    adjustSlider( triFineSizeScroll, 0.9f );
		    adjustSlider( attDistanceScroll, 0.2f );
		    adjustSlider( attStrengthScroll, 0.2f );

		    parent.setTriangulationColor( new Color( 0, 0, 0 ),
						  new Color( 255, 0, 0 ) );

		    refreshControls();
		    break;
		case '3':
		    adjustSlider( caeliScroll, 0.3f );
		    adjustSlider( planetScroll, 0.4f );
		    adjustSlider( triSizeScroll, 0.9f );
		    adjustSlider( triFineSizeScroll, 0.5f );
// 		    adjustSlider( attDistanceScroll, 0.2f );
// 		    adjustSlider( attStrengthScroll, 0.2f );

		    parent.setTriangulationColor( new Color( 0, 0, 0 ),
						  new Color( 50, 0, 50 ) );

		    refreshControls();
		    break;
		case '4':
		    adjustSlider( caeliScroll, 0.5f );
		    adjustSlider( planetScroll, 0.0f );
		    adjustSlider( triSizeScroll, 1.0f );
		    adjustSlider( triFineSizeScroll, 1.0f );
// 		    adjustSlider( attDistanceScroll, 0.2f );
// 		    adjustSlider( attStrengthScroll, 0.2f );

		    parent.setTriangulationColor( new Color( 0, 0, 0 ),
						  new Color( 0, 100, 100 ) );

		    refreshControls();
		    break;
		case '5':
		    adjustSlider( caeliScroll, 0.200000f );
		    adjustSlider( planetScroll, 0.050000f );
		    adjustSlider( triSizeScroll, 1.000000f );
		    adjustSlider( triFineSizeScroll, 1.000000f );
		    adjustSlider( attDistanceScroll, 0.838205f );
		    adjustSlider( attStrengthScroll, 0.816030f );

		    parent.setTriangulationColor( new Color( 10, 100, 100 ),
						  new Color( 100, 100, 255 ) );

		    refreshControls();
		    break;
		case '6':
		    adjustSlider( caeliScroll, 0.120000f );
		    adjustSlider( planetScroll, 0.050000f );
		    adjustSlider( triSizeScroll, 0.908399f );
		    adjustSlider( triFineSizeScroll, 0.010000f );
		    adjustSlider( attDistanceScroll, 0.235128f );
		    adjustSlider( attStrengthScroll, 0.188202f );

		    parent.setTriangulationColor( new Color( 255, 100, 0 ),
						  new Color( 255, 255, 0 ) );

		    refreshControls();
		    break;
		case '7':
		    break;
		case '8':
		    break;
		default:
		    break;
		}
	    }
	    // Check for note spacing/gating
	    else if ( msg.checkAddrPattern( "/ipad/maxcontrols/7" ) ) {
		// This packet carries a float in the range [0,1]
		// The current Max patch allows a note space up to 1 second (1000 ms)
		long howLongMs = (long)(1000 * (Float)msg.arguments()[0]);
		parent.setNoteSpace( howLongMs );
	    }

	    // Forward the message to Max.
	    parent.sendToMax( msg );
	}
	else if ( msg.checkAddrPattern("/1") || msg.checkAddrPattern("/2") ) {	// Messages from flipping pages
	    // Do nothing.
	}
	else {
	    System.out.println( "Unrecognized address pattern: "+msg.addrPattern() );
	    System.out.println( msg );
	}
    }

    public void sendToIpad( OscMessage msg ) {
	if ( msg.checkAddrPattern( "/toipad" ) ) {
	    OscMessage ipadMsg = new OscMessage("/ipad/maxcontrols", msg.arguments());
	    osc.send( ipadMsg, new NetAddress( ipadAddress, OSC_PORT_SEND ) );
	}
    }

    public void printPreset() {
	System.out.printf("adjustSlider( caeliScroll, %ff );\n"+
			  "adjustSlider( planetScroll, %ff );\n"+
			  "adjustSlider( triSizeScroll, %ff );\n"+
			  "adjustSlider( triFineSizeScroll, %ff );\n"+
			  "adjustSlider( attDistanceScroll, %ff );\n"+
			  "adjustSlider( attStrengthScroll, %ff );\n",
			  sliderFloatValue( caeliScroll ),
			  sliderFloatValue( planetScroll ),
			  sliderFloatValue( triSizeScroll ),
			  sliderFloatValue( triFineSizeScroll ),
			  sliderFloatValue( attDistanceScroll ),
			  sliderFloatValue( attStrengthScroll ) );
    }


    /**
     * This is called when we receive a MidiMessage from the selected
     * MIDI device.
     * */
    public void send( MidiMessage message, long timeStamp ) {
	// Only accept CC messages (which are short messages)
	if ( message instanceof ShortMessage ) {
	    ShortMessage shortMessage = (ShortMessage)message;
	    // CC
	    if ( shortMessage.getCommand() == 0xb0 ) {
		handleControlMessage( shortMessage.getData1(), shortMessage.getData2() );
	    }
	}
    }

    /**
     * required by the Receiver interface.
     */
    public void close() {
	try {
	    midi.getTransmitter().close();
	    midi.close();
	} catch ( MidiUnavailableException e ) {
	    // Nothing.
	}
    }
}