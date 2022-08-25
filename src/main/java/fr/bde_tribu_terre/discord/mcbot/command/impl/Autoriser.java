package fr.bde_tribu_terre.discord.mcbot.command.impl;

import fr.bde_tribu_terre.discord.mcbot.bdd.Bdd;
import fr.bde_tribu_terre.discord.mcbot.command.outils.CommandAction;
import fr.bde_tribu_terre.discord.mcbot.structures.Equipe;
import fr.bde_tribu_terre.discord.mcbot.structures.Joueur;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Autoriser extends CommandAction {
    public Autoriser(@Nonnull final SlashCommandInteractionEvent event, @Nonnull final Bdd bdd) {
        super(event, bdd);
    }

    @Override
    public void action() {
        try {
            // Variable argument.
            User membreCible = this.getEvent().getOption("membre").getAsUser();

            // Trouver le membre de l'équipe.
            Joueur membreSource = this.getBdd().getJoueurs().getJoueurByDiscordId(this.getEvent().getUser().getId());
            if (membreSource == null) {
                this.sendFailure(
                        "Joueur non inscrit",
                        "Vous n'avez pas enregistré de compte Minecraft lié au compte Discord " +
                                this.getEvent().getUser().getAsTag() +
                                ". Veuillez vous connecter au serveur Minecraft pour procéder à cette étape."
                );
                return;
            }

            // Est-ce que le membre a déjà une équipe ?
            Equipe equipe = this.getBdd().getEquipes().getEquipeByMembre(membreSource);
            if (equipe == null) {
                this.sendFailure(
                        "Joueur membre d'aucune équipe",
                        "Vous n'êtes membre d'aucune équipe."
                );
                return;
            }

            // Trouver l'autorisé.
            Joueur autorise = this.getBdd().getJoueurs().getJoueurByDiscordId(membreCible.getId());
            if (autorise == null) {
                this.sendFailure(
                        "Joueur non inscrit",
                        "Il n'y a pas de compte Minecraft lié au compte Discord " +
                                membreCible.getAsTag() +
                                ". Veuillez vous connecter au serveur Minecraft pour procéder à cette étape."
                );
                return;
            }

            // Est-ce que l'autorisé est déjà dans l'équipe ?
            Equipe equipeAutorise = this.getBdd().getEquipes().getEquipeByMembre(autorise);
            if (equipeAutorise != null && equipeAutorise.equals(equipe)) {
                this.sendFailure(
                        "Joueur déjà membre de l'équipe",
                        autorise.getDiscordTag(this.getEvent().getJDA()) +
                                " est déjà membre de l'équipe \"" + equipeAutorise.getNom() + "\"."
                );
                return;
            }

            // Est-ce que l'autorisé est déjà autorisé ?
            if (equipe.getAutorises().getJoueurByUuid(autorise.getUuid()) != null) {
                this.sendFailure(
                        "Joueur déjà autorisé",
                        autorise.getDiscordTag(this.getEvent().getJDA()) +
                                " est déjà autorisé à accéder à la région de l'équipe \"" + equipe.getNom() + "\"."
                );
                return;
            }

            // Création de la nouvelle équipe.
            List<Joueur> newAutorises = new ArrayList<>(equipe.getAutorises().getJoueurs());
            newAutorises.add(autorise);

            Equipe newEquipe = new Equipe(
                    equipe.getId(),
                    equipe.getNom(),
                    equipe.getCouleur(),
                    equipe.getSlogan(),
                    equipe.getRegion(),
                    equipe.getMembres().getJoueurs(),
                    newAutorises,
                    equipe.getDiscordRoleId()
            );

            // Mise à jour de l'équipe.
            this.getBdd().setEquipe(newEquipe);

            // Envoi du message de retour.
            this.sendSuccess(
                    autorise.getDiscordTag(this.getEvent().getJDA()) + " est désormais autorisé à accéder à votre région."
            );

        } catch (SQLException sqlException) {
            this.sendFailure(
                    "[" + sqlException.getSQLState() + " | " + sqlException.getErrorCode() + "]",
                    sqlException.getMessage()
            );
        }
    }
}
