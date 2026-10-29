package com.example.necromancy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(NecromancyMod.MODID)
public class NecromancyMod {
    public static final String MODID = "necromancy";
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    public static final RegistryObject<Item> SOUL =
            ITEMS.register("soul", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NECROMANCER_STAFF =
            ITEMS.register("necromancer_staff", () -> new NecromancerStaffItem(new Item.Properties().durability(180)));
    public static final RegistryObject<Block> SOUL_ALTAR =
            BLOCKS.register("soul_altar", () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE).strength(3.0F, 6.0F)
                    .sound(SoundType.STONE).lightLevel(state -> 7)));
    public static final RegistryObject<Item> SOUL_ALTAR_ITEM =
            ITEMS.register("soul_altar", () -> new BlockItem(SOUL_ALTAR.get(), new Item.Properties()));

    public NecromancyMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        BLOCKS.register(bus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (event.getEntity() instanceof Player) return;
        if (player.getRandom().nextFloat() > 0.22F) return;
        ItemStack soul = new ItemStack(SOUL.get());
        if (!player.getInventory().add(soul)) player.drop(soul, false);
        player.displayClientMessage(Component.literal("§5Получена душа"), true);
    }

    public static class NecromancerStaffItem extends Item {
        public NecromancerStaffItem(Properties properties) { super(properties); }

        @Override
        public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
            Level level = context.getLevel();
            Player player = context.getPlayer();
            if (player == null) return InteractionResult.PASS;
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
            if (level.isClientSide) return InteractionResult.SUCCESS;

            ItemStack staff = context.getItemInHand();
            if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;
            if (!player.isCreative() && !hasSoul(player)) {
                player.displayClientMessage(Component.literal("§5Нужна душа для призыва!"), true);
                return InteractionResult.CONSUME;
            }

            Mob minion;
            if (player.getRandom().nextBoolean()) {
                minion = EntityType.SKELETON.create(serverLevel);
            } else {
                minion = EntityType.ZOMBIE.create(serverLevel);
            }
            if (minion == null) return InteractionResult.FAIL;
            minion.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    player.getYRot(), 0);
            minion.setCustomName(Component.literal("§5Призванная нежить"));
            minion.setPersistenceRequired();
            if (!serverLevel.noCollision(minion, minion.getBoundingBox()) || !serverLevel.addFreshEntity(minion)) {
                player.displayClientMessage(Component.literal("§cЗдесь нельзя призвать нежить"), true);
                return InteractionResult.CONSUME;
            }
            if (!player.isCreative()) consumeSoul(player);
            staff.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            player.displayClientMessage(Component.literal("§5Нежить призвана!"), true);
            return InteractionResult.CONSUME;
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            // Right-click air summons at a nearby position in front of the player.
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;
            if (!player.isCreative() && !hasSoul(player)) {
                player.displayClientMessage(Component.literal("§5Нужна душа для призыва!"), true);
                return InteractionResult.CONSUME;
            }
            BlockPos pos = player.blockPosition().relative(player.getDirection(), 2);
            Mob minion = player.getRandom().nextBoolean()
                    ? EntityType.SKELETON.create(serverLevel) : EntityType.ZOMBIE.create(serverLevel);
            if (minion == null) return InteractionResult.FAIL;
            minion.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), 0);
            minion.setCustomName(Component.literal("§5Призванная нежить"));
            minion.setPersistenceRequired();
            if (!serverLevel.noCollision(minion, minion.getBoundingBox()) || !serverLevel.addFreshEntity(minion)) {
                player.displayClientMessage(Component.literal("§cЗдесь нельзя призвать нежить"), true);
                return InteractionResult.CONSUME;
            }
            if (!player.isCreative()) consumeSoul(player);
            player.getItemInHand(hand).hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            player.displayClientMessage(Component.literal("§5Нежить призвана!"), true);
            return InteractionResult.CONSUME;
        }

        private boolean hasSoul(Player player) {
            return player.getInventory().contains(new ItemStack(SOUL.get()));
        }
        private void consumeSoul(Player player) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(SOUL.get())) {
                    stack.shrink(1);
                    return;
                }
            }
        }
    }
}
