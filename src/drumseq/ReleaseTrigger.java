package drumseq;

import processing.core.PApplet;
import beads.Bead;
import beads.DelayTrigger;

public class ReleaseTrigger extends Bead {
  DrumSeq parent; // The parent PApplet that we will render ourselves onto
  private int gainIndex; // the index of the gain object we want to add the envelope segment to

  public ReleaseTrigger(PApplet p, int index) {
    super();
    parent = (DrumSeq) p;
    gainIndex = index;
  }
  
  protected void messageReceived(Bead message) {
    DelayTrigger dt;
    dt = new DelayTrigger(parent.getAc(), parent.kSustainLen, new Bead () {
      public void messageReceived(Bead message)
      {
        parent.kGainEnv[gainIndex].addSegment(0, parent.kRelease);
      }
    });
    parent.getAc().out.addDependent(dt);
    
  }
  
}
