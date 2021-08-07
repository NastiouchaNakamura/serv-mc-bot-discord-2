package fr.bde_tribu_terre.discord.mcbot.bdd;

import fr.bde_tribu_terre.discord.mcbot.couleur.MinecraftColor;
import fr.bde_tribu_terre.discord.mcbot.couleur.NotDefaultMinecraftColorException;
import fr.bde_tribu_terre.discord.mcbot.structures.*;
import fr.bde_tribu_terre.discord.mcbot.structures.Rectangle;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bdd implements AutoCloseable {
    // Attributs
    private final Connection connection;
    private List<Joueur> joueurs;
    private List<Equipe> equipes;

    // Constructeurs
    public Bdd(
            @Nonnull final String url,
            @Nonnull final String user,
            @Nonnull final String passwd
    ) throws SQLException {
        // Class.forName("com.mysql.jdbc.Driver");
        this.connection = DriverManager.getConnection(url, user, passwd);
        this.joueurs = null;
        this.equipes = null;
    }

    // Getteurs
    public ListeJoueurs getJoueurs() throws SQLException {
        if (this.joueurs == null) {
            PreparedStatement preparedStatement = this.connection.prepareStatement(
                    "SELECT uuid, username, discord FROM discordsrv_accounts NATURAL JOIN luckperms_players;"
            );
            ResultSet result = preparedStatement.executeQuery();

            List<Joueur> joueurs = new ArrayList<>();
            while (result.next()) {
                joueurs.add(
                        new Joueur(
                                result.getString(1),
                                result.getString(2),
                                result.getString(3)
                        )
                );
            }

            result.close();
            preparedStatement.close();

            // Enregistrement des joueurs qui ont tous été récupérés.
            this.joueurs = joueurs;
        }

        return new ListeJoueurs(this.joueurs);
    }

    public ListeEquipes getEquipes() throws SQLException {
        if (this.equipes == null) {
            // Récupération de tous les membres.
            // Préparation de la map.
            Map<String, List<String>> mapMembres = new HashMap<>();

            // Préparation de la requête.
            PreparedStatement preparedStatement = this.connection.prepareStatement(
                    "SELECT uuid, username, discord, permission FROM luckperms_user_permissions NATURAL JOIN luckperms_players NATURAL JOIN discordsrv_accounts WHERE permission LIKE 'group.%';"
            );
            ResultSet result = preparedStatement.executeQuery();

            // Récupération des données.
            while (result.next()) {
                if (!mapMembres.containsKey(result.getString(4).substring(6))) {
                    mapMembres.put(result.getString(4).substring(6), new ArrayList<>());
                }
                mapMembres.get(result.getString(4).substring(6)).add(result.getString(1));
            }

            // Récupération de tous les autorisés.
            // Préparation de la map.
            Map<String, List<String>> mapAutorises = new HashMap<>();

            // Préparation de la requête.
            preparedStatement = this.connection.prepareStatement(
                    "SELECT uuid, username, discord, region_id FROM worldguard_region_players NATURAL JOIN (SELECT id AS user_id, uuid FROM worldguard_user) AS T NATURAL JOIN luckperms_players NATURAL JOIN discordsrv_accounts;"
            );
            result = preparedStatement.executeQuery();

            // Récupération des données.
            while (result.next()) {
                if (!mapAutorises.containsKey(result.getString(4))) {
                    mapAutorises.put(result.getString(4), new ArrayList<>());
                }
                mapAutorises.get(result.getString(4)).add(result.getString(1));
            }

            // Récupération des équipes.
            preparedStatement = this.connection.prepareStatement(
                    "SELECT region_id AS id, min_x, min_z, max_x, max_z, value AS greeting_title, permission AS prefix, discord_role_id FROM (SELECT id AS region_id FROM worldguard_region WHERE parent = 'ville') AS T1 NATURAL JOIN (SELECT name AS region_id, permission FROM luckperms_group_permissions WHERE permission LIKE 'prefix.%') AS T2 NATURAL JOIN worldguard_region_cuboid NATURAL JOIN worldguard_region_flag NATURAL JOIN (SELECT id AS region_id, discord_role_id FROM mcbot_equipes) AS T3 WHERE flag = 'greeting-title';"
            );
            result = preparedStatement.executeQuery();

            List<Equipe> equipes = new ArrayList<>();
            Pattern patternPrefix = Pattern.compile("prefix\\..+\\.&(.+)\\[(.*)]");
            Pattern patternGreetingTitle = Pattern.compile("^\\|-\\n {2}.*\\n {2}(.*)\\n$");
            while (result.next()) {
                // Id
                String id = result.getString(1);

                // Nom et couleur
                Matcher matcherPrefixe = patternPrefix.matcher(result.getString(7));
                String nom = null;
                String matchingCouleur = null;
                while (matcherPrefixe.find()) {
                    nom = matcherPrefixe.group(2);
                    matchingCouleur = matcherPrefixe.group(1);
                }
                MinecraftColor couleur;
                try {
                    couleur = MinecraftColor.fromMinecraftCode(matchingCouleur);
                } catch (NotDefaultMinecraftColorException e) {
                    couleur = new MinecraftColor(new Color(255, 255, 255), "Erreur", "Erreur");
                }

                // Slogan
                Matcher matcherGreetingTitle = patternGreetingTitle.matcher(result.getString(6));
                String slogan = null;
                while (matcherGreetingTitle.find()) {
                    slogan = matcherGreetingTitle.group(1);
                }

                // Région
                Rectangle region = new Rectangle(
                        result.getInt(2),
                        result.getInt(3),
                        result.getInt(4),
                        result.getInt(5)
                );

                // Liste des membres
                List<Joueur> listeMembres = new ArrayList<>();
                if (mapMembres.containsKey(id)) {
                    for (String uuid : mapMembres.get(id)) {
                        Joueur membre = this.getJoueurs().getJoueurByUuid(uuid);
                        if (membre == null) {
                            throw new BddException(
                                    "L'utilisateur d'UUID `" + uuid + "` trouvé dans les groupes LuckPerms n'existe pas dans les synchronisations DiscordSRV ou est mal paramétrée."
                            );
                        }

                        listeMembres.add(this.getJoueurs().getJoueurByUuid(uuid));
                    }
                }

                // Liste des autorises
                List<Joueur> listeAutorises = new ArrayList<>();
                if (mapAutorises.containsKey(id)) {
                    for (String uuid : mapAutorises.get(id)) {
                        Joueur membre = this.getJoueurs().getJoueurByUuid(uuid);
                        if (membre == null) {
                            throw new BddException(
                                    "L'utilisateur d'UUID `" + uuid + "` trouvé dans les groupes LuckPerms n'existe pas dans les synchronisations DiscordSRV ou est mal paramétrée."
                            );
                        }

                        listeAutorises.add(this.getJoueurs().getJoueurByUuid(uuid));
                    }
                }

                // ID du rôle Discord
                String discordRoleId = result.getString(8);

                // Ajout de l'équipe.
                equipes.add(new Equipe(id, nom, couleur, slogan, region, listeMembres, listeAutorises, discordRoleId));
            }
            result.close();
            preparedStatement.close();

            // Enregistrement des équipes qui ont toutes été récupérées.
            this.equipes = equipes;
        }

        return new ListeEquipes(this.equipes);
    }

    // Setteurs
    public void setEquipe(Equipe equipe) throws SQLException {
        if (!this.getEquipes().containsEquipe(equipe)) {
            addEquipe(equipe);
        } else {
            updateEquipe(equipe);
        }
    }

    // Méthodes
    @Override
    public void close() throws SQLException {
        this.connection.close();
    }

    @Override
    public String toString() {
        StringBuilder r = new StringBuilder();
        r.append("BDDMySQL[\n");
        r.append("Joueurs:\n");
        for (Joueur joueur : this.joueurs) {
            r.append(joueur).append("\n");
        }
        r.append("Équipes:\n");
        for (Equipe equipe : this.equipes) {
            r.append(equipe).append("\n");
        }
        r.append("]");
        return r.toString();
    }

    // Méthodes privées
    private void addEquipe(Equipe equipe) throws SQLException {
        // Création de l'équipe.
        PreparedStatement preparedStatement = this.connection.prepareStatement(
                "INSERT INTO luckperms_groups VALUES (?);"
        );
        preparedStatement.setString(1, equipe.getId());
        preparedStatement.executeUpdate();

        // Paramétrage du displayname.
        preparedStatement = this.connection.prepareStatement(
                "INSERT INTO luckperms_group_permissions (name, permission, value, server, world, expiry, contexts) VALUES (?, ?, 1, 'global', 'global', 0, '{}');"
        );
        preparedStatement.setString(1, equipe.getId());
        preparedStatement.setString(2, "displayname.[" + equipe.getNom() + "]");
        preparedStatement.executeUpdate();

        // Paramétrage du préfixe.
        preparedStatement = this.connection.prepareStatement(
                "INSERT INTO luckperms_group_permissions (name, permission, value, server, world, expiry, contexts) VALUES (?, ?, 1, 'global', 'global', 0, '{}');"
        );
        preparedStatement.setString(1, equipe.getId());
        preparedStatement.setString(2, "prefix.0." + equipe.getCouleur().getMinecraftCode() + "[" + equipe.getNom() + "]");
        preparedStatement.executeUpdate();

        // Paramétrage des membres.
        for (Joueur joueur : equipe.getMembres().getJoueurs()) {
            preparedStatement = this.connection.prepareStatement(
                    "INSERT INTO luckperms_user_permissions (uuid, permission, value, server, world, expiry, contexts) VALUES (?, ?, 1, 'global', 'global', 0, '{}');"
            );
            preparedStatement.setString(1, joueur.getUuid());
            preparedStatement.setString(2, "group." + equipe.getId());
            preparedStatement.executeUpdate();
        }

        // Ajout du groupe dans Worldguard.
        preparedStatement = this.connection.prepareStatement(
                "INSERT INTO worldguard_group (name) VALUES (?);"
        );
        preparedStatement.setString(1, equipe.getId());
        preparedStatement.executeUpdate();

        // Ajout de la région dans Worldguard.
        preparedStatement = this.connection.prepareStatement(
                "INSERT INTO worldguard_region VALUES (?, 1, 'cuboid', 2, 'ville');"
        );
        preparedStatement.setString(1, equipe.getId());
        preparedStatement.executeUpdate();

        // Récupération de l'ID de groupe Worldguard généré.
        preparedStatement = this.connection.prepareStatement(
                "SELECT id FROM worldguard_group WHERE name = ? LIMIT 1;"
        );
        preparedStatement.setString(1, equipe.getId());
        ResultSet result = preparedStatement.executeQuery();
        int group_id = 0;
        if (result.next()) {
            group_id = result.getInt(1);
        }
        result.close();

        // Connexion de la région à l'équipe.
        preparedStatement = this.connection.prepareStatement(
                "INSERT INTO worldguard_region_groups VALUES (?, 1, ?, 1);"
        );
        preparedStatement.setString(1, equipe.getId());
        preparedStatement.setInt(2, group_id);
        preparedStatement.executeUpdate();

        // Ajout des coordonnées de la région.
        preparedStatement = this.connection.prepareStatement(
                "INSERT INTO worldguard_region_cuboid VALUES (?, 1, ?, 0, ?, ?, 256, ?);"
        );
        preparedStatement.setString(1, equipe.getId());
        preparedStatement.setInt(2, equipe.getRegion().getX1());
        preparedStatement.setInt(3, equipe.getRegion().getZ1());
        preparedStatement.setInt(4, equipe.getRegion().getX2());
        preparedStatement.setInt(5, equipe.getRegion().getZ2());
        preparedStatement.executeUpdate();

        // Ajout des paramètres Worldguard de la région.
        preparedStatement = this.connection.prepareStatement(
                "INSERT INTO worldguard_region_flag (region_id, world_id, flag, value) VALUES (?, 1, 'greeting-title', ?);"
        );
        preparedStatement.setString(1, equipe.getId());
        preparedStatement.setString(2, "|-\n  " + equipe.getNom() + "\n  " + equipe.getSlogan() + "\n");
        preparedStatement.executeUpdate();

        // Paramétrage des autorisés.
        for (Joueur joueur : equipe.getAutorises().getJoueurs()) {
            // Vérification si l'autorisé est déjà dans les utilisateurs WorldGuard.
            preparedStatement = this.connection.prepareStatement(
                    "SELECT id FROM worldguard_user WHERE uuid = ? LIMIT 1;"
            );
            preparedStatement.setString(1, joueur.getUuid());
            result = preparedStatement.executeQuery();

            int idJoueur;
            if (!result.next()) {
                result.close();

                // Prochain ID.
                preparedStatement = this.connection.prepareStatement(
                        "INSERT INTO worldguard_user (name, uuid) VALUES (NULL, ?);"
                );
                preparedStatement.setString(1, joueur.getUuid());
                preparedStatement.executeUpdate();

                // Récup ID joueur.
                preparedStatement = this.connection.prepareStatement(
                        "SELECT id FROM worldguard_user WHERE uuid = ? LIMIT 1;"
                );
                preparedStatement.setString(1, joueur.getUuid());
                result = preparedStatement.executeQuery();
                result.next();
            }
            idJoueur = result.getInt(1);
            result.close();

            // Ajout de l'autorisé.
            preparedStatement = this.connection.prepareStatement(
                    "INSERT INTO worldguard_region_players (region_id, world_id, user_id, owner) VALUES (?, 1, ?, 0);"
            );
            preparedStatement.setString(1, equipe.getId());
            preparedStatement.setInt(2, idJoueur);
            preparedStatement.executeUpdate();
        }

        // Ajout de l'ID du rôle Discord.
        preparedStatement = this.connection.prepareStatement(
                "INSERT INTO mcbot_equipes VALUES (?, ?);"
        );
        preparedStatement.setString(1, equipe.getId());
        preparedStatement.setString(2, equipe.getDiscordRoleId());
        preparedStatement.executeUpdate();

        // Fermeture de la commande.
        preparedStatement.close();

        // Ajout de l'équipe à la BDD Java.
        this.equipes.add(equipe);
    }

    private void updateEquipe(Equipe projectEquipe) throws SQLException {
        // Raccourci de l'équipe actuelle.
        Equipe currentEquipe = this.getEquipes().getEquipeById(projectEquipe.getId());

        // Initialisation des variables.
        PreparedStatement preparedStatement = null;
        ResultSet result;

        // Displayname (nom)
        if (!currentEquipe.getNom().equals(projectEquipe.getNom())) {
            preparedStatement = this.connection.prepareStatement(
                    "UPDATE luckperms_group_permissions SET permission = ? WHERE name = ? AND permission LIKE 'displayname.%';"
            );
            preparedStatement.setString(1, "displayname.[" + projectEquipe.getNom() + "]");
            preparedStatement.setString(2, currentEquipe.getId());
            preparedStatement.executeUpdate();
        }

        // Préfixe (nom ou couleur).
        if (
                !currentEquipe.getCouleur().getMinecraftId().equals(projectEquipe.getCouleur().getMinecraftId()) ||
                        !currentEquipe.getNom().equals(projectEquipe.getNom())
        ) {
            preparedStatement = this.connection.prepareStatement(
                    "UPDATE luckperms_group_permissions SET permission = ? WHERE name = ? AND permission LIKE 'prefix.0.%';"
            );
            preparedStatement.setString(1, "prefix.0." + projectEquipe.getCouleur().getMinecraftCode() + "[" + projectEquipe.getNom() + "]");
            preparedStatement.setString(2, currentEquipe.getId());
            preparedStatement.executeUpdate();
        }

        // Titre d'entrée dans la région (nom ou slogan).
        if (
                !currentEquipe.getSlogan().equals(projectEquipe.getSlogan()) ||
                        !currentEquipe.getNom().equals(projectEquipe.getNom())
        ) {
            preparedStatement = this.connection.prepareStatement(
                    "UPDATE worldguard_region_flag SET value = ? WHERE region_id = ? AND flag = 'greeting-title';"
            );
            preparedStatement.setString(1, "|-\n  " + projectEquipe.getNom() + "\n  " + projectEquipe.getSlogan() + "\n");
            preparedStatement.setString(2, currentEquipe.getId());
            preparedStatement.executeUpdate();
        }

        // Coordonnées de la région (région)
        if (!currentEquipe.getRegion().equals(projectEquipe.getRegion())) {
            preparedStatement = this.connection.prepareStatement(
                    "UPDATE worldguard_region_cuboid SET min_x = ?, min_y = 0, min_z = ?, max_x = ?, max_y = 256, max_z = ? WHERE region_id = ?;"
            );
            preparedStatement.setInt(1, projectEquipe.getRegion().getX1());
            preparedStatement.setInt(2, projectEquipe.getRegion().getZ1());
            preparedStatement.setInt(3, projectEquipe.getRegion().getX2());
            preparedStatement.setInt(4, projectEquipe.getRegion().getZ2());
            preparedStatement.setString(5, currentEquipe.getId());
            preparedStatement.executeUpdate();
        }

        // Membres
        List<Joueur> membresAAjouter = new ArrayList<>(projectEquipe.getMembres().getJoueurs());
        membresAAjouter.removeAll(currentEquipe.getMembres().getJoueurs());
        for (Joueur membre : membresAAjouter) {
            preparedStatement = this.connection.prepareStatement(
                    "INSERT INTO luckperms_user_permissions (uuid, permission, value, server, world, expiry, contexts) VALUES (?, ?, 1, 'global', 'global', 0, '{}');"
            );
            preparedStatement.setString(1, membre.getUuid());
            preparedStatement.setString(2, "group." + currentEquipe.getId());
            preparedStatement.executeUpdate();
        }

        List<Joueur> membresASupprimer = new ArrayList<>(currentEquipe.getMembres().getJoueurs());
        membresASupprimer.removeAll(projectEquipe.getMembres().getJoueurs());
        for (Joueur membre : membresASupprimer) {
            preparedStatement = this.connection.prepareStatement(
                    "DELETE FROM luckperms_user_permissions WHERE uuid = ? AND permission = ?;"
            );
            preparedStatement.setString(1, membre.getUuid());
            preparedStatement.setString(2, "group." + currentEquipe.getId());
            preparedStatement.executeUpdate();
        }

        // Autorisés
        List<Joueur> autorisesAAjouter = new ArrayList<>(projectEquipe.getAutorises().getJoueurs());
        autorisesAAjouter.removeAll(currentEquipe.getAutorises().getJoueurs());
        for (Joueur autorise : autorisesAAjouter) {
            // Vérification si l'autorisé est déjà dans les utilisateurs WorldGuard.
            preparedStatement = this.connection.prepareStatement(
                    "SELECT id FROM worldguard_user WHERE uuid = ? LIMIT 1;"
            );
            preparedStatement.setString(1, autorise.getUuid());
            result = preparedStatement.executeQuery();

            int idJoueur;
            if (!result.next()) {
                result.close();

                // Prochain ID.
                preparedStatement = this.connection.prepareStatement(
                        "INSERT INTO worldguard_user (name, uuid) VALUES (NULL, ?);"
                );
                preparedStatement.setString(1, autorise.getUuid());
                preparedStatement.executeUpdate();

                // Récup ID joueur.
                preparedStatement = this.connection.prepareStatement(
                        "SELECT id FROM worldguard_user WHERE uuid = ? LIMIT 1;"
                );
                preparedStatement.setString(1, autorise.getUuid());
                result = preparedStatement.executeQuery();
                result.next();
            }
            idJoueur = result.getInt(1);
            result.close();

            // Ajout de l'autorisé.
            preparedStatement = this.connection.prepareStatement(
                    "INSERT INTO worldguard_region_players (region_id, world_id, user_id, owner) VALUES (?, 1, ?, 0);"
            );
            preparedStatement.setString(1, currentEquipe.getId());
            preparedStatement.setInt(2, idJoueur);
            preparedStatement.executeUpdate();
        }

        List<Joueur> autorisesASupprimer = new ArrayList<>(currentEquipe.getAutorises().getJoueurs());
        autorisesASupprimer.removeAll(projectEquipe.getAutorises().getJoueurs());
        for (Joueur autorise : autorisesASupprimer) {
            // Récupération ID joueur
            preparedStatement = this.connection.prepareStatement(
                    "SELECT id FROM worldguard_user WHERE uuid = ? LIMIT 1;"
            );
            preparedStatement.setString(1, autorise.getUuid());
            result = preparedStatement.executeQuery();
            result.next();
            int idJoueur = result.getInt(1);
            result.close();

            // Suppression du joueur.
            preparedStatement = this.connection.prepareStatement(
                    "DELETE FROM worldguard_region_players WHERE region_id = ? AND user_id = ?;"
            );
            preparedStatement.setString(1, currentEquipe.getId());
            preparedStatement.setInt(2, idJoueur);
            preparedStatement.executeUpdate();
        }

        // ID rôle Discord.
        if (!currentEquipe.getDiscordRoleId().equals(projectEquipe.getDiscordRoleId())) {
            preparedStatement = this.connection.prepareStatement(
                    "UPDATE mcbot_equipes SET discord_role_id = ? WHERE id = ?;"
            );
            preparedStatement.setString(2, projectEquipe.getDiscordRoleId());
            preparedStatement.setString(1, projectEquipe.getId());
            preparedStatement.executeUpdate();
        }

        // Fermeture de la commande.
        if (preparedStatement != null) {
            preparedStatement.close();
        }

        // Ajout de l'équipe à la BDD Java.
        for (Equipe equipe : this.equipes) {
            if (equipe.getId().equals(currentEquipe.getId())) {
                this.equipes.remove(equipe);
                break;
            }
        }
        this.equipes.add(projectEquipe);
    }
}
