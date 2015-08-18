package drumseq;

import processing.core.PApplet;
import beads.Bead;
import beads.DelayTrigger;

/**
 * to create shuffle effect later on, will probably need a separate clock
 * trigger for each instrument
 * 
 * @author tristan
 *
 */
public class KickClockTrigger extends Bead {

  DrumSeq parent; // The parent PApplet that we will render ourselves onto

  public KickClockTrigger(PApplet p) {
    super();
    parent = (DrumSeq) p;
  }

//TODO change this so that each waveplayer is never paused, instead a pauseTrigger will be added to the waveplayer so that after a certain time(GATE) that wp is paused
  protected void messageReceived(Bead message) {
    long b = parent.getClock().getCount() % 16;
    for (int i = 0; i < 16; i++) {
      if (b == i) {
        //adsr amplitude envelope
        parent.kGainEnv[i].clear();
        //attack
        parent.kGainEnv[i].addSegment(1f, parent.kAttack);
        //for just decay: (0, parent.kDecay) 
        //decays to sustain volume, then triggers release envelope
        parent.kGainEnv[i].addSegment(parent.kSustain,
                                      parent.kDecay, new ReleaseTrigger(parent, i));
        //play wave player
        parent.getKwp()[i].pause(false);
      }
    }
  }
}