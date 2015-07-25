package drumseq;

import processing.core.PApplet;
import beads.AudioContext;
import beads.Bead;
import beads.DelayTrigger;

/**
 * to create shuffle effect later on, will probably need a separate clock
 * trigger for each instrument
 * 
 * @author tristan
 *
 */
public class KGateTrigger extends Bead {

  DrumSeq parent; // The parent PApplet that we will render ourselves onto

  private int id;

  public KGateTrigger(PApplet p, int id) {
    super();
    parent = (DrumSeq) p;
    this.id = id;
  }

//TODO change this so that each waveplayer is never paused, instead a pauseTrigger will be added to the waveplayer so that after a certain time(GATE) that wp is paused
  protected void messageReceived(Bead message) {
    KGateTrigger kg = (KGateTrigger) message;
    int step = kg.getId();
    parent.getKwp()[step].pause(true);
    System.out.println("pauseNote: " + step);
    //once a note has been paused, a new delayTrigger is needed as it is killed after sending the message
    KGateTrigger kgate = new KGateTrigger(parent, step);
    //delay should never be smaller than 12 ms to be audible
    parent.getDelayTriggers().setBeadAt(step,
                                        new DelayTrigger(parent.getAc(), parent.getkGate()[step],
                                                         kgate, kgate));
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}