package de.selbi.colorfetch.data;

import java.awt.Color;

public class ColorFetchResult {
  public static final ColorFetchResult FALLBACK = new ColorFetchResult(RGB.DEFAULT_RGB, RGB.DEFAULT_RGB, 0.5);

  private RGB primary;
  private RGB secondary;
  private double averageBrightness;

  public ColorFetchResult() {
  }

  public ColorFetchResult(RGB primary, RGB secondary, double averageBrightness) {
    this.primary = primary;
    this.secondary = secondary;
    this.averageBrightness = averageBrightness;
  }

  public static ColorFetchResult of(ColorFetchResult.RGB primary, ColorFetchResult.RGB secondary, double averageBrightness) {
    return new ColorFetchResult(primary, secondary, averageBrightness);
  }

  public static ColorFetchResult of(Color primary, Color secondary, double averageBrightness) {
    return new ColorFetchResult(ColorFetchResult.RGB.of(primary), ColorFetchResult.RGB.of(secondary), averageBrightness);
  }

  public ColorFetchResult.RGB getPrimary() {
    return this.primary;
  }

  public void setPrimary(ColorFetchResult.RGB primary) {
    this.primary = primary;
  }

  public ColorFetchResult.RGB getSecondary() {
    return this.secondary;
  }

  public void setSecondary(ColorFetchResult.RGB secondary) {
    this.secondary = secondary;
  }

  public double getAverageBrightness() {
    return this.averageBrightness;
  }

  public static class RGB {
    public static final RGB DEFAULT_RGB = RGB.of(Color.WHITE);

    private int r;
    private int g;
    private int b;

    public RGB() {
    }

    public RGB(int r, int g, int b) {
      this.r = r;
      this.g = g;
      this.b = b;
    }

    public static ColorFetchResult.RGB of(int r, int g, int b) {
      return new ColorFetchResult.RGB(r, g, b);
    }

    public static ColorFetchResult.RGB of(int[] rgb) {
      return new ColorFetchResult.RGB(rgb[0], rgb[1], rgb[2]);
    }

    public static ColorFetchResult.RGB of(Color c) {
      return new ColorFetchResult.RGB(c.getRed(), c.getGreen(), c.getBlue());
    }

    public int getR() {
      return r;
    }

    public int getG() {
      return g;
    }

    public int getB() {
      return b;
    }

    @Override
    public String toString() {
      return String.format("R %d / G %d / B %d", r, g, b);
    }
  }
}
