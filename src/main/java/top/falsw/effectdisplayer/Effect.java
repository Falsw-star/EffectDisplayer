package top.falsw.effectdisplayer;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

public class Effect {

    public Vec3d pos;
    public World world;
    public Random random = new Random();

    public Effect(Vec3d pos, World world) {
        this.pos = pos;
        this.world = world;
        EffectDisplayEntity main = newEntity(Items.WHITE_CONCRETE);
        main.setGlowColorOverride(0xFFFFFF);
        main.setGlowing(true);
        main.setAnimation(new Animation(main)
                .at(0)
                .addMovement((entity, animation) -> entity.setInterpolation(0, 5).scale(19, 19, 19).rotation(new Quaternionf( 10, 10 , 10, 1), null).addTransformation()).next(5)
                .addMovement((entity, animation) -> entity.setInterpolation(0, 50).scale(-20, -20, -20).addTransformation()).next(50)
                .autoDelete()
        );
        main.spawn();
        for (int i = 0; i < 100; i++) {
            EffectDisplayEntity part = newEntity(random.nextBoolean() ? Items.MAGMA_BLOCK : Items.SHROOMLIGHT);
            int duration = random.nextInt(100) + 200;
            int duration_part1 = random.nextInt(3) * (duration / 10);
            int duration_part2 = random.nextInt(5) * (duration / 10);
            part.setInterpolation(0, duration);
            part.setAnimation(new Animation(part)
                    .at(0)
                    .addMovement((entity, animation) -> entity
                            .translation(new Vector3f(random.nextFloat() - 0.5f, 0, random.nextFloat() - 0.5f).mul(random.nextFloat(50) + 150).add(0, random.nextFloat(10) + 20, 0))
                            .rotation(new Quaternionf(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f, random.nextFloat() - 0.5f, 1).mul(random.nextFloat(10)), null)
                            .scale(new Vector3f(random.nextFloat(0.5f)).mul(20))
                            .addTransformation())
                    .next(duration_part1).addMovement((entity, animation) -> entity.setItemStack(new ItemStack(random.nextBoolean() ? Items.MAGMA_BLOCK : Items.ORANGE_CONCRETE)))
                    .next(duration_part2).addMovement((entity, animation) -> entity.setItemStack(new ItemStack(random.nextBoolean() ? Items.WHITE_STAINED_GLASS : Items.BLACK_STAINED_GLASS)))
                    .next(duration - duration_part1 - duration_part2)
                    .autoDelete()
            ).spawn();
        }
    }

    public EffectDisplayEntity newEntity(ItemConvertible item) {
        EffectDisplayEntity entity = new EffectDisplayEntity(world, pos, new ItemStack(item));
        return entity;
    }
}
