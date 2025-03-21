package top.falsw.effectdisplayer.EffectDisplayer;

import java.util.LinkedList;
import java.util.function.BiConsumer;

/**
 * Animation class for EffectDisplayEntity.
 * It is basically a linked list of movements, which will be executed in order.
 */
public class Animation {

    public LinkedList<Movement> movements = new LinkedList<>();
    public LinkedList<Movement> trash = new LinkedList<>();
    public EffectDisplayEntity entity;
    public int at;
    public int length;
    public int repeat = 1;

    public Animation(EffectDisplayEntity entity) {
        this.entity = entity;
        this.at = 0;
        this.length = this.entity.startInterpolation;
    }

    /**
     * Set the relative age of the animation.
     * @param relativeAge the relative age.
     * @return This.
     */
    public Animation at(int relativeAge) {
        this.at = relativeAge;
        this.length = Math.max(this.length, this.at);
        return this;
    }

    /**
     * Get the length of the animation.
     * @return The length age of the animation.
     */
    public int getLength() { return this.length; }

    public Animation autoDelete() {
        this.entity.setMaxAge(this.getLength());
        return this;
    }

    /**
     * Move 'at' to the next age point,
     * at that point, the former movement should have been finished.
     * Normally the length of each movement is 'interpolationDuration', so we just add it to 'at',
     * so you can add the next movement, and it will be executed on the right time (age).
     * @return This.
     */
    public Animation next() {
        this.at += this.entity.interpolationDuration;
        this.length = Math.max(this.length, this.at);
        return this;
    }

    /**
     * Actually the 'interpolationDuration' only affects the transformation,
     * but the ItemDisplayEntity has a lot more you can customize.
     * So sometimes you want to get rid of the limitation of 'interpolationDuration',
     * in this case, you can use this method to add the custom duration.
     * For instance, now the 'interpolationDuration' is 100, you can separate the movement into two parts:
     * .addMovement((ent, ani) -> ent.setGlowing(true)).next(50).addMovement((ent, ani) -> ent.setGlowing(false)).next(50)
     * @param customInterpolationDuration The custom duration.
     * @return This.
     */
    public Animation next(int customInterpolationDuration) {
        this.at += customInterpolationDuration;
        this.length = Math.max(this.length, this.at);
        return this;
    }

    /**
     * Add a movement to the animation.
     * .addMovement((entity, animation) -> entity.translation(10,10,10).addTransformation()).next()
     * @param movement The movement to be added.
     *                 You need to operate on the 'entity'.
     *                 The Animation is passed in for more information, but usually it's useless.
     * @return This.
     */
    public Animation addMovement(BiConsumer<EffectDisplayEntity, Animation> movement) {
        this.movements.add(new Movement(this.at, movement));
        return this;
    }

    /**
     * Repeat the animation.
     * @param times The times to repeat. Default is 1.
     * @return This.
     */
    public Animation repeat(int times) {
        this.repeat = times;
        this.length = this.length * times;
        return this;
    }

    public void runMovement(Movement movement, EffectDisplayEntity entity) {
        movement.content.accept(entity, this);
        this.trash.add(movement);
    }
}