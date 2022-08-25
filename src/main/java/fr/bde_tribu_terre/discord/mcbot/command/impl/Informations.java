package fr.bde_tribu_terre.discord.mcbot.command.impl;

import fr.bde_tribu_terre.discord.mcbot.bdd.Bdd;
import fr.bde_tribu_terre.discord.mcbot.command.outils.CommandAction;
import fr.bde_tribu_terre.discord.mcbot.fonctions.Fonctions;
import fr.bde_tribu_terre.discord.mcbot.structures.Equipe;
import fr.bde_tribu_terre.discord.mcbot.structures.Joueur;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nonnull;
import java.sql.SQLException;

public class Informations extends CommandAction {
    public Informations(@Nonnull final SlashCommandInteractionEvent event, @Nonnull final Bdd bdd) {
        super(event, bdd);
    }

    @Override
    public void action() {
        try {
            // Variable argument.
            String id = this.getEvent().getOption("id").getAsString();

            // Vérification que l'équipe existe.
            Equipe equipeSujet = this.getBdd().getEquipes().getEquipeById(id);
            if (equipeSujet == null) {
                this.sendFailure(
                        "Équipe inexistante",
                        "L'équipe d'ID `" + id + "` n'existe pas."
                );
                return;
            }

            // Réponse.
            StringBuilder reponse = new StringBuilder();
            reponse.append("Nom : \"").append(equipeSujet.getNom()).append("\".\n");
            reponse.append("ID : `").append(equipeSujet.getId()).append("`.\n");
            reponse.append("La région a une surface de : `").append(equipeSujet.getRegion().getSurface()).append("`.\n");
            reponse.append("La région peut avoir une surface maximale de : `").append(Fonctions.surfaceMax(equipeSujet.getMembres().getSize())).append("`.\n");
            reponse.append("Liste des membres de l'équipe :\n");
            if (equipeSujet.getMembres().getSize() == 0) {
                reponse.append("/");
            }
            for (Joueur membre : equipeSujet.getMembres().getJoueurs()) {
                reponse.append("- `").append(membre.getUsername()).append(" | ").append(membre.getDiscordTag(this.getEvent().getJDA())).append("`\n");
            }
            reponse.append("Liste des joueurs dont l'accès à la région est autorisée :\n");
            if (equipeSujet.getAutorises().getSize() == 0) {
                reponse.append("/");
            }
            for (Joueur autorise : equipeSujet.getAutorises().getJoueurs()) {
                reponse.append("- `").append(autorise.getUsername()).append(" | ").append(autorise.getDiscordTag(this.getEvent().getJDA())).append("`\n");
            }

            this.sendSuccess(
                    reponse.toString()
            );
        } catch (SQLException sqlException) {
            this.sendFailure(
                    "[" + sqlException.getSQLState() + " | " + sqlException.getErrorCode() + "]",
                    sqlException.getMessage()
            );
        }
    }
}
