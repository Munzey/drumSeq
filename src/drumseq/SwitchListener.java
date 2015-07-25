package drumseq;

import processing.core.PApplet;
import controlP5.ControlEvent;
import controlP5.ControlListener;
/**
 * a class for handling the hidden toggles/switches which sequence the step rectangle lights
 * @author tristan
 *
 */
public class SwitchListener implements ControlListener {
  
  DrumSeq parent; // The parent PApplet that we will render ourselves onto

  public SwitchListener(PApplet p) {
    parent = (DrumSeq) p;
  }
  
  @Override
  public void controlEvent(ControlEvent theEvent) {
    // if play/pause button pressed
    if (theEvent.getId() == -1) {
      System.out.println("play/pause button pressed");
      if (parent.getPlayPauseTog().getState()) {
        parent.setPlayPause(parent.getPause());
        parent.getAc().start();
      }
      else {
        parent.setPlayPause(parent.getPlay());
        parent.getAc().stop();
      }
    }
    else {
      if (parent.getSteps()[theEvent.getId()].getState()) {
        parent.getColorSteps()[theEvent.getId()] = parent.color(198, 99, 99);
      } else {
        if (theEvent.getId() == 0) {
          parent.getColorSteps()[theEvent.getId()] = parent.color(113, 110, 110);
        } else if (theEvent.getId() % 4 == 0) {
          parent.getColorSteps()[theEvent.getId()] = parent.color(113, 110, 110);
        } else {
          parent.getColorSteps()[theEvent.getId()] = parent.color(42, 42, 42);
        }
      }
    }
  }

}
