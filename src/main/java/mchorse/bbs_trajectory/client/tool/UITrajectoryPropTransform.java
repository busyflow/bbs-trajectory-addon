package mchorse.bbs_trajectory.client.tool;

import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.forms.entities.IEntity;
import mchorse.bbs_mod.settings.values.IValueListener;
import mchorse.bbs_mod.ui.film.controller.UIFilmController;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.input.UIPropTransform;
import mchorse.bbs_mod.ui.framework.elements.input.drag.TransformSpace;
import mchorse.bbs_mod.ui.utils.Gizmo;
import mchorse.bbs_mod.utils.Axis;
import mchorse.bbs_mod.utils.MathUtils;
import mchorse.bbs_mod.utils.keyframes.Keyframe;
import mchorse.bbs_mod.utils.keyframes.KeyframeChannel;
import mchorse.bbs_mod.utils.pose.Transform;
import mchorse.bbs_trajectory.client.TrajectoryState;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class UITrajectoryPropTransform extends UIPropTransform
{
    public static final Gizmo.HandleMask MASK = Gizmo.HandleMask.of(
        EnumSet.of(Gizmo.Op.MOVE, Gizmo.Op.SCREEN, Gizmo.Op.ROTATE),
        EnumSet.of(Axis.Y)
    );

    public static class ChannelSnapshot
    {
        public final double value;
        public final float tick;

        public ChannelSnapshot(double value, float tick)
        {
            this.value = value;
            this.tick = tick;
        }
    }

    private final Transform scratch = new Transform();

    private UIFilmController controller;
    private Replay replay;
    private IEntity entity;
    private float tick;

    private double pivotX;
    private double pivotY;
    private double pivotZ;
    private double pivotYaw;

    private boolean snapshotTaken = false;
    private final List<ChannelSnapshot> snapX = new ArrayList<>();
    private final List<ChannelSnapshot> snapY = new ArrayList<>();
    private final List<ChannelSnapshot> snapZ = new ArrayList<>();
    private final List<ChannelSnapshot> snapYaw = new ArrayList<>();
    private final List<ChannelSnapshot> snapBodyYaw = new ArrayList<>();
    private final List<ChannelSnapshot> snapHeadYaw = new ArrayList<>();
    private final List<ChannelSnapshot> snapVx = new ArrayList<>();
    private final List<ChannelSnapshot> snapVz = new ArrayList<>();

    public UITrajectoryPropTransform()
    {
        this.callbacks(
            () -> this.notifyKeyframes(0),
            () -> {
                if (this.replay != null)
                {
                    this.replay.keyframes.postNotify();
                }
            },
            () -> {
                this.notifyKeyframes(IValueListener.FLAG_UNMERGEABLE);
                this.onGestureEnd();
            }
        );

        this.setVisible(false);
    }

    public void setController(UIFilmController controller)
    {
        this.controller = controller;
    }

    @Override
    public UIContext getContext()
    {
        UIContext ctx = super.getContext();
        if (ctx != null)
        {
            return ctx;
        }
        if (this.controller != null && this.controller.getContext() != null)
        {
            return this.controller.getContext();
        }
        if (TrajectoryState.getCurrentPanel() != null && TrajectoryState.getCurrentPanel().getContext() != null)
        {
            return TrajectoryState.getCurrentPanel().getContext();
        }
        return null;
    }

    @Override
    public TransformSpace pickedSpace()
    {
        return TransformSpace.GLOBAL;
    }

    @Override
    public boolean isRotationConstrained()
    {
        return false;
    }

    @Override
    public boolean isModel()
    {
        return false;
    }

    private void notifyKeyframes(int flag)
    {
        if (this.replay != null)
        {
            this.replay.keyframes.preNotify(flag);
        }
    }

    public Replay getReplay()
    {
        return this.replay;
    }

    public Vector3d getPivot()
    {
        return new Vector3d(this.pivotX, this.pivotY, this.pivotZ);
    }

    public double getPivotYaw()
    {
        return this.pivotYaw;
    }

    public void syncFromReplay(Replay replay, IEntity entity, float tick)
    {
        if (this.isEditing())
        {
            this.tick = tick;
            return;
        }

        this.replay = replay;
        this.entity = entity;
        this.tick = tick;
        this.snapshotTaken = false;

        if (replay == null)
        {
            this.setTransform(null);
            return;
        }

        Vector3d pivot = TrajectoryState.getPivot(replay, tick);
        double yaw = TrajectoryState.getPivotYaw(replay, tick);

        this.pivotX = pivot.x;
        this.pivotY = pivot.y;
        this.pivotZ = pivot.z;
        this.pivotYaw = yaw;

        this.scratch.rotationMode = Transform.RotationMode.EULER;
        this.scratch.translate.set((float) this.pivotX, (float) this.pivotY, (float) this.pivotZ);
        this.scratch.rotate.set(0F, (float) MathUtils.toRad((float) -yaw), 0F);
        this.scratch.scale.set(1F, 1F, 1F);

        this.setTransform(this.scratch);
    }

    @Override
    public void beginGesture()
    {
        super.beginGesture();
        this.ensureSnapshot();
    }

    private void ensureSnapshot()
    {
        if (this.snapshotTaken || this.replay == null)
        {
            return;
        }

        this.snapX.clear();
        this.snapY.clear();
        this.snapZ.clear();
        this.snapYaw.clear();
        this.snapBodyYaw.clear();
        this.snapHeadYaw.clear();
        this.snapVx.clear();
        this.snapVz.clear();

        this.captureChannel(this.replay.keyframes.x, this.snapX);
        this.captureChannel(this.replay.keyframes.y, this.snapY);
        this.captureChannel(this.replay.keyframes.z, this.snapZ);
        this.captureChannel(this.replay.keyframes.yaw, this.snapYaw);
        this.captureChannel(this.replay.keyframes.bodyYaw, this.snapBodyYaw);
        this.captureChannel(this.replay.keyframes.headYaw, this.snapHeadYaw);
        this.captureChannel(this.replay.keyframes.vX, this.snapVx);
        this.captureChannel(this.replay.keyframes.vZ, this.snapVz);

        this.snapshotTaken = true;
    }

    @SuppressWarnings("rawtypes")
    private void captureChannel(KeyframeChannel channel, List<ChannelSnapshot> list)
    {
        if (channel == null) return;
        List kfs = channel.getKeyframes();
        for (Object item : kfs)
        {
            if (item instanceof Keyframe kf)
            {
                double val = kf.getFactory().getY(kf.getValue());
                list.add(new ChannelSnapshot(val, (float) kf.getTick()));
            }
        }
    }

    @Override
    public boolean isRotationChannelOnly()
    {
        return true;
    }

    @Override
    public String readoutPrefix()
    {
        return this.replay == null ? "Trajectory: " : "Trajectory (" + this.replay.getName() + "): ";
    }

    @Override
    public void setT(Axis axis, double x, double y, double z)
    {
        if (this.replay == null)
        {
            return;
        }

        this.ensureSnapshot();
        this.preCallback();

        this.scratch.translate.set((float) x, (float) y, (float) z);
        this.applyTransform();

        this.postCallback();
    }

    @Override
    public void setS(Axis axis, double x, double y, double z)
    {
        /* Trajectory scaling not enabled */
    }

    @Override
    public void setR(Axis axis, double x, double y, double z)
    {
        if (this.replay == null)
        {
            return;
        }

        this.ensureSnapshot();
        this.preCallback();

        this.scratch.rotate.set(0F, (float) MathUtils.toRad((float) y), 0F);
        this.applyTransform();

        this.postCallback();
    }

    private static double sampleSnapshot(List<ChannelSnapshot> snaps, float tick, double fallback)
    {
        if (snaps == null || snaps.isEmpty())
        {
            return fallback;
        }

        if (snaps.size() == 1 || tick <= snaps.get(0).tick)
        {
            return snaps.get(0).value;
        }

        int last = snaps.size() - 1;
        if (tick >= snaps.get(last).tick)
        {
            return snaps.get(last).value;
        }

        for (int i = 0; i < last; i++)
        {
            ChannelSnapshot a = snaps.get(i);
            ChannelSnapshot b = snaps.get(i + 1);

            if (tick >= a.tick && tick <= b.tick)
            {
                if (Math.abs(b.tick - a.tick) < 1E-5F)
                {
                    return a.value;
                }
                double factor = (tick - a.tick) / (double) (b.tick - a.tick);
                return a.value + (b.value - a.value) * factor;
            }
        }

        return snaps.get(last).value;
    }

    @SuppressWarnings("rawtypes")
    private void applyTransform()
    {
        if (this.replay == null || !this.snapshotTaken)
        {
            return;
        }

        double curTx = this.scratch.translate.x;
        double curTy = this.scratch.translate.y;
        double curTz = this.scratch.translate.z;

        double dx = curTx - this.pivotX;
        double dy = curTy - this.pivotY;
        double dz = curTz - this.pivotZ;

        double curRotYRad = this.scratch.rotate.y;
        double curYawDeg = -Math.toDegrees(curRotYRad);
        double deltaYawDeg = curYawDeg - this.pivotYaw;
        double alphaRad = Math.toRadians(deltaYawDeg);

        double cosA = Math.cos(alphaRad);
        double sinA = Math.sin(alphaRad);

        boolean hasRotation = Math.abs(deltaYawDeg) >= 1E-4;

        // Apply X channel transformation
        if (this.replay.keyframes.x.isEmpty())
        {
            this.replay.keyframes.x.insert(0F, this.pivotX + dx);
        }
        else
        {
            List kfsX = this.replay.keyframes.x.getKeyframes();
            int countX = Math.min(this.snapX.size(), kfsX.size());
            for (int i = 0; i < countX; i++)
            {
                ChannelSnapshot snap = this.snapX.get(i);
                double finalX;
                if (hasRotation)
                {
                    double ox = snap.value - this.pivotX;
                    double oz = sampleSnapshot(this.snapZ, snap.tick, this.pivotZ) - this.pivotZ;
                    double rotX = ox * cosA - oz * sinA;
                    finalX = this.pivotX + dx + rotX;
                }
                else
                {
                    finalX = snap.value + dx;
                }
                Keyframe kfX = (Keyframe) kfsX.get(i);
                kfX.setValue(kfX.getFactory().yToValue(finalX), true);
            }
        }

        // Apply Z channel transformation
        if (this.replay.keyframes.z.isEmpty())
        {
            this.replay.keyframes.z.insert(0F, this.pivotZ + dz);
        }
        else
        {
            List kfsZ = this.replay.keyframes.z.getKeyframes();
            int countZ = Math.min(this.snapZ.size(), kfsZ.size());
            for (int i = 0; i < countZ; i++)
            {
                ChannelSnapshot snap = this.snapZ.get(i);
                double finalZ;
                if (hasRotation)
                {
                    double ox = sampleSnapshot(this.snapX, snap.tick, this.pivotX) - this.pivotX;
                    double oz = snap.value - this.pivotZ;
                    double rotZ = ox * sinA + oz * cosA;
                    finalZ = this.pivotZ + dz + rotZ;
                }
                else
                {
                    finalZ = snap.value + dz;
                }
                Keyframe kfZ = (Keyframe) kfsZ.get(i);
                kfZ.setValue(kfZ.getFactory().yToValue(finalZ), true);
            }
        }

        // Apply translation to height (y)
        if (this.replay.keyframes.y.isEmpty())
        {
            this.replay.keyframes.y.insert(0F, this.pivotY + dy);
        }
        else
        {
            List kfsY = this.replay.keyframes.y.getKeyframes();
            int countY = Math.min(this.snapY.size(), kfsY.size());
            for (int i = 0; i < countY; i++)
            {
                double finalY = this.snapY.get(i).value + dy;
                Keyframe kfY = (Keyframe) kfsY.get(i);
                kfY.setValue(kfY.getFactory().yToValue(finalY), true);
            }
        }

        // Apply rotation to yaw channels
        this.applyAngleDelta(this.replay.keyframes.yaw, this.snapYaw, deltaYawDeg);
        this.applyAngleDelta(this.replay.keyframes.bodyYaw, this.snapBodyYaw, deltaYawDeg);
        this.applyAngleDelta(this.replay.keyframes.headYaw, this.snapHeadYaw, deltaYawDeg);

        // Apply rotation to velocities
        if (hasRotation)
        {
            List kfsVx = this.replay.keyframes.vX.getKeyframes();
            int countVx = Math.min(this.snapVx.size(), kfsVx.size());
            for (int i = 0; i < countVx; i++)
            {
                ChannelSnapshot snap = this.snapVx.get(i);
                double vx = snap.value;
                double vz = sampleSnapshot(this.snapVz, snap.tick, 0D);

                double rotVx = vx * cosA - vz * sinA;
                Keyframe kfVx = (Keyframe) kfsVx.get(i);
                kfVx.setValue(kfVx.getFactory().yToValue(rotVx), true);
            }

            List kfsVz = this.replay.keyframes.vZ.getKeyframes();
            int countVz = Math.min(this.snapVz.size(), kfsVz.size());
            for (int i = 0; i < countVz; i++)
            {
                ChannelSnapshot snap = this.snapVz.get(i);
                double vx = sampleSnapshot(this.snapVx, snap.tick, 0D);
                double vz = snap.value;

                double rotVz = vx * sinA + vz * cosA;
                Keyframe kfVz = (Keyframe) kfsVz.get(i);
                kfVz.setValue(kfVz.getFactory().yToValue(rotVz), true);
            }
        }

        // Post channel notifications to refresh caches and timeline
        this.replay.keyframes.x.postNotify();
        this.replay.keyframes.y.postNotify();
        this.replay.keyframes.z.postNotify();
        this.replay.keyframes.yaw.postNotify();
        this.replay.keyframes.bodyYaw.postNotify();
        this.replay.keyframes.headYaw.postNotify();
        this.replay.keyframes.vX.postNotify();
        this.replay.keyframes.vZ.postNotify();
        this.replay.keyframes.postNotify();

        // Update live entity for fluid real-time feedback
        IEntity liveEntity = this.entity != null ? this.entity : (this.controller != null ? this.controller.getCurrentEntity() : null);
        if (liveEntity != null)
        {
            double ex = this.replay.keyframes.x.interpolate(this.tick);
            double ey = this.replay.keyframes.y.interpolate(this.tick);
            double ez = this.replay.keyframes.z.interpolate(this.tick);
            float eyaw = this.replay.keyframes.yaw.interpolate(this.tick).floatValue();
            float ebodyYaw = this.replay.keyframes.bodyYaw.interpolate(this.tick).floatValue();
            float eheadYaw = this.replay.keyframes.headYaw.interpolate(this.tick).floatValue();

            liveEntity.setPosition(ex, ey, ez);
            liveEntity.setPrevX(ex);
            liveEntity.setPrevY(ey);
            liveEntity.setPrevZ(ez);

            liveEntity.setYaw(eyaw);
            liveEntity.setPrevYaw(eyaw);
            liveEntity.setBodyYaw(ebodyYaw);
            liveEntity.setPrevBodyYaw(ebodyYaw);
            liveEntity.setHeadYaw(eheadYaw);
            liveEntity.setPrevHeadYaw(eheadYaw);
        }
    }

    @SuppressWarnings("rawtypes")
    private void applyAngleDelta(KeyframeChannel channel, List<ChannelSnapshot> snaps, double deltaYawDeg)
    {
        if (channel == null || snaps == null) return;
        List kfs = channel.getKeyframes();
        int count = Math.min(snaps.size(), kfs.size());
        for (int i = 0; i < count; i++)
        {
            double finalAngle = snaps.get(i).value + deltaYawDeg;
            Keyframe kf = (Keyframe) kfs.get(i);
            kf.setValue(kf.getFactory().yToValue(finalAngle), true);
        }
    }

    private void onGestureEnd()
    {
        this.snapshotTaken = false;
    }
}
