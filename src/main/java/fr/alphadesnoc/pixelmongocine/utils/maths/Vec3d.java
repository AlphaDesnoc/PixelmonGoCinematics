package fr.alphadesnoc.pixelmongocine.utils.maths;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3i;

public class Vec3d extends VecNd<Vec3d> {
    public double x;
    public double y;
    public double z;

    public Vec3d(Vector3i vec) {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vec3d vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public double get(int dim) {
        switch (dim) {
            case 0:
                return this.x;
            case 1:
                return this.y;
            case 2:
                return this.z;
            default:
                return 0.0;
        }
    }

    public double get(Direction.Axis axis) {
        switch (axis) {
            case X:
                return this.x;
            case Y:
                return this.y;
            case Z:
                return this.z;
            default:
                return 0.0;
        }
    }

    public int dimensions() {
        return 3;
    }

    public void add(Vec3d vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Vec3d)) {
            return false;
        } else {
            return ((Vec3d)obj).x == this.x && ((Vec3d)obj).y == this.y && ((Vec3d)obj).z == this.z;
        }
    }

    public double distance(Vec3d vec) {
        return this.distance(vec.x, vec.y, vec.z);
    }

    public double distance(double x, double y, double z) {
        double posX = this.x - x;
        double posY = this.y - y;
        double posZ = this.z - z;
        return Math.sqrt(posX * posX + posY * posY + posZ * posZ);
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }
}