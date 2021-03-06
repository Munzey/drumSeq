package drumseq;

import processing.core.PApplet;
import beads.Bead;


/**
 * to create shuffle effect later on, will probably need a separate clock trigger for each instrument
 * @author tristan
 *
 */
public class StepClockTrigger extends Bead {

	DrumSeq parent; // The parent PApplet that we will render ourselves onto

	public StepClockTrigger(PApplet p) {
		super();
		parent = (DrumSeq) p;
	}
//TODO change this so that each waveplayer is never paused, instead a pauseTrigger will be added to the waveplayer so that after a certain time(GATE) that wp is paused
	protected void messageReceived(Bead message) {
			long b = parent.getClock().getCount() % 16;
			for (int i = 0; i < 16; i++) {
				if (b == i) {
					parent.getSteps()[i].toggle();
				} else {
				  if (parent.getSteps()[i].getState()){
				    parent.getSteps()[i].toggle();
				  }
				}
			}
	}
}