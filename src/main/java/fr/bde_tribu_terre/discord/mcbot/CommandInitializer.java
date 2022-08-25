package fr.bde_tribu_terre.discord.mcbot;

import fr.bde_tribu_terre.discord.mcbot.couleur.MinecraftColor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

public class CommandInitializer {
    public static void initialize(@Nonnull final JDA jda) {
        jda.upsertCommand(
                "ping",
                "Renvoie pong."
        ).and(
                jda.upsertCommand(
                        new CommandDataImpl(
                                "autoriser",
                                "Autorise l'accès à la région de votre équipe au joueur."
                        ).addOptions(
                                new OptionData(
                                        OptionType.USER,
                                        "membre",
                                        "Mention Discord du joueur à autoriser.",
                                        true
                                )
                        )
                )
        ).and(
                jda.upsertCommand(
                        new CommandDataImpl(
                                "echo",
                                "Renvoie le message envoyé."
                        ).addOptions(
                                new OptionData(
                                        OptionType.STRING,
                                        "message",
                                        "Message à renvoyer.",
                                        true
                                )
                        )
                )
        ).and(
                jda.upsertCommand(
                        new CommandDataImpl(
                                "fonder",
                                "Fonde une équipe rassemblant les 2 membres : le lanceur de la commande et la personne identifiée."
                        ).addOptions(
                                new OptionData(
                                        OptionType.STRING,
                                        "identifiant",
                                        "Identifiant de l'équipe, NE PEUT PAS ÊTRE CHANGÉ, qui identifiera l'équipe dans les commandes.",
                                        true
                                ),
                                new OptionData(
                                        OptionType.STRING,
                                        "nom",
                                        "Nom de l'équipe qui sera affiché dans le pseudo et lors de l'entrée sur la zone de propriété.",
                                        true
                                ),
                                new OptionData(
                                        OptionType.STRING,
                                        "couleur",
                                        "Couleur de l'équipe.",
                                        true
                                ).addChoices(
                                        MinecraftColor.getDefaultMinecraftColorIds()
                                                .stream()
                                                .map(
                                                        color -> new Command.Choice(color, color)
                                                )
                                                .collect(Collectors.toList())
                                ),
                                new OptionData(
                                        OptionType.STRING,
                                        "slogan",
                                        "Slogan de l'équipe qui sera affiché lors de l'entrée sur la zone de propriété.",
                                        true
                                ),
                                new OptionData(
                                        OptionType.INTEGER,
                                        "x1",
                                        "Valeur sur l'axe X du premier point.",
                                        true
                                ),
                                new OptionData(
                                        OptionType.INTEGER,
                                        "z1",
                                        "Valeur sur l'axe Z du premier point.",
                                        true
                                ),
                                new OptionData(
                                        OptionType.INTEGER,
                                        "x2",
                                        "Valeur sur l'axe X du deuxième point.",
                                        true
                                ),
                                new OptionData(
                                        OptionType.INTEGER,
                                        "z2",
                                        "Valeur sur l'axe Z du deuxième point.",
                                        true
                                )
                        )
                )
        ).and(
                jda.upsertCommand(
                        new CommandDataImpl(
                                "informations",
                                "Indique toutes les informations correspondantes à l'équipe."
                        ).addOptions(
                                new OptionData(
                                        OptionType.STRING,
                                        "id",
                                        "ID de l'équipe.",
                                        true
                                )
                        )
                )
        ).and(
                jda.upsertCommand(
                        new CommandDataImpl(
                                "integrer",
                                "Intègre le joueur dans votre équipe en tant que coéquipier."
                        ).addOptions(
                                new OptionData(
                                        OptionType.USER,
                                        "membre",
                                        "Mention Discord du joueur à intégrer à l'équipe.",
                                        true
                                )
                        )
                )
        ).and(
                jda.upsertCommand(
                        new CommandDataImpl(
                                "interdire",
                                "Interdit l'accès à la région de votre équipe au joueur qui était précédemment autorisé."
                        ).addOptions(
                                new OptionData(
                                        OptionType.USER,
                                        "membre",
                                        "Mention Discord du joueur à annuler l'autorisation.",
                                        true
                                )
                        )
                )
        ).and(
                jda.upsertCommand(
                        new CommandDataImpl(
                                "liste",
                                "Affiche la liste des équipes ainsi que leurs identifiants."
                        )
                )
        ).and(
                jda.upsertCommand(
                        new CommandDataImpl(
                                "modifier",
                                "Modifie l'attribut désiré de votre équipe."
                        ).addOptions(
                                new OptionData(
                                        OptionType.STRING,
                                        "attribut",
                                        "L'attribut à modifier. Attributs modifiables : Nom, Couleur, Slogan.",
                                        true
                                ).addChoice(
                                        "Nom",
                                        "nom"
                                ).addChoice(
                                        "Couleur",
                                        "couleur"
                                ).addChoice(
                                        "Slogan",
                                        "slogan"
                                )
                        )
                )
        ).and(
                jda.upsertCommand(
                        new CommandDataImpl(
                                "redefinir",
                                "Redéfinit la région de votre équipe telle qu'elle soit le rectangle formé à partir des deux points."
                        ).addOptions(
                                new OptionData(
                                        OptionType.INTEGER,
                                        "x1",
                                        "Valeur sur l'axe X du premier point.",
                                        true
                                ),
                                new OptionData(
                                        OptionType.INTEGER,
                                        "z1",
                                        "Valeur sur l'axe Z du premier point.",
                                        true
                                ),
                                new OptionData(
                                        OptionType.INTEGER,
                                        "x2",
                                        "Valeur sur l'axe X du deuxième point.",
                                        true
                                ),
                                new OptionData(
                                        OptionType.INTEGER,
                                        "z2",
                                        "Valeur sur l'axe Z du deuxième point.",
                                        true
                                )
                        )
                )
        ).and(
                jda.upsertCommand(
                        new CommandDataImpl(
                                "surface",
                                "Indique la surface occupée par le rectangle défini par les coordonnées."
                        ).addOptions(
                                new OptionData(
                                        OptionType.INTEGER,
                                        "x1",
                                        "Valeur sur l'axe X du premier point.",
                                        true
                                ),
                                new OptionData(
                                        OptionType.INTEGER,
                                        "z1",
                                        "Valeur sur l'axe Z du premier point.",
                                        true
                                ),
                                new OptionData(
                                        OptionType.INTEGER,
                                        "x2",
                                        "Valeur sur l'axe X du deuxième point.",
                                        true
                                ),
                                new OptionData(
                                        OptionType.INTEGER,
                                        "z2",
                                        "Valeur sur l'axe Z du deuxième point.",
                                        true
                                )
                        )
                )
        ).queue();
    }
}
