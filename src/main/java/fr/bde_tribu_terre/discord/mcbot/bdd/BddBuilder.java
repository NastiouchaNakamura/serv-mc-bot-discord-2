package fr.bde_tribu_terre.discord.mcbot.bdd;

import javax.annotation.Nonnull;
import java.sql.SQLException;

public class BddBuilder {
    // Attributs
    private final String url;
    private final String user;
    private final String passwd;

    // Constructeurs
    public BddBuilder(@Nonnull final String url, @Nonnull final String user, @Nonnull final String passwd) {
        this.url = url;
        this.user = user;
        this.passwd = passwd;
    }

    // Méthodes
    public Bdd buildBdd() throws SQLException {
        return new Bdd(this.url, this.user, this.passwd);
    }
}
