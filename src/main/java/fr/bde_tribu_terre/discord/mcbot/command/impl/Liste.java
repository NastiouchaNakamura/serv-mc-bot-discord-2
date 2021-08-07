package fr.bde_tribu_terre.discord.mcbot.command.impl;

import fr.bde_tribu_terre.discord.mcbot.bdd.Bdd;
import fr.bde_tribu_terre.discord.mcbot.command.outils.CommandAction;
import fr.bde_tribu_terre.discord.mcbot.structures.Equipe;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.annotation.Nonnull;
import java.sql.SQLException;

public class Liste extends CommandAction {
    public Liste(@Nonnull final SlashCommandEvent event, @Nonnull final Bdd bdd) {
        super(event, bdd);
    }

    @Override
    public void action() {
        try {
            // Construction de la réponse.
            StringBuilder r = new StringBuilder();
            r.append("Liste des équipes :");
            for (Equipe equipe : this.getBdd().getEquipes().getEquipes()) {
                r.append("\n`").append(equipe.getId()).append("` : \"").append(equipe.getNom()).append("\"");
            }

            this.sendSuccess(
                    r.toString()
            );
        } catch (SQLException sqlException) {
            this.sendFailure(
                    "[" + sqlException.getSQLState() + " | " + sqlException.getErrorCode() + "]",
                    sqlException.getMessage()
            );
        }
    }
}
