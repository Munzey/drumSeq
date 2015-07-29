package drumseq;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jaudiolibs.beads.AudioServerIO;
import org.jaudiolibs.jnajack.JackException;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PShape;
import controlP5.*;
import beads.*;


/**
 * will we need a separate audiocontext for each drum if we want to output on separate channels?
 * add logger
 * note: pfont doest seem to work very well, sticking with the pixelfont for now
 * how custom tabs works is quite hacky, maybe we should switch to using cp5's snapshot/properties methods instead
 * fix: rectangle color problem, static final colors, tab issue using keyboard, knob valuelabel change using ctrl
 * ideas: keyboard, mute, clear, metronome, visuals,draw envelopes, save presets, tap tempo, record
 * for visuals look at controlp5 frame example
 * sculpt3dproject: how can the parameters be affected in a 2d plane before we look at 3d
 * @author Tristan Hamilton 
 * https://github.com/Munzey
 *
 */
public class DrumSeq extends PApplet {
  //processing stuff
  private final int myColorBackground = color(30,30,30);
  private PFont font;
  private PShape play;
  private PShape pause;
  private PShape playPause;
  private PShape k,s,h,t;
  private int kickFontColor = color(255);
  private int snareFontColor = color(255);
  private int hihatFontColor = color(255);
  private int tomFontColor = color(255);
  private int[] fontColors = {kickFontColor, snareFontColor, hihatFontColor, tomFontColor};
  private HashMap<Integer,Integer> colorMap = new HashMap<Integer,Integer>();
  
  //cp5 stuff  
  private ControlP5 cp5;
  private Tab pitchTab;
  private Tab gateTab;
  private Tab customTab;
  private boolean custTabOn;
  private List<Tab> tabs;
  private int[] colorSteps;
  private Toggle[] steps;
  private Toggle playPauseTog;
  private Button[] buttons;
  private Knob[][] kickKnobs;
  private Knob[][] snareKnobs;
  private Knob[][] hihatKnobs;
  private Knob[][] tomKnobs;
  private List<Knob[][]> knobContainer;
  private String[] drumHits= { "kick", "snare", "hihat", "tom" };
  private String[] params= { "amp", "pitch", "gate" }; //tab ids correspond to array indexes
  private Character[] groups = {'K', 'S', 'H', 'T'};
  private int currentTab = 0;
  private int previousTab;
  private int prevButEvent;
  private int lastOnSideButton; //last known side button to be on, MAY NOT BE ON
  private HashMap<Integer,Integer> custMap = new HashMap<Integer,Integer>(); 
  
  //beads stuff
  private Clock clock;
  private AudioContext ac;
  private Glide tempoGl, gainGl;
  private Glide[] kickPitchGlides = new Glide[16];
  private WavePlayer[] kwp = new WavePlayer[16];
  private Glide[] kGainGl = new Glide[16];
  private ScalingMixer sc;
  private Gain masterGain;
  private int tempo = 2000; //default tempo 120
  private float masterVol = 0.025f;
  private float[] kAmp = {0.25f,0,0,0,0.25f,0,0,0,0.25f,0,0,0,0.25f,0,0,0};
  private float[] kGate = {100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100};
  private BetterBeadArray delayTriggers;

    public void settings() {
      size(1138, 384, P2D);
      smooth(8);
    }
    
	public void setup() {
      //size((int)(displayWidth/1.2), displayHeight/2, P2D);
      //smooth(8);
	  //noStroke();

	  initialiseCP5GUI();
	  drawLetters(); 
	  initialiseAudio();
	  adjustKnobRanges();
  }

	private void adjustKnobRanges() {
    // change pitch knob range + change amp knob range
	  int j =0;
	  for(Knob[][] knobs: knobContainer) { 
	    for(int i = 0; i < knobs[1].length ; i ++) {
	      // pitch range probably no point allowing higher than a B7
	      knobs[1][i].setRange(21, 108).setNumberOfTickMarks(87).snapToTickMarks(true).showTickMarks(true).setDefaultValue(60).setValue(60).setValueLabel(Integer.toString(i+1));
	      knobs[0][i].setRange(0, .25f).setDefaultValue(kAmp[i]).setValue(kAmp[i]).setValueLabel(Integer.toString(i+1));
	      // limit for gate? 2 seconds? need longer?
	      knobs[2][i].setRange(12, 2000).setDefaultValue(kGate[i]).setValue(kGate[i]).setValueLabel(Integer.toString(i+1));
	    }
	    //j++;
	  }
    
  }

