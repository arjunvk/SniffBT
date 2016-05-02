package arjunvijayakumar.smellbt.customRowWithCB;

public class RowItem {
    String name;
    int value; /* 0 -> Checkbox deselected ; 1 -> Checkbox selected */

    public RowItem(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public int getValue() {
        return this.value;
    }
}
