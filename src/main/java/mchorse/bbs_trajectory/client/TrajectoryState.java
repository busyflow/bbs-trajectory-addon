package mchorse.bbs_trajectory.client;

import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.film.FilmControllerContext;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.framework.elements.input.drag.TransformSpace;
import mchorse.bbs_mod.ui.framework.elements.utils.StencilMap;
import mchorse.bbs_mod.ui.utils.Gizmo;
import mchorse.bbs_trajectory.client.tool.UITrajectoryPropTransform;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3d;

public class TrajectoryState
{
    public enum PivotMode
    {
        START_FRAME,
        CURRENT_CURSOR
    }

    private static boolean active = false;
    private static UIFilmPanel currentPanel = null;
    private static PivotMode pivotMode = PivotMode.START_FRAME;

    private static boolean hasCustomPivot = false;
    private static final Vector3d customPivot = new Vector3d();
    private static double customPivotYaw = 0.0;

    public static void setPanel(UIFilmPanel panel)
    {
        currentPanel = panel;
    }

    public static boolean isActive()
    {
        return active;
    }

    public static boolean isActive(UIFilmPanel panel)
    {
        if (panel != null)
        {
            currentPanel = panel;
        }

        return active && getSelectedReplay(panel) != null;
    }

    public static void toggle(UIFilmPanel panel)
    {
        if (panel != null)
        {
            currentPanel = panel;
        }

        active = !active;

        if (active)
        {
            resetPivot(panel);
        }
    }

    public static void setPivotMode(PivotMode mode)
    {
        pivotMode = mode;
        hasCustomPivot = false;
    }

    public static void resetPivot(UIFilmPanel panel)
    {
        hasCustomPivot = false;
    }

    public static void updatePivot(double x, double y, double z, double yaw)
    {
        hasCustomPivot = true;
        customPivot.set(x, y, z);
        customPivotYaw = yaw;
    }

    public static Replay getSelectedReplay(UIFilmPanel panel)
    {
        if (panel == null)
        {
            panel = currentPanel;
        }

        if (panel == null || panel.replayEditor == null)
        {
            return null;
        }

        Replay replay = panel.replayEditor.getReplay();
        return (replay != null && replay.enabled.get()) ? replay : null;
    }

    public static Replay getSelectedReplay()
    {
        return getSelectedReplay(currentPanel);
    }

    public static Vector3d getPivot(Replay replay, float tick)
    {
        if (hasCustomPivot)
        {
            return new Vector3d(customPivot);
        }

        if (replay == null)
        {
            return new Vector3d(0, 0, 0);
        }

        float sampleTick = (pivotMode == PivotMode.START_FRAME) ? 0F : tick;

        double x = replay.keyframes.x.interpolate(sampleTick);
        double y = replay.keyframes.y.interpolate(sampleTick);
        double z = replay.keyframes.z.interpolate(sampleTick);

        return new Vector3d(x, y, z);
    }

    public static double getPivotYaw(Replay replay, float tick)
    {
        if (hasCustomPivot)
        {
            return customPivotYaw;
        }

        if (replay == null)
        {
            return 0.0;
        }

        float sampleTick = (pivotMode == PivotMode.START_FRAME) ? 0F : tick;
        return replay.keyframes.yaw.interpolate(sampleTick);
    }

    public static boolean drawGizmo(FilmControllerContext context, StencilMap stencilMap, MatrixStack stack)
    {
        if (!active || context == null || context.camera == null)
        {
            return false;
        }

        Replay selectedReplay = getSelectedReplay(currentPanel);
        if (selectedReplay == null || context.replay != selectedReplay)
        {
            return false;
        }

        Vector3d pivot = getPivot(selectedReplay, context.transition);
        double cx = context.camera.getPos().x;
        double cy = context.camera.getPos().y;
        double cz = context.camera.getPos().z;

        stack.push();
        stack.translate(pivot.x - cx, pivot.y - cy, pivot.z - cz);

        Gizmo.INSTANCE.reorientForSpace(stack, TransformSpace.GLOBAL, context.gizmoView, null);

        if (stencilMap == null)
        {
            Gizmo.INSTANCE.captureVisual(stack, UITrajectoryPropTransform.MASK);
        }
        else
        {
            Gizmo.INSTANCE.renderStencil(stack, UITrajectoryPropTransform.MASK);
        }

        RenderSystem.enableDepthTest();
        stack.pop();

        return true;
    }
}