  private void initialiseCP5GUI() {
	  /*
     * CP5
     */
    
    cp5 = new ControlP5(this);
    cp5.setAutoDraw(false);
    //font = createFont("HelveticaNeue-Thin.vlw", 8, true);
    //cp5.setControlFont(font,8);
    
    /*
     * TABS
     */
    tabs = new ArrayList<Tab>();
    
    pitchTab = cp5.addTab("pitch")
                  .setWidth(displayWidth/50)
                  .setHeight(displayHeight/50)
                  .setId(1)
                  .setColorBackground(color(93, 84, 84))
                  .setColorLabel(color(255))
                  .setColorActive(color(237,56,56))
                  .activateEvent(true);
    tabs.add(pitchTab);
    
    gateTab = cp5.addTab("gate")
                  .setWidth(displayWidth/50)
                  .setHeight(displayHeight/50)
                  .setId(2)
                  .setColorBackground(color(93, 84, 84))
                  .setColorLabel(color(255))
                  .setColorActive(color(237,56,56))
                  .activateEvent(true);
    tabs.add(gateTab);
    
    cp5.getTab("default")
       .setWidth(displayWidth/50)
       .setHeight(displayHeight/50)
       .setLabel("amp")
       .setId(0)
       .setColorBackground(color(93, 84, 84))
       .setColorLabel(color(255))
       .setColorActive(color(237,56,56))
       .activateEvent(true);
    tabs.add(0, cp5.getTab("default"));
    
    /*
     * STEP TOGGLES
     */
    //create step toggles + rectangles
    steps = new Toggle[16];
    colorSteps = new int[16];
    for (int ii =0; ii < steps.length; ii++) {
      if (ii == 0) {
        colorSteps[ii] = color(113,110,110);
      }
      else if (ii % 4 == 0) {
        colorSteps[ii] = color(113,110,110);
      }
      else {
        colorSteps[ii] = color(42,42,42);
      }
      steps[ii] = cp5.addToggle("step" + ii)
                     .hide()
                     .setId(ii)
                     .addListener(new SwitchListener(this));
    }
    //play/pause toggle
    playPauseTog = cp5.addToggle("play/pause")
       .setId(-1)
       .setState(false)
       .setTab("global")
       .setPosition(0,(int)(displayHeight/2.6))
       .setCaptionLabel("")
       .setColorActive(color(30,30,30))
       .setColorBackground(color(30,30,30))
       .setColorActive(color(30,30,30))
       .setColorForeground(color(30,30,30))
       .setSize(displayWidth/25, displayHeight/10)
       .addListener(new SwitchListener(this))
       ;
       
    
    /*
     * GLOBAL SIDE BUTTONS (K, S, H, T)
     */
    int bheight = displayHeight/28;
    int z =0;
    buttons = new Button[drumHits.length];
    for (String drum:drumHits) {
      buttons[z] = cp5.addButton(drum)
       .setId(z)
       .setSwitch(true)
       .setValue(10)
       .setPosition(0,bheight)
       .setSize(displayWidth/31,(int)(displayHeight/13.5))
       //.setColorBackground(color(175,174,174))
       .setColorBackground(color(30,30,30))
       .setCaptionLabel("")
       .setColorForeground(color(30,30,30))
       .setColorActive(color(30,30,30))
       .addListener(new SideButtonListener(this));
      bheight += displayHeight/11;
      //move button to global tab
      cp5.getController(drum).moveTo("global");
      z++;
    }
    /*
     * CREATE PARAM KNOBS
     */
    knobContainer = new ArrayList<Knob[][]>();
    //KICK
    int x = displayWidth/25; //x axis button position
    int y = displayHeight/27; //y axis button position
    int radius = displayHeight/35;
    kickKnobs = new Knob[3][16];
    for(int i =0 ; i < params.length; i++) {
      x = displayWidth/25;
      for(int j = 0 ; j < kickKnobs[i].length; j++) {
        String cntrl = "K" + params[i] + j;
        kickKnobs[i][j] = cp5.addKnob(cntrl)
                   .setRange(0,100)
                   //.setAngleRange(6.3) //sets how far the tick marks go
                   //.setShowAngleRange(false)
                   .setValue(0)
                   .setPosition(x,y)
                   .setRadius(radius) 
                   //.setNumberOfTickMarks(20) //add this later as an option when holding ctrl
                   //.snapToTickMarks(true)
                   //.showTickMarks(false)
                   .setColorForeground(color(200))
                   //.setColorBackground(color(0, 160, 100))
                   //.setColorBackground(color(92, 190, 153))
                   .setColorBackground(color(30,30,30))
                   .setColorActive(color(80,79,76))
                   .setViewStyle(3)
                   .setTickMarkWeight(0)
                   .setTickMarkLength(15) 
                   
                   .setCaptionLabel(params[i])
                   .setValueLabel(Integer.toString(j+1))
                   .setId(j)
                   //.scrolled(10) //check in source if its using the old awt mouse scroll
                   .setDragDirection(Knob.VERTICAL)
                   ;
        //add knob listener
        kickKnobs[i][j].addListener(new KnobListener(this));
        //add knob callback listener
        kickKnobs[i][j].addCallback(new ReleaseKnobListener(this));
        x += displayWidth/20;
        //move knob to specific tab
        if(i !=0) { //tab default has diff label in our case
          cp5.getController(cntrl).moveTo(params[i]);
        }
      }
    }
    knobContainer.add(kickKnobs);
    
    //SNARE
    x = displayWidth/25; //x axis button position
    y += displayHeight/11;
    snareKnobs = new Knob[3][16];
     for(int i =0 ; i < params.length; i++) {
        x = displayWidth/25;
        for(int j = 0 ; j < snareKnobs[0].length; j++) {
          String cntrl = "S" + params[i] + j;
          snareKnobs[i][j] = cp5.addKnob(cntrl)
                 .setRange(0,100)
                 .setValue(0)
                 .setPosition(x,y)
                 .setRadius(radius)
                 .showTickMarks(false) 
                 .setColorForeground(color(200))
                 //.setColorBackground(color(214, 105, 192))
                 //.setColorBackground(color(218, 125, 156))
                 .setColorBackground(color(30,30,30))
                 .setColorActive(color(80,79,76))
                 .setViewStyle(3)
                 .setTickMarkWeight(0)
                 .setTickMarkLength(15) 
                 .bringToFront()
                 .setCaptionLabel(params[i])
                 .setValueLabel(Integer.toString(j+1))
                 .setId(j)
                 .setDragDirection(Knob.VERTICAL)
                 ;
          //add knob listener
          snareKnobs[i][j].addListener(new KnobListener(this));
          //add knob callback listener
          snareKnobs[i][j].addCallback(new ReleaseKnobListener(this));
          x += displayWidth/20;
          //move knob to specific tab
          if(i !=0) { //tab default has diff label in our case
            cp5.getController(cntrl).moveTo(params[i]);
          }
        }
     }
     knobContainer.add(snareKnobs);
     
    //HIHAT
    x = displayWidth/25; //x axis button position
    //y = (int)(displayHeight/4.6);
    y += displayHeight/11;
    hihatKnobs = new Knob[3][16];
    for(int i =0 ; i < params.length; i++) {
      x = displayWidth/25;
      for(int j = 0 ; j < hihatKnobs[0].length; j++) {
        String cntrl = "H" + params[i] + j;
        hihatKnobs[i][j] = cp5.addKnob(cntrl)
                 .setRange(0,100)
                 .setValue(0)
                 .setPosition(x,y)
                 .setRadius(radius)
                 .showTickMarks(false) 
                 .setColorForeground(color(200))
                 //.setColorBackground(color(61, 171, 217))
                 //.setColorBackground(color(117, 184, 201))
                 .setColorBackground(color(30,30,30))
                 .setColorActive(color(80,79,76))
                 .setViewStyle(3)
                 .setTickMarkWeight(0)
                 .setTickMarkLength(15) 
                 .bringToFront()
                 .setCaptionLabel(params[i])
                 .setValueLabel(Integer.toString(j+1))
                 .setId(j)
                 .setDragDirection(Knob.VERTICAL)
                 ;
        //add knob listener
        hihatKnobs[i][j].addListener(new KnobListener(this));
        //add knob callback listener
        hihatKnobs[i][j].addCallback(new ReleaseKnobListener(this));
        x += displayWidth/20;
        //move knob to specific tab
        if(i !=0) { //tab default has diff label in our case
          cp5.getController(cntrl).moveTo(params[i]);
        }
      }
    }
    knobContainer.add(hihatKnobs);
    
    //TOM
    x = displayWidth/25; //x axis button position
    //y = (int)(displayHeight/3.2);
    y += displayHeight/11;
    tomKnobs = new Knob[3][16];
    for(int i =0 ; i < params.length; i++) {
      x = displayWidth/25;
      for(int j = 0 ; j < tomKnobs[0].length; j++) {
        String cntrl = "T" + params[i] + j;
        tomKnobs[i][j] = cp5.addKnob(cntrl)
                 .setRange(0,100)
                 .setValue(0)
                 .setPosition(x,y)
                 .setRadius(radius)
                 .showTickMarks(false) 
                 .setColorForeground(color(200))
                 //.setColorBackground(color(246, 63, 72))
                 //.setColorBackground(color(212, 177, 70))
                 .setColorBackground(color(30,30,30))
                 .setColorActive(color(80,79,76))
                 .setViewStyle(3)
                 .setTickMarkWeight(0)
                 .setTickMarkLength(15) 
                 .bringToFront()
                 .setCaptionLabel(params[i])
                 .setValueLabel(Integer.toString(j+1))
                 .setId(j)
                 .setDragDirection(Knob.VERTICAL)
                 ;
        //add knob listener
        tomKnobs[i][j].addListener(new KnobListener(this));
        //add knob callback listener
        tomKnobs[i][j].addCallback(new ReleaseKnobListener(this));
        x += displayWidth / 20;
        //move knob to specific tab
        if (i != 0) { //tab default has diff label in our case
          cp5.getController(cntrl).moveTo(params[i]);
        }
      }
    }
    knobContainer.add(tomKnobs);
    
    /*
     * CREATE GLOBAL SETTINGS
     */
    //tempo
    Knob tempoKnob = cp5.addKnob("time")
        .setRange((int)50,(int)250) //tempo min/max is there a standard?
        .setValue(120)
        .setPosition(displayWidth/25,(int)(displayHeight/2.5))
        .setRadius(displayHeight/30)
        .showTickMarks(false) 
        .setColorForeground(color(255))
        .setColorBackground(color(246, 63, 72))
        .setColorActive(color(243,122,144))
        .setViewStyle(2)
        .setTickMarkWeight(0)
        .setTickMarkLength(0) 
        .setNumberOfTickMarks(200) //add this later as an option when holding ctrl
        .snapToTickMarks(true)
        .showTickMarks(false)
        .bringToFront()
        .setId(-1)
        .setCaptionLabel("tempo")
        .setDragDirection(Knob.VERTICAL)
        .moveTo("global")
        .addListener(new KnobListener(this))
        ;
    
    //master volume
    Knob volKnob = cp5.addKnob("volume")
        .setRange((int)0,(float) 0.25) //tempo min/max is there a standard?
        .setValue((float).025)
        .setPosition((int)(displayWidth/1.3),(int)(displayHeight/2.5))
        .setRadius(displayHeight/30)
        .showTickMarks(false) 
        .setColorForeground(color(255))
        .setColorBackground(color(246, 63, 72))
        .setColorActive(color(243,122,144))
        .setColorValueLabel(color(246, 63, 72))
        .setViewStyle(2)
        .setTickMarkWeight(0)
        .setTickMarkLength(0) 
        .setNumberOfTickMarks(100) //add this later as an option when holding ctrl
        .snapToTickMarks(true)
        .showTickMarks(false)
        .bringToFront()
        .setId(-2)
        .setCaptionLabel("volume")
        .setDragDirection(Knob.VERTICAL)
        .moveTo("global")
        .addListener(new KnobListener(this))
        ;
    
  }

