/*
 * Author: Chuning (Tony) Zhu
 * Date: November 28, 2018
 * CIS 120 Final Project: How Far Can You Go?
 */

import java.awt.*;
import java.util.List;
import java.util.LinkedList;

public class Block extends Object3D{

    private static final int INIT_HEIGHT = 20;
    private final Color color;

    //Coordinates with respect to the center of plane
    private final List<Point> points;
    private final int[] projection;

    public Block(int x1, int y1, int x2, int y2, int velX, int velY, int velZ, Color color) {
        super(x1 + SHIFT, D_HEIGHT - 1, y1 + SHIFT, x2-x1, INIT_HEIGHT, y2-y1, velX, velY, velZ);

        if ((int) camera == 1) {
            this.updateFinalPosition(Direction.CCW);
        } else if ((int) camera == 3) {
            this.updateFinalPosition(Direction.CCW);
            this.updateFinalPosition(Direction.CCW);
        } else if ((int) camera == 4) {
            this.updateFinalPosition(Direction.CW);
        }

        this.points = new LinkedList<>();
        this.points.add(new Point(x1, y1));
        this.points.add(new Point(x1, y2));
        this.points.add(new Point(x2, y2));
        this.points.add(new Point(x2, y1));

        this.color = color;
        this.projection = new int[3];
        updateProjection();

    }

    public Color getColor() {return this.color;}
    public int[] getProjection() {return this.projection.clone();}

    @Override
    public void updateInternal() { }

    @Override
    public void updateProjection() {
        int left = Integer.MAX_VALUE, mid = 0, depth = left, right = Integer.MIN_VALUE;

        for (Point pt : this.points) {
            int new_x = (int) (pt.getX() * Math.cos(camera) - pt.getY() * Math.sin(camera));
            int new_y = (int) (pt.getX() * Math.sin(camera) + pt.getY() * Math.cos(camera));
            if (new_x < left) {
                left = new_x;
            }
            if (new_x > right) {
                right = new_x;
            }
            if (new_x >= left && new_x <= right && new_y < depth) {
                depth = new_y;
                mid = new_x;
            }
        }
        projection[0] = left + SHIFT;
        projection[1] = mid + SHIFT;
        projection[2] = right + SHIFT;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(this.getPx(), this.getPy(), this.getWidth(), this.getHeight());
        g.setColor(Color.gray);
        g.drawRect(this.getPx(), this.getPy(), this.getWidth(), this.getHeight());
    }

    @Override
    public void rDraw(Graphics g) {
        g.setColor(color);
        g.fillRect(projection[0], this.getPy(), projection[2] - projection[0], this.getHeight());
        g.setColor(Color.gray);
        g.drawRect(projection[0], this.getPy(), projection[2] - projection[0], this.getHeight());
        g.drawLine(projection[1], this.getPy(), projection[1], this.getPy() + this.getHeight());
    }
}