package fr.bde_tribu_terre.discord.mcbot.command.impl;

import fr.bde_tribu_terre.discord.mcbot.bdd.Bdd;
import fr.bde_tribu_terre.discord.mcbot.command.outils.CommandAction;
import fr.bde_tribu_terre.discord.mcbot.couleur.MinecraftColor;
import fr.bde_tribu_terre.discord.mcbot.couleur.NotDefaultMinecraftColorException;
import fr.bde_tribu_terre.discord.mcbot.fonctions.Fonctions;
import fr.bde_tribu_terre.discord.mcbot.structures.Equipe;
import fr.bde_tribu_terre.discord.mcbot.structures.Joueur;
import fr.bde_tribu_terre.discord.mcbot.structures.Rectangle;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Fonder extends CommandAction {
    public Fonder(@Nonnull final SlashCommandEvent event, @Nonnull final Bdd bdd) {
        super(event, bdd);
    }

    @Override
    public void action() {
        try {
            // Variable argument.
            User coequipier = this.getEvent().getOption("coequipier").getAsUser();
            String identifiant = this.getEvent().getOption("identifiant").getAsString();
            String nom = this.getEvent().getOption("nom").getAsString();
            String couleur = this.getEvent().getOption("couleur").getAsString();
            String slogan = this.getEvent().getOption("slogan").getAsString();
            int x1 = (int) this.getEvent().getOption("x1").getAsLong();
            int z1 = (int) this.getEvent().getOption("z1").getAsLong();
            int x2 = (int) this.getEvent().getOption("x2").getAsLong();
            int z2 = (int) this.getEvent().getOption("z2").getAsLong();

            // Vérification que l'identifiant est valide.
            if (!identifiant.toLowerCase().matches("^[a-z0-9_]*$") && identifiant.length() > 0) {
                this.sendFailure(
                        "Format invalide",
                        "`" + identifiant + "` n'est pas identifiant valide. " +
                                "Veuillez ne saisir que des lettres en minuscule non-accentuées, des chiffres ou " +
                                "_ (underscore)."
                );
                return;
            }

            // Vérification que le nom est valide.
            if (nom.length() == 0) {
                this.sendFailure(
                        "Format invalide",
                        "L'équipe doit avoir un nom."
                );
                return;
            }

            // Création du rectangle correspondant.
            Rectangle rectangle = new Rectangle(
                    x1,
                    z1,
                    x2,
                    z2
            );

            // Trouver le premier membre.
            Joueur membre1 = this.getBdd().getJoueurs().getJoueurByDiscordId(this.getEvent().getUser().getId());
            if (membre1 == null) {
                this.sendFailure(
                        "Joueur non inscrit",
                        "Vous n'avez pas enregistré de compte Minecraft lié au compte Discord " +
                                this.getEvent().getUser().getAsTag() +
                                ". Veuillez vous connecter au serveur Minecraft pour procéder à cette étape."
                );
                return;
            }

            // Est-ce que le membre 1 a déjà une équipe ?
            Equipe equipe1 = this.getBdd().getEquipes().getEquipeByMembre(membre1);
            if (equipe1 != null) {
                this.sendFailure(
                        "Joueur déjà membre d'une équipe",
                        membre1.getDiscordTag(this.getEvent().getJDA()) +
                                ", vous êtes déjà membre de l'équipe \"" + equipe1.getNom() + "\"."
                );
                return;
            }

            // Trouver le deuxième membre.
            Joueur membre2 = this.getBdd().getJoueurs().getJoueurByDiscordId(coequipier.getId());
            if (membre2 == null) {
                this.sendFailure(
                        "Joueur non inscrit",
                        "Il n'y a pas de compte Minecraft lié au compte Discord " +
                                coequipier.getAsTag() +
                                ". Veuillez vous connecter au serveur Minecraft pour procéder à cette étape."
                );
                return;
            }

            // Est-ce que le membre 2 a déjà une équipe ?
            Equipe equipe2 = this.getBdd().getEquipes().getEquipeByMembre(membre2);
            if (equipe2 != null) {
                this.sendFailure(
                        "Joueur déjà membre d'une équipe",
                        membre2.getDiscordTag(this.getEvent().getJDA()) +
                                ", vous êtes déjà membre de l'équipe \"" + equipe2.getNom() + "\"."
                );
                return;
            }

            // Est-ce que les deux joueurs sont identiques ?
            if (membre1.equals(membre2)) {
                this.sendFailure(
                        "Auteur et coéquipier identiques",
                        "Vous ne pouvez pas être votre propre coéquipier."
                );
                return;
            }

            // Vérification que l'ID n'existe pas déjà.
            Equipe equipeId = this.getBdd().getEquipes().getEquipeById(identifiant);
            if (equipeId != null) {
                this.sendFailure(
                        "ID déjà existant",
                        "L'équipe \"" + equipeId.getNom() + "\" a déjà pour ID `" + equipeId.getId() + "`."
                );
                return;
            }

            // Vérification qu'il n'y a pas de chevauchement.
            for (Equipe equipe : this.getBdd().getEquipes().getEquipes()) {
                if (equipe.getRegion().chevauche(rectangle)) {
                    this.sendFailure(
                            "Chevauchement",
                            "La région définie chevauche une autre région déjà mise en place."
                    );
                    return;
                }
            }

            // Vérification que la taille est valide.
            if (rectangle.getSurface() > Fonctions.surfaceMax(2)) {
                this.sendFailure(
                        "Surface trop grande",
                        "La surface donnée est de `" + rectangle.getSurface() + "`.\n" +
                                "La surface maximale autorisée pour 2 membres est de `" + Fonctions.surfaceMax(2) + "`."
                );
                return;
            }

            // Création de la nouvelle équipe.
            List<Joueur> membres = new ArrayList<>();
            membres.add(membre1);
            membres.add(membre2);

            Equipe newEquipe = new Equipe(
                    identifiant.toLowerCase(),
                    nom,
                    MinecraftColor.fromMinecraftId(couleur),
                    slogan,
                    rectangle,
                    membres,
                    new ArrayList<>(),
                    null
            );

            // Création du rôle Discord de l'équipe.
            newEquipe.setDiscordRole(this.getEvent().getJDA());

            // Mise à jour de l'équipe.
            this.getBdd().setEquipe(newEquipe);

            // Envoi du message de retour.
            this.sendSuccess(
                    "L'équipe `" + nom + "`, " +
                            "d'ID `" + identifiant + "` a bien été fondée.\n" +
                            "Surface totale : `" + rectangle.getSurface() + "`."
            );
        } catch (NotDefaultMinecraftColorException notDefaultMinecraftColorException) {
            this.sendFailure(
                    "Couleur invalide",
                    notDefaultMinecraftColorException.getMessage()
            );
        } catch (SQLException sqlException) {
            this.sendFailure(
                    "[" + sqlException.getSQLState() + " | " + sqlException.getErrorCode() + "]",
                    sqlException.getMessage()
            );
        }
    }
}
