package drumseq;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
 * @author tristan
 *
 */
public class DrumSeq extends PApplet {
  //processing stuff
  int myColorBackground = color(30,30,30);
  //int myColorBackground = color(250,250,250);
  PFont font;
  PShape play;
  PShape pause;
  PShape playPause;
  PShape k,s,h,t;
  int kickFontColor = color(255);
  int snareFontColor = color(255);
  int hihatFontColor = color(255);
  int tomFontColor = color(255);
  int[] fontColors = {kickFontColor, snareFontColor, hihatFontColor, tomFontColor};
  HashMap<Integer,Integer> colorMap = new HashMap<Integer,Integer>();
  
  //cp5 stuff  
  ControlP5 cp5;
  Tab pitchTab;
  Tab gateTab;
  Tab customTab;
  boolean custTabOn;
  List<Tab> tabs;
  int[] colorSteps;
  Toggle[] steps;
  Toggle playPauseTog;
  Button[] buttons;
  Knob[][] kickKnobs;
  Knob[][] snareKnobs;
  Knob[][] hihatKnobs;
  Knob[][] tomKnobs;
  List<Knob[][]> knobContainer;
  String[] drumHits= { "kick", "snare", "hihat", "tom" };
  String[] params= { "amp", "pitch", "gate" }; //tab ids correspond to array indexes
  Character[] groups = {'K', 'S', 'H', 'T'};
  int currentTab = 0;
  int previousTab;
  int prevButEvent;
  int lastOnSideButton; //last known side button to be on, MAY NOT BE ON
  HashMap<Integer,Integer> custMap = new HashMap<Integer,Integer>(); 
  
  //beads stuff
  Clock cl;
  AudioContext ac;
  Glide gl;
  Glide[] kickGlides = new Glide[16];
  WavePlayer[] kwp = new WavePlayer[16];
  ScalingMixer sc;
  Gain g;
  int tempo = 2000; //default tempo 120
  
	public void setup() {
	  size((int)(displayWidth/1.2), displayHeight/2, P2D);
	  smooth(8);
	  noStroke();

	  //initialiseCP5GUI();
	  //initialiseAudio();
	  //drawExtraShapes();
	  
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
	   * Global side buttons
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
	  
	  /*
	   * BEADS
	   */
	  //setting up timer, this is just for testing should move to separate method
	  ac = new AudioContext(new JavaSoundAudioIO(), 512);
    sc = new ScalingMixer(ac);
    gl = new Glide(ac, tempo, 50);
    cl = new Clock(ac, gl);
    //cl.setClick(true);
    ClockTrigger tr = new ClockTrigger(this);
    cl.addMessageListener(tr);
    ac.out.addDependent(cl);
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
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { drumseq.DrumSeq.class.getName() });
	}
}
