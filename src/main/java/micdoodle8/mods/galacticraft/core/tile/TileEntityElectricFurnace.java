package micdoodle8.mods.galacticraft.core.tile;

import micdoodle8.mods.galacticraft.core.GCBlocks;
import micdoodle8.mods.galacticraft.core.blocks.BlockMachineTiered;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.energy.tile.EnergyStorageTile;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseElectricBlockWithInventory;
import micdoodle8.mods.galacticraft.core.network.IPacketReceiver;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.miccore.Annotations.NetworkedField;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.Set;

public class TileEntityElectricFurnace extends TileBaseElectricBlockWithInventory implements ISidedInventory, IPacketReceiver, IMachineSides
{
    //The electric furnace is 50% faster than a vanilla Furnace
    //but at a cost of some inefficiency:
    //It uses 46800 gJ to smelt 8 ingots quickly
    //compared with the energy generated by 1 coal which is 38400 gJ
    //
    //The efficiency can be increased using a Tier 2 furnace

    public static int PROCESS_TIME_REQUIRED = 130;

    @NetworkedField(targetSide = Side.CLIENT)
    public int processTimeRequired = PROCESS_TIME_REQUIRED;

    @NetworkedField(targetSide = Side.CLIENT)
    public int processTicks = 0;

    private ItemStack[] containingItems = new ItemStack[3];
    public final Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

    private boolean initialised = false;

    public TileEntityElectricFurnace()
    {
        this(1);
    }

    /*
     * @param tier: 1 = Electric Furnace  2 = Electric Arc Furnace
     */
    public TileEntityElectricFurnace(int tier)
    {
        this.initialised = true;
        if (tier == 1)
        {
            this.storage.setMaxExtract(ConfigManagerCore.hardMode ? 60 : 45);
            return;
        }

        this.setTier2();
    }

    private void setTier2()
    {
        this.storage.setCapacity(25000);
        this.storage.setMaxExtract(ConfigManagerCore.hardMode ? 90 : 60);
        this.processTimeRequired = 100;
        this.setTierGC(2);
    }

    @Override
    public void update()
    {
        if (!this.initialised)
        {
            int metadata = this.getBlockMetadata();
            //for version update compatibility
            Block b = this.worldObj.getBlockState(this.getPos()).getBlock();
            if (b == GCBlocks.machineBase)
            {
                this.worldObj.setBlockState(this.getPos(), GCBlocks.machineTiered.getDefaultState()/*,s 4*/, 2);
            }
            else if (metadata >= 8)
            {
                this.setTier2();
            }
            this.initialised = true;
        }

        super.update();

        if (!this.worldObj.isRemote)
        {
            if (this.canProcess())
            {
                if (this.hasEnoughEnergyToRun)
                {
                    //50% extra speed boost for Tier 2 machine if powered by Tier 2 power
                    if (this.tierGC == 2)
                    {
                        this.processTimeRequired = 200 / (1 + this.poweredByTierGC);
                    }

                    if (this.processTicks == 0)
                    {
                        this.processTicks = this.processTimeRequired;
                    }
                    else
                    {
                        if (--this.processTicks <= 0)
                        {
                            this.smeltItem();
                            this.processTicks = this.canProcess() ? this.processTimeRequired : 0;
                        }
                    }
                }
                else if (this.processTicks > 0 && this.processTicks < this.processTimeRequired)
                {
                    //Apply a "cooling down" process if the electric furnace runs out of energy while smelting
                    if (this.worldObj.rand.nextInt(4) == 0)
                    {
                        this.processTicks++;
                    }
                }
            }
            else
            {
                this.processTicks = 0;
            }
        }
    }

