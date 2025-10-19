# Changelog

All notable changes to this project will be documented in this file.

## [3.0.0] - 2025-10-19

### 🚀 Features

- Add system prompt input in configScreen for openai
- Add model input to screen
- Add saved messages (from db) to conversationHistory
- Add deleting convos when NPC has been deleted
- Add error handling for commandErrors

### 🐛 Bug Fixes

- Error loading resources when internal server didnt started yet
- Chiseled gradle task doesnt exist anymore
- Fix gradle not found
- Fix gradle not found
- Typo in gradle task
- Couldnt find timestamp column sql error
- Mojmap issues when using carpet
- Role doesnt exists
- Player2api content field missing
- Cannot parse player2api response, index field missing
- To many fields unnecessary fields are in the response
- Prevent some nullpointers
- Rejoin in world crashes client
- Npcs doesnt spawn
- Can only manipulate existing players
- Model field is shown in player2 config
- Prevent classCastError
- ApiKey overwritten with default string
- ModelInput is shown twice
- Api key is overwritten when updating on client with default text
- Prevent exceptions when spawning an npc that already exists
- Api key still overwritten
- Convos not saved
- Prevent printing several times same response
- Prevent printing several times same response
- Fix-runtime-mismatch-class-file-versions (#43)

### 💼 Other

- Integrate to structured output -> dev (#39)
- Integrate secondbrainengine -> main
- Add publish gradle tasks
- Mc 1.21.8 support -> main
- Use java 17 for older versions
- Downgrade to java 17
- Use java17 for older mc versions -> main
- Up wispforest deps

### 🚜 Refactor

- Remove recipeRepo
- Remove npc modes
- Remove some more unnecessary stuff
- Remove more unuseful stuff
- Make networkHandler 1.20 compatible
- Main class, npcFactory
- ResourceProvider
- ConversationHistory
- Function calling api
- Add getItems function for testing
- Make sttHudElement 1.20.1 compatible
- Adjust system, summary and init prompt
- Tweak prompts
- Add commands to system prompt
- Set owner on controller
- Switch to using structured output
- Remove functionCallable interface
- Rename player2 types
- Remove functionCalling completely
- Add openAiClient again
- Build script
- Check llm reachable before spawn npc
- Remove print exception message into game chat

### 📚 Documentation

- Update readme

### 🧪 Testing

- Add latest mc version (1.21.8)
- Use latest git-cliff-action

### ⚙️ Miscellaneous Tasks

- Bump stonecutter version
- Add secondbrainengine dep
- Remove gradle properties for 1.20.4 and 1.21.3
- Up some deps
- Up fabric loader
- Add secondbrainengine 1.21.1
- Add carpet
- Switch back to engine 1.1.2
- Up fapi + engine
- Add support for mc version 1.21.8
- Bump loom, gradle wrapper
- Add publish task
- Bump version
- Remove not available avatar pic
- Update publish workflow
- Add java 17 compiled engine
- Bump engine
- Publish only 1.20.1

Download the new release on [Modrinth](https://modrinth.com/mod/secondbrain/versions).
