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

package org.orecruncher.mobeffects.footsteps.accents;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.mobeffects.config.Config;
import org.orecruncher.mobeffects.library.FootstepLibrary;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
class RainSplashAccent implements IFootstepAccentProvider {

    @Override
    public boolean isEnabled() {
        return Config.CLIENT.footsteps.enableRainSplashAccent.get();
    }

    @Override
    public void provide(
            @Nonnull final LivingEntity entity,
            @Nonnull final BlockPos blockPos,
            @Nonnull final BlockState posState,
            @Nonnull final ObjectArray<IAcoustic> acoustics)
    {
        final Level world = entity.getCommandSenderWorld();
        if (world.isRaining()) {
            // Get the precipitation type at the location
            final Biome.Precipitation rainType = WorldUtils.getCurrentPrecipitationAt(world, blockPos.above());
            if (rainType == Biome.Precipitation.RAIN)
                acoustics.add(FootstepLibrary.getRainSplashAcoustic());
        }
    }

}