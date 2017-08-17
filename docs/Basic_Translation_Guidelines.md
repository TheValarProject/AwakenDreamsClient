# Basic Translation Guidelines

Thanks for taking an interesting in this project, we are always in need of translators! Our mod is currently available in English, French and Czech, and some work has been done to translate to it into Danish. These languages could use help with proofreading and maintenance, and we welcome the translation into any new language. If you are familiar with git, or are feeling ambitious, you may want to follow the instructions in our [Advanced Translation Guidelines](Advanced_Translation_Guidelines.md) instead of the instructions below.

## Adding Translations for a New Language

Adding support for an entirely new language is the most difficult translation job we have, but it is also very rewarding to be see it all come together when you play this game in your own language. We try to make it as easy as possible for translators to submit their work, just follow this simple step-by-step guide and hopefully you will have no difficulties contributing new translations.

To start, [create a Github account](https://github.com/join) if you do not already have one. The rest of this section will assume that you have a Github account and are signed in.

Next, [open an issue](https://github.com/TheValarProject/AwakenDreamsClient/issues/new) titled `Add <language> translation` where you replace <language> with the language you wish to translate to. Talk briefly in the body of the issue about the experience you have with the language you want to translate to. In particular, mention what dialect you speak/write. For example, if you speak French, then you could be a French Canadian or from France, or somewhere else entirely. This is important because sometimes Minecraft has multiple version of the same language and we want to make sure your changes are added to the right one. One of our collaborators (likely @scribblemaniac), will set up a file for you containing all the translation for the vanilla Minecraft things as well as all current mod strings that need translation. They will reply to your issue with a download link for this translation file for your language. A translation file contains a list of one translation per line which follow this format:
```
category.key=User displayed string
```
The `category.key` component is an identifier used by the program, and should *not* be translated or edited. The content after the = sign is what is actually displayed in-game, and should be translated. Lines from regular Minecraft should already be translated, and lines from the mod will be in English and will have `[TODO]` prefixed so you can find them easily.

Open this file with your favorite text editor (this doesn't require anything special, even Notepad will do), and go through each of the lines with a `[TODO]` prefix, translating the string on the right side of the line, and then removing the `[TODO]` prefix to mark it as finished. Try to follow the same conventions that are used by vanilla minecraft (such as for capitalization). Avoid slang wherever possible.

When you are done editing the file, save it and then return to the issue you made on Github. Create a response saying that you've finished your translation, and then attach the finished translation file by simply dragging it onto your response, and then submit your comment. A collaborator (again probably @scribblemaniac) will take care of the technical task of merging it into the source code, and will let you know when it is done and will close the issue you created. In the next development or stable build, you should be able to see the fruits of your labor. You are strongly encouraged to become a maintainer of the language after making your submission, see the section "Maintaining a Language" for details.

## Proofreading a Language

All of our translations could use some proofreading to make sure there are no spelling mistakes and that improper words are not used. To do this, go [here](https://github.com/TheValarProject/AwakenDreamsClient/tree/master/mcp/src/minecraft/assets/minecraft/lang) and click on the .lang file corresponding to your language. The file names are formatted like this: `<langugage>_<REGION>.lang`. Both the language and region are abbreviated to you will have to do a bit of thinking to figure out which one you want. For example the language english is en, and the region of the United States is US, so American English is in the `en_US.lang` file. You can always guess and check what language is in the file if you are in doubt.

Once you have selected the correct language file, look for sections belonging to the Awaken Dreams mod. At the time of writing this, there are four sections, which should be visually separated by blank lines. Here are the first entries in each of the sections so you can easily search for them:

| First Item in Section   | Section Description |
| ----------------------- | ------------------- |
| item.lembas.name        | Contains the names for all of the mod items |
| tile.oreJade.name       | Contains the names for all of the mod blocks |
| container.elvenCrafting | Used for strings in GUIs, such as for the rucksack or custom crafting tables |
| entity.Duck.name        | Specifies the names for entities added by the mod, such as new animals or NPCs |

Go through all of the lines in each of these sections, reading the strings to the right of the equal sign. If there is a mistake with any of them, copy the full line to a separate text document and make the necessary changes to fix it.

When you have finished reading through all of the lines added by the mod, find the maintenance thread for the language you proofread. Here is a list of links to said threads for convenience:

- [English](https://github.com/TheValarProject/AwakenDreamsClient/issues/39)
- [French](https://github.com/TheValarProject/AwakenDreamsClient/issues/38)
- [Czech](https://github.com/TheValarProject/AwakenDreamsClient/issues/43)

Make a new comment on that issue and include all of the corrected lines in it. One of our collaborators will take care of updating the translation.

## Maintaining a Language

Because maintaining a translation will likely require numerous submissions for changes, the basic approach we use in the above sections is simply not practical here as it is too time consuming for our programmers. However maintaining a language is really important to assure that it remains supported, especially since there are plans to add a great number of new features which will require translation. Thus, we recommend you follow the Maintaining a Language section of the [Advanced Translation Guidelines](Advanced_Translation_Guidelines.md) which will not be very difficult when you get the hang of it.
