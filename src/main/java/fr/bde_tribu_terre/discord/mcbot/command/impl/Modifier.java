package fr.bde_tribu_terre.discord.mcbot.command.impl;

import fr.bde_tribu_terre.discord.mcbot.bdd.Bdd;
import fr.bde_tribu_terre.discord.mcbot.command.outils.CommandAction;
import fr.bde_tribu_terre.discord.mcbot.couleur.MinecraftColor;
import fr.bde_tribu_terre.discord.mcbot.couleur.NotDefaultMinecraftColorException;
import fr.bde_tribu_terre.discord.mcbot.structures.Equipe;
import fr.bde_tribu_terre.discord.mcbot.structures.Joueur;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Modifier extends CommandAction {
    public Modifier(@Nonnull final SlashCommandEvent event, @Nonnull final Bdd bdd) {
        super(event, bdd);
    }

    @Override
    public void action() {
        try {
            // Variable argument.
            String attribut = this.getEvent().getOption("attribut").getAsString().toLowerCase();
            String valeur = this.getEvent().getOption("valeur").getAsString();

            // Vérification de l'attribut.
            List<String> attributsPossibles = new ArrayList<>();
            attributsPossibles.add("nom");
            attributsPossibles.add("couleur");
            attributsPossibles.add("slogan");

            if (!attributsPossibles.contains(attribut)) {
                this.sendFailure(
                        "Attribut invalide",
                        "L'attribut `" + attribut + "` n'est pas un attribut valide."
                );
                return;
            }

            // Le joueur est-il enregistré ?
            Joueur sujet = this.getBdd().getJoueurs().getJoueurByDiscordId(this.getEvent().getUser().getId());
            if (sujet == null) {
                this.sendFailure(
                        "Joueur non inscrit",
                        "Vous n'avez pas enregistré de compte Minecraft lié au compte Discord " +
                                this.getEvent().getUser().getAsTag() +
                                ". Veuillez vous connecter au serveur Minecraft pour procéder à cette étape."
                );
                return;
            }

            // Récupération de l'équipe du sujet.
            Equipe equipeSujet = this.getBdd().getEquipes().getEquipeByMembre(sujet);
            if (equipeSujet == null) {
                this.sendFailure(
                        "Membre d'aucune équipe",
                        "Vous n'êtes membre d'aucune équipe."
                );
                return;
            }

            // Vérification que l'attribut n'est pas déjà correct.
            String currentValeur;
            switch (attribut) {
                case "nom" -> currentValeur = equipeSujet.getNom();
                case "couleur" -> currentValeur = equipeSujet.getCouleur().getMinecraftId();
                case "slogan" -> currentValeur = equipeSujet.getSlogan();
                default -> {
                    this.sendFailure(
                            "Attribut invalide",
                            "L'attribut `" + attribut + "` n'est pas un attribut valide.\n" +
                                    "La base de donnée a été appelée, ce qui est anormal."
                    );
                    return;
                }
            }

            if (currentValeur.equals(valeur)) {
                this.sendFailure(
                        "Valeur déjà fixée",
                        "L'attribut `" + attribut + "` " +
                                "a déjà pour valeur \"" + currentValeur + "\".\n"
                );
                return;
            }

            // Création de la nouvelle équipe.
            Equipe newEquipe = new Equipe(
                    equipeSujet.getId(),
                    attribut.equals("nom") ? valeur : equipeSujet.getNom(),
                    attribut.equals("couleur") ? MinecraftColor.fromMinecraftId(valeur) : equipeSujet.getCouleur(),
                    attribut.equals("slogan") ? valeur : equipeSujet.getSlogan(),
                    equipeSujet.getRegion(),
                    equipeSujet.getMembres().getJoueurs(),
                    equipeSujet.getAutorises().getJoueurs(),
                    equipeSujet.getDiscordRoleId()
            );

            // Modification du rôle Discord.
            newEquipe.setDiscordRole(this.getEvent().getJDA());

            // Mise à jour de l'équipe.
            this.getBdd().setEquipe(newEquipe);

            // Réponse.
            this.sendSuccess(
                    "L'attribut `" + attribut + "` de l'équipe \"" + equipeSujet.getNom() + "\" " +
                            "a désormais pour valeur `" + valeur + "`."
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
