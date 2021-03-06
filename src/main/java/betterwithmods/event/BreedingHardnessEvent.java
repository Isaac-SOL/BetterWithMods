package betterwithmods.event;

import betterwithmods.common.items.ItemBreedingHarness;
import betterwithmods.util.InvUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import static net.minecraft.network.datasync.DataSerializers.ITEM_STACK;

/**
 * Purpose:
 *
 * @author Tyler Marshall
 * @version 11/27/16
 */
public class BreedingHardnessEvent {

    private static final String TAG_HARNESS = "betterwithmods:harness";

    private static final DataParameter<ItemStack> COW_DATA = EntityDataManager.createKey(EntityCow.class, ITEM_STACK),
            PIG_DATA = EntityDataManager.createKey(EntityPig.class, ITEM_STACK),
            SHEEP_DATA = EntityDataManager.createKey(EntitySheep.class, ITEM_STACK);

    private static DataParameter<ItemStack> getHarnessData(Entity e) {
        if (e instanceof EntityCow)
            return COW_DATA;
        else if (e instanceof EntityPig)
            return PIG_DATA;
        else if (e instanceof EntitySheep)
            return SHEEP_DATA;
        else
            return null;
    }

    public static boolean isValidAnimal(Entity animal) {
        return animal instanceof EntityCow || animal instanceof EntityPig || animal instanceof EntitySheep;
    }

    public static ItemStack getHarness(EntityLivingBase living) {
        return living.getDataManager().get(getHarnessData(living));
    }


    @SubscribeEvent
    public void onEntityInit(EntityEvent.EntityConstructing event) {
        if (isValidAnimal(event.getEntity())) {
            EntityDataManager manager = event.getEntity().getDataManager();
            manager.register(getHarnessData(event.getEntity()), ItemStack.EMPTY);
        }
    }

    @SubscribeEvent
    public void onEntity(EntityJoinWorldEvent e) {
        if (isValidAnimal(e.getEntity())) {
            EntityLiving animal = (EntityLiving) e.getEntity();
            ItemStack dataStack = animal.getDataManager().get(getHarnessData(animal));

            ItemStack nbtStack = ItemStack.EMPTY;
            if(animal.getEntityData().hasKey(TAG_HARNESS)) {
                NBTTagCompound cmp = animal.getEntityData().getCompoundTag(TAG_HARNESS);
                if (!cmp.hasNoTags())
                    nbtStack = new ItemStack(cmp);
            }
            if (dataStack != nbtStack)
                e.getEntity().getDataManager().set(getHarnessData(e.getEntity()), nbtStack);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != EnumHand.MAIN_HAND)
            return;
        Entity target = event.getTarget();
        EntityPlayer player = event.getEntityPlayer();
        World world = event.getWorld();
        if (isValidAnimal(target) && !target.getPassengers().contains(player)) {
            EntityLiving animal = (EntityLiving) target;
            IItemHandlerModifiable playerInv = (IItemHandlerModifiable) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

            ItemStack harness = getHarness(animal);
            ItemStack held = player.getHeldItemMainhand();
            if (!harness.isEmpty()) {
                if (held.isEmpty()) {
                    InvUtils.insert(playerInv, harness, false);
                    animal.getDataManager().set(getHarnessData(animal), ItemStack.EMPTY);
                    animal.getEntityData().setTag(TAG_HARNESS, new NBTTagCompound());
                    world.playSound(null, animal.getPosition(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.NEUTRAL, 0.5f, 1.3f);
                    world.playSound(null, animal.getPosition(), SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundCategory.NEUTRAL, 0.5f, 1.3f);
                }
            } else if (held.getItem() instanceof ItemBreedingHarness) {
                if (!getHarness(animal).isEmpty())
                    return;
                ItemStack copyStack = held.copy();
                InvUtils.consumeItemsInInventory(playerInv, held, 1, false);
                copyStack.setCount(1);
                NBTTagCompound cmp = new NBTTagCompound();
                copyStack.writeToNBT(cmp);
                animal.getDataManager().set(getHarnessData(animal), copyStack);
                animal.getEntityData().setTag(TAG_HARNESS, cmp);
                if (animal instanceof EntitySheep)
                    ((EntitySheep) animal).setSheared(true);
                world.playSound(null, animal.getPosition(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.NEUTRAL, 1, 1);
                world.playSound(null, animal.getPosition(), SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundCategory.NEUTRAL, 1, 1f);
                player.swingArm(EnumHand.MAIN_HAND);
            }
        }
    }

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingUpdateEvent e) {
        EntityLivingBase entity = e.getEntityLiving();
        if (isValidAnimal(entity)) {
            if (!getHarness(entity).isEmpty())
                entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(-1);
            else
                entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25);
        }
    }

}
