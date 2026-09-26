package adventureconstructor.models;

import java.util.ArrayList;
import java.util.List;

public class DialogNode {
    private String id;
    private String text;
    private String reward;
    private boolean endDialog = false;
    private boolean triggerCombat = false;
    private boolean success = false;
    private List<DialogOption> options = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

    public boolean isEndDialog() {
        return endDialog;
    }

    public void setEndDialog(boolean endDialog) {
        this.endDialog = endDialog;
    }

    public boolean isTriggerCombat() {
        return triggerCombat;
    }

    public void setTriggerCombat(boolean triggerCombat) {
        this.triggerCombat = triggerCombat;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<DialogOption> getOptions() {
        return options;
    }

    public void setOptions(List<DialogOption> options) {
        this.options = options;
    }
}
