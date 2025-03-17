package top.falsw.effectdisplayer;

import java.util.LinkedList;
import java.util.function.BiConsumer;

public class Animation {

    public LinkedList<Movement<Integer, BiConsumer<ModItemDisplayEntity, Animation>>> movements = new LinkedList<>();
    public int at;
    public int startInterpolation;
    public int interpolationDuration;
    public boolean autoMaxAge;
    public int length;

    public Animation(ModItemDisplayEntity entity, boolean autoMaxAge) {
        this.autoMaxAge = autoMaxAge;
        this.startInterpolation = entity.startInterpolation;
        this.interpolationDuration = entity.interpolationDuration;
        this.at = this.startInterpolation;
        this.length = this.startInterpolation;
    }

    public Animation at(int age) {
        this.at = this.startInterpolation + age;
        this.length = Math.max(this.length, age);
        return this;
    }

    public Animation next() {
        this.at += this.interpolationDuration;
        this.length = Math.max(this.length, this.at);
        return this;
    }

    public Animation addMovement(BiConsumer<ModItemDisplayEntity, Animation> movement) {
        this.movements.add(new Movement<>(this.at, movement));
        return this;
    }
}