package top.falsw.effectdisplayer;

import java.util.function.BiConsumer;

public class Movement {
    public Integer age;
    public BiConsumer<ModItemDisplayEntity, Animation> content;
    public Movement(Integer age, BiConsumer<ModItemDisplayEntity, Animation> content) {
        this.age = age;
        this.content = content;
    }
}