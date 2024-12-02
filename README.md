# lazyyyyy
  
You know, we are lazy. Of course, both the game and computer are lazy as well.  
This mod here for let them be lazyyyyy.  
  
Mainly through asynchronous and lazy loading so that unnecessary logic doesn't prevent the game from launching.  
Take advantage from index for trading the cpu time with the memory and disk.  

## Features
- **Faster mixin config loading**
- (YACL) [**YACL image resource lazy and async loading.** ](https://github.com/isXander/YetAnotherConfigLib/issues/218)
    - Load when trying to render it. Won't render before loaded
- (Kiwi) **Faster manifest finding**
    - The manifest has to be in the jar of the mod itself. No trading honestly. No one will put a manifest of another mod in their own mod.
- **Lazy entity/block entity/player renderer**
    - Load when trying to render it. Won't render before loaded
- (Entity Sound Features) **Async sound event loading.**
    - Load when trying to play it. Won't play before loaded
- (MoreMcmeta) Optimize the memory usage
- Pack resource cache
    - Like how ModernFix works but for more pack types.
  
  
![yourkit](https://www.yourkit.com/images/yklogo.png)  
YourKit supports open source projects with innovative and intelligent tools
for monitoring and profiling Java and .NET applications.
YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>,
<a href="https://www.yourkit.com/dotnet-profiler/">YourKit .NET Profiler</a>,
and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.
