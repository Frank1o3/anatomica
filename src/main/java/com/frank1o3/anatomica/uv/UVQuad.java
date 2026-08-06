package com.frank1o3.anatomica.uv;

public record UVQuad(int x1, int y1, int x2, int y2) {

    public UVQuad {
        x1 = Math.clamp(x1, 0, 64);
        y1 = Math.clamp(y1, 0, 64);
        x2 = Math.clamp(x2, 0, 64);
        y2 = Math.clamp(y2, 0, 64);
    }

    public int width() {
        return Math.abs(x2 - x1);
    }

    public int height() {
        return Math.abs(y2 - y1);
    }

    public UVQuad addX1(int delta) {
        return new UVQuad(x1 + delta, y1, x2, y2);
    }

    public UVQuad addY1(int delta) {
        return new UVQuad(x1, y1 + delta, x2, y2);
    }

    public UVQuad addX2(int delta) {
        return new UVQuad(x1, y1, x2 + delta, y2);
    }

    public UVQuad addY2(int delta) {
        return new UVQuad(x1, y1, x2, y2 + delta);
    }

    public UVQuad withX1(int newX1) {
        return new UVQuad(newX1, y1, x2, y2);
    }

    public UVQuad withY1(int newY1) {
        return new UVQuad(x1, newY1, x2, y2);
    }

    public UVQuad withX2(int newX2) {
        return new UVQuad(x1, y1, newX2, y2);
    }

    public UVQuad withY2(int newY2) {
        return new UVQuad(x1, y1, x2, newY2);
    }

    public UVQuad offset(int dx, int dy) {
        return new UVQuad(x1 + dx, y1 + dy, x2 + dx, y2 + dy);
    }
}
