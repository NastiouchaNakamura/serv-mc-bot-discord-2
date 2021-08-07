package fr.bde_tribu_terre.discord.mcbot.structures;

import net.dv8tion.jda.api.JDA;

import javax.annotation.Nonnull;
import java.util.List;

public class ListeJoueurs {
    // Attributs
    private final List<Joueur> joueurs;

    // Constructeurs
    public ListeJoueurs(List<Joueur> joueurs) {
        this.joueurs = joueurs;
    }

    // Getteurs
    public List<Joueur> getJoueurs() {
        return this.joueurs;
    }

    public int getSize() {
        return this.joueurs.size();
    }

    public Joueur getJoueurByUuid(String uuid) {
        for (Joueur joueur : this.joueurs) {
            if (joueur.getUuid().equals(uuid)) {
                return joueur;
            }
        }
        return null;
    }

    public Joueur getJoueurByUsername(String username) {
        for (Joueur joueur : this.joueurs) {
            if (joueur.getUsername().equals(username)) {
                return joueur;
            }
        }
        return null;
    }

    public Joueur getJoueurByDiscordId(String discordId) {
        for (Joueur joueur : this.joueurs) {
            if (joueur.getDiscordId().equals(discordId)) {
                return joueur;
            }
        }
        return null;
    }

    public Joueur getJoueurByDiscordTag(String discordTag, @Nonnull JDA jda) {
        for (Joueur joueur : this.joueurs) {
            if (joueur.getDiscordTag(jda).equals(discordTag)) {
                return joueur;
            }
        }
        return null;
    }

    // Méthodes
    public boolean containsJoueur(Joueur joueur) {
        return this.getJoueurByUuid(joueur.getUuid()) != null;
    }
}
