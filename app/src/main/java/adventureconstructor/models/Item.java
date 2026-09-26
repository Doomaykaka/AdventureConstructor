package adventureconstructor.models;

public class Item {
    private String name;
    private String type;
    private int value;

    public Item(String name, String type, int value) {
        this.name = name;
        this.setType(type);
        this.setValue(value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return this.name + " (" + this.getType() + ": " + this.getValue() + ")";
    }
}
