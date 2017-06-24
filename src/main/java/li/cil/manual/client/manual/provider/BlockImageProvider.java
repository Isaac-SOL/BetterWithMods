package li.cil.manual.client.manual.provider;

import com.google.common.base.Strings;
import li.cil.manual.api.API;
import li.cil.manual.api.manual.ImageProvider;
import li.cil.manual.api.manual.ImageRenderer;
import li.cil.manual.client.manual.segment.render.ItemStackImageRenderer;
import li.cil.manual.client.manual.segment.render.MissingItemRenderer;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;

public final class BlockImageProvider implements ImageProvider {
    private static final String WARNING_BLOCK_MISSING = API.MOD_ID + ".manual.warning.missing.block";

    @Override
    @Nullable
    public ImageRenderer getImage(final String data) {
        final int splitIndex = data.lastIndexOf('@');
        final String name, optMeta;
        if (splitIndex > 0) {
            name = data.substring(0, splitIndex);
            optMeta = data.substring(splitIndex);
        } else {
            name = data;
            optMeta = "";
        }
        final int meta = (Strings.isNullOrEmpty(optMeta)) ? 0 : parseMeta(optMeta.substring(1));
        final Block block = Block.REGISTRY.getObject(new ResourceLocation(name));
        if (Item.getItemFromBlock(block) != Items.AIR) {
            return new ItemStackImageRenderer(new ItemStack(block, 1, meta));
        } else {
            return new MissingItemRenderer(WARNING_BLOCK_MISSING);
        }
    }

    public int parseMeta(String optMeta) {
        if(optMeta=="*") {
            return OreDictionary.WILDCARD_VALUE;
        } else {
            return Integer.parseInt(optMeta);
        }
    }
}
