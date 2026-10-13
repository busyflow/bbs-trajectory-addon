package mchorse.bbs_trajectory.client.tool;

import mchorse.bbs_mod.api.client.editor.FilmEditorTool;
import mchorse.bbs_mod.film.FilmTarget;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.film.controller.UIFilmController;
import mchorse.bbs_mod.ui.film.replays.UIReplaysEditorUtils;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.input.drag.TransformSpace;
import mchorse.bbs_mod.ui.utils.Gizmo;
import mchorse.bbs_mod.ui.utils.GizmoDrag;
import mchorse.bbs_trajectory.client.TrajectoryState;

public class TrajectoryTransformTool extends FilmEditorTool
{
    private final UIFilmController controller;
    private final UITrajectoryPropTransform propTransform = new UITrajectoryPropTransform();

    public TrajectoryTransformTool(UIFilmController controller)
    {
        this.controller = controller;
    }

    @Override
    public FilmTarget target(FilmTarget original)
    {
        if (TrajectoryState.isActive(this.controller.panel))
        {
            Replay replay = TrajectoryState.getSelectedReplay(this.controller.panel);
            if (replay != null)
            {
                return FilmTarget.root(TransformSpace.GLOBAL);
            }
        }

        return null;
    }

    @Override
    public Boolean startGizmo(UIContext context, int stencilIndex)
    {
        if (!TrajectoryState.isActive(this.controller.panel))
        {
            return null;
        }

        Replay replay = TrajectoryState.getSelectedReplay(this.controller.panel);
        if (replay == null)
        {
            return null;
        }

        UIFilmPanel panel = this.controller.panel;
        float transition = panel.getRunner().getTransition(context.getTransition());
        float cursor = panel.getKeyframeCursor(transition);

        this.propTransform.syncFromReplay(replay, this.controller.getCurrentEntity(), cursor);

        GizmoDrag drag = UIReplaysEditorUtils.buildFilmGizmoDrag(
            panel,
            panel.getCamera(),
            panel.preview.getViewport(),
            this.propTransform,
            transition
        );

        if (drag == null)
        {
            return null;
        }

        boolean started = Gizmo.INSTANCE.start(stencilIndex, context.mouseX, context.mouseY, this.propTransform, drag);
        return started ? Boolean.TRUE : null;
    }

    @Override
    public void stopTool()
    {
        if (this.propTransform.isEditing())
        {
            this.propTransform.endGesture();
        }
    }
}
