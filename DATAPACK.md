# Datapack d’emojis TFOT

Le mod Forge lit les définitions via le type de recette datapack déjà fourni par Emojiful. Une définition utilise maintenant un fichier local du datapack au lieu d’une URL :

```json
{
  "type": "emojiful:emoji_recipe",
  "category": "Serveur",
  "name": "bonjour",
  "texture": "mon_pack:emoji/bonjour.png",
  "strings": [":bonjour:", ":hello:"],
  "texts": [],
  "sort": 0
}
```

La recette se place dans `data/mon_pack/recipes/bonjour.json` et son image dans `data/mon_pack/emoji/bonjour.png`. Le serveur transmet automatiquement les images aux joueurs lors de leur connexion et après `/reload`.

Le workflow GitHub Actions construit automatiquement `TFOT-Emojiful-Datapack-1.20.1.zip`, qui contient les Twemoji et les emojis personnalisés auparavant récupérés par URL. Le ZIP doit être copié sans extraction dans `<monde>/datapacks/`.
