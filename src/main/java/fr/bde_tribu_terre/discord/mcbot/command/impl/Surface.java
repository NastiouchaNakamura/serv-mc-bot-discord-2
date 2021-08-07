package fr.bde_tribu_terre.discord.mcbot.command.impl;

import fr.bde_tribu_terre.discord.mcbot.bdd.Bdd;
import fr.bde_tribu_terre.discord.mcbot.command.outils.CommandAction;
import fr.bde_tribu_terre.discord.mcbot.structures.Rectangle;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.annotation.Nonnull;

public class Surface extends CommandAction {
    public Surface(@Nonnull final SlashCommandEvent event, @Nonnull final Bdd bdd) {
        super(event, bdd);
    }

    @Override
    public void action() {
        // Variable argument.
        int x1 = (int) this.getEvent().getOption("x1").getAsLong();
        int z1 = (int) this.getEvent().getOption("z1").getAsLong();
        int x2 = (int) this.getEvent().getOption("x2").getAsLong();
        int z2 = (int) this.getEvent().getOption("z2").getAsLong();

        Rectangle rectangle = new Rectangle(
                x1,
                z1,
                x2,
                z2
        );

        this.sendSuccess(
                "La surface est de : `" + rectangle.getSurface() + "`."
        );
    }
}
