# Emojis locaux TFOT

Cette version d’Emojiful fonctionne uniquement côté client. Elle ne nécessite ni mod serveur, ni datapack, ni téléchargement à la connexion.

Les images sont regroupées dans une seule archive selon cette structure :

```text
emojiful/
├── emojis.zip
│   ├── Émotions/
│   │   ├── smiley.png
│   │   └── dance.gif
│   └── Pepe/
│       └── feelsbadman.png
└── categories.json
```

`Émotions/smiley.png` dans `emojis.zip` crée automatiquement la catégorie `Émotions` et l’emoji `:smiley:`. Les formats PNG et GIF sont acceptés. Le mod lit les noms UTF-8 et bascule automatiquement sur CP437 pour les archives créées par certains outils Windows ou WinRAR.

L’ordre d’affichage se règle dans `emojiful/categories.json` :

```json
{
  "order": ["Émotions", "Animaux & Nature", "Pepe"]
}
```

Les catégories absentes de cette liste sont ajoutées ensuite par ordre alphabétique français.

Un fichier JSON facultatif portant le même nom que l’image permet de conserver plusieurs alias :

```json
{
  "name": "smiley",
  "aliases": [":smiley:", ":happy:"],
  "texts": [":)"],
  "sort": 10
}
```

L’archive est lue une fois au démarrage du client Minecraft. Il faut donc redémarrer le jeu après avoir remplacé `emojis.zip` ou modifié `categories.json`.

Le workflow GitHub Actions construit également une archive contenant les emojis auparavant récupérés par URL. Il faut extraire son dossier `emojiful` directement dans la racine du jeu ou du modpack. Il n’est pas nécessaire d’extraire `emojis.zip`.
