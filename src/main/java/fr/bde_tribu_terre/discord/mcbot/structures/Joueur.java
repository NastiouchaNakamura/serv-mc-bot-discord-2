package fr.bde_tribu_terre.discord.mcbot.structures;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Joueur {
    // Attributs
    private final String uuid;
    private final String username;
    private final UserSnowflake discordId;
    private String discordTag;
    private User discordUser;

    // Constructeurs
    public Joueur(String uuid, String username, String discordId) {
        this.uuid = uuid;
        this.username = username;
        this.discordId = UserSnowflake.fromId(discordId);
    }

    public Joueur(String uuid, String username, UserSnowflake discordId) {
        this.uuid = uuid;
        this.username = username;
        this.discordId = discordId;
    }

    // Getteurs
    public String getUuid() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public UserSnowflake getDiscordId() {
        return this.discordId;
    }

    public String getDiscordTag(@Nonnull JDA jda) {
        if (this.discordTag == null) {
            this.discordTag = this.getUser(jda).getAsTag();
        }
        return this.discordTag;
    }

    public User getUser(@Nonnull JDA jda) {
        if (this.discordUser == null) {
            this.discordUser = jda.getUserById(this.getDiscordId().getId());
            if (this.discordUser == null) {
                this.discordUser = jda.retrieveUserById(this.getDiscordId().getId()).complete();
            }
        }
        return this.discordUser;
    }

    // Méthodes
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Joueur joueur = (Joueur) o;
        return joueur.getUuid().equals(this.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getUuid(), this.getUsername(), this.getDiscordId());
    }

    @Override
    public String toString() {
        return "Joueur[" +
                "UUID: " + this.getUuid() + ", " +
                "Username: " + this.getUsername() + ", " +
                "Discord ID: " + this.getDiscordId() +
                "]";
    }
}
