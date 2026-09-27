package adventureconstructor.models;

public class Item {
    private Long id = null;
    private String name;
    private String type;
    private int value;

    public Item() {}

    public Item(String name, String type, int value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public adventureconstructor.models.db.Item toDb() {
        adventureconstructor.models.db.Item db = new adventureconstructor.models.db.Item();
        if (id != null) db.setId(id);
        db.setName(name);
        db.setType(type);
        db.setValue(value);
        return db;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return name + " (" + type + ", +" + value + ")";
    }

    public static Item fromDb(adventureconstructor.models.db.Item db) {
        Item biz = new Item();
        biz.setId(db.getId());
        biz.setName(db.getName());
        biz.setType(db.getType());
        biz.setValue(db.getValue());
        return biz;
    }
}
