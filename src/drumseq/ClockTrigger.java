package drumseq;

import processing.core.PApplet;
import beads.Bead;


/**
 * to create shuffle effect later on, will probably need a separate clock trigger for each instrument
 * @author tristan
 *
 */
public class ClockTrigger extends Bead {

	DrumSeq parent; // The parent PApplet that we will render ourselves onto

	public ClockTrigger(PApplet p) {
		super();
		parent = (DrumSeq) p;
	}

	protected void messageReceived(Bead message) {
		//if (parent.switches[4].on) {
			long b = parent.cl.getCount() % 16;
			for (int i = 0; i < 16; i++) {
				if (b == i) {
					parent.steps[i].toggle();
					//parent.kwp[i].pause(false);
				} else {
				  if (parent.steps[i].getState()){
				    parent.steps[i].toggle();
				  }
					//parent.kwp[i].pause(true);
				}
			}
		//}
	}
}