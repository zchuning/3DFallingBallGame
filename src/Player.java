/*
 * Author: Chuning (Tony) Zhu
 * Date: November 28, 2018
 * CIS 120 Final Project: How Far Can You Go?
 */

import java.awt.*;
import java.util.Collection;

public class Player extends Object3D {
    private static final int RADIUS = 10;
    private static final int ACCEL = 1;

    //Initial XZ-Plane position relative to center of the plane
    private static final int INIT_CENTER_X = 0;
    private static final int INIT_CENTER_Y = 0;

    //Initial XY-Plane position relative to the top left corner
    private static final int INIT_POS_X = INIT_CENTER_X - RADIUS + SHIFT;
    private static final int INIT_POS_Y = 0;
    private static final int INIT_POS_Z = INIT_CENTER_Y - RADIUS + SHIFT;

    private static final Color COLOR = new Color (255,84,84);

    private final Point planarCenter; //In planar coordinates
    private int projection; //In display coordinates

    private Object3D platform = null;

    public Player() {
        super(INIT_POS_X, INIT_POS_Y, INIT_POS_Z, 2 * RADIUS,2 * RADIUS, 2 * RADIUS, 0,0,0);
        this.planarCenter = new Point (INIT_CENTER_X, INIT_CENTER_Y);
        this.projection = INIT_POS_X;
        updateProjection();
    }

    public boolean isFalling() {
        return this.platform == null;
    }

    public void jump() {
        if(platform != null) {
            this.setVy(this.getVy() - 10);
            this.platform = null;
        }
    }

    public void tryStayOn() {
        // See if player is still on the platform
        if(this.getPx() + this.getWidth() < platform.getPx() ||
                this.getPx() > platform.getPx() + platform.getWidth()) {
            this.platform = null;
        }
    }

    public void tryCollideObjects(Collection<Object3D> blks) {

        for (Object3D b : blks) {
            Direction d = this.willIntersect2D(b);
            if (d == null) {
                continue;
            }
            switch (d) {
                case UP:
                    this.setPy(b.getPy() + b.getHeight());
                    this.setVy(b.getVy() + 1);
                    break;
                case DOWN:
                    this.setPy(b.getPy() - this.getHeight());
                    this.setVy(b.getVy());
                    this.setPz(b.getPz() + b.getDepth() / 2);
                    this.platform = b;
                    break;
                case LEFT:
                    this.setPx(b.getPx() + b.getWidth());
                    this.setVy(this.getVy() + ACCEL);
                    break;
                case RIGHT:
                    this.setPx(b.getPx() - this.getWidth());
                    this.setVy(this.getVy() + ACCEL);
                    break;
            }
            return;
        }

        this.setVy(this.getVy() + ACCEL);
    }

    @Override
    public void updateInternal() {
        double theta = 0 - Object3D.camera;
        int planarX = (int) ((this.getPx() + RADIUS - SHIFT) * Math.cos(theta) -
                (this.getPz() + RADIUS - SHIFT) * Math.sin(theta));
        int planarY = (int) ((this.getPx() + RADIUS - SHIFT) * Math.sin(theta) +
                (this.getPz() + RADIUS - SHIFT) * Math.cos(theta));
        this.planarCenter.setLocation(planarX, planarY);
    }

    @Override
    public void updateProjection() {
        this.projection = (int) (planarCenter.x * Math.cos(camera) - planarCenter.y * Math.sin(camera)) + SHIFT;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(COLOR);
        g.fillOval(this.getPx(), this.getPy(), this.getWidth(), this.getHeight());
        g.setColor(Color.GRAY);
        g.drawOval(this.getPx(), this.getPy(), this.getWidth(), this.getHeight());
    }

    @Override
    public void rDraw(Graphics g) {
        g.setColor(COLOR);
        g.fillOval(this.projection - RADIUS, this.getPy(), this.getWidth(), this.getHeight());
        g.setColor(Color.GRAY);
        g.drawOval(this.projection - RADIUS, this.getPy(), this.getWidth(), this.getHeight());
    }

}