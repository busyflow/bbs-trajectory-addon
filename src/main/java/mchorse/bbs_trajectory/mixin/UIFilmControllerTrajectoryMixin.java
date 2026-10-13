package mchorse.bbs_trajectory.mixin;

import mchorse.bbs_mod.film.FilmTarget;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.film.controller.UIFilmController;
import mchorse.bbs_mod.ui.framework.elements.input.drag.TransformSpace;
import mchorse.bbs_trajectory.client.TrajectoryState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = UIFilmController.class, remap = false)
public abstract class UIFilmControllerTrajectoryMixin
{
    @Shadow
    public UIFilmPanel panel;

    @Inject(method = "getEditTarget", at = @At("HEAD"), cancellable = true)
    private void bbs_trajectory$overrideEditTarget(CallbackInfoReturnable<FilmTarget> cir)
    {
        if (TrajectoryState.isActive(this.panel))
        {
            Replay replay = TrajectoryState.getSelectedReplay(this.panel);
            if (replay != null)
            {
                cir.setReturnValue(FilmTarget.root(TransformSpace.GLOBAL));
            }
        }
    }
}
