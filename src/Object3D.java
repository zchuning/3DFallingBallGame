/*
 * Author: Chuning (Tony) Zhu
 * Date: November 28, 2018
 * CIS 120 Final Project: How Far Can You Go?
 */

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

abstract class Object3D implements Comparable<Object3D>{

    // Position
    private int px;
    private int py;
    private int pz;

    // Size
    private int width;
    private int height;
    private int depth;

    // Velocity
    private int vx;
    private int vy;
    private int vz;

    // Upper bounds of positions
    private int maxX;
    private final int maxY;
    private int maxZ;

    // Static rotation fields shared by all instances
    private static final double rotationLapse = 0.6;
    private static boolean isRotating = false;
    private static Direction direction = Direction.CCW;
    private static List<Double> angularVels = getAngularVels();
    private static int rotationCount = 0;

    // Static fields shared by all instances
    public static final int SHIFT = Display.DISP_WIDTH / 2;
    public static final int INIT_VEL_Y = -2;

    // Static camera fields, can take on 0, pi/2, pi, 3pi/2
    public static double camera = 0;
    private static double camera_old = 0;
    private static double camera_new = 0;

    public static final int D_WIDTH = Display.DISP_WIDTH;
    public static final int D_HEIGHT = Display.DISP_HEIGHT;

    public Object3D(int px, int py, int pz, int width, int height, int depth, int vx, int vy, int vz) {
        this.px = px;
        this.py = py;
        this.pz = pz;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.maxX = Wall.RIGHT_BOUND - width;
        this.maxY = D_HEIGHT;
        this.maxZ = Wall.RIGHT_BOUND - depth;
    }

    public int getPx() { return px; }
    public int getPy() { return py; }
    public int getPz() { return pz; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDepth() { return depth; }
    public int getVx() { return vx; }
    public int getVy() { return vy; }
    public int getVz() { return vz; }

    public void setPx(int px) {
        this.px = px;
        this.bound();
    }

    public void setPy(int py) {
        this.py = py;
        this.bound();
    }

    public void setPz(int pz) {
        this.pz = pz;
        this.bound();
    }

    public void setWidth(int width) {
        this.width = width;
        this.maxX = Wall.RIGHT_BOUND - width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setDepth(int depth) {
        this.depth = depth;
        this.maxZ = Wall.RIGHT_BOUND - depth;
    }

    public void setVx(int vx) { this.vx = vx; }
    public void setVy(int vy) { this.vy = vy; }
    public void setVz(int vz) { this.vz = vz; }

    private void bound () {
        this.px = Math.min(Math.max(Wall.LEFT_BOUND, this.px), maxX);
        this.py = Math.min(Math.max(-this.getHeight(), this.py), maxY);
        this.pz = Math.min(Math.max(Wall.LEFT_BOUND, this.pz), maxZ);
        if (this.py == maxY) {
            this.vy = 0;
        }
    }

    public boolean move() {
        this.px += vx;
        this.py += vy;
        this.pz += vz;
        if (this.py < -this.height || this.py > this.maxY)
            return false;
        bound();
        updateInternal();
        return true;
    }

    private static List<Double> getAngularVels() {
        List<Double> avs = new LinkedList<>();
        double start = camera, delta = Math.PI / 2;
        for (double time = 0; time < rotationLapse; time = time + 0.01) {
            double temp = time / (Object3D.rotationLapse / 2);
            if (temp < 1) {
                avs.add(delta / 2 * temp * temp + start);
                continue;
            }
            temp--;
            avs.add(-delta / 2 * (temp * (temp - 2) - 1) + start);
        }
        return avs;
    }

    public static void beginRotation(Direction d) {
        direction = d;
        camera_old = camera;
        if (d == Direction.CCW) {
            camera_new = camera + Math.PI / 2;
            if (camera_new > 2 * Math.PI) {
                camera_new -= 2 * Math.PI;
            }
        } else if (d == Direction.CW) {
            camera_new = camera - Math.PI / 2;
            if (camera_new < 0) {
                camera_new += 2 * Math.PI;
            }
        }
        isRotating = true;
    }

    public static boolean isRotating() {
        return isRotating;
    }

    public static void rotate() {
        if (direction == Direction.CCW) {
            camera = camera_old + angularVels.get(rotationCount);
        } else {
            camera = camera_old - angularVels.get(rotationCount);
        }
        rotationCount++;
        if (rotationCount == rotationLapse/0.01) {
            camera = camera_new;
            rotationCount = 0;
            isRotating = false;
        }
    }

    public void updateFinalPosition(Direction direction) {
        int x = this.getPx();
        int z = this.getPz();
        int d = this.getDepth();
        int w = this.getWidth();
        if (direction == Direction.CCW) {
            this.px = D_WIDTH - d - z;
            this.pz = x;
        }
        else {
            this.px = z;
            this.pz = D_WIDTH - w - x;
        }
        this.setDepth(w);
        this.setWidth(d);
    }

    public Direction willIntersect2D(Object3D that) {
        int thisNextX = this.px + this.vx;
        int thisNextY = this.py + this.vy;
        int thatNextX = that.getPx() + that.getVx();
        int thatNextY = that.getPy() + that.getVy();

        if (thisNextX + this.width >= thatNextX
                && thisNextY + this.height >= thatNextY
                && thisNextX <= thatNextX + that.getWidth()
                && thisNextY <= thatNextY + that.getHeight()) {
            if (this.py + this.height <= that.getPy()) {
                return Direction.DOWN;
            } else if (this.py >= that.getPy() + that.getHeight()) {
                return Direction.UP;
            } else if (this.px + this.width/2 <= that.getPx() + that.getWidth()/2) {
                return Direction.RIGHT;
            } else if (this.px + this.width/2 >= that.getPx() + that.getWidth()/2) {
                return Direction.LEFT;
            } else {
                return null;
            }
        }
        return null;
    }

    public abstract void updateInternal();

    public abstract void updateProjection();

    public abstract void draw(Graphics g);

    public abstract void rDraw(Graphics g);

    public static void reset() {
        camera = 0;
        camera_old = 0;
        camera_new = 0;

        isRotating = false;
        direction = Direction.CCW;
        angularVels = getAngularVels();
        rotationCount = 0;
    }

    public int compareTo(Object3D obj) {
        if(direction == Direction.CCW) {
            if (obj.getPx() + obj.getWidth() <= this.px || obj.getPz() + obj.getDepth() <= this.pz) {
                return -1;
            } else {
                return 1;
            }
        } else {
            if (obj.getPx() >= this.px + this.width || obj.getPz() + obj.getDepth() <= this.pz) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
