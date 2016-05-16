package millennia.sniffbt.customRowWithCB;

import java.io.Serializable;

public class RowItem implements Serializable {
    String name;
    boolean value;

    public RowItem(String name, boolean value) {
        this.name = name;
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public boolean getValue() {
        return this.value;
    }
}