  private void drawLetters() {
	  /*
     * processing shapes
     */
    play = createShape();
    play.beginShape();
    play.strokeWeight((float)1.5);
    //play.strokeJoin(ROUND);
    play.noFill();
    play.stroke(255);
    play.vertex(displayWidth/95, (int)(displayHeight/2.5));
    play.vertex(displayWidth/28, (int)(displayHeight/2.3));
    play.vertex(displayWidth/95, (int)(displayHeight/2.1));
    play.endShape();
    
    pause = createShape();
    pause.beginShape(LINES);
    //pause.noFill();
    pause.strokeWeight((float)1.5);
    //pause.strokeJoin(ROUND);
    pause.stroke(255);
    pause.vertex(displayWidth/40, (int)(displayHeight/2.5));
    pause.vertex(displayWidth/40, (int)(displayHeight/2.1));
    pause.vertex(displayWidth/92, (int)(displayHeight/2.5));
    pause.vertex(displayWidth/92, (int)(displayHeight/2.1));
    pause.endShape();
    
    k = createShape(GROUP);
    PShape k1 = createShape();
    k1.beginShape();
    k1.noFill();
    k1.stroke(kickFontColor);
    k1.strokeWeight((float)1.5);
    k1.strokeJoin(ROUND);
    k1.vertex(displayWidth/45, (int)(displayHeight/22));
    k1.vertex(displayWidth/95, (int)(displayHeight/15));
    k1.vertex(displayWidth/35, (int)(displayHeight/10));
    k1.endShape();
    PShape k2 = createShape();
    k2.beginShape(LINES);
    k2.noFill();
    k2.stroke(kickFontColor);
    k2.strokeWeight((float)1.5);
    k2.strokeJoin(ROUND);
    k2.vertex(displayWidth/95, (int)(displayHeight/25));
    k2.vertex(displayWidth/95, (int)(displayHeight/10));
    k2.endShape();
    k.addChild(k1);
    k.addChild(k2);
    colorMap.put(0, color(92, 190, 153));
    
    s = createShape(GROUP);
    PShape s1 = createShape(ARC, (float)(displayWidth/96), (float)(displayHeight/6.5), displayWidth/37, displayHeight/8, 0, PI+(QUARTER_PI), OPEN); 
    s1.setStroke(true);
    s1.setStroke(snareFontColor);
    s1.setStrokeWeight((float)1.5);
    s1.setFill(false);
    s1.setStrokeJoin(ROUND);
    PShape s2 = createShape(ARC, (float)(displayWidth/110), (float)(displayHeight/6.4), displayWidth/49, displayHeight/29, 0-(QUARTER_PI + QUARTER_PI/2), PI);
    s2.setStroke(true);
    s2.setStroke(snareFontColor);
    s2.setStrokeWeight((float)1.5);
    s2.setFill(false);
    s2.setStrokeJoin(ROUND);
    PShape s3 = createShape();
    s3.beginShape(LINES);
    s3.noFill();
    s3.stroke(snareFontColor);
    s3.strokeWeight((float)1.5);
    s3.strokeJoin(ROUND);
    s3.vertex(displayWidth/78, (int)(displayHeight/6.65));
    s3.vertex(displayWidth/42, (int)(displayHeight/6.25));
    s3.endShape();
    s.addChild(s1);
    s.addChild(s3);
    s.addChild(s2);
    colorMap.put(1, color(218, 125, 156));
    
    h = createShape();
    h.beginShape(LINES);
    h.noFill();
    h.stroke(hihatFontColor);
    h.strokeWeight((float)1.5);
    h.strokeJoin(ROUND);
    h.vertex(displayWidth/93, (int)(displayHeight/4.65));
    h.vertex(displayWidth/93, (int)(displayHeight/3.6));
    h.vertex(displayWidth/93, (int)(displayHeight/3.9));
    h.vertex(displayWidth/36, (int)(displayHeight/3.9));
    h.vertex(displayWidth/36, (int)(displayHeight/4.65));
    h.vertex(displayWidth/36, (int)(displayHeight/3.6));
    h.endShape();
    colorMap.put(2, color(117, 184, 201));
    
    t = createShape();
    t.beginShape(LINES);
    t.noFill();
    t.stroke(tomFontColor);
    t.strokeWeight((float)1.5);
    t.strokeJoin(ROUND);
    t.vertex(displayWidth/93, (int)(displayHeight/3.25));
    t.vertex(displayWidth/36, (int)(displayHeight/3.25));
    t.vertex((int)(displayWidth/52.5), (int)(displayHeight/3.25));
    t.vertex((int)(displayWidth/52.5), (int)(displayHeight/2.7));
    t.endShape();
    colorMap.put(3, color(212, 177, 70));
    
    playPause = play;
    
  }

