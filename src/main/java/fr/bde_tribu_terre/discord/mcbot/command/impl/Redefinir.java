package fr.bde_tribu_terre.discord.mcbot.command.impl;

import fr.bde_tribu_terre.discord.mcbot.bdd.Bdd;
import fr.bde_tribu_terre.discord.mcbot.command.outils.CommandAction;
import fr.bde_tribu_terre.discord.mcbot.fonctions.Fonctions;
import fr.bde_tribu_terre.discord.mcbot.structures.Equipe;
import fr.bde_tribu_terre.discord.mcbot.structures.Joueur;
import fr.bde_tribu_terre.discord.mcbot.structures.Rectangle;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.annotation.Nonnull;
import java.sql.SQLException;

public class Redefinir extends CommandAction {
    public Redefinir(@Nonnull final SlashCommandEvent event, @Nonnull final Bdd bdd) {
        super(event, bdd);
    }

    @Override
    public void action() {
        try {
            // Variable argument.
            int x1 = (int) this.getEvent().getOption("x1").getAsLong();
            int z1 = (int) this.getEvent().getOption("z1").getAsLong();
            int x2 = (int) this.getEvent().getOption("x2").getAsLong();
            int z2 = (int) this.getEvent().getOption("z2").getAsLong();

            // Création du rectangle correspondant.
            Rectangle futurRectangle = new Rectangle(
                    x1,
                    z1,
                    x2,
                    z2
            );

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

            // Vérification qu'il n'y a pas de chevauchement.
            for (Equipe equipe : this.getBdd().getEquipes().getEquipes()) {
                if (!equipe.equals(equipeSujet) && equipe.getRegion().chevauche(futurRectangle)) {
                    this.sendFailure(
                            "Chevauchement",
                            "La région définie chevauche une autre région déjà mise en place."
                    );
                    return;
                }
            }

            // Vérification que la taille est valide.
            if (futurRectangle.getSurface() > Fonctions.surfaceMax(equipeSujet.getMembres().getSize())) {
                this.sendFailure(
                        "Surface trop grande",
                        "La surface donnée est de `" + futurRectangle.getSurface() + "`.\n" +
                                "La surface maximale autorisée pour l'équipe \"" + equipeSujet.getNom() + "\", " +
                                "qui comprend `" + equipeSujet.getMembres().getSize() + "` membres " +
                                "est de `" + Fonctions.surfaceMax(equipeSujet.getMembres().getSize()) + "`."
                );
                return;
            }

            // Création de la nouvelle équipe.
            Equipe newEquipe = new Equipe(
                    equipeSujet.getId(),
                    equipeSujet.getNom(),
                    equipeSujet.getCouleur(),
                    equipeSujet.getSlogan(),
                    futurRectangle,
                    equipeSujet.getMembres().getJoueurs(),
                    equipeSujet.getAutorises().getJoueurs(),
                    equipeSujet.getDiscordRoleId()
            );

            // Mise à jour de l'équipe.
            this.getBdd().setEquipe(newEquipe);

            // Réponse.
            this.sendSuccess(
                    "La région a bien été redéfinie.\n" +
                            "Surface totale : `" + newEquipe.getRegion().getSurface() + "`."
            );
        } catch (SQLException sqlException) {
            this.sendFailure(
                    "[" + sqlException.getSQLState() + " | " + sqlException.getErrorCode() + "]",
                    sqlException.getMessage()
            );
        }
    }
}
