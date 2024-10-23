package de.timongcraft.velopacketimpl.utils.network;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;

public class PlayerPosition {

    public static PlayerPosition read(ByteBuf buffer, boolean legacy) {
        return new PlayerPosition(
                Vec3d.read(buffer),
                legacy ? null : Vec3d.read(buffer),
                buffer.readFloat(),
                buffer.readFloat()
        );
    }

    public PlayerPosition(Vec3d pos, Vec3d deltaMovement, float yaw, float pitch) {
        this.pos = pos;
        this.deltaMovement = deltaMovement;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    private Vec3d pos;
    private Vec3d deltaMovement;
    private float yaw;
    private float pitch;

    public void write(ByteBuf buffer, boolean legacy) {
        pos.write(buffer);
        if (!legacy) {
            if (deltaMovement == null)
                throw new IllegalStateException("Legacy pos written as modern");

            deltaMovement.write(buffer);
        }
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
    }

    public Vec3d pos() {
        return pos;
    }

    public PlayerPosition pos(Vec3d pos) {
        this.pos = pos;
        return this;
    }

    /**
     * May return null if the position is legacy (<1.21.2)
     */
    public @Nullable Vec3d deltaMovement() {
        return deltaMovement;
    }

    public PlayerPosition deltaMovement(Vec3d deltaMovement) {
        this.deltaMovement = deltaMovement;
        return this;
    }

    public float yaw() {
        return yaw;
    }

    public PlayerPosition yaw(float yaw) {
        this.yaw = yaw;
        return this;
    }

    public float pitch() {
        return pitch;
    }

    public PlayerPosition pitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

}