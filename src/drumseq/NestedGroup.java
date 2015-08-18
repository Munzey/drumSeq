package drumseq;

import java.util.ArrayList;

import controlP5.ControllerInterface;
import controlP5.Group;

public class NestedGroup {

  private ArrayList<Group> groups;

  public NestedGroup() {
    groups = new ArrayList<>();
  }

  public void add(Group group) {
    groups.add(group);
  }

  public void setVisible(boolean b) {
    // TODO iterate through arrylists and set each to either visible or invisible
    for(Group g: groups) {
      g.setVisible(b);
    }
  }
  
  
}
