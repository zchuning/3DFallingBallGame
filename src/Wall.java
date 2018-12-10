/*
 * Author: Chuning (Tony) Zhu
 * Date: November 28, 2018
 * CIS 120 Final Project: How Far Can You Go?
 */

import java.awt.*;

public class Wall extends Block{
    public static final int WALL_WIDTH = 400;
    public static final int LEFT_BOUND = (D_WIDTH - WALL_WIDTH)/2;
    public static final int RIGHT_BOUND = (D_WIDTH + WALL_WIDTH)/2;

    private int[] projection = null;

    public Wall () {
        super(-WALL_WIDTH/2, -WALL_WIDTH/2, WALL_WIDTH/2, WALL_WIDTH/2, 0, 0, 0, Color.BLACK);
        this.setPy(0);
        this.setHeight(D_HEIGHT);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(this.getColor());
        g.drawRect(this.getPx(), this.getPy(), this.getWidth(), this.getHeight());
        g.setColor(new Color(255, 227, 192));
        g.fillRect(this.getPx(), 0, this.getWidth(), this.getHeight());
    }

    @Override
    public void rDraw(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawRect(projection[0], this.getPy(), projection[2] - projection[0], this.getHeight());
        g.drawLine(projection[1], this.getPy(), projection[1], this.getPy() + this.getHeight());

    }

    public void drawBackEdge (Graphics g) {
        this.projection = this.getProjection();
        int displacement = 2 * (projection[1] - D_WIDTH / 2);
        g.setColor(new Color(255, 227, 192));
        g.fillRect(projection[0],0, projection[2] - projection[0], this.getHeight());
        g.setColor(new Color(0,0,0, 70));
        g.drawLine(projection[1] - displacement, this.getPy(),
                projection[1] - displacement, this.getPy() + this.getHeight());
    }
}
