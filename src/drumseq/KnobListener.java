package drumseq;

import processing.core.PApplet;
import controlP5.*;

public class KnobListener implements ControlListener {
  
  DrumSeq parent; // The parent PApplet that we will render ourselves onto

  public KnobListener(PApplet p) {
    parent = (DrumSeq) p;
  }
  
  public void controlEvent(ControlEvent theEvent) {
    //parent.myColorBackground = parent.color(theEvent.getValue());
    if (theEvent.getId() == -1) {
      //System.out.println("tempo change from: " + parent.tempo);
      parent.tempo = (int)((60000/theEvent.getValue())*4);
      parent.gl.setValue(parent.tempo);
      //System.out.println("tempo change to: " + parent.tempo);
    }
    //System.out.println(theEvent.getName() + " " + theEvent.getLabel());
  }
}
