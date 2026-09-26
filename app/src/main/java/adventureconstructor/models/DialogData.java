package adventureconstructor.models;

import java.util.ArrayList;
import java.util.List;

public class DialogData {
    private String npcName;
    private String greeting;
    private List<DialogNode> nodes = new ArrayList<>();

    public DialogNode findNode(String id) {
        for (DialogNode n : getNodes()) if (n.getId().equals(id)) return n;
        return null;
    }

    public String getNpcName() {
        return npcName;
    }

    public void setNpcName(String npcName) {
        this.npcName = npcName;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public List<DialogNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<DialogNode> nodes) {
        this.nodes = nodes;
    }
}
