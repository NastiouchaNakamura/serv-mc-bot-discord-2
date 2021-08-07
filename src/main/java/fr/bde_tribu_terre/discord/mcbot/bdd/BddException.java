package fr.bde_tribu_terre.discord.mcbot.bdd;

import javax.annotation.Nonnull;

public class BddException extends RuntimeException {
    // Constructeurs
    public BddException(@Nonnull final String message) {
        super(message);
    }
}
