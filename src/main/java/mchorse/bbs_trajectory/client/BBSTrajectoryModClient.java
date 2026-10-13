package mchorse.bbs_trajectory.client;

import mchorse.bbs_mod.api.client.events.FilmGizmoEvents;
import mchorse.bbs_mod.ui.film.controller.UIFilmController;
import mchorse.bbs_trajectory.client.tool.TrajectoryTransformTool;
import net.fabricmc.api.ClientModInitializer;

public class BBSTrajectoryModClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        UIFilmController.registerTool(TrajectoryTransformTool::new);

        FilmGizmoEvents.DRAW.register((context, stencilMap, stack) ->
        {
            return TrajectoryState.drawGizmo(context, stencilMap, stack);
        });
    }
}
