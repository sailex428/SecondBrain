# Changelog

All notable changes to this project will be documented in this file.

## [2.1.0] - 2025-05-28

### 🚀 Features

- Implement Player2APIClient for communicating with player2 service
- Add verbose config option
- Impl functionCalling in player2Client
- Add player2 character synchronizing at server start
- Add player2ActionCommand to make manual char sync possible
- Add tts and stt
- Add healthchecker
- Add llmType specific icon in config gui for every npc
- Add checkbox for tts to gui
- Put history into top of user prompt
- Add feedback on press of sttkey
- Add init prompt of player2 npcs
- Feat: optimize conversationhistory
- add user prompt and responses to conversation history in own message object
- adjust response types that prompting works with new player2 api changes
- adjust system prompt for player2


### 🐛 Bug Fixes

- Remove unused imports
- Add more meaningful error message
- Deleting instead of removing npcs at sync
- Move health checking into npcSynchronizer
- Prevent null pointer
- Only add header if not empty
- Npc cant get respawned
- Add missing fields in pojo that are not documented but still in the response
- Wrong json structure of functions
- Use uuid of entity instead of the one of the config
- Put uuid in deletePacket instead of name
- Make textures get shown right
- Dont listen to own chat messages
- Messages doesnt got added to chatRequest
- Skinurl after respawn null
- Encoding issue caused by null strings
- Prevent llmExceptions while performing stt actions
- Add npcName to stt error log
- Prevent executor isTerminated status
- Use correct endpoint on stt stop
- Add better error handling for stt requests
- Fix: smaller ui issues
- add scrollable container to npc screen so that multiple ones can get shown
- adjust timeout label in baseConfigScreen

- Timeout from baseConfig now also gets used from player2 client
- Make config editable of player2 also if npc is active
- Prevent some concurrent modification exceptions
- Remove some unnecessary logs + fix translations
- Use heartbeat api to check if service is reachable

### 💼 Other

- Integration player2 -> main



### 🚜 Refactor

- Refactor publish and build workflow
- Use consistent package structure
- Use uuid as id for every config instead of npcName
- CheckServiceIsAvail, remove url arg
- Llmtype specific icons in npc label (gui)
- IsReachable method in player2Client
- Error handling and logging
- Cleanup npc spawning
- Use string template to add more context to messages in conversationHistory
- Mr review adjustments

### 📚 Documentation

- Update README.md
- Add some java docs
- Update readme to player2 integration

### 🎨 Styling

- Remove spotless

### ⚙️ Miscellaneous Tasks

- Remove spotless from pipe
- Bump version

Download the new release on [Modrinth](https://modrinth.com/mod/secondbrain/versions).
