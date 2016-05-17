package millennia.sniffbt.customRowWithCB;

import java.io.Serializable;

public class RowItem implements Serializable {
    private String name;
    private boolean blnIsCBChecked;

    public RowItem(String name, boolean blnCBChecked) {
        this.name = name;
        this.blnIsCBChecked = blnCBChecked;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCB(boolean blnCBChecked) {
        this.blnIsCBChecked = blnCBChecked;
    }

    public String getName() {
        return this.name;
    }

    public boolean isCBChecked() {
        return this.blnIsCBChecked;
    }
}
