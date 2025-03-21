package top.falsw.effectdisplayer.EffectDisplayer;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public abstract class Effect {

    public Vec3d pos;
    public World world;
    public Random random = new Random();

    public Effect(Vec3d pos, World world) {
        this.pos = pos;
        this.world = world;
    }

    /**
     * While called, Generate the Effect
     */
    public abstract void spawn();

    public EffectDisplayEntity newEntity(ItemConvertible item) {
        return new EffectDisplayEntity(world, pos, new ItemStack(item));
    }
}