  private void initialiseAudio() {
	  //with JACK server running
	  //try {
	    //remember: the values need to match with the JACK server instance
	    //ac = new AudioContext(new AudioServerIO.Jack(),1024,AudioContext.defaultAudioFormat(0,0));
	    //ac.start();
	  //javaSound
	  //} catch (Exception jackEx) {
	    ac = new AudioContext(new JavaSoundAudioIO(), 512);
	  //}
	    	    
    sc = new ScalingMixer(ac);
    
    //BeadArray of delayTriggers
    delayTriggers = new BetterBeadArray();
    for (int y = 0; y < 16; y++) {
      KGateTrigger kgate = new KGateTrigger(this, y);
      //delay should never be smaller than 12 ms to be audible
      delayTriggers.add(new DelayTrigger(ac, kGate[y], kgate, kgate));
    }
    
    // clock
    tempoGl = new Glide(ac, tempo, 50);
    clock = new Clock(ac, tempoGl);
    //cl.setClick(true);
    StepClockTrigger stepClock = new StepClockTrigger(this);
    KickClockTrigger kickClock = new KickClockTrigger(this);
    KGateClockTrigger kGateClock = new KGateClockTrigger(this, ac);
    clock.addMessageListener(stepClock);
    clock.addMessageListener(kickClock);
    ac.out.addDependent(clock);
    ac.out.addDependent(kGateClock);
    
    
    // oscillators/waveplayers
    //TODO add a gain to each kwp and then route them all to the master gain
    for (int i =0; i<16; i++){
      // could we create a mode to glide from the previous note up?
      kickPitchGlides[i] = new Glide(ac, Pitch.mtof(60), 50);
      kwp[i] = new WavePlayer(ac, kickPitchGlides[i], Buffer.SQUARE);
      kwp[i].pause(true);
      kGainGl[i] = new Glide(ac, kAmp[i] , 50);
      Gain temp = new Gain(ac, 1, kGainGl[i]);
      temp.addInput(kwp[i]);
      sc.addInput(temp);
    }
    //Master-gain
    gainGl = new Glide(ac, masterVol, 50);
    masterGain = new Gain(ac, 1, gainGl);
    
    masterGain.addInput(sc);
    ac.out.addInput(masterGain);
    ac.stop();
    
  }

