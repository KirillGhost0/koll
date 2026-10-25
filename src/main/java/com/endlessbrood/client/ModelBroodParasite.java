package com.endlessbrood.client;

import com.endlessbrood.common.EntityParasite;
import com.endlessbrood.common.ParasiteGenome;
import com.endlessbrood.common.ParasiteGenes;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/**
 * Six original, animated Minecraft-style parasite meshes. No external model
 * library, OBJ importer or dependency is required on the client.
 *
 * 0: Stalker (four legged, long jaw); 1: Scuttler (six legs);
 * 2: Bulwark (heavy plated); 3: Thornback (six legs and spines);
 * 4: Colossus (large six-legged alpha); 5: Matriarch (citadel leader).
 * Each variant uses its own 128x64 skin, but shares the same texture atlas UVs.
 */
public final class ModelBroodParasite extends ModelBase {
    private static final float DEG = (float) Math.PI / 180.0F;
    private final Variant[] variants = new Variant[6];

    public ModelBroodParasite() {
        textureWidth = 128;
        textureHeight = 64;
        variants[0] = stalker();
        variants[1] = scuttler();
        variants[2] = bulwark();
        variants[3] = thornback();
        variants[4] = colossus();
        variants[5] = matriarch();
    }

    private ModelRenderer box(int u, int v, float px, float py, float pz,
                              float x, float y, float z, int width, int height, int depth) {
        ModelRenderer part = new ModelRenderer(this, u, v);
        part.addBox(x, y, z, width, height, depth);
        part.setRotationPoint(px, py, pz);
        return part;
    }

    private static final class Variant {
        ModelRenderer[] parts;
        ModelRenderer[] legs;
        ModelRenderer head;
        ModelRenderer jaw;
        ModelRenderer tail;
        float[] baseZ;
        ModelRenderer extraPlate;
        ModelRenderer extraSpore;
        ModelRenderer extraHorn;
    }

    private Variant make(int count) {
        Variant result = new Variant();
        result.parts = new ModelRenderer[count];
        result.legs = new ModelRenderer[6];
        result.baseZ = new float[6];
        // Variable phenotypes: append-only render parts; avoid creating new entities.
        result.extraPlate = box(0, 0, 0, 7, 0, -5, -3, -5, 10, 2, 10);
        result.extraSpore = box(65, 28, 0, 7, 4, -3, -3, -3, 6, 5, 6);
        result.extraHorn = box(0, 52, 0, 6, -10, -1, -7, -2, 2, 8, 4);
        return result;
    }

    private Variant stalker() {
        Variant v = make(14);
        v.parts[0] = box(0, 0, 0, 13, 1, -4, -4, -8, 8, 7, 15); // ribcage
        v.head = box(62, 0, 0, 12, -7, -4, -3, -5, 8, 6, 9);
        v.parts[1] = v.head;
        v.jaw = box(25, 30, 0, 15, -10, -4, 0, -4, 8, 2, 7);
        v.parts[2] = v.jaw;
        for (int i = 0; i < 4; ++i) {
            float px = i % 2 == 0 ? -4 : 4;
            float pz = i < 2 ? -5 : 6;
            ModelRenderer leg = box(0, 30, px, 15, pz, px < 0 ? -3 : 0, 0, -2, 3, 9, 4);
            v.legs[i] = leg;
            v.parts[3 + i] = leg;
        }
        v.tail = box(82, 28, 0, 14, 8, -1, -1, 0, 2, 3, 9);
        v.tail.rotateAngleX = -20 * DEG;
        v.parts[7] = v.tail;
        for (int i = 0; i < 5; ++i) {
            ModelRenderer spike = box(65, 28, 0, 8, -5 + i * 3, -1, -5, -1, 2, 6, 2);
            spike.rotateAngleX = -25 * DEG;
            v.parts[8 + i] = spike;
        }
        v.parts[13] = box(0, 52, 0, 10, -11, -1, -2, -6, 2, 4, 4); // sensory horn
        return v;
    }

    private Variant scuttler() {
        Variant v = make(15);
        v.parts[0] = box(0, 0, 0, 18, 0, -5, -3, -7, 10, 5, 14);
        v.head = box(62, 0, 0, 17, -8, -5, -2, -4, 10, 4, 8);
        v.parts[1] = v.head;
        v.jaw = box(25, 30, 0, 19, -11, -5, 0, -3, 10, 2, 6);
        v.parts[2] = v.jaw;
        for (int i = 0; i < 6; ++i) {
            int row = i / 2;
            float px = (i % 2 == 0) ? -5.0F : 5.0F;
            float pz = -5 + row * 6;
            ModelRenderer leg = box(0, 30, px, 19, pz, px < 0 ? -2 : 0, -1, -1, 2, 6, 2);
            leg.rotateAngleZ = px < 0 ? -20 * DEG : 20 * DEG;
            v.baseZ[i] = leg.rotateAngleZ;
            v.legs[i] = leg;
            v.parts[3 + i] = leg;
        }
        v.tail = box(82, 28, 0, 19, 6, -1, -2, 0, 2, 3, 10);
        v.parts[9] = v.tail;
        v.parts[10] = box(0, 52, -3, 16, -13, -1, -4, -1, 2, 5, 2);
        v.parts[11] = box(0, 52, 3, 16, -13, -1, -4, -1, 2, 5, 2);
        for (int i = 0; i < 3; ++i) {
            v.parts[12 + i] = box(65, 28, 0, 14, -4 + i * 5, -1, -3, -1, 2, 4, 2);
        }
        return v;
    }

