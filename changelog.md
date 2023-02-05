## Fzzy Core Refactor update
As of version 1.0.0, Amethyst Core is no longer standalone. I broke the library apart into more refined and focused pieces. Amethyst Core is now a expansion library built off my new base library [Fzzy Core](https://github.com/fzzyhmstrs/fc)

Amethyst Core is the Magic Mod piece of that refactoring process. Many of the functions it contained are now in Fzzy Core, but it retains:
* Scepters: The abstract scepters and the augment scepter framework is still in place
* Augment Modifiers: Amethyst Core is the home for the scepter-relevant Augment Modifiers still. The abstract modifier framework is moved to FC.
* AbstractAugmentBookItem and AbstractAugmentJewelryItem both retained, as they are important pieces of the Amethyst Imbuement style magic system.

#### _Going forward, I will also no longer be embedding AC. This is because I am getting closer to releasing more content mods that utilize the FC family of libraries, so don't want multiple embedded copies to be wasting space, potentially version fighting, etc._