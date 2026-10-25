package net.minecraft.client.renderer.entity;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
public class RenderLiving {
    public RenderLiving(ModelBase model,float shadow){}
    protected ResourceLocation getEntityTexture(Entity entity){return null;}
    protected void preRenderCallback(EntityLivingBase entity,float tick){}
}
