/*
 *  Dynamic Surroundings
 *  Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.mobeffects.effects.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.particles.AgeableMote;
import org.orecruncher.mobeffects.footsteps.FootprintStyle;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class FootprintMote extends AgeableMote {

    // Texture properties of the print
    private static final float TEXEL_WIDTH = 1F / 8F;
    private static final float TEXEL_PRINT_WIDTH = TEXEL_WIDTH / 2F;

    // Basic layout of the footprint
    private static final float WIDTH = 0.125F;
    private static final float LENGTH = WIDTH * 2.0F;
    private static final Vec2 FIRST_POINT = new Vec2(-WIDTH, LENGTH);
    private static final Vec2 SECOND_POINT = new Vec2(WIDTH, LENGTH);
    private static final Vec2 THIRD_POINT = new Vec2(WIDTH, -LENGTH);
    private static final Vec2 FOURTH_POINT = new Vec2(-WIDTH, -LENGTH);

    // Micro Y adjuster to avoid z-fighting when rendering
    // multiple overlapping prints.
    private static float zFighter = 0F;

    protected final boolean isSnowLayer;
    protected final BlockPos downPos;

    protected final float texU1, texU2;
    protected final float texV1, texV2;
    protected final float scale;

    protected final Vec2 firstPoint;
    protected final Vec2 secondPoint;
    protected final Vec2 thirdPoint;
    protected final Vec2 fourthPoint;

    public FootprintMote(@Nonnull final FootprintStyle style, @Nonnull final BlockGetter world, final double x,
                         final double y, final double z, final float rotation, final float scale, final boolean isRight) {
        super(world, x, y, z);

        this.maxAge = 200;

        if (++zFighter > 20)
            zFighter = 1;

        final BlockState state = world.getBlockState(this.position);
        this.isSnowLayer = state.getBlock() == Blocks.SNOW;

        this.posY += zFighter * 0.001F;

        // Make sure that the down position is calculated from the display position!
        final float fraction = (float) (y - (int) y);
        if (this.isSnowLayer || fraction <= 0.0625F) {
            this.downPos = new BlockPos(this.posX, this.posY, this.posZ).below();
        } else {
            this.downPos = this.position.immutable();
        }

        float u1 = style.ordinal() * TEXEL_WIDTH + 1 / 256F;
        if (isRight)
            u1 += TEXEL_PRINT_WIDTH;
        this.texU1 = u1;
        this.texU2 = u1 + TEXEL_PRINT_WIDTH;
        this.texV1 = 0F;
        this.texV2 = 1F;
        this.scale = scale;

        // Rotate our vertex coordinates. Since prints are static doing the rotation on the vertex points during
        // constructions makes for a much more efficient render process.
        final float theRotation = MathStuff.toRadians(-rotation + 180);
        this.firstPoint = MathStuff.rotateScale(FIRST_POINT, theRotation, this.scale);
        this.secondPoint = MathStuff.rotateScale(SECOND_POINT, theRotation, this.scale);
        this.thirdPoint = MathStuff.rotateScale(THIRD_POINT, theRotation, this.scale);
        this.fourthPoint = MathStuff.rotateScale(FOURTH_POINT, theRotation, this.scale);
    }

    @Override
    protected boolean advanceAge() {
        // Footprints age faster when raining
        if (world instanceof Level && ((Level) world).isRaining())
            this.age += (WorldUtils.getRainStrength((Level) world, 1F) * 100F) / 25;
        return super.advanceAge();
    }

    @Override
    protected void update() {
        if (this.isSnowLayer && world.getBlockState(this.position).getBlock() != Blocks.SNOW) {
            kill();
        } else {
            final BlockState state = this.world.getBlockState(this.downPos);
            if (!state.getMaterial().isSolid())
                kill();
        }
    }

    @Override
    public void renderParticle(@Nonnull final VertexConsumer buffer, @Nonnull final Camera info, float partialTicks) {

        float f = (this.age + partialTicks) / ((float) this.maxAge + 1);
        f *= f;
        this.alpha = MathStuff.clamp1(1.0F - f) * 0.4F;

        final double x = renderX(info, partialTicks);
        final double y = renderY(info, partialTicks);
        final double z = renderZ(info, partialTicks);

        drawVertex(buffer, x + this.firstPoint.x, y, z + this.firstPoint.y, this.texU1, this.texV2);
        drawVertex(buffer, x + this.secondPoint.x, y, z + this.secondPoint.y, this.texU2, this.texV2);
        drawVertex(buffer, x + this.thirdPoint.x, y, z + this.thirdPoint.y, this.texU2, this.texV1);
        drawVertex(buffer, x + this.fourthPoint.x, y, z + this.fourthPoint.y, this.texU1, this.texV1);
    }

}