  public void draw() {
	  background(myColorBackground);
	  //rectangle steps
	  int x = displayWidth/28;
	  for(int a = 0; a <colorSteps.length; a++){
	    stroke(colorSteps[a]);
	    //fill(color(45,45,45));
	    fill(color(30,30,30));
	    rect(x,(int)(displayHeight/31),displayWidth/25,(int)(displayHeight/2.86), 5);
	    x += displayWidth/20;
	  }
	  //global bottom box
	  noStroke();
	  noFill();
    stroke(color(44,62,57));
    rect(0, (int)(displayHeight/2.6), (int)(displayWidth/1.21), displayHeight/9, 5);
    
    //knob outlines
    float elx = (float)(displayWidth/25.95);
    int ely = displayHeight/29;
    for(int b = 0; b <kickKnobs[0].length; b++) {
      ellipseMode(CORNER);
      stroke(color(92, 190, 153));
      ellipse(elx, ely, (int)(displayHeight/17), (int)(displayHeight/17));
      elx += displayWidth/20;
    }
    elx = (float)(displayWidth/25.95);
    ely = (int)(displayHeight/8.05);
    for(int b = 0; b <snareKnobs[0].length; b++) {
      ellipseMode(CORNER);
      stroke(color(218, 125, 156));
      ellipse(elx, ely, (int)(displayHeight/17), (int)(displayHeight/17));
      elx += displayWidth/20;
    }
    elx = (float)(displayWidth/25.95);
    ely = (int)(displayHeight/4.67);
    for(int b = 0; b <hihatKnobs[0].length; b++) {
      ellipseMode(CORNER);
      stroke(color(117, 184, 201));
      ellipse(elx, ely, (int)(displayHeight/17), (int)(displayHeight/17));
      elx += displayWidth/20;
    }
    elx = (float)(displayWidth/25.95);
    ely = (int)(displayHeight/3.29);
    for(int b = 0; b <tomKnobs[0].length; b++) {
      ellipseMode(CORNER);
      stroke(color(212, 177, 70));
      ellipse(elx, ely, (int)(displayHeight/17), (int)(displayHeight/17));
      elx += displayWidth/20;
    }
    cp5.draw();
    shape(playPause);
    k.setStroke(fontColors[0]);
    shape(k);
    s.setStroke(fontColors[1]);
    shape(s);
    h.setStroke(fontColors[2]);
    shape(h);
    t.setStroke(fontColors[3]);
    shape(t);
	}
	
