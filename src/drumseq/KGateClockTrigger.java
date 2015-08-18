package drumseq;

import processing.core.PApplet;
import beads.AudioContext;
import beads.Bead;
import beads.DelayTrigger;
import beads.UGen;


/**
 * this works because delay triggers work the same as clock, i.e. calculateBuffer
 * @author Tristan
 *
 */
public class KGateClockTrigger extends UGen {

  DrumSeq parent; // The parent PApplet that we will render ourselves onto

  public KGateClockTrigger(PApplet p, AudioContext ac) {
    super(ac);
    parent = (DrumSeq) p;
  }

  @Override
  public void calculateBuffer() {
    // TODO Auto-generated method stub
    parent.getDelayTriggers().pauseOnly(parent.getKwp());
    parent.getDelayTriggers().calculateBuffers();
  }
}