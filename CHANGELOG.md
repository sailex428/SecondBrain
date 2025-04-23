# Changelog

All notable changes to this project will be documented in this file.

## [2.0.1] - 2025-04-06

### 🚀 Features

- Add conversation history
- Add all npc capabilities as function tool
- Add getConversation function tool
- Add actions to queue on function call
- Implement openai function calling
- Feat: impl efficient resource saving to db on server stop
- Feat: add function calling support for ollama
- created constants of descriptions and names
- Add functionRepository
- Add vectorizing functions on init of functionManager
- Add getting relevant llmFunctions based on the prompt
- Get all events working on server
- Feat: remove functions that are already called
- set temperature of llm to 0.3 (so llm uses more tool calls)
- Add modeController + default self-reliance mode
- Feat: big improvements in controller and npc creation
- moved npc creation logic into spawner class
- add npc functions that the llm can easier understand
- add chunkManager that handles contextBuilding of current loadedChunks (caching)
- make factory compatible with new config system
- add goalThread that executes new goals in the queue
- impl basic config screen
- Add packets for updating base and single npc config
- Add receivers for new update packets on server
- Add full packet functionality to screens
- Impl all config screens
- Add layout for screens

### 🐛 Bug Fixes

- Fix some stupid runtime errors caused wrong usage of automatone
- Refactor contextGen to util class; adjust logger prefix to SecondBrain
- Inner class not usable for function calling; close open threads on server stop
- Fix player not found on spawn command
- Remove adding latest convos to system prompt
- fix npc message doesnt have a name in it
- fix recursive function calling
- Add instruction to send firstly chat message
- Adjust project structure, do some renaming
- Registering default functions ollama
- Add always needed function for llm requests
- Try to fix some eventListeners, so there're working on server
- Add better error handling
- Register one event listener for all npcs
- Remove param to set llmmodel in command + smaller tweaks
- Fix missing ollama jar
- Deactivate event listening of block interaction

### 💼 Other

- Add autorespawn on npc death
- Add model suggestion depending on user input
- Try to optimize function calling, by giving llm only most relevant functions
- Optimize tool calling
- Renaming of packages to secondbrain
- Removed chat tool_function
- adjust promptFormatter
- add promptTemplate and default sys prompt
- Add isActive to npc config


### 🚜 Refactor

- Refactor NPCFactory, add start ticking of controller in constructor
- Refactor config logic
- Refactor Context generating
- Move despawn logic into spawner class
