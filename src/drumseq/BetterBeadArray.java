package drumseq;

import beads.Bead;
import beads.BeadArray;
import beads.UGen;

public class BetterBeadArray extends BeadArray {

  /**
   * given an array of UGens, pauses/unpauses the beads in this array to
   * correspond to the paused/unpaused in the UGen array
   * 
   * @param ugens
   *          - in this case our array of WavePlayers
   */
  public void pauseOnly(UGen[] ugens) {
    assert ugens.length == this.size();
    BeadArray clone = clone();
    for (int i = 0; i < clone.getBeads().size(); i++) {
      if (ugens[i].isPaused()) {
        clone.getBeads().get(i).pause(true);
      } else {
        clone.getBeads().get(i).pause(false);
      }
    }
  }

  /**
   * for each bead item(assert(and then cast) that they are UGens) in the array
   * call calculateBuffer on those not paused
   */
  public void calculateBuffers() {
    BeadArray clone = clone();
    for (Bead bead : clone.getBeads()) {
      if (!bead.isPaused()) {
        assert bead instanceof UGen;
        UGen theBead = (UGen) bead;
        theBead.calculateBuffer();
      }
    }
  }

  public Bead getBeadAt(int index) {
    return this.getBeads().get(index);
  }

  public void setBeadAt(int index, Bead b) {
      this.getBeads().set(index, b);
  }
}
