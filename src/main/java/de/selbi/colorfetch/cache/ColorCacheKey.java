package de.selbi.colorfetch.cache;

import java.util.Objects;

public class ColorCacheKey {
  public enum Strategy {
    COLOR_THIEF,
    ANDROID_PALETTE
  }

  private final String url;
  private final Strategy strategy;
  private final float normalize;

  private ColorCacheKey(String url, Strategy strategy, float normalize) {
    this.url = url;
    this.strategy = strategy;
    this.normalize = normalize;
  }

  public static ColorCacheKey of(String url, Strategy strategy, float normalize) {
    return new ColorCacheKey(url, strategy, normalize);
  }

  public String getUrl() {
    return url;
  }

  public Strategy getStrategy() {
    return strategy;
  }

  public float getNormalize() {
    return normalize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ColorCacheKey))
      return false;
    ColorCacheKey that = (ColorCacheKey) o;
    return Float.compare(that.normalize, normalize) == 0 && Objects.equals(url, that.url) && strategy == that.strategy;
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, strategy, normalize);
  }
}
