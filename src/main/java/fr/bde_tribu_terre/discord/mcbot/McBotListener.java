package fr.bde_tribu_terre.discord.mcbot;

import fr.barodine.anael.discord.launcher.AbstractBaseListener;
import fr.bde_tribu_terre.discord.mcbot.bdd.Bdd;
import fr.bde_tribu_terre.discord.mcbot.bdd.BddBuilder;
import fr.bde_tribu_terre.discord.mcbot.command.impl.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.annotation.Nonnull;

public class McBotListener extends AbstractBaseListener {
    public McBotListener(long idBot, @Nonnull JDA jda) {
        super(idBot, jda);
    }

    // Attributs
    private BddBuilder bddBuilder;

    // Méthodes
    public void logInfo(@Nonnull final String message) {
        this.log("[INFO] " + message);
    }

    public void logWarning(@Nonnull final String message) {
        this.log("[WARNING] " + message);
    }

    public void logError(@Nonnull final String message) {
        this.log("[ERROR] " + message);
    }

    public void logCommand(@Nonnull final SlashCommandEvent event) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("[COMMAND] ");

        stringBuilder.append(event.getGuild());
        stringBuilder.append(" | ");
        stringBuilder.append(event.getChannel());
        stringBuilder.append(" | ");
        stringBuilder.append(event.getUser());
        stringBuilder.append(" | ");
        stringBuilder.append(event.getName());
        stringBuilder.append(" | ");
        event.getOptions().forEach(
                optionMapping ->
                        stringBuilder
                                .append("[")
                                .append(optionMapping.getType())
                                .append(" ")
                                .append(optionMapping.getName())
                                .append(": ")
                                .append(optionMapping.getAsString())
                                .append("]")
        );
        
        this.log(stringBuilder.toString());
    }

    // Événements
    @Override
    public void onReady(@Nonnull final ReadyEvent event) {
        // BDD
        String databaseUrl = this.getVariable("SQL_DATABASE_URL");
        if (databaseUrl == null) this.logError("Database URL environment variable is undefined or null");

        String databaseUser = this.getVariable("SQL_DATABASE_USER");
        if (databaseUser == null) this.logError("Database user environment variable is undefined or null");

        String databasePasswd = this.getVariable("SQL_DATABASE_PASSWD");
        if (databasePasswd == null) this.logError("Database password environment variable is undefined or null");

        if (databaseUrl == null || databaseUser == null || databasePasswd == null) {
            this.logError("Please specify all database environment variables");
            this.getJda().shutdownNow();
        } else {
            this.bddBuilder = new BddBuilder(databaseUrl, databaseUser, databasePasswd);

            // Initialisation des commandes
            //CommandInitializer.initialize(this.getJda());
        }
    }

    @Override
    public void onSlashCommand(@Nonnull SlashCommandEvent event) {
        try (Bdd bdd = this.bddBuilder.buildBdd()) {
            this.logCommand(event);

            switch (event.getName()) {
                case "autoriser" -> new Autoriser(event, bdd).action();
                case "echo" -> new Echo(event, bdd).action();
                case "fonder" -> new Fonder(event, bdd).action();
                case "informations" -> new Informations(event, bdd).action();
                case "integrer" -> new Integrer(event, bdd).action();
                case "interdire" -> new Interdire(event, bdd).action();
                case "liste" -> new Liste(event, bdd).action();
                case "modifier" -> new Modifier(event, bdd).action();
                case "ping" -> new Ping(event, bdd).action();
                case "redefinir" -> new Redefinir(event, bdd).action();
                case "surface" -> new Surface(event, bdd).action();
            }
        } catch (Exception e) {
            this.logError("Error on slash command: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
