package fr.bde_tribu_terre.discord.mcbot.fonctions;

import static java.lang.Math.*;

public class Fonctions {
    public static int surfaceMax(int nbJoueurs) {
        return (int) (80000 * pow(log(nbJoueurs), 2) / sqrt(nbJoueurs) + 4000 * nbJoueurs);
    }
}
