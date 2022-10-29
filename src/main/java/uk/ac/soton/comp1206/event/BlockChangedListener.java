package uk.ac.soton.comp1206.event;

public interface BlockChangedListener {

  void blockChanged(int[] oldBlock, int[] newBlock);
}
