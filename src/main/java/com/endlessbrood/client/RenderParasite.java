package com.endlessbrood.client;

import com.endlessbrood.EndlessBrood;
import com.endlessbrood.common.EntityParasite;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;
import com.endlessbrood.common.ParasiteGenome;
import net.minecraft.util.ResourceLocation;

/** Six skins and six matching original blocky meshes. */
public final class RenderParasite extends RenderLiving {
    private static final ResourceLocation[] SKINS = new ResourceLocation[] {
        new ResourceLocation(EndlessBrood.MODID, "textures/entity/stalker.png"),
        new ResourceLocation(EndlessBrood.MODID, "textures/entity/scuttler.png"),
        new ResourceLocation(EndlessBrood.MODID, "textures/entity/bulwark.png"),
        new ResourceLocation(EndlessBrood.MODID, "textures/entity/thornback.png"),
        new ResourceLocation(EndlessBrood.MODID, "textures/entity/colossus.png"),
        new ResourceLocation(EndlessBrood.MODID, "textures/entity/matriarch.png")
    };

    public RenderParasite() {
        super(new ModelBroodParasite(), 0.45F);
    }

    /** Size differences reflect inherited mutations, beyond the five base shapes. */
    @Override
    protected void preRenderCallback(EntityLivingBase entity, float partialTick) {
        if (!(entity instanceof EntityParasite)) return;
        int genome = ((EntityParasite) entity).getGenome();
        float broad = (genome & ParasiteGenome.PLATES) != 0 ? 1.09F : 1.0F;
        float tall = (genome & ParasiteGenome.SWIFT) != 0 ? 1.07F : 1.0F;
        // The colossus is already built as the largest mesh; extra scaling is modest.
        float boss = ((EntityParasite) entity).getMorph() == 4 ? 1.18F :
            ((EntityParasite) entity).getMorph() == 5 ? 1.10F : 1.0F;
        GL11.glScalef(broad * boss, tall * boss, broad * boss);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        int kind = entity instanceof EntityParasite ? ((EntityParasite) entity).getMorph() : 0;
        return SKINS[kind >= 0 && kind < SKINS.length ? kind : 0];
    }
}
