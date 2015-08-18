package drumseq;

import processing.core.PApplet;
import controlP5.ControlEvent;
import controlP5.ControlListener;

public class EnvelopeListener implements ControlListener {

  DrumSeq parent; // The parent PApplet that we will render ourselves onto

  public EnvelopeListener(PApplet p) {
    parent = (DrumSeq) p;
  }

  @Override
  public void controlEvent(ControlEvent theEvent) {
    // attack
    switch (theEvent.getId()) {
    case 1:
      System.out.println("change");
      System.out.println(theEvent.getValue());
      parent.kAttack = theEvent.getValue();
      break;
    case 2:
      System.out.println("change");
      System.out.println(theEvent.getValue());
      parent.kDecay = theEvent.getValue();
      break;
    case 3:
      System.out.println("change");
      System.out.println(theEvent.getValue());
      parent.kSustain = theEvent.getValue();
      break;
    case 4:
      System.out.println("change");
      System.out.println(theEvent.getValue());
      parent.kSustainLen = theEvent.getValue();
      break;
    case 5:
      //TODO in the case of release, we need to add the release value to (all)the gate value(s)
      System.out.println("change");
      System.out.println(theEvent.getValue());
      parent.kRelease = theEvent.getValue();
      /*
      for(int i=0; i<parent.getkGate().length; i++) {
        parent.getkGate()[i] += parent.kRelease;
      }
      */
      break;
    }

  }

}
