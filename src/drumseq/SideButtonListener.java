package drumseq;

import controlP5.ControlEvent;
import controlP5.ControlListener;
import processing.core.PApplet;

public class SideButtonListener implements ControlListener {

  DrumSeq parent; // The parent PApplet that we will render ourselves onto

  public SideButtonListener(PApplet p) {
    parent = (DrumSeq) p;
  }

  /**
   * used so that only one side button can be "on" at one time
   */
  @Override
  public void controlEvent(ControlEvent theEvent) {
    System.out.println("side button pressed!");
    int selectedButton = theEvent.getId();
    if (parent.buttons[selectedButton].isOn()) {
      parent.prevButEvent = theEvent.getId();
      parent.kickFontColor = parent.color(92, 190, 153);
    }
    for (int i = 0; i < parent.buttons.length; i++) {
      if (parent.buttons[i].isOn() && i != parent.prevButEvent) {
        parent.buttons[i].setOff();
        parent.kickFontColor = parent.color(255);
      }
    }
  }
}
