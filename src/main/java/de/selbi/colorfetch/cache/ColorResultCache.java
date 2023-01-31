package de.selbi.colorfetch.cache;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.selbi.colorfetch.data.ColorFetchResult;
import de.selbi.colorfetch.provider.AndroidPaletteColorProvider;
import de.selbi.colorfetch.provider.ColorThiefColorProvider;
import de.selbi.colorfetch.util.ColorUtil;

@Component
public class ColorResultCache {
  private static final long MAX_CACHE_ENTRIES = 50000;
  private static final long EXPIRATION_DAYS = 30;
  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

  private final LoadingCache<ColorCacheKey, Optional<ColorFetchResult>> colorCache;

  public ColorResultCache(ColorThiefColorProvider colorThiefColorProvider, AndroidPaletteColorProvider androidPaletteColorProvider) {
    this.colorCache = CacheBuilder.newBuilder()
        .maximumSize(MAX_CACHE_ENTRIES)
        .expireAfterAccess(EXPIRATION_DAYS, TimeUnit.DAYS)
        .build(createCache(colorThiefColorProvider, androidPaletteColorProvider));
  }

  /**
   * Get the color for the given color cache key (might be a cached result).
   * If an error occurred, plain white is returned as fallback.
   *
   * @param colorCacheKey the given color cache key
   * @return the color fetch result
   * @throws ExecutionException if an exception occurred inside the cache
   */
  public ColorFetchResult getColor(ColorCacheKey colorCacheKey) throws ExecutionException {
    return colorCache.get(colorCacheKey)
        .orElse(ColorFetchResult.FALLBACK);
  }

  //////////////////

  private URL openValidUrl(String url) throws IOException {
    URL parsedUrl = URI.create(url).toURL();
    if (isFileSizeWithinBounds(parsedUrl)) {
      HttpURLConnection urlConnection = (HttpURLConnection) parsedUrl.openConnection();
      int responseCode = urlConnection.getResponseCode();
      if (HttpURLConnection.HTTP_OK == responseCode) {
        return parsedUrl;
      } else {
        throw new IOException("Unable to open input stream to URL");
      }
    } else {
      throw new IOException("File size exceeds limit (10 MiB)");
    }
  }

  private BufferedImage getBufferedImage(URL url) throws IOException {
    BufferedImage img = ImageIO.read(url);
    if (img != null) {
      return img;
    }
    throw new IOException("Unable to parse image");
  }

  private boolean isFileSizeWithinBounds(URL url) throws IOException {
    HttpURLConnection conn = null;
    try {
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("HEAD");
      return conn.getContentLengthLong() < MAX_FILE_SIZE;
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  //////////////////

  private CacheLoader<ColorCacheKey, Optional<ColorFetchResult>> createCache(ColorThiefColorProvider colorThiefColorProvider, AndroidPaletteColorProvider androidPaletteColorProvider) {
    return CacheLoader.from(colorCacheKey -> {
      try {
        String urlString = Objects.requireNonNull(colorCacheKey).getUrl();
        URL url = openValidUrl(urlString);
        BufferedImage bufferedImage = getBufferedImage(url);

        ColorFetchResult colorFetchResult;
        switch (colorCacheKey.getStrategy()) {
        case COLOR_THIEF:
          colorFetchResult = colorThiefColorProvider.getColorFetchResultFromBufferedImage(bufferedImage);
          break;
        case ANDROID_PALETTE:
          colorFetchResult = androidPaletteColorProvider.getColorFetchResultFromBufferedImage(bufferedImage);
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + colorCacheKey.getStrategy());
        }

        if (colorCacheKey.isNormalized()) {
          ColorUtil.normalizeAllForReadability(colorFetchResult);
        }
        return Optional.of(colorFetchResult);
      } catch (IOException e) {
        return Optional.empty();
      }
    });
  }
}
