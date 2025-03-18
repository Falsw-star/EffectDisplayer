package top.falsw.effectdisplayer;

import java.util.LinkedList;
import java.util.function.BiConsumer;

public class Animation {

    public LinkedList<Movement> movements = new LinkedList<>();
    public LinkedList<Movement> trash = new LinkedList<>();
    public ModItemDisplayEntity entity;
    public int at;
    public int length;
    public int repeat = 1;

    public Animation(ModItemDisplayEntity entity) {
        this.entity = entity;
        this.at = 0;
        this.length = this.entity.startInterpolation;
    }

    public Animation at(int relativeAge) {
        this.at = relativeAge;
        this.length = Math.max(this.length, this.at);
        return this;
    }

    public int getLength() { return this.length; }

    public Animation autoDelete() {
        this.entity.setMaxAge(this.getLength());
        return this;
    }

    public Animation next() {
        this.at += this.entity.interpolationDuration;
        this.length = Math.max(this.length, this.at);
        return this;
    }

    public Animation addMovement(BiConsumer<ModItemDisplayEntity, Animation> movement) {
        this.movements.add(new Movement(this.at, movement));
        return this;
    }

    public Animation repeat(int times) {
        this.repeat = times;
        this.length = this.length * times;
        return this;
    }

    public void runMovement(Movement movement, ModItemDisplayEntity entity) {
        movement.content.accept(entity, this);
        this.trash.add(movement);
    }
}