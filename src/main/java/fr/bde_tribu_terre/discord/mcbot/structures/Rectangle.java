package fr.bde_tribu_terre.discord.mcbot.structures;

import java.util.regex.Pattern;

import static java.lang.StrictMath.*;

public class Rectangle {
    // Attributs
    private final int x1;
    private final int z1;
    private final int x2;
    private final int z2;

    // Constructeurs
    public Rectangle(int x1, int z1, int x2, int z2) {
        this.x1 = min(
                x1,
                x2
        );
        this.z1 = min(
                z1,
                z2
        );
        this.x2 = max(
                x1,
                x2
        );
        this.z2 = max(
                z1,
                z2
        );
    }

    public Rectangle(String x1, String z1, String x2, String z2) throws NumberFormatException {
        Pattern pattern = Pattern.compile("-?\\d+$");
        for (String coord : new String[]{x1, z1, x2, z2}) {
            if (!pattern.matcher(coord).matches()) {
                throw new NumberFormatException("`" + coord + "` n'est pas un entier valide.");
            }
        }
        this.x1 = min(
                Integer.parseInt(x1),
                Integer.parseInt(x2)
        );
        this.z1 = min(
                Integer.parseInt(z1),
                Integer.parseInt(z2)
        );
        this.x2 = max(
                Integer.parseInt(x1),
                Integer.parseInt(x2)
        );
        this.z2 = max(
                Integer.parseInt(z1),
                Integer.parseInt(z2)
        );
    }

    // Getteurs
    public int getX1() {
        return this.x1;
    }

    public int getZ1() {
        return this.z1;
    }

    public int getX2() {
        return this.x2;
    }

    public int getZ2() {
        return this.z2;
    }

    public int getSurface() {
        return abs(((this.x1 - this.x2) * (this.z1 - this.z2)));
    }

    // Méthodes
    public boolean chevauche(Rectangle r) {
        return this.x1 < r.getX2() && this.x2 > r.getX1() && this.z1 < r.getZ2() && this.z2 > r.getZ1();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Rectangle rectangle = (Rectangle) o;
        return this.getX1() == rectangle.getX1() &&
                this.getZ1() == rectangle.getZ1() &&
                this.getX2() == rectangle.getX2() &&
                this.getZ2() == rectangle.getZ2();
    }

    @Override
    public String toString() {
        return "Rectangle[((" + this.getX1() + ", " + this.getZ1() + "), (" + this.getX2() + ", " + this.getZ2() + ")]";
    }
}
