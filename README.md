# lazyyyyy
  
You know, we are lazy. Of course, both the game and computer are lazy as well.  
This mod here for let them be lazyyyyy.  
  
Mainly through asynchronous and lazy loading so that unnecessary logic doesn't prevent the game from launching.  
Take advantage from index for trading the cpu time with the memory and disk.  

## Features
Tests are run on my own pc and pack with 700+ mods on Forge.  
All the time here is **on my machine with my pack** and provided by profiler that is time on multi threads.  
It's not rigorous at all, and it's not a value that anyone except me can refer to, because I'm too lazy.  

- Faster mixin config loading
    - 50 secs → 1200ms
- [YACL image resource lazy and async loading. ](https://github.com/isXander/YetAnotherConfigLib/issues/218)
    - 20 secs → None
    - Trade: the images will need to wait a few seconds to load before you can see them in the config screen
- Faster Kiwi manifest finding
    - 50 secs -> 200ms
    - Trade: The manifest has to be in the jar of the mod itself. No trading honestly. No one will put a manifest of another mod in their own mod.