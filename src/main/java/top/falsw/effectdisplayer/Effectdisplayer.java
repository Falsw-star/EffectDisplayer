package top.falsw.effectdisplayer;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class Effectdisplayer implements ModInitializer {

    @Override
    public void onInitialize() {
        // 这个项目用于测试通过 1.19 的 Minecraft 引入的 DisplayEntity 制作一些特效的想法
        // 添加一些指令以便测试

        // 生成手中物品
        Command<ServerCommandSource> onhand = (context) -> {
            ServerCommandSource source = context.getSource();
            MinecraftServer server = source.getServer();
            World world = source.getWorld();
            PlayerEntity player = source.getPlayer();
            Vec3d pos = source.getPosition().add(0, 1, 0).offset(player.getHorizontalFacing(), 3); // 使初始位置稍作移动以便观察

            // 新建 ItemDisplayEntity
            DisplayEntity.ItemDisplayEntity displayEntity = new DisplayEntity.ItemDisplayEntity(EntityType.ITEM_DISPLAY, world);
            // 设置初始数据
            displayEntity.setItemStack(player.getMainHandStack());
            displayEntity.setPosition(pos);
            displayEntity.setTransformation(new AffineTransformation(
                    new Vector3f(0, 0, 0), // 平移
                    null, // 旋转
                    new Vector3f(1, 1, 1), // 缩放
                    null // 旋转
            ));
            // 设置插值， 单位为 tick
            displayEntity.setStartInterpolation(0);
            displayEntity.setInterpolationDuration(100);
            // 奇怪，有时插值不会生效，物块会直接变为变换后的样子
            server.execute(() -> {
                // 添加到世界
                world.spawnEntity(displayEntity);
                // 设置变换
                displayEntity.setTransformation(new AffineTransformation(
                        new Vector3f(0, 0, 0),
                        null,
                        new Vector3f(5, 0.2f, 5), // 压扁
                        null
                ));
            });
            source.sendFeedback(() -> Text.literal(
                    "生成于 " + pos
            ), false);
            return 1;
        };

        // 注册指令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("onhand").executes(onhand));
        });
    }
}
