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

public class Integrer extends CommandAction {
    public Integrer(@Nonnull final SlashCommandInteractionEvent event, @Nonnull final Bdd bdd) {
        super(event, bdd);
    }

    @Override
    public void action() {
        try {
            // Variable argument.
            User membreCible = this.getEvent().getOption("membre").getAsUser();

            // Trouver le premier membre.
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

            // Est-ce que le membre 1 a déjà une équipe ?
            Equipe equipe = this.getBdd().getEquipes().getEquipeByMembre(membreSource);
            if (equipe == null) {
                this.sendFailure(
                        "Joueur membre d'aucune équipe",
                        "Vous n'êtes membre d'aucune équipe."
                );
                return;
            }

            // Trouver le deuxième membre.
            Joueur newMembre = this.getBdd().getJoueurs().getJoueurByDiscordId(membreCible.getId());
            if (newMembre == null) {
                this.sendFailure(
                        "Joueur non inscrit",
                        "Il n'y a pas de compte Minecraft lié au compte Discord " +
                                membreCible.getAsTag() +
                                ". Veuillez vous connecter au serveur Minecraft pour procéder à cette étape."
                );
                return;
            }

            // Est-ce que le membre 2 a déjà une équipe ?
            Equipe equipeNewMembre = this.getBdd().getEquipes().getEquipeByMembre(newMembre);
            if (equipeNewMembre != null) {
                this.sendFailure(
                        "Joueur déjà membre d'une équipe",
                        newMembre.getDiscordTag(this.getEvent().getJDA()) +
                                ", vous êtes déjà membre de l'équipe \"" + equipeNewMembre.getNom() + "\"."
                );
                return;
            }

            // Est-ce que les deux joueurs sont identiques ?
            if (membreSource.equals(newMembre)) {
                this.sendFailure(
                        "Auteur et coéquipier identiques",
                        "Vous ne pouvez pas être votre propre coéquipier."
                );
                return;
            }

            // Création de la nouvelle équipe.
            List<Joueur> newMembres = new ArrayList<>(equipe.getMembres().getJoueurs());
            newMembres.add(newMembre);

            List<Joueur> newAutorises = new ArrayList<>(equipe.getAutorises().getJoueurs());
            newAutorises.remove(newMembre);

            Equipe newEquipe = new Equipe(
                    equipe.getId(),
                    equipe.getNom(),
                    equipe.getCouleur(),
                    equipe.getSlogan(),
                    equipe.getRegion(),
                    newMembres,
                    newAutorises,
                    equipe.getDiscordRoleId()
            );

            // Ajout des nouveaux membres dans le rôle Discord.
            newEquipe.setDiscordRole(this.getEvent().getJDA());

            // Mise à jour de l'équipe.
            this.getBdd().setEquipe(newEquipe);

            // Envoi du message de retour.
            this.sendSuccess(
                    newMembre.getDiscordTag(this.getEvent().getJDA()) + " est désormais membre de votre équipe."
            );
        } catch (SQLException sqlException) {
            this.sendFailure(
                    "[" + sqlException.getSQLState() + " | " + sqlException.getErrorCode() + "]",
                    sqlException.getMessage()
            );
        }
    }
}