    /**
     * @return Is this machine able to process its specific task?
     */
    public boolean canProcess()
    {
        if (this.containingItems[1] == null || FurnaceRecipes.instance().getSmeltingResult(this.containingItems[1]) == null)
        {
            return false;
        }

        if (this.containingItems[2] != null)
        {
            if (!this.containingItems[2].isItemEqual(FurnaceRecipes.instance().getSmeltingResult(this.containingItems[1])))
            {
                return false;
            }

            if (this.containingItems[2].stackSize + 1 > 64)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Turn one item from the furnace source stack into the appropriate smelted
     * item in the furnace result stack
     */
    public void smeltItem()
    {
        if (this.canProcess())
        {
            ItemStack resultItemStack = FurnaceRecipes.instance().getSmeltingResult(this.containingItems[1]);

            if (this.containingItems[2] == null)
            {
                this.containingItems[2] = resultItemStack.copy();
                if (this.tierGC > 1)
                {
                    String nameSmelted = this.containingItems[1].getUnlocalizedName().toLowerCase();
                    if (resultItemStack.getUnlocalizedName().toLowerCase().contains("ingot") && (nameSmelted.contains("ore") || nameSmelted.contains("raw") || nameSmelted.contains("moon") || nameSmelted.contains("mars") || nameSmelted.contains("shard")))
                    {
                        this.containingItems[2].stackSize += resultItemStack.stackSize;
                    }
                }
            }
            else if (this.containingItems[2].isItemEqual(resultItemStack))
            {
                this.containingItems[2].stackSize += resultItemStack.stackSize;
                if (this.tierGC > 1)
                {
                    String nameSmelted = this.containingItems[1].getUnlocalizedName().toLowerCase();
                    if (resultItemStack.getUnlocalizedName().toLowerCase().contains("ingot") && (nameSmelted.contains("ore") || nameSmelted.contains("raw") || nameSmelted.contains("moon") || nameSmelted.contains("mars") || nameSmelted.contains("shard")))
                    {
                        this.containingItems[2].stackSize += resultItemStack.stackSize;
                    }
                }
            }

            this.containingItems[1].stackSize--;

            if (this.containingItems[1].stackSize <= 0)
            {
                this.containingItems[1] = null;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        if (this.storage.getEnergyStoredGC() > EnergyStorageTile.STANDARD_CAPACITY)
        {
            this.setTier2();
            this.initialised = true;
        }
        else
        {
            this.initialised = false;
        }
        this.processTicks = par1NBTTagCompound.getInteger("smeltingTicks");
        this.containingItems = this.readStandardItemsFromNBT(par1NBTTagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        if (this.tierGC == 1 && this.storage.getEnergyStoredGC() > EnergyStorageTile.STANDARD_CAPACITY)
        {
            this.storage.setEnergyStored(EnergyStorageTile.STANDARD_CAPACITY);
        }
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("smeltingTicks", this.processTicks);
        this.writeStandardItemsToNBT(par1NBTTagCompound);
    }

    @Override
    protected ItemStack[] getContainingItems()
    {
        return this.containingItems;
    }

    @Override
    public String getName()
    {
        return GCCoreUtil.translate(this.tierGC == 1 ? "tile.machine.2.name" : "tile.machine.7.name");
    }

//    @Override
//    public boolean hasCustomName()
//    {
//        return true;
//    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring
     * stack size) into the given slot.
     */
    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
    {
        if (itemStack == null)
        {
            return false;
        }
        return slotID == 1 ? FurnaceRecipes.instance().getSmeltingResult(itemStack) != null : slotID == 0 && ItemElectricBase.isElectricItem(itemStack.getItem());
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        return new int[] { 0, 1, 2 };
    }

    @Override
    public boolean canInsertItem(int slotID, ItemStack par2ItemStack, EnumFacing par3)
    {
        return this.isItemValidForSlot(slotID, par2ItemStack);
    }

    @Override
    public boolean canExtractItem(int slotID, ItemStack par2ItemStack, EnumFacing par3)
    {
        return slotID == 2;
    }

    @Override
    public boolean shouldUseEnergy()
    {
        return this.canProcess();
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return null;
    }

    @Override
    public EnumFacing getFront()
    {
        return this.worldObj.getBlockState(getPos()).getValue(BlockMachineTiered.FACING);
    }

    @Override
    public EnumFacing getElectricInputDirection()
    {
        switch (this.getSide(MachineSide.ELECTRIC_IN))
        {
        case RIGHT:
            return getFront().rotateYCCW();
        case REAR:
            return getFront().getOpposite();
        case TOP:
            return EnumFacing.UP;
        case BOTTOM:
            return EnumFacing.DOWN;
        case LEFT:
        default:
            return getFront().rotateY();
        }
    }

    //------------------
    //Added these methods and field to implement IMachineSides properly 
    //------------------
    @Override
    public MachineSide[] listConfigurableSides()
    {
        return new MachineSide[] { MachineSide.ELECTRIC_IN };
    }

    @Override
    public Face[] listDefaultFaces()
    {
        return new Face[] { Face.LEFT };
    }
    
    private MachineSidePack[] machineSides;

    @Override
    public MachineSidePack[] getAllMachineSides()
    {
        if (this.machineSides == null)
        {
            this.initialiseSides();
        }

        return this.machineSides;
    }

    @Override
    public void setupMachineSides(int length)
    {
        this.machineSides = new MachineSidePack[length];
    }
    
    @Override
    public void validate()
    {
        super.validate();
        this.clientValidate();
    }
    
    @Override
    public IMachineSidesProperties getConfigurationType()
    {
        return BlockMachineTiered.MACHINESIDES_RENDERTYPE;
    }
    //------------------END OF IMachineSides implementation
}