package de.selbi.colorfetch.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import de.selbi.colorfetch.data.ColorFetchResult;

public interface ColorProvider {
  /**
   * Returns an approximation of the two most dominant colors from an image URL.
   * The primary color is intended for the text and icons,
   * whereas the secondary one is to be used as the background overlay.<br/>
   * <br/>
   * The algorithm favors bright, vibrant colors over dull ones and will
   * completely ignore colors that fall below a certain threshold in regard to
   * pixel population or brightness. For particularly dull images that don't even
   * manage to find two colors meeting the minimum requirement at all, WHITE is
   * returned for any blank ones.
   *
   * @param imageUri the URL of the image
   * @return a ColorFetchResult with the two result colors
   * (full white for fallback cases)
   * @throws IOException when the image couldn't be parsed for any reason
   */
  ColorFetchResult getDominantColorFromImageUrl(URL imageUri) throws IOException;

  /**
   * Convert the input image URL to a BufferedImage (download it)
   * @param imageUrl the url to the image
   * @return the downloaded BufferedImage
   * @throws IOException when the image couldn't be parsed for any reason
   */
  default BufferedImage getBufferedImage(String imageUrl) throws IOException {
    BufferedImage img = ImageIO.read(new URL(imageUrl));
    if (img == null) {
      throw new IOException("Unable to parse image");
    }
    return img;
  }
}
