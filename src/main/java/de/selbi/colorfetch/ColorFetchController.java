package de.selbi.colorfetch;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.selbi.colorfetch.cache.ColorCacheKey;
import de.selbi.colorfetch.cache.ColorResultCache;
import de.selbi.colorfetch.data.ColorFetchResult;

@RestController
public class ColorFetchController {
  private final ColorResultCache colorResultCache;

  ColorFetchController(ColorResultCache colorResultCache) {
    this.colorResultCache = colorResultCache;
  }

  @GetMapping("/color")
  public ResponseEntity<ColorFetchResult> getColorForImageUrl(
      @RequestParam String url,
      @RequestParam(defaultValue = "color_thief") String strategy,
      @RequestParam(defaultValue = "0.0") String normalize)
      throws IllegalArgumentException {
    ColorCacheKey.Strategy strategyEnumValue;
    switch (strategy) {
      case "color_thief":
        strategyEnumValue = ColorCacheKey.Strategy.COLOR_THIEF;
        break;
      case "android_palette":
        strategyEnumValue = ColorCacheKey.Strategy.ANDROID_PALETTE;
        break;
      default:
        throw new IllegalArgumentException(strategy + " is an invalid strategy. Allowed strategies are: color_thief, android_palette");
    }

    float normalizeFloat = Float.parseFloat(normalize);
    if (normalizeFloat < 0.0 || normalizeFloat > 1.0) {
      throw new IllegalArgumentException("'normalize' must be between 0.0 and 1.0");
    }

    ColorCacheKey colorCacheKey = ColorCacheKey.of(url, strategyEnumValue, normalizeFloat);
    ColorFetchResult colorFetchResult = colorResultCache.getColor(colorCacheKey);
    return ResponseEntity.of(Optional.of(colorFetchResult));
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<String> handleBadUrl(IOException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(e.toString());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleInvalidStrategy(IllegalArgumentException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleOtherError(Exception e) {
    e.printStackTrace();
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(e.getMessage());
  }
}
