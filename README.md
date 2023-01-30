# ColorFetch
ColorFetch is a simple web-based service that allows getting the two most dominant colors of any image URL, along with its average brightness.

## Installation
The service is written as a SpringBoot service. As a result, the best idea is to simply compile the service as `bootJar` and keep that running.

## Usage
From the running service, the colors can simply be fetched by sending a `GET` request, like so:

```
http://localhost:8999/color?url=https://i.scdn.co/image/ab67616d0000b2738b2c42026277efc3e058855b
```

The result is returned as JSON object in the following format (RGB format 0-255 per channel, average brightness 0.0-1.0):

```json
{
  "primary": {
    "r": 255,
    "g": 243,
    "b": 225
  },
  "secondary": {
    "r": 255,
    "g": 250,
    "b": 199
  },
  "averageBrightness": 0.5976384398815804
}
```
Note: Colors are cached per URL for one week.

## Strategy

The specific strategy to determine the colors can be set with an optional paramter in the URL request:

```
http://localhost:8999/color?url=https://i.scdn.co/image/ab67616d0000b2738b2c42026277efc3e058855b&strategy=color_thief
```

Currently, two strategies are supported:

![Comparison of Strategies](https://i.imgur.com/wuXAbnH.png)
*Picture: AURORA – Infections Of A Different Kind – Step 1 (© 2018 Universal Music)*

### `color_thief` (Default)
[Color Thief](https://lokeshdhakar.com/projects/color-thief) is a simple library that determines the most dominant colors using pixel buckets. A lot of fine-tuning was done for ColorFetch, so it's the default strategy.

The primary color favors light colors while the secondary one favors saturated ones. An example usage would be to take the primary color as text color, while taking the secondary one as background color.

### `android_palette`
The [Android Palette API](https://developer.android.com/develop/ui/views/graphics/palette-colors) is a powerful but complex tool to determine dominant colors and finds common use on any modern Android device. It works with what are called Swatches. Depending on your use case, you might get better results with this strategy. Your best bet is to try it yourself!

Compared to the Color Thief, the resulting primary and secondary colors are pretty much inverted. The primary color is saturated while the secondary one is light.
