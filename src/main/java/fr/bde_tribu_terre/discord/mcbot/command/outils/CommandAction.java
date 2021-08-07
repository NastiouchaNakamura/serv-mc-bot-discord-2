package fr.bde_tribu_terre.discord.mcbot.command.outils;

import fr.bde_tribu_terre.discord.mcbot.bdd.Bdd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.annotation.Nonnull;
import java.awt.*;

public abstract class CommandAction {
    // Attributs
    private final SlashCommandEvent event;
    private final Bdd bdd;

    // Constructeurs
    public CommandAction(
            @Nonnull final SlashCommandEvent event,
            @Nonnull final Bdd bdd
    ) {
        this.event = event;
        this.bdd = bdd;
    }

    // Getteurs
    public SlashCommandEvent getEvent() {
        return this.event;
    }

    public Bdd getBdd() {
        return this.bdd;
    }

    // Méthodes
    public abstract void action();

    public void sendSuccess(@Nonnull final String successMessage) {
        this.getEvent().replyEmbeds(
                new EmbedBuilder()
                        .setTitle("__**" + this.getEvent().getName().substring(0, 1).toUpperCase() + this.getEvent().getName().substring(1) + "**__")
                        .setColor(new Color(0, 255, 0))
                        .setFooter("Exécutée par @" + this.getEvent().getUser().getAsTag(), this.getEvent().getUser().getAvatarUrl())
                        .addField("Succès", successMessage, false)
                        .build()
        ).queue();
    }

    public void sendFailure(@Nonnull final String failureTitle, @Nonnull final String failureMessage) {
        this.getEvent().replyEmbeds(
                new EmbedBuilder()
                        .setTitle("__**" + this.getEvent().getName().substring(0, 1).toUpperCase() + this.getEvent().getName().substring(1) + "**__")
                        .setColor(new Color(255, 0, 0))
                        .setFooter("Exécutée par @" + this.getEvent().getUser().getAsTag(), this.getEvent().getUser().getAvatarUrl())
                        .addField("Échec : " + failureTitle, failureMessage, false)
                        .build()
        ).queue();
    }
}
