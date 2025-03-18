package top.falsw.effectdisplayer;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.slf4j.Logger;

public class Effectdisplayer implements ModInitializer {

    public static Logger LOGGER = org.slf4j.LoggerFactory.getLogger("Effectdisplayer");
    @Override
    public void onInitialize() {
        // 这个项目用于测试通过 1.19 的 Minecraft 引入的 DisplayEntity 制作一些特效的想法
        // 添加一些指令以便测试

        // 生成手中物品
        Command<ServerCommandSource> onhand = (context) -> {
            ServerCommandSource source = context.getSource();
            World world = source.getWorld();
            MinecraftServer server = source.getServer();
            PlayerEntity player = source.getPlayer();
            if (player == null) {
                source.sendError(Text.literal("仅玩家可使用此命令"));
                return 0;
            }
            Vec3d pos = source.getPosition(); // 使初始位置稍作移动以便观察？

            // 新建 ItemDisplayEntity
            ModItemDisplayEntity displayEntity = new ModItemDisplayEntity(
                    world,
                    pos,
                    player.getMainHandStack()
            ).spawn();
            displayEntity.at(0).setAnimation(new Animation(displayEntity)
                    .at(0)
                    // animation here
                    .addMovement((entity, animation) -> entity.translation(new Vector3f(0, 10, 0)).addTransformation()).next()
                    .addMovement((entity, animation) -> entity.translation(new Vector3f(0, -10, 0)).addTransformation()).next()
                    .repeat(5)
                    .autoDelete()
            );

            source.sendFeedback(() -> Text.literal(
                    "生成于 " + pos
            ), false);
            return 1;
        };

        // 注册指令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("onhand").executes(onhand));
        });
        // 监听服务器关闭事件
        ServerLifecycleEvents.SERVER_STOPPING.register((MinecraftServer server) -> {
            server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/kill @e[type=item_display]");
        });
    }
}