    private Variant bulwark() {
        Variant v = make(16);
        v.parts[0] = box(0, 0, 0, 12, 1, -7, -5, -7, 14, 10, 14);
        v.head = box(62, 0, 0, 10, -8, -5, -4, -5, 10, 8, 9);
        v.parts[1] = v.head;
        v.jaw = box(25, 30, 0, 15, -11, -5, 0, -3, 10, 3, 8);
        v.parts[2] = v.jaw;
        for (int i = 0; i < 4; ++i) {
            float px = i % 2 == 0 ? -6 : 6;
            float pz = i < 2 ? -4 : 6;
            ModelRenderer leg = box(0, 30, px, 14, pz, px < 0 ? -4 : -1, 0, -3, 5, 11, 6);
            v.legs[i] = leg;
            v.parts[3 + i] = leg;
        }
        v.tail = box(82, 28, 0, 12, 7, -2, -2, 0, 4, 4, 10);
        v.parts[7] = v.tail;
        v.parts[8] = box(0, 0, -6, 6, -2, -3, 0, -5, 5, 5, 10); // left shield plate
        v.parts[9] = box(0, 0, 6, 6, -2, -2, 0, -5, 5, 5, 10);  // right plate
        for (int i = 0; i < 6; ++i) {
            v.parts[10 + i] = box(65, 28, i % 2 == 0 ? -5 : 5, 7,
                    -6 + (i / 2) * 5, -1, -6, -1, 2, 7, 3);
        }
        return v;
    }

    private Variant thornback() {
        Variant v = make(18);
        v.parts[0] = box(0, 0, 0, 14, 0, -4, -4, -8, 8, 7, 16);
        v.head = box(62, 0, 0, 13, -8, -4, -3, -5, 8, 5, 9);
        v.parts[1] = v.head;
        v.jaw = box(25, 30, 0, 16, -12, -4, 0, -3, 8, 2, 8);
        v.parts[2] = v.jaw;
        for (int i = 0; i < 6; ++i) {
            float px = i % 2 == 0 ? -4 : 4;
            float pz = -6 + (i / 2) * 6;
            ModelRenderer leg = box(0, 30, px, 16, pz, px < 0 ? -3 : 0, 0, -2, 3, 9, 4);
            leg.rotateAngleZ = px < 0 ? -13 * DEG : 13 * DEG;
            v.baseZ[i] = leg.rotateAngleZ;
            v.legs[i] = leg;
            v.parts[3 + i] = leg;
        }
        v.tail = box(82, 28, 0, 14, 8, -1, -2, 0, 2, 3, 12);
        v.parts[9] = v.tail;
        for (int i = 0; i < 8; ++i) {
            float x = i % 2 == 0 ? -3 : 3;
            float z = -7 + (i / 2) * 5;
            ModelRenderer spike = box(65, 28, x, 9, z, -1, -7, -1, 2, 8, 2);
            spike.rotateAngleZ = x < 0 ? -18 * DEG : 18 * DEG;
            v.parts[10 + i] = spike;
        }
        return v;
    }

    /** An original armored alpha with a broad carapace, jaw and paired stalks. */
    private Variant colossus() {
        Variant v = make(20);
        v.parts[0] = box(0, 0, 0, 12, 1, -8, -6, -9, 16, 11, 19);
        v.head = box(62, 0, 0, 11, -10, -7, -5, -5, 14, 9, 11);
        v.parts[1] = v.head;
        v.jaw = box(25, 30, 0, 18, -14, -6, 0, -3, 12, 3, 10);
        v.parts[2] = v.jaw;
        for (int i = 0; i < 6; ++i) {
            float px = i % 2 == 0 ? -7 : 7;
            float pz = -7 + (i / 2) * 7;
            ModelRenderer leg = box(0, 30, px, 14, pz, px < 0 ? -4 : 0, -1, -3, 4, 13, 6);
            v.legs[i] = leg;
            v.parts[3 + i] = leg;
        }
        v.tail = box(82, 28, 0, 13, 10, -2, -2, 0, 4, 5, 12);
        v.parts[9] = v.tail;
        v.parts[10] = box(0, 0, -7, 7, 0, -3, -4, -5, 4, 7, 13);
        v.parts[11] = box(0, 0, 7, 7, 0, -1, -4, -5, 4, 7, 13);
        for (int i = 0; i < 8; ++i) {
            float px = i % 2 == 0 ? -5 : 5;
            float pz = -9 + (i / 2) * 5;
            ModelRenderer spine = box(65, 28, px, 6, pz, -1, -7, -1, 2, 9, 2);
            spine.rotateAngleZ = px < 0 ? -20 * DEG : 20 * DEG;
            v.parts[12 + i] = spine;
        }
        return v;
    }

