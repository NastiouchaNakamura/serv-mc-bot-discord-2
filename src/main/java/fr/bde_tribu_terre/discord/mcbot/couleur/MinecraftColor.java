package fr.bde_tribu_terre.discord.mcbot.couleur;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MinecraftColor {
    // Attributs statiques
    private static final List<MinecraftColor> couleurs = new ArrayList<MinecraftColor>();

    // Méthodes statiques
    public static List<MinecraftColor> getDefaultMinecraftColors() {
        if (couleurs.isEmpty()) {
            // Black
            couleurs.add(new MinecraftColor(new Color(0, 0, 0), "black", "0"));

            // Dark Blue
            couleurs.add(new MinecraftColor(new Color(0, 0, 170), "dark_blue", "1"));

            // Dark green
            couleurs.add(new MinecraftColor(new Color(0, 170, 0), "dark_green", "2"));

            // Dark Aqua
            couleurs.add(new MinecraftColor(new Color(0, 170, 170), "dark_aqua", "3"));

            // Dark Red
            couleurs.add(new MinecraftColor(new Color(170, 0, 0), "dark_red", "4"));

            // Dark Purple
            couleurs.add(new MinecraftColor(new Color(170, 0, 170), "dark_purple", "5"));

            // Gold
            couleurs.add(new MinecraftColor(new Color(255, 170, 0), "gold", "6"));

            // Gray
            couleurs.add(new MinecraftColor(new Color(170, 170, 170), "gray", "7"));

            // Dark Gray
            couleurs.add(new MinecraftColor(new Color(85, 85, 85), "dark_gray", "8"));

            // Blue
            couleurs.add(new MinecraftColor(new Color(85, 85, 255), "blue", "9"));

            // Green
            couleurs.add(new MinecraftColor(new Color(85, 255, 85), "green", "a"));

            // Aqua
            couleurs.add(new MinecraftColor(new Color(85, 255, 255), "aqua", "b"));

            // Red
            couleurs.add(new MinecraftColor(new Color(255, 85, 85), "red", "c"));

            // Light Purple
            couleurs.add(new MinecraftColor(new Color(255, 85, 255), "light_purple", "d"));

            // Yellow
            couleurs.add(new MinecraftColor(new Color(255, 255, 85), "yellow", "e"));

            // White
            couleurs.add(new MinecraftColor(new Color(255, 255, 255), "white", "f"));
        }

        return couleurs;
    }

    public static List<String> getDefaultMinecraftColorIds() {
        return getDefaultMinecraftColors().stream().map(MinecraftColor::getMinecraftId).collect(Collectors.toList());
    }

    public static MinecraftColor fromMinecraftId(String minecraftId) throws NotDefaultMinecraftColorException {
        for (MinecraftColor minecraftColor : getDefaultMinecraftColors()) {
            if (minecraftColor.getMinecraftId().equals(minecraftId)) {
                return minecraftColor;
            }
        }
        throw new NotDefaultMinecraftColorException(
                "Il n'y a aucune couleur Minecraft d'id `" + minecraftId + "`."
        );
    }

    public static MinecraftColor fromMinecraftCode(String minecraftCode) throws NotDefaultMinecraftColorException {
        for (MinecraftColor minecraftColor : getDefaultMinecraftColors()) {
            if (minecraftCode.charAt(0) != '&') {
                minecraftCode = "&" + minecraftCode;
            }
            if (minecraftColor.getMinecraftCode().equals(minecraftCode)) {
                return minecraftColor;
            }
        }
        throw new NotDefaultMinecraftColorException(
                "Il n'y a aucune couleur Minecraft dont le code est `" + minecraftCode + "`."
        );
    }

    // Attributs
    private final Color color;
    private final String minecraftId;
    private final String minecraftCode;

    // Constructeurs
    public MinecraftColor(Color color, String minecraftId, String minecraftCode) {
        this.color = color;
        this.minecraftId = minecraftId;
        this.minecraftCode = minecraftCode;
    }

    // Getteurs
    public Color getColor() {
        return this.color;
    }

    public String getMinecraftId() {
        return this.minecraftId;
    }

    public String getMinecraftCode() {
        return "&" + this.minecraftCode;
    }

    // Méthodes
    @Override
    public String toString() {
        return "MinecraftColor[Color: " + this.getColor() + ", Minecraft ID:" + this.getMinecraftId() + ", Minecraft Code:" + this.getMinecraftCode() + "]";
    }
}
