package drumseq;

import beads.Pitch;
import processing.core.PApplet;
import controlP5.*;

public class KnobListener implements ControlListener {

  DrumSeq parent; // The parent PApplet that we will render ourselves onto

  public KnobListener(PApplet p) {
    parent = (DrumSeq) p;
  }

  public void controlEvent(ControlEvent theEvent) {
    char knobGroup = theEvent.getController().getName().charAt(0);
    int groupIndex = java.util.Arrays.asList(parent.getGroups()).indexOf(knobGroup);
    String knobType = theEvent.getController().getCaptionLabel().getText();
    int index = java.util.Arrays.asList(parent.getParams()).indexOf(knobType);

    //tempo
    if (theEvent.getId() == -1) {
      //TODO eliminate variable by having the conversion happen inside setValue
      parent.setTempo((int)((60000/theEvent.getValue())*4));
      parent.getTempoGl().setValue(parent.getTempo());
    }

    // master gain
    else if (theEvent.getId() == -2) {
      parent.setMasterVol(theEvent.getValue());
      parent.getGainGl().setValue(parent.getMasterVol());
    }
    else {
      switch(index) {
      case 0:
        //amp
        parent.getkAmp()[theEvent.getController()
                      .getId()] = theEvent.getValue();
        parent.getkGainGl()[theEvent.getId()].setValue(theEvent.getValue());
        break;
      case 1:
        //pitch
        parent.getKickPitchGlides()[theEvent.getId()].setValue(Pitch.mtof(theEvent.getValue()));
        break;
      case 2:
        //gate
        parent.getkGate()[theEvent.getController()
                            .getId()] = theEvent.getValue();
        break;
      }
    }
  }
}
