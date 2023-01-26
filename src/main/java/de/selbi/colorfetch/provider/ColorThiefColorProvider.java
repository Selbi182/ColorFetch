package de.selbi.colorfetch.provider;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.androidpit.colorthief.ColorThief;
import de.androidpit.colorthief.MMCQ.VBox;
import de.selbi.colorfetch.data.ColorFetchResult;
import de.selbi.colorfetch.util.ColorUtil;

/**
 * Implementation of the dominant color finding algorithm using ColorThief. This
 * implementation has been fine-tuned and tested with hundreds of album cover
 * arts. It's not always perfect, such as when dealing with very colorful images
 * that don't necessarily have one particular color stand out, but it should
 * still do a decent job.
 */
@Component
public class ColorThiefColorProvider implements ColorProvider {

  private static final int PALETTE_SAMPLE_SIZE = 10;
  private static final int PALETTE_SAMPLE_QUALITY = 5;
  private static final double MIN_BRIGHTNESS = 0.075;
  private static final double MIN_COLORFULNESS = 0.1;
  private static final int MIN_POPULATION = 1000;
  private static final int MIN_COLORED_PIXELS = 3000;
  private static final int BRIGHTNESS_CALCULATION_STEP_DIVIDER = 20;

  private final LoadingCache<String, Optional<ColorFetchResult>> cachedDominantColorsForUrl;

  public ColorThiefColorProvider() {
    this.cachedDominantColorsForUrl = CacheBuilder.newBuilder()
        .build(CacheLoader.from((imageUrl) -> {
          try {
            ColorFetchResult colors = getDominantColors(imageUrl);
            ColorUtil.normalizeAllForReadability(colors);
            return Optional.of(colors);
          } catch (IOException e) {
            return Optional.empty();
          }
        }));
  }

  @Override
  public ColorFetchResult getDominantColorFromImageUrl(URL imageUrl) throws IOException {
    if (imageUrl != null) {
      try {
        Optional<ColorFetchResult> colorFetchResult = cachedDominantColorsForUrl.get(imageUrl.toString());
        if (colorFetchResult.isPresent()) {
          return colorFetchResult.get();
        } else {
          throw new IOException("Unable to parse image");
        }
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    return ColorFetchResult.FALLBACK;
  }

  private ColorFetchResult getDominantColors(String imageUrl) throws IOException {
    BufferedImage img = getBufferedImage(imageUrl);

    List<VBox> vBoxes = ColorThief.getColorMap(img, PALETTE_SAMPLE_SIZE, PALETTE_SAMPLE_QUALITY, true).vboxes.stream()
        .filter(this::isValidVbox)
        .sorted(Comparator.comparingInt(this::calculateWeightedPopulation).reversed())
        .collect(Collectors.toList());

    int totalPopulationOfColor = 0;
    for (VBox vBox2 : vBoxes) {
      totalPopulationOfColor += vBox2.count(false);
    }
    if (totalPopulationOfColor < MIN_COLORED_PIXELS) {
      vBoxes.clear();
    }

    double averageBrightness = calculateAvgImageBrightness(img);
    if (vBoxes.isEmpty()) {
      // Grayscale image
      ColorFetchResult.RGB textColor = ColorFetchResult.RGB.DEFAULT_RGB;
      ColorFetchResult.RGB backgroundOverlay = ColorFetchResult.RGB.of(
          (int) (textColor.getR() * averageBrightness),
          (int) (textColor.getG() * averageBrightness),
          (int) (textColor.getB() * averageBrightness));
      return ColorFetchResult.of(textColor, backgroundOverlay, averageBrightness);
    } else if (vBoxes.size() == 1) {
      // Monochrome image
      int[] pal = vBoxes.get(0).avg(false);
      ColorFetchResult.RGB rgb = ColorFetchResult.RGB.of(pal[0], pal[1], pal[2]);
      return ColorFetchResult.of(rgb, rgb, averageBrightness);
    } else {
      // Normal image (at least two colors)
      int[] pal1 = vBoxes.get(0).avg(false);
      int[] pal2 = vBoxes.get(1).avg(false);
      ColorFetchResult.RGB rgb1 = ColorFetchResult.RGB.of(pal1[0], pal1[1], pal1[2]);
      ColorFetchResult.RGB rgb2 = ColorFetchResult.RGB.of(pal2[0], pal2[1], pal2[2]);
      if (ColorUtil.calculatePerceivedBrightness(rgb1) > ColorUtil.calculatePerceivedBrightness(rgb2)) {
        return ColorFetchResult.of(rgb1, rgb2, averageBrightness);
      } else {
        return ColorFetchResult.of(rgb2, rgb1, averageBrightness);
      }
    }
  }

  private boolean isValidVbox(VBox vBox) {
    int[] pal = vBox.avg(false);
    int r = pal[0];
    int g = pal[1];
    int b = pal[2];

    double brightness = ColorUtil.calculatePerceivedBrightness(ColorFetchResult.RGB.of(r, g, b));
    int population = vBox.count(false);
    double colorfulness = ColorUtil.calculateColorfulness(r, g, b);

    return (population > MIN_POPULATION && brightness > MIN_BRIGHTNESS && colorfulness > MIN_COLORFULNESS);
  }

  private int calculateWeightedPopulation(VBox vBox) {
    int[] pal = vBox.avg(false);
    int r = pal[0];
    int g = pal[1];
    int b = pal[2];
    int population = vBox.count(false);
    double brightness = ColorUtil.calculatePerceivedBrightness(ColorFetchResult.RGB.of(r, g, b));
    return (int) (population * Math.pow(brightness, 2.0));
  }

  private double calculateAvgImageBrightness(BufferedImage img) {
    final int sampleStepSize = Math.min(img.getWidth(), img.getHeight()) / BRIGHTNESS_CALCULATION_STEP_DIVIDER; // 32px for most images
    long samples = 0; // will be 400 for a 640x640 image
    double acc = 0; // will be a value between 0..1.0

    for (int x = 0; x < img.getWidth(); x += img.getWidth() / sampleStepSize) {
      for (int y = 0; y < img.getHeight(); y += img.getHeight() / sampleStepSize) {
        acc += calcPerceivedBrightnessAtLocation(img, x, y);
        samples++;
      }
    }

    double avg = acc / (double) samples;

    // Gamma correction, see: https://stackoverflow.com/a/16521343/3216060
    return Math.pow(avg, 1 / 2.2) * 0.85;
  }

  private double calcPerceivedBrightnessAtLocation(BufferedImage img, int x, int y) {
    Color color = new Color(img.getRGB(x, y));
    return ColorUtil.calculatePerceivedBrightness(ColorFetchResult.RGB.of(color));
  }
}
