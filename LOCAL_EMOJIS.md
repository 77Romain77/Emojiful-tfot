# Emojis locaux TFOT

Cette version d’Emojiful fonctionne uniquement côté client. Elle ne nécessite ni mod serveur, ni datapack, ni téléchargement à la connexion.

Les images sont placées dans le dossier du jeu selon cette structure :

```text
emojiful/
└── emojis/
    ├── Emotions/
    │   ├── smiley.png
    │   └── dance.gif
    └── Pepe/
        └── feelsbadman.png
```

`emojiful/emojis/Emotions/smiley.png` crée automatiquement la catégorie `Emotions` et l’emoji `:smiley:`. Les formats PNG et GIF sont acceptés. Le dossier est lu une fois au démarrage du client Minecraft.

Un fichier JSON facultatif portant le même nom que l’image permet de conserver plusieurs alias :

```json
{
  "name": "smiley",
  "aliases": [":smiley:", ":happy:"],
  "texts": [":)"],
  "sort": 10
}
```

Le workflow GitHub Actions construit également une archive contenant les 2 008 emojis auparavant récupérés par URL. Il faut extraire son dossier `emojiful` directement dans la racine du jeu ou du modpack.
