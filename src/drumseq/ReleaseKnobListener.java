package drumseq;

import processing.core.PApplet;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.ControllerInterface;

/**
 * This class is for making the displayed knob values set back to there step id
 * after there values have been manipulated
 * 
 * @author tristan
 *
 */
public class ReleaseKnobListener implements CallbackListener {

  DrumSeq parent; // The parent PApplet that we will render ourselves onto;

  public ReleaseKnobListener(PApplet p) {
    parent = (DrumSeq) p;
  }

  @Override
  public void controlEvent(CallbackEvent theEvent) {
    char knobGroup = theEvent.getController().getName().charAt(0);
    int groupIndex = java.util.Arrays.asList(parent.getGroups()).indexOf(knobGroup);
    String knobType = theEvent.getController().getCaptionLabel().getText();
    int index = java.util.Arrays.asList(parent.getParams()).indexOf(knobType);
    if (theEvent.getAction() == ControlP5.ACTION_RELEASED
        || theEvent.getAction() == ControlP5.ACTION_RELEASEDOUTSIDE) {
      parent.getKnobContainer().get(groupIndex)[index][theEvent.getController()
          .getId()].setValueLabel(Integer.toString(theEvent.getController()
          .getId() + 1));
    }

  }

}
