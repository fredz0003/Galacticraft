package micdoodle8.mods.galacticraft.core.blocks;

import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseUniversalElectrical;
import micdoodle8.mods.galacticraft.core.items.ItemBlockDesc;
import micdoodle8.mods.galacticraft.core.tile.TileEntityCoalGenerator;
import micdoodle8.mods.galacticraft.core.tile.TileEntityElectricFurnace;
import micdoodle8.mods.galacticraft.core.tile.TileEntityEnergyStorageModule;
import micdoodle8.mods.galacticraft.core.tile.TileEntityIngotCompressor;
import micdoodle8.mods.galacticraft.core.util.EnumSortCategoryBlock;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class BlockMachine extends BlockTileGC implements ItemBlockDesc.IBlockShiftDesc, ISortableBlock
{
    public static final int COAL_GENERATOR_METADATA = 0;
    public static final int COMPRESSOR_METADATA = 12;

    /*private IIcon iconMachineSide;
    private IIcon iconOutput;

    private IIcon iconCoalGenerator;
    private IIcon iconCompressor;*/
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyEnum TYPE = PropertyEnum.create("type", EnumMachineType.class);

    public enum EnumMachineType implements IStringSerializable
    {
        COAL_GENERATOR(0, "coal_generator"),
        COMPRESSOR(3, "ingot_compressor"); // 3 for backwards compatibility

        private final int meta;
        private final String name;

        private EnumMachineType(int meta, String name)
        {
            this.meta = meta;
            this.name = name;
        }

        public int getMeta()
        {
            return this.meta;
        }

        public static EnumMachineType byMetadata(int meta)
        {
            switch (meta)
            {
            case 3:
                return COMPRESSOR;
            default:
                return COAL_GENERATOR;
            }
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    public BlockMachine(String assetName)
    {
        super(GCBlocks.machine);
        this.setHardness(1.0F);
        this.setStepSound(Block.soundTypeMetal);
        //this.setBlockTextureName(GalacticraftCore.TEXTURE_PREFIX + assetName);
        this.setUnlocalizedName(assetName);
    }

    @Override
    public CreativeTabs getCreativeTabToDisplayOn()
    {
        return GalacticraftCore.galacticraftBlocksTab;
    }

    @Override
    public int getRenderType()
    {
        return GalacticraftCore.proxy.getBlockRender(this);
    }

    /*@Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        this.blockIcon = iconRegister.registerIcon(GalacticraftCore.TEXTURE_PREFIX + "machine");
        this.iconOutput = iconRegister.registerIcon(GalacticraftCore.TEXTURE_PREFIX + "machine_output");
        this.iconMachineSide = iconRegister.registerIcon(GalacticraftCore.TEXTURE_PREFIX + "machine_side");

        this.iconCoalGenerator = iconRegister.registerIcon(GalacticraftCore.TEXTURE_PREFIX + "coalGenerator");
        this.iconCompressor = iconRegister.registerIcon(GalacticraftCore.TEXTURE_PREFIX + "compressor");
    }*/

    @Override
    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        TileEntity tile = worldIn.getTileEntity(pos);

        if (tile instanceof TileEntityCoalGenerator)
        {
            TileEntityCoalGenerator tileEntity = (TileEntityCoalGenerator) tile;
            if (tileEntity.heatGJperTick > 0)
            {
                int metadata = getMetaFromState(state);
                float particlePosX = pos.getX() + 0.5F;
                float particlePosY = pos.getY() + 0.0F + rand.nextFloat() * 6.0F / 16.0F;
                float particlePosZ = pos.getZ() + 0.5F;
                float particleSize0 = 0.52F;
                float particleSize1 = rand.nextFloat() * 0.6F - 0.3F;

                if (metadata == 0)
                {
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, particlePosX - particleSize0, particlePosY, particlePosZ + particleSize1, 0.0D, 0.0D, 0.0D);
                    worldIn.spawnParticle(EnumParticleTypes.FLAME, particlePosX - particleSize0, particlePosY, particlePosZ + particleSize1, 0.0D, 0.0D, 0.0D);
                }
                else if (metadata == 1)
                {
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, particlePosX + particleSize0, particlePosY, particlePosZ + particleSize1, 0.0D, 0.0D, 0.0D);
                    worldIn.spawnParticle(EnumParticleTypes.FLAME, particlePosX + particleSize0, particlePosY, particlePosZ + particleSize1, 0.0D, 0.0D, 0.0D);
                }
                else if (metadata == 2)
                {
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, particlePosX + particleSize1, particlePosY, particlePosZ + particleSize0, 0.0D, 0.0D, 0.0D);
                    worldIn.spawnParticle(EnumParticleTypes.FLAME, particlePosX + particleSize1, particlePosY, particlePosZ + particleSize0, 0.0D, 0.0D, 0.0D);
                }
                else if (metadata == 3)
                {
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, particlePosX + particleSize1, particlePosY, particlePosZ - particleSize0, 0.0D, 0.0D, 0.0D);
                    worldIn.spawnParticle(EnumParticleTypes.FLAME, particlePosX + particleSize1, particlePosY, particlePosZ - particleSize0, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    /*@Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
    {
        int metadata = world.getBlockMetadata(x, y, z);

        return this.getIcon(side, world.getBlockMetadata(x, y, z));
    }

    @Override
    public IIcon getIcon(int side, int metadata)
    {
        if (side == 0 || side == 1)
        {
            return this.blockIcon;
        }

        if (metadata >= BlockMachine.COMPRESSOR_METADATA)
        {
            metadata -= BlockMachine.COMPRESSOR_METADATA;

            if (metadata == 0 && side == 4 || metadata == 1 && side == 5 || metadata == 2 && side == 3 || metadata == 3 && side == 2)
            {
                return this.iconCompressor;
            }
        }
        else
        {
            // If it is the front side
            if (side == metadata + 2)
            {
                return this.iconOutput;
            }
            // If it is the back side
            if (metadata == 0 && side == 4 || metadata == 1 && side == 5 || metadata == 2 && side == 3 || metadata == 3 && side == 2)
            {
                return this.iconCoalGenerator;
            }
        }

        return this.iconMachineSide;
    }*/

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        int metadata = getMetaFromState(state);

        final int angle = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int change = EnumFacing.getHorizontal(angle).getOpposite().getHorizontalIndex();

        worldIn.setBlockState(pos, getStateFromMeta((metadata & 12) + change), 3);
    }

    @Override
    public boolean onUseWrench(World world, BlockPos pos, EntityPlayer entityPlayer, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        int metadata = getMetaFromState(world.getBlockState(pos));
        int change = world.getBlockState(pos).getValue(FACING).rotateY().getHorizontalIndex();

        if (metadata < BlockMachine.COMPRESSOR_METADATA)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileBaseUniversalElectrical)
            {
                ((TileBaseUniversalElectrical) te).updateFacing();
            }
        }

        world.setBlockState(pos, getStateFromMeta((metadata & 12) + change), 3);
        return true;
    }

    /**
     * Called when the block is right clicked by the player
     */
    @Override
    public boolean onMachineActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityPlayer, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        int metadata = getMetaFromState(world.getBlockState(pos));

        if (!world.isRemote)
        {
            if (metadata >= BlockMachine.COMPRESSOR_METADATA)
            {
                entityPlayer.openGui(GalacticraftCore.instance, -1, world, pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
            else
            {
                entityPlayer.openGui(GalacticraftCore.instance, -1, world, pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
        }

        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        int metadata = getMetaFromState(state) & 12;
        if (metadata == BlockMachine.COMPRESSOR_METADATA)
        {
            return new TileEntityIngotCompressor();
        }
        else if (metadata == 4)
        {
            return new TileEntityEnergyStorageModule();
        }
        else if (metadata == 8)
        {
            return new TileEntityElectricFurnace();
        }
        else
        {
            return new TileEntityCoalGenerator();
        }
    }

    public ItemStack getCompressor()
    {
        return new ItemStack(this, 1, BlockMachine.COMPRESSOR_METADATA);
    }

    public ItemStack getCoalGenerator()
    {
        return new ItemStack(this, 1, BlockMachine.COAL_GENERATOR_METADATA);
    }

    @Override
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List)
    {
        par3List.add(this.getCoalGenerator());
        par3List.add(this.getCompressor());
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return getMetaFromState(state) & 12;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player)
    {
        int metadata = this.getDamageValue(world, pos);

        return new ItemStack(this, 1, metadata);
    }

    @Override
    public String getShiftDescription(int meta)
    {
        switch (meta)
        {
        case COAL_GENERATOR_METADATA:
            return GCCoreUtil.translate("tile.coal_generator.description");
        case COMPRESSOR_METADATA:
            return GCCoreUtil.translate("tile.compressor.description");
        }
        return "";
    }

    @Override
    public boolean showDescription(int meta)
    {
        return true;
    }

    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing enumfacing = EnumFacing.getHorizontal(meta % 4);
        EnumMachineType type = EnumMachineType.byMetadata((int)Math.floor(meta / 4.0));
        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(TYPE, type);
    }

    public int getMetaFromState(IBlockState state)
    {
        return ((EnumFacing)state.getValue(FACING)).getHorizontalIndex() + ((EnumMachineType)state.getValue(TYPE)).getMeta() * 4;
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, FACING, TYPE);
    }

    @Override
    public EnumSortCategoryBlock getCategory(int meta)
    {
        return EnumSortCategoryBlock.MACHINE;
    }
}
