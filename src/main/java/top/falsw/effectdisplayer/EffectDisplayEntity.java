package top.falsw.effectdisplayer;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Util;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.LinkedList;

/**
 * EffectDisplayEntity
 * This entity is the core of the project.
 * While spawn() is called, the entity will be spawned.
 * Before that you should set all the things you need.
 * The parent class DisplayEntity.ItemDisplayEntity was added by mojang in 1.19.
 * @author Falsw
 */
public class EffectDisplayEntity extends DisplayEntity.ItemDisplayEntity {

    static final Logger LOGGER = LogUtils.getLogger();

    private int maxAge;
    public int at = 0;
    private boolean maxAgeSet = false;
    private LinkedList<NbtCompound> nbtList = new LinkedList<>();
    public int startInterpolation = 0;
    public int interpolationDuration = 20;
    private AffineTransformation other_transformation = new AffineTransformation(
            new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf()
    );
    private Animation animation = null;

    /**
     * Spawn the entity.
     * @return This.
     */
    public EffectDisplayEntity spawn() {
        this.getWorld().spawnEntity(this);
        return this;
    }

    public EffectDisplayEntity(World world, Vec3d pos, ItemStack itemStack) {
        super(EntityType.ITEM_DISPLAY, world);
        super.setPosition(pos);
        super.setItemStack(itemStack);
        this.maxAge = this.age;
    }

    /**
     * @param startAge the animation will start at this age.
     * @return This.
     */
    public EffectDisplayEntity at(int startAge) {
        this.at = startAge;
        return this;
    }

    /**
     * @param animation Animation.
     * @return This.
     */
    public EffectDisplayEntity setAnimation(Animation animation) {
        this.animation = animation;
        return this;
    }

    /**
     * @param startInterpolation Each transformation will delay for this amount of ticks after nbt is set by setTransformation().
     * @param interpolationDuration Each transformation will last for this amount of ticks.
     * @return This.
     */
    public EffectDisplayEntity setInterpolation(int startInterpolation, int interpolationDuration) {
        this.startInterpolation = startInterpolation;
        this.interpolationDuration = interpolationDuration;
        return this;
    }

    /**
     * If set, the entity will be removed at this age.
     * @param maxAge the max age.
     * @return This.
     */
    public EffectDisplayEntity setMaxAge(int maxAge) {
        this.maxAgeSet = true;
        this.maxAge = this.age + maxAge;
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.maxAgeSet && this.age >= this.maxAge) {
            this.remove(RemovalReason.KILLED);
        }
        if (this.animation != null) {
            for (int i = 0; i < this.animation.movements.size(); i++) {
                Movement movement = this.animation.movements.get(i);
                if (this.age >= movement.age + this.at) {
                    this.animation.runMovement(movement, this);
                    this.animation.movements.remove(i);
                    i--;
                }
                if (this.animation.movements.isEmpty()) {
                    this.animation.repeat -= 1;
                    if (this.animation.repeat > 0) {
                        this.at = this.age + this.interpolationDuration;
                        this.animation.movements.addAll(this.animation.trash);
                        this.animation.trash.clear();
                    }
                }
            }
        }
        if (!nbtList.isEmpty()) {
            this.readNbt(nbtList.removeFirst());
        }
    }

    /**
     * Set the nbt of the transformation of the entity.
     * @param other transformation.
     * @param add if true, the transformation will be added to the current transformation; if false, the transformation will replace the current transformation.
     */
    public void setTransformation(AffineTransformation other, boolean add) {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        nbt.putInt("start_interpolation", startInterpolation);
        nbt.putInt("interpolation_duration", interpolationDuration);
        if (!nbt.contains("transformation")) {
            AffineTransformation.ANY_CODEC
                    .encodeStart(NbtOps.INSTANCE, new AffineTransformation(new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf()))
                    .ifSuccess(nbtCompound -> nbt.put("transformation", nbtCompound));
        } else {
            AffineTransformation.CODEC
                    .decode(NbtOps.INSTANCE, nbt.get("transformation"))
                    .resultOrPartial(Util.addPrefix("Display entity", LOGGER::error))
                    .ifPresent(nbtCompound -> {
                        AffineTransformation transformation = nbtCompound.getFirst();
                        if (add) {
                            transformation = new AffineTransformation(
                                    transformation.getTranslation().add(other.getTranslation()),
                                    transformation.getLeftRotation().add(other.getLeftRotation()),
                                    transformation.getScale().add(other.getScale()),
                                    transformation.getRightRotation().add(other.getRightRotation())
                            );
                        } else {
                            transformation = other;
                        }
                        AffineTransformation.CODEC
                                .encodeStart(NbtOps.INSTANCE, transformation)
                                .ifSuccess(nbtCompound1 -> nbt.put("transformation", nbtCompound1));
                    });
        }
        nbtList.add(nbt);
    }

    /**
     * Set the translation of the 'other' transformation of the entity.
     * @param vector3f translation.
     * @return This.
     */
    public EffectDisplayEntity translation(Vector3f vector3f) {
        other_transformation = new AffineTransformation(
                vector3f,
                other_transformation.getLeftRotation(),
                other_transformation.getScale(),
                other_transformation.getRightRotation()
        );
        return this;
    }

    public EffectDisplayEntity translation(float x, float y, float z) {
        return translation(new Vector3f(x, y, z));
    }

    /**
     * Set the rotation of the 'other' transformation of the entity.
     * @param leftRotation leftRotation
     * @param rightRotation rightRotation
     * @return This.
     */
    public EffectDisplayEntity rotation(Quaternionf leftRotation, Quaternionf rightRotation) {
        other_transformation = new AffineTransformation(
                other_transformation.getTranslation(),
                leftRotation != null ? leftRotation : other_transformation.getLeftRotation(),
                other_transformation.getScale(),
                rightRotation != null ? rightRotation : other_transformation.getRightRotation()
        );
        return this;
    }

    /**
     * Set the scale of the 'other' transformation of the entity.
     * @param vector3f scale. Default is 1 (times).
     * @return This.
     */
    public EffectDisplayEntity scale(Vector3f vector3f) {
        other_transformation = new AffineTransformation(
                other_transformation.getTranslation(),
                other_transformation.getLeftRotation(),
                vector3f,
                other_transformation.getRightRotation()
        );
        return this;
    }

    public EffectDisplayEntity scale(float x, float y, float z) {
        return scale(new Vector3f(x, y, z));
    }

    /**
     * Make the 'other' transformation take the place of the entity's.
     * The 'other' transformation will be reset.
     */
    public void setTransformation() {
        setTransformation(other_transformation, false);
        other_transformation = new AffineTransformation(
                new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf()
        );
    }

    /**
     * Add the 'other' transformation to the entity's.
     * The 'other' transformation will be reset.
     */
    public void addTransformation() {
        setTransformation(other_transformation, true);
        other_transformation = new AffineTransformation(
                new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf()
        );
    }

    // 修复有时候直接设置变化时插值不生效
    // 错误原因推测为 start_interpolation 被重置为 0 ，Minecraft 执行时该 tick 已经过去
    // Update：此问题仍未解决
    // 已知的信息：在游戏内使用 /data merge 不会发生此问题

    // 思路：待实体生成后再修改其nbt
    // Update：此问题已解决！
    // Update：此问题仍未解决！
    // Update：此问题可能已经找到解决方案！
    // Update：此问题已解决！
}