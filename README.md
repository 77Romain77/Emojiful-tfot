# Emojiful TFOT — Forge 1.20.1

Fork d’[Emojiful](https://github.com/InnovativeOnlineIndustries/Emojiful) adapté pour **The Fortress Of Thieves**.

Cette version fonctionne entièrement côté client : elle ne nécessite ni mod serveur, ni datapack, ni téléchargement d’images à la connexion. Les emojis sont lus directement depuis une archive locale `emojiful/emojis.zip`.

## Fonctionnalités

- Affichage des codes comme `:smiley:` sous forme d’emojis dans le chat.
- Rendu immédiat dans le champ de saisie, avant l’envoi du message.
- Support des images PNG et des GIF animés.
- Autocomplétion et recherche dans le sélecteur d’emojis.
- Catégories locales avec espaces, caractères spéciaux et accents français.
- Ordre des catégories configurable dans `categories.json`.
- Défilement à la molette limité à la fenêtre survolée.
- Barres de défilement des catégories et des emojis déplaçables à la souris.
- Limites de défilement corrigées : il n’est plus possible de continuer loin après le dernier emoji.
- Saisie de la recherche isolée du chat, y compris pour la suppression de caractères.
- Alignement corrigé pour les textes en gras.
- Bouton du sélecteur d’emojis réparé, même si les catégories ont été renommées.

## Installation

Téléchargez les deux artefacts produits par le dernier [workflow GitHub Actions](https://github.com/77Romain77/Emojiful-tfot/actions) :

1. `Emojiful-1.20.1-...` contient le JAR Forge à placer dans le dossier `mods`.
2. `TFOT-Emojiful-Local-Emojis-1.20.1-...` contient le pack local à extraire dans la racine de l’instance Minecraft.

Après extraction, la structure doit être la suivante :

```text
<instance Minecraft>/
├── mods/
│   └── Emojiful-Forge-tfot-1.20.1-4.2.0.jar
└── emojiful/
    ├── emojis.zip
    ├── categories.json
    └── README.txt
```

Il ne faut **pas extraire** le fichier intérieur `emojis.zip`. Avec Modrinth, utilisez le bouton permettant d’ouvrir le dossier de l’instance afin d’installer les fichiers dans le bon profil.

Redémarrez Minecraft après avoir remplacé `emojis.zip` ou modifié les catégories.

## Ajouter des emojis et des catégories

Le premier niveau de `emojis.zip` contient les dossiers de catégories. Une image doit être placée directement dans sa catégorie :

```text
emojis.zip
├── Émotions/
│   ├── smiley.png
│   └── dance.gif
└── Pepe/
    └── feelsbadman.png
```

`Émotions/smiley.png` crée automatiquement la catégorie `Émotions` et le code `:smiley:`.

Le mod essaie d’abord de lire les noms du ZIP en UTF-8, puis utilise automatiquement CP437 pour les archives créées par certains outils Windows ou WinRAR. Le même pack peut ainsi être utilisé sur Windows, macOS et Linux.

Un fichier JSON facultatif portant le même nom que l’image permet de définir plusieurs alias, des raccourcis textuels et une position :

```json
{
  "name": "smiley",
  "aliases": [":smiley:", ":happy:"],
  "texts": [":)"],
  "sort": 10
}
```

## Choisir l’ordre des catégories

L’ordre d’affichage est défini dans `emojiful/categories.json` :

```json
{
  "order": [
    "Émotions",
    "Personnes & Corps",
    "Animaux & Nature",
    "Pepe"
  ]
}
```

Les noms doivent correspondre aux dossiers présents dans `emojis.zip`. Les catégories non indiquées sont ajoutées ensuite par ordre alphabétique français.

## Fonctionnement réseau

Le message envoyé au serveur conserve son code texte, par exemple `:smiley:`. Les joueurs possédant le mod voient l’image correspondante ; les autres voient simplement le code. Le serveur n’envoie aucune image et n’a pas besoin d’installer Emojiful.

Toutes les anciennes requêtes d’images par URL ont été retirées. Le workflow génère un pack local contenant les emojis auparavant récupérés en ligne afin qu’ils puissent être distribués avec le modpack.

Pour davantage de détails sur le format du pack, consultez [LOCAL_EMOJIS.md](LOCAL_EMOJIS.md).

## Compilation

Le workflow GitHub Actions compile uniquement la version **Forge 1.20.1** avec Java 17 et publie :

- le JAR du mod ;
- le pack local TFOT prêt à installer.

## Crédits

- Projet original : [InnovativeOnlineIndustries/Emojiful](https://github.com/InnovativeOnlineIndustries/Emojiful)
- Métadonnées et images Twemoji utilisées par le générateur : [iamcal/emoji-data](https://github.com/iamcal/emoji-data)
- Assets personnalisés : [InnovativeOnlineIndustries/emojiful-assets](https://github.com/InnovativeOnlineIndustries/emojiful-assets)