  public void keyPressed() { //would be better as a switch statement?
    if (keyCode == CONTROL) {
      for (int i = 0; i < kickKnobs[currentTab].length; i++) {
        if (kickKnobs[currentTab][i].isMouseOver()) {
          kickKnobs[currentTab][i].setNumberOfTickMarks(20)
              .snapToTickMarks(true).showTickMarks(false);
        }
        if (snareKnobs[currentTab][i].isMouseOver()) {
          snareKnobs[currentTab][i].setNumberOfTickMarks(20).snapToTickMarks(true)
              .showTickMarks(false);
        }
        if (hihatKnobs[currentTab][i].isMouseOver()) {
          hihatKnobs[currentTab][i].setNumberOfTickMarks(20).snapToTickMarks(true)
              .showTickMarks(false);
        }
        if (tomKnobs[currentTab][i].isMouseOver()) {
          tomKnobs[currentTab][i].setNumberOfTickMarks(20).snapToTickMarks(true)
              .showTickMarks(false);
        }
      }
    } else if (keyCode == KeyEvent.VK_1) {
      if(sideButtonIsOn()) {
        cp5.getTab("default").mousePressed();
      }
      else {
        cp5.getTab("default").bringToFront();
      }

    } else if (keyCode == KeyEvent.VK_2) {
      if(sideButtonIsOn()) {
        cp5.getTab("pitch").mousePressed();
      }
      else{
        cp5.getTab("pitch").bringToFront();
      }

    } else if (keyCode == KeyEvent.VK_3) {
      if(sideButtonIsOn()) {
        cp5.getTab("gate").mousePressed();
      }
      else{
        cp5.getTab("gate").bringToFront();
      }
    } else if (keyCode == KeyEvent.VK_4) {
      if(custTabOn){
        if(sideButtonIsOn()) {
          cp5.getTab("cust").mousePressed();
        }
        else{
          cp5.getTab("cust").bringToFront();
        }
      }
    }
    
  }
	
	
	public void keyReleased() {
	  if(keyCode==CONTROL) {
	    for(int i = 0 ; i < kickKnobs.length; i++) {
	      kickKnobs[currentTab][i].snapToTickMarks(false)
	                  .showTickMarks(false);
	      snareKnobs[currentTab][i].snapToTickMarks(false)
	                  .showTickMarks(false);
	      hihatKnobs[currentTab][i].snapToTickMarks(false)
	                  .showTickMarks(false);
	      tomKnobs[currentTab][i].snapToTickMarks(false)
	                  .showTickMarks(false);            
	    }
	  }
	}
	
