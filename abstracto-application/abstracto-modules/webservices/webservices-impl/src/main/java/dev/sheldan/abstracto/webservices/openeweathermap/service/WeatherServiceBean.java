package dev.sheldan.abstracto.webservices.openeweathermap.service;

import dev.sheldan.abstracto.webservices.openweathermap.service.WeatherService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class WeatherServiceBean implements WeatherService {

    private static final Map<Pair<Integer, Integer>, String> TEMPERATURE_COLOR_MAP = new HashMap<>();

    // source for colors: https://www.esri.com/arcgis-blog/products/arcgis-pro/mapping/a-meaningful-temperature-palette/
    static {
        TEMPERATURE_COLOR_MAP.put(Pair.of(-100, -48), "#E4EFFF");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-48, -45), "#DCE9FA");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-45, -42), "#D3E2F7");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-42, -40), "#CBDBF4");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-40, -37), "#C0D4ED");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-37, -34), "#B8CDEA");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-34, -31), "#AFC6E6");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-31, -28), "#A7BFE3");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-28, -26), "#9CB8DF");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-26, -23), "#93B1D6");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-23, -20), "#89A4CD");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-20, -17), "#7F9BC3");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-17, -15), "#7590B9");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-15, -12), "#617AA8");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-12, -9), "#56719C");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-9, -6), "#4D6591");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-6, -3), "#415C87");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-3, -1), "#39517F");
        TEMPERATURE_COLOR_MAP.put(Pair.of(-1, 1), "#2F4577");
        TEMPERATURE_COLOR_MAP.put(Pair.of(1, 4), "#26436F");
        TEMPERATURE_COLOR_MAP.put(Pair.of(4, 7), "#254F77");
        TEMPERATURE_COLOR_MAP.put(Pair.of(7, 10), "#275B80");
        TEMPERATURE_COLOR_MAP.put(Pair.of(10, 12), "#27678A");
        TEMPERATURE_COLOR_MAP.put(Pair.of(12, 15), "#287593");
        TEMPERATURE_COLOR_MAP.put(Pair.of(15, 18), "#438190");
        TEMPERATURE_COLOR_MAP.put(Pair.of(18, 21), "#648C89");
        TEMPERATURE_COLOR_MAP.put(Pair.of(21, 23), "#879A84");
        TEMPERATURE_COLOR_MAP.put(Pair.of(23, 26), "#ABA87D");
        TEMPERATURE_COLOR_MAP.put(Pair.of(26, 29), "#C2A875");
        TEMPERATURE_COLOR_MAP.put(Pair.of(29, 32), "#C19D61");
        TEMPERATURE_COLOR_MAP.put(Pair.of(32, 35), "#C38A53");
        TEMPERATURE_COLOR_MAP.put(Pair.of(35, 37), "#BE704C");
        TEMPERATURE_COLOR_MAP.put(Pair.of(37, 40), "#AF4D4C");
        TEMPERATURE_COLOR_MAP.put(Pair.of(40, 43), "#9F294C");
        TEMPERATURE_COLOR_MAP.put(Pair.of(43, 46), "#87203E");
        TEMPERATURE_COLOR_MAP.put(Pair.of(46, 48), "#631531");
        TEMPERATURE_COLOR_MAP.put(Pair.of(48, 65), "#560C25");
        TEMPERATURE_COLOR_MAP.put(Pair.of(65, 100), "#3D0216");

    }

    @Override
    public Color getColorForTemperature(Float temperature) {
        return Color.decode(TEMPERATURE_COLOR_MAP.get(TEMPERATURE_COLOR_MAP
                .keySet()
                .stream()
                .filter(pair -> pair.getLeft() < temperature && pair.getRight() > temperature)
                .findFirst().orElse(TEMPERATURE_COLOR_MAP.keySet().iterator().next())));
    }
}
