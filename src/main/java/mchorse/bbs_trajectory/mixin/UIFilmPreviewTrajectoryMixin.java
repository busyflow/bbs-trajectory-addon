package mchorse.bbs_trajectory.mixin;

import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.film.UIFilmPreview;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIIcon;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import mchorse.bbs_mod.utils.Direction;
import mchorse.bbs_trajectory.client.TrajectoryState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = UIFilmPreview.class, remap = false)
public abstract class UIFilmPreviewTrajectoryMixin
{
    @Shadow
    public UIElement icons;

    @Shadow
    private UIFilmPanel panel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void bbs_trajectory$onInit(UIFilmPanel filmPanel, CallbackInfo ci)
    {
        TrajectoryState.setPanel(filmPanel);

        UIIcon trajectoryIcon = new UIIcon(
            Icons.ALL_DIRECTIONS,
            (b) -> TrajectoryState.toggle(this.panel)
        );
        trajectoryIcon.highlight(
            () -> TrajectoryState.isActive(this.panel),
            Direction.BOTTOM
        );
        trajectoryIcon.tooltip(IKey.raw("Transform Replay Trajectory (Gizmo)"));
        trajectoryIcon.context((menu) ->
        {
            menu.action(Icons.MOVE_TO, IKey.raw("Pivot at Start Frame (0)"), () -> TrajectoryState.setPivotMode(TrajectoryState.PivotMode.START_FRAME));
            menu.action(Icons.MAIN_HANDLE, IKey.raw("Pivot at Current Cursor"), () -> TrajectoryState.setPivotMode(TrajectoryState.PivotMode.CURRENT_CURSOR));
            menu.action(Icons.REFRESH, IKey.raw("Reset Trajectory Pivot"), () -> TrajectoryState.resetPivot(this.panel));
        });

        this.icons.add(trajectoryIcon);
        this.icons.row().resize();
    }
}
