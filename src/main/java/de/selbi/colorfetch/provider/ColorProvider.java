package de.selbi.colorfetch.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;

import de.selbi.colorfetch.data.ColorFetchResult;

public interface ColorProvider {
  /**
   * Returns an approximation of the two most dominant colors from an image URL.
   * The primary color is intended for the text and icons,
   * whereas the secondary one is to be used as the background overlay.
   *
   * The algorithm favors bright, vibrant colors over dull ones and will
   * completely ignore colors that fall below a certain threshold in regard to
   * pixel population or brightness. For particularly dull images that don't even
   * manage to find two colors meeting the minimum requirement at all, WHITE is
   * returned for any blank ones.
   *
   * @param img the preloaded BufferedImage
   * @return a ColorFetchResult with the two result colors
   * (full white for fallback cases)
   * @throws IOException when the image couldn't be parsed for any reason
   */
  ColorFetchResult getColorFetchResultFromBufferedImage(BufferedImage img) throws IOException;

}
