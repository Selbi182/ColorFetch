package de.selbi.colorfetch;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.selbi.colorfetch.data.ColorFetchResult;
import de.selbi.colorfetch.provider.AndroidPaletteColorProvider;
import de.selbi.colorfetch.provider.ColorProvider;
import de.selbi.colorfetch.provider.ColorThiefColorProvider;

@RestController
public class ColorFetchController {
  private final ColorThiefColorProvider colorThiefColorProvider;
  private final AndroidPaletteColorProvider androidPaletteColorProvider;

  ColorFetchController(ColorThiefColorProvider colorThiefColorProvider, AndroidPaletteColorProvider androidPaletteColorProvider) {
    this.colorThiefColorProvider = colorThiefColorProvider;
    this.androidPaletteColorProvider = androidPaletteColorProvider;
  }

  @GetMapping("/color")
  public ResponseEntity<ColorFetchResult> getColorForImageUrl(@RequestParam String url, @RequestParam(defaultValue = "color_thief") String strategy) throws IllegalArgumentException, IOException {
    ColorProvider colorProvider;
    switch (strategy) {
      case "color_thief":
        colorProvider = colorThiefColorProvider;
        break;
      case "android_palette":
        colorProvider = androidPaletteColorProvider;
        break;
      default:
        throw new IllegalArgumentException(strategy + " is an invalid strategy. Allowed strategies are: color_thief, android_palette");
    }

    URL parsedUrl = URI.create(url).toURL();
    HttpURLConnection urlConnection = (HttpURLConnection) parsedUrl.openConnection();
    int responseCode = urlConnection.getResponseCode();
    if (HttpURLConnection.HTTP_OK == responseCode) {
      ColorFetchResult colorFetchResult = colorProvider.getDominantColorFromImageUrl(parsedUrl);
      return ResponseEntity.of(Optional.of(colorFetchResult));
    } else {
      throw new IOException("Unable to open input stream to URL");
    }

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