    /** A sixth genuine mesh: the citadel matriarch with crown, abdomen and six legs. */
    private Variant matriarch() {
        Variant v = make(21);
        v.parts[0] = box(0, 0, 0, 12, 3, -7, -6, -8, 14, 11, 15);
        v.parts[1] = box(0, 0, 0, 13, 8, -8, -7, 0, 16, 10, 12); // swollen rear sac
        v.head = box(62, 0, 0, 9, -10, -6, -5, -5, 12, 8, 10);
        v.parts[2] = v.head;
        v.jaw = box(25, 30, 0, 17, -13, -6, 0, -3, 12, 3, 9);
        v.parts[3] = v.jaw;
        for (int i = 0; i < 6; ++i) {
            float x = i % 2 == 0 ? -7 : 7;
            float z = -5 + (i / 2) * 7;
            ModelRenderer leg = box(0, 30, x, 15, z, x < 0 ? -4 : 0, -1, -3, 4, 11, 5);
            leg.rotateAngleZ = x < 0 ? -16 * DEG : 16 * DEG;
            v.baseZ[i] = leg.rotateAngleZ;
            v.legs[i] = leg;
            v.parts[4 + i] = leg;
        }
        v.tail = box(82, 28, 0, 15, 12, -2, -3, 0, 4, 6, 13);
        v.parts[10] = v.tail;
        for (int i = 0; i < 4; ++i) {
            float x = i % 2 == 0 ? -5 : 5;
            ModelRenderer horn = box(65, 28, x, 6, -11 + (i / 2) * 5,
                -1, -9, -1, 2, 10, 3);
            horn.rotateAngleZ = x < 0 ? -22 * DEG : 22 * DEG;
            v.parts[11 + i] = horn;
        }
        for (int i = 0; i < 6; ++i) {
            v.parts[15 + i] = box(0, 52, (i % 2 == 0 ? -4 : 4), 6,
                5 + (i / 2) * 3, -2, -3, -1, 4, 4, 4);
        }
        return v;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount,
                       float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks,
                     netHeadYaw, headPitch, scale);
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks,
                          netHeadYaw, headPitch, scale, entity);
        int morph = entity instanceof EntityParasite ? ((EntityParasite) entity).getMorph() : 0;
        if (morph < 0 || morph >= variants.length) morph = 0;
        Variant variant = variants[morph];
        for (ModelRenderer part : variant.parts) {
            if (part != null) part.render(scale);
        }
        if (entity instanceof EntityParasite) {
            EntityParasite parasite = (EntityParasite) entity;
            int traits = parasite.getGenome();
            if ((traits & ParasiteGenome.PLATES) != 0 ||
                parasite.getGene() == ParasiteGenes.BRUTE) variant.extraPlate.render(scale);
            if ((traits & ParasiteGenome.SPORES) != 0) variant.extraSpore.render(scale);
            if ((traits & ParasiteGenome.SWIFT) != 0) variant.extraHorn.render(scale);
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale, Entity entity) {
        int morph = entity instanceof EntityParasite ? ((EntityParasite) entity).getMorph() : 0;
        if (morph < 0 || morph >= variants.length) morph = 0;
        Variant v = variants[morph];
        v.head.rotateAngleY = MathHelper.clamp_float(netHeadYaw * DEG, -0.65F, 0.65F);
        v.head.rotateAngleX = MathHelper.clamp_float(headPitch * DEG, -0.45F, 0.45F);
        v.jaw.rotateAngleX = 0.08F + 0.19F * (1.0F + MathHelper.sin(ageInTicks * 0.29F));
        v.tail.rotateAngleY = MathHelper.sin(ageInTicks * 0.12F) * 0.22F;
        for (int i = 0; i < v.legs.length; ++i) {
            if (v.legs[i] == null) continue;
            // Alternating tripod gait for six legs; opposite stance for four legs.
            float phase = (i % 2 == 0 ? 0.0F : (float) Math.PI) +
                    (v.legs[4] != null && i >= 4 ? (float) Math.PI : 0.0F);
            v.legs[i].rotateAngleX = MathHelper.cos(limbSwing * 0.67F + phase) *
                    0.75F * limbSwingAmount;
            v.legs[i].rotateAngleZ = v.baseZ[i] +
                    MathHelper.sin(limbSwing * 0.67F + phase) * 0.12F * limbSwingAmount;
        }
    }
}
