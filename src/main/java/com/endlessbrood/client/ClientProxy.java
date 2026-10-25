package com.endlessbrood.client;

import com.endlessbrood.common.CommonProxy;
import com.endlessbrood.common.EntityParasite;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
    @Override
    public void registerRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityParasite.class, new RenderParasite());
    }
}
