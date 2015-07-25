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
    if (parent.getButtons()[selectedButton].isOn()) {
      parent.setPrevButEvent(theEvent.getId());
      parent.getFontColors()[selectedButton] = parent.getColorMap().get(selectedButton);
    }
    else {
      parent.getFontColors()[selectedButton] = parent.color(255);
    }
    for (int i = 0; i < parent.getButtons().length; i++) {
      if (parent.getButtons()[i].isOn() && i != parent.getPrevButEvent()) {
        parent.getButtons()[i].setOff();
        parent.getFontColors()[i] = parent.color(255);
      }
    }
  }
}
