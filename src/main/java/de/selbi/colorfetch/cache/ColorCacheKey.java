package de.selbi.colorfetch.cache;

import com.google.common.base.Objects;

public class ColorCacheKey {
  public enum Strategy {
    COLOR_THIEF,
    ANDROID_PALETTE
  }

  private final String url;
  private final Strategy strategy;
  private final Float normalize;

  private ColorCacheKey(String url, Strategy strategy, Float normalize) {
    this.url = url;
    this.strategy = strategy;
    this.normalize = normalize;
  }

  public static ColorCacheKey of(String url, Strategy strategy, Float normalize) {
    return new ColorCacheKey(url, strategy, normalize);
  }

  public String getUrl() {
    return url;
  }

  public Strategy getStrategy() {
    return strategy;
  }

  public Float getNormalize() {
    return normalize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ColorCacheKey))
      return false;
    ColorCacheKey that = (ColorCacheKey) o;
    return Objects.equal(url, that.url) && strategy == that.strategy && Objects.equal(normalize, that.normalize);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(url, strategy, normalize);
  }
}
