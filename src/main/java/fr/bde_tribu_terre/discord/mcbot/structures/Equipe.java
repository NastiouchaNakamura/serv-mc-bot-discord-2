package fr.bde_tribu_terre.discord.mcbot.structures;

import fr.bde_tribu_terre.discord.mcbot.couleur.MinecraftColor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.RoleAction;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Equipe {
    // Attributs
    private final String id;
    private final String nom;
    private final MinecraftColor couleur;
    private final String slogan;
    private final Rectangle region;
    private final List<Joueur> membres;
    private final List<Joueur> autorises;
    private String discordRoleId;

    // Constructeurs
    public Equipe(String id, String nom, MinecraftColor couleur, String slogan, Rectangle rectangle, List<Joueur> membres, List<Joueur> autorises, String discordRoleId) {
        this.id = id;
        this.nom = nom;
        this.couleur = couleur;
        this.slogan = slogan;
        this.region = rectangle;
        this.membres = membres;
        this.autorises = autorises;
        this.discordRoleId = discordRoleId;
    }

    // Getteurs
    public String getId() {
        return this.id;
    }

    public String getNom() {
        return this.nom;
    }

    public MinecraftColor getCouleur() {
        return this.couleur;
    }

    public String getSlogan() {
        return this.slogan;
    }

    public Rectangle getRegion() {
        return this.region;
    }

    public ListeJoueurs getMembres() {
        return new ListeJoueurs(this.membres);
    }

    public ListeJoueurs getAutorises() {
        return new ListeJoueurs(this.autorises);
    }

    public String getDiscordRoleId() {
        return this.discordRoleId;
    }

    // Méthodes
    private String addDiscordRole(@Nonnull JDA jda) throws DiscordException {
        Guild serveur = jda.getGuildById("775843942066683965");
        if (serveur == null) {
            throw new DiscordException(
                    "Le serveur n'a pas pu être récupéré. L'ID peut être incorrect."
            );
        }

        RoleAction roleAction = serveur.createRole()
                .setName(this.getNom())
                .setColor(this.getCouleur().getColor())
                .setMentionable(true)
                .setHoisted(true);

        Role role = roleAction.complete();

        for (Joueur joueur : this.getMembres().getJoueurs()) {
            serveur.addRoleToMember(joueur.getDiscordId(), role).queue();
        }

        return role.getId();
    }

    private void updateDiscordRole(@Nonnull JDA jda) throws DiscordException {
        Guild serveur = jda.getGuildById("775843942066683965");
        if (serveur == null) {
            throw new DiscordException(
                    "Le serveur n'a pas pu être récupéré. L'ID peut être incorrect."
            );
        }

        Role role = serveur.getRoleById(this.getDiscordRoleId());
        if (role == null) {
            throw new DiscordException(
                    "Le rôle n'a pas pu être récupéré. L'ID peut être incorrect, ou il a pu être effacé."
            );
        }

        role.getManager()
                .setName(this.getNom())
                .setColor(this.getCouleur().getColor())
                .queue();

        List<User> currentUsers = new ArrayList<>();
        for (Member member : serveur.getMembersWithRoles(role)) {
            currentUsers.add(member.getUser());
        }

        List<User> nouveauxUsers = new ArrayList<>();
        for (Joueur joueur : this.getMembres().getJoueurs()) {
            nouveauxUsers.add(joueur.getUser(jda));
        }

        List<User> usersAAjouter = new ArrayList<>(nouveauxUsers);
        usersAAjouter.removeAll(currentUsers);

        List<User> usersARetirer = new ArrayList<>(currentUsers);
        usersARetirer.removeAll(nouveauxUsers);

        for (User user : usersAAjouter) {
            serveur.addRoleToMember(user, role).queue();
        }

        for (User user : usersARetirer) {
            serveur.removeRoleFromMember(user, role).queue();
        }
    }

    public void setDiscordRole(@Nonnull JDA jda) throws DiscordException {
        if (this.getDiscordRoleId() == null) {
            this.discordRoleId = this.addDiscordRole(jda);
        } else {
            this.updateDiscordRole(jda);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Equipe equipe = (Equipe) o;
        return equipe.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), this.getNom(), this.getCouleur(), this.getSlogan(), this.getRegion());
    }

    @Override
    public String toString() {
        StringBuilder r = new StringBuilder();
        r.append("Equipe[\n");
        r.append("ID: ").append(this.getId()).append("\n");
        r.append("Nom: ").append(this.getNom()).append("\n");
        r.append("Couleur: ").append(this.getCouleur()).append("\n");
        r.append("Slogan: ").append(this.getSlogan()).append("\n");
        r.append("Région: ").append(this.getRegion()).append("\n");
        r.append("Membres:\n");
        for (Joueur joueur: this.membres) {
            r.append(joueur).append("\n");
        }
        r.append("Autorisés:\n");
        for (Joueur joueur: this.autorises) {
            r.append(joueur).append("\n");
        }
        r.append("]");
        return r.toString();
    }
}
