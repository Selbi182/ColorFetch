package de.selbi.colorfetch.cache;

import java.util.Objects;

public class ColorCacheKey {
  public enum Strategy {
    COLOR_THIEF,
    ANDROID_PALETTE
  }

  private final String url;
  private final Strategy strategy;
  private final boolean normalized;

  private ColorCacheKey(String url, Strategy strategy, boolean normalized) {
    this.url = url;
    this.strategy = strategy;
    this.normalized = normalized;
  }

  public static ColorCacheKey of(String url, Strategy strategy, boolean normalized) {
    return new ColorCacheKey(url, strategy, normalized);
  }

  public String getUrl() {
    return url;
  }

  public Strategy getStrategy() {
    return strategy;
  }

  public boolean isNormalized() {
    return normalized;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ColorCacheKey that = (ColorCacheKey) o;
    return normalized == that.normalized && Objects.equals(url, that.url) && strategy == that.strategy;
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, strategy, normalized);
  }
}
