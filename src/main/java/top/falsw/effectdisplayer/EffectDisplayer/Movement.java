package top.falsw.effectdisplayer.EffectDisplayer;

import java.util.function.BiConsumer;

public class Movement {
    public Integer age;
    public BiConsumer<EffectDisplayEntity, Animation> content;
    public Movement(Integer age, BiConsumer<EffectDisplayEntity, Animation> content) {
        this.age = age;
        this.content = content;
    }
}