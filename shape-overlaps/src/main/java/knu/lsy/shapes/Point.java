package knu.lsy.shapes;

import org.json.JSONObject;

public class Point {
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("x", x);
        json.put("y", y);
        return json;
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}