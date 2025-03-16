package top.falsw.effectdisplayer;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class EffectEntity extends DisplayEntity.ItemDisplayEntity {

    public Set<ItemDisplayEntity> entityPool = new HashSet<>();

    public EffectEntity(EntityType<?> entityType, World world, Vec3d pos) {
        super(entityType, world);
        this.setPosition(pos);
    }

    public void append(ItemDisplayEntity entity) {
        this.entityPool.add(entity);
    }
}
