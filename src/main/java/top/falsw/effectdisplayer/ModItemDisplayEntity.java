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

public class ModItemDisplayEntity extends DisplayEntity.ItemDisplayEntity {

    static final Logger LOGGER = LogUtils.getLogger();

    private int maxAge;
    private boolean maxAgeSet = false;
    private LinkedList<NbtCompound> nbtList = new LinkedList<>();
    private int startInterpolation = 0;
    private int interpolationDuration = 20;
    private AffineTransformation other_transformation = new AffineTransformation(
            new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf()
    );

    public ModItemDisplayEntity spawn() {
        this.getWorld().spawnEntity(this);
        return this;
    }

    public ModItemDisplayEntity(World world, Vec3d pos, ItemStack itemStack) {
        super(EntityType.ITEM_DISPLAY, world);
        super.setPosition(pos);
        super.setItemStack(itemStack);
        this.maxAge = this.age;
    }

    public void setInterpolation(int startInterpolation, int interpolationDuration) {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        nbt.putInt("start_interpolation", startInterpolation);
        nbt.putInt("interpolation_duration", interpolationDuration);
        nbtList.add(nbt);
    }

    public ModItemDisplayEntity setMaxAge(int maxAge) {
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
        if (!nbtList.isEmpty()) {
            this.readNbt(nbtList.removeFirst());
        }
    }

    public void setTransformation(AffineTransformation other, boolean add) {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        nbt.putInt("start_interpolation", startInterpolation);
        nbt.putInt("interpolation_duration", interpolationDuration);
        if (!nbt.contains("transformation")) {
            AffineTransformation.ANY_CODEC
                    .encodeStart(NbtOps.INSTANCE, AffineTransformation.identity())
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

    public ModItemDisplayEntity translation(Vector3f vector3f) {
        other_transformation = new AffineTransformation(
                vector3f,
                other_transformation.getLeftRotation(),
                other_transformation.getScale(),
                other_transformation.getRightRotation()
        );
        return this;
    }

    public ModItemDisplayEntity rotation(Quaternionf leftRotation, Quaternionf rightRotation) {
        other_transformation = new AffineTransformation(
                other_transformation.getTranslation(),
                leftRotation != null ? leftRotation : other_transformation.getLeftRotation(),
                other_transformation.getScale(),
                rightRotation != null ? rightRotation : other_transformation.getRightRotation()
        );
        return this;
    }

    public ModItemDisplayEntity scale(Vector3f scale) {
        other_transformation = new AffineTransformation(
                other_transformation.getTranslation(),
                other_transformation.getLeftRotation(),
                scale,
                other_transformation.getRightRotation()
        );
        return this;
    }

    public void setTransformation() {
        setTransformation(other_transformation, false);
        other_transformation = new AffineTransformation(
                new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf()
        );
    }

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