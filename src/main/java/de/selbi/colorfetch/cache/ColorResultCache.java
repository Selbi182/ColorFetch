package de.selbi.colorfetch.cache;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.springframework.stereotype.Component;

import de.selbi.colorfetch.data.ColorFetchResult;
import de.selbi.colorfetch.provider.AndroidPaletteColorProvider;
import de.selbi.colorfetch.provider.ColorThiefColorProvider;
import de.selbi.colorfetch.util.ColorUtil;

@Component
public class ColorResultCache {
  private static final long MAX_CACHE_ENTRIES = 50000;
  private static final long MAX_FILE_SIZE = 10 * (2 << 19);

  private final ColorThiefColorProvider colorThiefColorProvider;
  private final AndroidPaletteColorProvider androidPaletteColorProvider;

  private final Cache<ColorCacheKey, ColorFetchResult> colorCache;

  public ColorResultCache(ColorThiefColorProvider colorThiefColorProvider, AndroidPaletteColorProvider androidPaletteColorProvider) {
    this.colorThiefColorProvider = colorThiefColorProvider;
    this.androidPaletteColorProvider = androidPaletteColorProvider;

    this.colorCache = Cache2kBuilder.of(ColorCacheKey.class, ColorFetchResult.class)
      .loader(this::getColorFetchResult)
      .eternal(true)
      .entryCapacity(MAX_CACHE_ENTRIES)
      .build();
  }

  /**
   * Get the color for the given color cache key (might be a cached result).
   * If an error occurred, plain white is returned as fallback.
   *
   * @param colorCacheKey the given color cache key
   * @return the color fetch result
   */
  public ColorFetchResult getColor(ColorCacheKey colorCacheKey) {
    return colorCache.get(colorCacheKey);
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

  private ColorFetchResult getColorFetchResult(ColorCacheKey colorCacheKey) {
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
      return ColorUtil.normalizeColorFetchResult(colorFetchResult, colorCacheKey.getNormalize());
    } catch (IOException e) {
      return ColorFetchResult.FALLBACK;
    }
  }
}
