package adventureconstructor.models;

public class LocTempCloner {
    public static LocTemp clone(LocTemp src) {
        LocTemp l = new LocTemp();
        l.setId(src.getId());
        l.setType(src.getType());
        l.setName(src.getName());
        l.setDesc(src.getDesc());
        l.setImage(src.getImage());
        l.setAmbient(src.getAmbient());
        l.setNpcName(src.getNpcName());
        l.setDialogId(src.getDialogId());
        l.setOnSuccess(src.getOnSuccess());
        l.setOnFail(src.getOnFail());
        l.setEnemyName(src.getEnemyName());
        l.setEnemyHp(src.getEnemyHp());
        l.setEnemyDmg(src.getEnemyDmg());
        l.setEnemyDef(src.getEnemyDef());
        l.setOnVictory(src.getOnVictory());
        l.setOnDefeat(src.getOnDefeat());
        l.getActions().addAll(src.getActions());
        return l;
    }
}