	//currently active only on tab objects
	// basically some complicated logic to allow a custom tab to be created
	public void controlEvent(ControlEvent theControlEvent) {
	  if (theControlEvent.isTab()) {
	    previousTab = currentTab;
	    currentTab = theControlEvent.getTab().getId();
	    System.out.println("currentTab:" + theControlEvent.getTab().getId());
	    
	    if(sideButtonIsOn() && previousTab == 3) { //if were already on the custom tab
	      if(previousTab != currentTab && previousTab != knobContainer.get(lastOnSideButton)[currentTab][0].getTab().getId()) {
	        //swap target tab row out for current
	        for (int i = 0; i<knobContainer.get(lastOnSideButton)[currentTab].length ; i++){
	          cp5.getController(knobContainer.get(lastOnSideButton)[currentTab][i].getName()).moveTo("cust");
	        }
	        //we need to know which param the swapping out knobs belong to.
	        for (Knob k: knobContainer.get(lastOnSideButton)[custMap.get(lastOnSideButton)] ){
	          k.moveTo(tabs.get(custMap.get(lastOnSideButton)));
	        }
	        //update custMap
	        custMap.put(lastOnSideButton, currentTab);
	      }
	      customTab.bringToFront();
	      currentTab = 3;
	    }
	    
	    else if (sideButtonIsOn() && (previousTab != currentTab) && (currentTab != 3)) {
	      //create "custom" tab
	      //move that knob-rows target tab knobs to custom
	      //move rest of knob-rows' current tab(which is referred to as previousTab) knobs to custom
	      custTabOn = true;
	      List<Integer> sideBut = new ArrayList<Integer>();
	      for (int j = 0 ; j < drumHits.length; j++) {
	        if (j != lastOnSideButton) {
	          sideBut.add(j);
	        }
	      }
	      customTab = cp5.addTab("cust")
            .setWidth(displayWidth/50)
            .setHeight(displayHeight/50)
            .setId(3)
            .bringToFront()
            .setColorBackground(color(93, 84, 84))
            .setColorLabel(color(255))
            .setColorActive(color(254,240,38))
            .setColorLabel(color(3,3,3))
	          .activateEvent(true);
	      //move target knobs to custom
	      for (int i = 0; i<knobContainer.get(lastOnSideButton)[currentTab].length ; i++){
	        cp5.getController(knobContainer.get(lastOnSideButton)[currentTab][i].getName()).moveTo("cust");
	      }
	      // map knob set
	      custMap.put(lastOnSideButton, currentTab);
	      //move rest of knobs to custom
	      for (int x: sideBut) {
	        for (int i = 0; i<knobContainer.get(x)[previousTab].length ; i++){
	          cp5.getController(knobContainer.get(x)[previousTab][i].getName()).moveTo("cust");
	        }
	        custMap.put(x, previousTab);
	      }
	      currentTab = 3;
	    }
	    
	    //finally if we want to move off the custom tab, move knobs back to there tabs
	    else if(!sideButtonIsOn() && previousTab == 3 && currentTab != 3) {
	      for (int i =0; i < drumHits.length; i++) {
	        for (Knob k: knobContainer.get(i)[custMap.get(i)] ){
            k.moveTo(tabs.get(custMap.get(i)));
          }
	      }
	    }
	    
	    else if(currentTab == 3) {
	      for (int i =0; i < drumHits.length; i++) {
          for (Knob k: knobContainer.get(i)[custMap.get(i)] ){
            k.moveTo("cust");
          }
        }
	    }
	  }
	}
	
