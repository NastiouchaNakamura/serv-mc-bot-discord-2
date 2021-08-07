package fr.bde_tribu_terre.discord.mcbot.structures;

import java.util.List;

public class ListeEquipes {
    // Attributs
    private final List<Equipe> equipes;

    // Constructeurs
    public ListeEquipes(List<Equipe> equipes) {
        this.equipes = equipes;
    }

    // Getteurs
    public List<Equipe> getEquipes() {
        return this.equipes;
    }

    public int getSize() {
        return this.equipes.size();
    }

    public Equipe getEquipeById(String id) {
        for (Equipe equipe : equipes) {
            if (equipe.getId().equals(id)) {
                return equipe;
            }
        }
        return null;
    }

    public Equipe getEquipeByMembre(Joueur joueur) {
        for (Equipe equipe : equipes) {
            if (equipe.getMembres().containsJoueur(joueur)) {
                return equipe;
            }
        }
        return null;
    }

    public Equipe getEquipeByAutorise(Joueur joueur) {
        for (Equipe equipe : equipes) {
            if (equipe.getAutorises().containsJoueur(joueur)) {
                return equipe;
            }
        }
        return null;
    }

    // Méthodes
    public boolean containsEquipe(Equipe equipe) {
        return this.getEquipeById(equipe.getId()) != null;
    }
}
