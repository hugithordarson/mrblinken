package app.shelly;

/**
 * State of an RGBW light.
 *
 * @param red, green, blue 0-255
 * @param brightness 0-100 (percent, applies to the RGB channels)
 * @param white 0-255
 */

public record RGBW( boolean on, int red, int green, int blue, int brightness, int white ) {}
