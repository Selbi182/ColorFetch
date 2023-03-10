package de.selbi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ColorFetch {

  /**
   * Main entry point of the bot
   * @param args ignore, no args are available
   */
  public static void main(String[] args) {
    SpringApplication.run(ColorFetch.class, args);
  }
}