	 /**
	  * checks which side button is on (if any), and as side effect assigns this to lastOnSideButton
	  * @return 
	  */
	public boolean sideButtonIsOn() {
	  for (int i = 0; i < buttons.length; i++) {
	    if (buttons[i].isOn()) {
	      lastOnSideButton = i;
	      return true;
	    }
	  }
	  return false;
	}
	
	public Clock getClock() {
    return clock;
  }

  public void setClock(Clock clock) {
    this.clock = clock;
  }

  public Toggle[] getSteps() {
    return steps;
  }

  public WavePlayer[] getKwp() {
    return kwp;
  }

  public Toggle getPlayPauseTog() {
    return playPauseTog;
  }

  public AudioContext getAc() {
    return ac;
  }

  public PShape getPlay() {
    return play;
  }

  public void setPlay(PShape play) {
    this.play = play;
  }

  public PShape getPause() {
    return pause;
  }

  public void setPause(PShape pause) {
    this.pause = pause;
  }

  public PShape getPlayPause() {
    return playPause;
  }

  public void setPlayPause(PShape playPause) {
    this.playPause = playPause;
  }

  public int[] getColorSteps() {
    return colorSteps;
  }

  public void setColorSteps(int[] colorSteps) {
    this.colorSteps = colorSteps;
  }

  public Button[] getButtons() {
    return buttons;
  }

  public int getPrevButEvent() {
    return prevButEvent;
  }

  public void setPrevButEvent(int prevButEvent) {
    this.prevButEvent = prevButEvent;
  }

  public int[] getFontColors() {
    return fontColors;
  }

  public HashMap<Integer, Integer> getColorMap() {
    return colorMap;
  }

  public int getTempo() {
    return tempo;
  }

  public void setTempo(int tempo) {
    this.tempo = tempo;
  }

  public Glide getTempoGl() {
    return tempoGl;
  }

  public String[] getParams() {
    return params;
  }

  public void setParams(String[] params) {
    this.params = params;
  }

  public Character[] getGroups() {
    return groups;
  }

  public void setGroups(Character[] groups) {
    this.groups = groups;
  }

  public List<Knob[][]> getKnobContainer() {
    return knobContainer;
  }

  public Glide[] getKickPitchGlides() {
    return kickPitchGlides;
  }

  public void setKickPitchGlides(Glide[] kickPitchGlides) {
    this.kickPitchGlides = kickPitchGlides;
  }

  public float getMasterVol() {
    return masterVol;
  }

  public void setMasterVol(float masterVol) {
    this.masterVol = masterVol;
  }

  public Glide getGainGl() {
    return gainGl;
  }

  public void setGainGl(Glide gainGl) {
    this.gainGl = gainGl;
  }

  public Glide[] getkGainGl() {
    return kGainGl;
  }

  public void setkGainGl(Glide[] kGainGl) {
    this.kGainGl = kGainGl;
  }

  public float[] getkAmp() {
    return kAmp;
  }

  public void setkAmp(float[] kAmp) {
    this.kAmp = kAmp;
  }

  public BetterBeadArray getDelayTriggers() {
    return delayTriggers;
  }

  public float[] getkGate() {
    return kGate;
  }

  public static void main(String _args[]) {
		PApplet.main(new String[] { drumseq.DrumSeq.class.getName() });
	}
}
