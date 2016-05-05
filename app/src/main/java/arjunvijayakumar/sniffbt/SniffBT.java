package arjunvijayakumar.sniffbt;

public class SniffBT {
    private boolean blnDisplayDiscoveredList;

    public SniffBT() {
        blnDisplayDiscoveredList = false;
    }

    public void setDisplayDiscoveredListFlag(boolean flag) {
        this.blnDisplayDiscoveredList = flag;
    }

    public boolean getDisplayDiscoveredListFlag() {
        return this.blnDisplayDiscoveredList;
    }
}
