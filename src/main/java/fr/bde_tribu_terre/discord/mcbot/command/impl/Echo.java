package fr.bde_tribu_terre.discord.mcbot.command.impl;

import fr.bde_tribu_terre.discord.mcbot.bdd.Bdd;
import fr.bde_tribu_terre.discord.mcbot.command.outils.CommandAction;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nonnull;

public class Echo extends CommandAction {
    public Echo(@Nonnull final SlashCommandInteractionEvent event, @Nonnull final Bdd bdd) {
        super(event, bdd);
    }

    @Override
    public void action() {
        sendSuccess(this.getEvent().getOption("message").getAsString());
    }
}
