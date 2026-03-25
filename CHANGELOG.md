# Changelog

All notable changes to this project will be documented in this file.

## [3.1.6] - 2026-03-25

### 🚀 Features

- Add mc 1.21.11 support
- Impl versioned components/containers

### 💼 Other

- Bump loom to 1.15
- Bump gradle
- Bump carpet and owo-lib
- Bump version

## [3.1.5-1.21.8] - 2025-12-06

### 🚀 Features

- Use llama3.2 as default model

### 🐛 Bug Fixes

- Query does not returns results error
- Parsing error using smaller models

### 💼 Other

- Add mcVersion to display name of gh release
- Optimizations for small models

### 📚 Documentation

- Update changelog for version 3.1.4
- Update changelog for version 3.1.5
- Update changelog for version 3.1.5

### ⚙️ Miscellaneous Tasks

- Bump version

## [3.1.4-1.21.8] - 2025-11-23

### 🚀 Features

- Update mineskin resolving to mc 1.21.10
- Add npc name validation

### 🐛 Bug Fixes

- Prevent currentModificationException of playerList when removing npcs
- NoSuchElement exception thrown if npcConfig not present
- Fix couple runtime issues

### 📚 Documentation

- Update changelog for version 3.1.4

### ⚙️ Miscellaneous Tasks

- Bump version

## [3.1.2-1.21.10] - 2025-11-22

### 🚀 Features

- Clean jsonresponse before parsing
- Add translations for keybind category

### 💼 Other

- Bump engine dependencies and add support for mc 1.21.10
- Update to 1.21.10 -> main
- Bump mod version

### 🚜 Refactor

- NpcFactory
- Refactor npcFactory -> main
- Integrate EntityVer for cross-version world access and update npc spawning logic

### 📚 Documentation

- Update changelog for version 3.1.2

### ⚙️ Miscellaneous Tasks

- Remove footer content from cliff.toml

## [3.1.2-1.21.8] - 2025-11-16

### 🚀 Features

- Update error handling -> log only rootcause into chat

### 📚 Documentation

- Update changelog for version 3.1.1
- Update changelog for version 3.1.2

### ⚙️ Miscellaneous Tasks

- Bump version

## [3.1.1-1.21.8] - 2025-11-15

### 💼 Other

- Set unique gh tag for each mc version

### 📚 Documentation

- Update changelog for version 3.1.1

## [3.1.1] - 2025-11-15

### 🚀 Features

- Add mineSkinProxyClient
- Apply skins from player2 on npc

### 💼 Other

- Add skin loader for player2 -> main

### 📚 Documentation

- Update changelog for version 3.1.1

### ⚙️ Miscellaneous Tasks

- Bump version
- Use publishMods task for publishing

## [3.0.1] - 2025-10-26

### 🚀 Features

- Update to 1.21.8

### 🐛 Bug Fixes

- Update README with version info and formatting fixes

Removed unnecessary image and fixed minor formatting issues.
- Endec class not found
- Fix attacking doesnt work

### 💼 Other

- Revise README for clarity and updates

Updated README to reflect changes in NPC functionality and dependencies.
- Update Minecraft version support information

Clarified Minecraft version support in README.
- Shorten dc message if its to large
- Add modname to the refmap file
- Add modname property
- Bump engine to 1.21.1
- Add config for publishing to curseforge
- Change carpet repo
- Update to mc 1.21.8 -> main
- Add projectSlug to curseforge publish config

### 📚 Documentation

- Update changelog for version 3.0.0
- Update changelog for version 3.0.1
- Update changelog for version 3.0.1
- Update changelog for version 3.0.1

### ⚙️ Miscellaneous Tasks

- Update workflow
- Update engine to 1.21.8
- Bump engin
- Bump engine version
- Bump version
- Add CURSEFORGE_API_KEY to publish workflow
- Use publishMods task in publish workflow

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
- Update changelog for version 3.0.0

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

## [2.1.2] - 2025-07-05

### 🚀 Features

- Register client/server prodTest gradle task

### 🐛 Bug Fixes

- Fix workflow_run has no referenced wf
- Fix built-jars missing in runTime test job
- Fix gradle wrapper not found

### 💼 Other

- Add missing httpcomponents dep

### 🚜 Refactor

- Refactor runTimeTest workflow

### 📚 Documentation

- Adjust heights of headings
- Update changelog for version 2.1.2

### 🧪 Testing

- Add prod runTime test
- Add gradle setup to runTime test
- Comment out the prod runtime test

### ⚙️ Miscellaneous Tasks

- Up fabric loom
- Up gradle wrapper
- Put all deps in the jar
- Run runTimeTest after every completed build
- Make gradlew executable
- Add java setup
- Add right apache lib
- Bump version

## [2.1.1] - 2025-05-30

### 🐛 Bug Fixes

- Optimize responsiveness
- Only provide top six relevant functions
- Adjust dropItem function description and player2 systemprompt

### 💼 Other

- Optimize responsiveness -> main
- Optimize systemprompt -> main

### 📚 Documentation

- Update changelog for version 2.1.0
- Update changelog for version 2.1.1

### ⚙️ Miscellaneous Tasks

- Bump version

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
- Update changelog for version 2.1.0

### 🎨 Styling

- Remove spotless

### ⚙️ Miscellaneous Tasks

- Remove spotless from pipe
- Bump version

## [2.0.4] - 2025-05-03

### 📚 Documentation

- Update CHANGELOG.md + bump version

## [2.0.3] - 2025-05-02

### 🐛 Bug Fixes

- Runtime issues of automatone in singleplayer
- Fix run arg is missing

### 📚 Documentation

- Add footer git cliff config
- Update readme

### ⚙️ Miscellaneous Tasks

- Add publish workflow
- Bump dawidd6/action-download-artifact from 2 to 6 in /.github/workflows

## [2.0.2] - 2025-04-24

### 🚀 Features

- Add commitlinting

### 🐛 Bug Fixes

- Fix wrong gap size between npc elements in gui
- Fix compile issues on client
- Npc-playername doesnt match name typed in
- Put chat messages from llm also in goal queue
- Npc configs doesnt get deleted
- Despawn/spawn button not updating
- Remove init delay of chunk scanning
- Wrong inventory field mapping
- Fix doubled entity names in prompt

by overriding hashcode
- Fix handle llm errors on the right layer
- Fix print error log if server instance is null
- Only kill llmClient if its not used in resourceProvider
- Npc config screen issues
- Refactor main screen

- show edit button only when npc isnt active
- set fixed width of npcContainer
- Adjust dropItem call to new automatone interface
- Replace spaces with underscore in blocktypes
- Logging issue, too many recipes passed to llm
- Shutdown all npc services on server stop
- Merge pull request #23 from sailex428/v2/dev

🐛  v2 final fixes -> main
- Publish gradle task
- Merge pull request #24 from sailex428/v2/dev

fix: publish gradle task
- Runtime issues in prod env
- Merge pull request #25 from sailex428/v2/dev

fix: runtime issues in prod env

### 💼 Other

- Merge pull request #22 from sailex428/v2/add-npc-config-screen

v2.1 -> main
- Optimized npc adding/deleting

- add max spawn limit of parallel running npcs
- removed unnecessary isNewConfig property from addPacket
- catch some unhandled npcCreation exceptions
- Prevent memory leak by shutdown threadpool

- also close sqliteClient db connection on server close
- Clean up build.gradle
- Save only characteristics of llm in config

- remove saving system-prompt template
- rename field to llmCharacteristics
- Remove possibility to edit unique npcname

- add npcScreen title
- Some optimisations

- remove unused eventListener
- rename jar filename
- let user edit npcname only on creation
- Remove static server instance in main class
- Prevent cast/parse exception

### 🚜 Refactor

- Refactor npcEntity spawning logic

- throw exception if spawning takes longer than three seconds
- use threadpool of one thread to run npc creation
- handle exceptions from npc creation thread in main thread
- Refactor logUtil
- Refactor general error logging
- Remove autorespawn of npcs

- remove npc when it dies (so it needs to be respawned manually)
- refactor npcfactory to singleton so it can be used in mixin
- prevent null pointer in ollamaClient when toolcalls are null
- remove unused info sent to llm at entity load event
- Remove old run configs

### 📚 Documentation

- Update changelog and readme
- Update changelog

### ⚡ Performance

- Run chunkScanning in separate thread

### ⚙️ Miscellaneous Tasks

- Clean up build.gradle and mod.json
- Add gitcliff config for generating changelogs from commits
- Bump version

### 🛡️ Security

- Hide api_key when sending npcConfigs to the clients

## [2.0.1] - 2025-04-06

### 🚀 Features

- Implement openai function calling
- Feat: impl efficient resource saving to db on server stop
next -> maybe check if recipe is in db if not only rebuild it
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
- make factory compatible with new config sys
- add goalThread that executes new goals in the queue
- Add networkhandling for npcConfigScreen
- Impl all config screens

### 🐛 Bug Fixes

- Fix pipe issues
- Try to fix pipe errors take #2
- Fix workflow
- Fix pipe issues
- Finally fix stupid build issues
- Fix some stupid runtime errors caused wrong usage of automatone lol
- Refactor contextGen to util class; adjust logger prefix to SecondBrain
- Fix some small runtime errors
- Inner class not usable for function calling; close open threads on server stop
- Fix player not found on spawn command
- Remove adding latest convos to system prompt
- fix npc message doesnt have a name in it
- fix stupid recursive function calling
- Fix classCast error
- Stupid inefficient columns types
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

- Merge branch 'main' into v2/dev
- Merge pull request #19 from sailex428/v2/dev

first-great-changes-for-v2 -> main
- Prevent nullpointer that can be thrown
- Merge branch 'main' into v2/use-function-calling
- Remove non-existing tasks from workflow
- Add actions to queue on function call
- Merge pull request #18 from sailex428/v2/use-function-calling

impl-function-calling -> main
- Optimized recipe and convos saving and using
- Add getRelevant recipes logic + function tool (openai)
- Add resource recipes/conversation saving on server stop
- Add actions to queue on function call
- Add getConversation function tool
- Merge pull request #20 from sailex428/v2/rework-conversation-history

rework-conversation-history
- First runnable v2
- Merge remote-tracking branch 'origin/v2/dev' into v2/dev
- Merge remote-tracking branch 'origin/v2/dev' into v2/dev
- Add conversation history
- Add basic ConversationHistory impl
- Finish convo history impl for openai
- Add autorespawn on npc death
- Also stop llmClients on shutdown
- Some smaller tweaks
- Add model suggestion depending on user input
- Adjust function description to animate the llm to chat with the players
- Wip
- Wip vectorizeFunctions
- Try to optimize function calling, by giving llm only most relevant functions
- Optimize tool calling
- Merge pull request #21 from sailex428/v2/dev

v2 -> main
- Add spotless check to pipe
- Up automatone version, add owo
- Renaming of packages to secondbrain
- Removed chat tool_function
- adjust promptFormatter
- add promptTemplate and default sys prompt
- Add isActive to npc config

to track whether npc is in the world or not
- Update serverbound packets

so that are also npc de/spawns are possible
- Add basic ConfigScreen
- Add packets for updating base and single npc config
- Add receivers for new update packets on server
- Add uuid to npcconfig
- Add full packet functionality to screens

(now only styling is missing)
- Add layout for screens
- Close screen after spawn/despawn npcs
- Adjust styling of npcs in main screen
- Remove unnecessary error log
- Bump version

### 🚜 Refactor

- Refactor NPCFactory, add start ticking of controller in constructor
- Refactor config logic
- Context generating
- Move despawn logic into spawner class

## [1.0.10] - 2025-01-15

### 🐛 Bug Fixes

- Fix some stupid runtime errors caused wrong usage of automatone lol
- Refactor contextGen to util class; adjust logger prefix to SecondBrain

### 💼 Other

- Minor README.md Subheading Fix

Exactly the title, this change also needs to be made on the modrinth page.
- Merge pull request #16 from virtualspan/patch-1

Minor README.md Subheading Fix

## [2.0.0] - 2025-01-18

### 🐛 Bug Fixes

- Could not open login browser popup window on win11 and linux systems
- Merge pull request #14 from sailex428/bug/fix-cannot-open-login-window

fix: could not open login browser popup window

### 💼 Other

- First runnable v2

## [1.0.9] - 2024-12-28

### 🚀 Features

- Feat: add attack action to npc capabilities
- add example skills
- add attack logic to npcController
- Merge pull request #12 from sailex428/dev

feat: add attack capability -> main

### 🐛 Bug Fixes

- Fix security issues
- remove login command
- add device login for local and credentials login (via configfile) for dedicated servers
- add separate auth config file
- Merge remote-tracking branch 'origin/bug/fix-security-issues' into dev
- Fix empty relevant blocks section
- use own block context to find matching blocks instead of baritones
- Unnecessary reindexing on every npc client start; also some renaming
- Only react on damage on npc + catch format exception

### 💼 Other

- Merge branch 'main' into dev
- Merge pull request #11 from sailex428/dev

1.0.8 -> main
- Add/adjust player attack events
- add playerDamageCallback and listener
- use client callbacks instead of server ones (in all listeners)
- Prevent null pointer
- Update changelog
- Update README.md
- Update README.md

add credits

## [1.0.8] - 2024-12-15

### 💼 Other

- Add unique tagName

## [1.0.7-1.21.3] - 2024-12-15

### 🐛 Bug Fixes

- Hotfix load mod also client side
- Fix double mod version in jar filename

## [1.0.6-1.21.3-launcher] - 2024-12-11

### 🐛 Bug Fixes

- Fix some stupidity
- Fix wrong mc dir bug; remove narrator sounds on npc client start
- Remove version var to create jars without suffix
- Fix double mod version in jar filename
- Major changes for better npc interaction
- cleanup sqliteClient
- add repository classes for every data type that cam select and insert
- add empty json templates (will be filled in the future)
- add data types for every db table
- rename package to ai.npc.client/launcher
- refactor embedding comparing
- add recipeIndexer that add all default recipes to the db
- fix some code quality issues (sonarQube)
- Fix loom bug caused by multiple/missing plugins
- Fix pipe errors
- Fix wrong versioned comment
- Finally fix loom build error
- Fix runtime errors
- include kotlin lang lib into jar
- add openai embedding model
- Fix errors communicating with sqldb
- use prepared statements for byte values
- fix templates resources loading
- bump version
- Fix errors at requirements indexing
- catch exception occurred in response gen (openai)
- index in parallel thread
- adjust sql statement to update record if name is already taken
- Add login command to fix capability issues on dedicated servers (credentials are now needed)
- refactor LogUtil
- Fix example actions indexing
- remove requirements from actions (also in db)
- adjust actions-examples json
- Fix select doesnt return data
- Fix config property missing bug, add launcher param to change openAi base url
- Fix wrong version commit
- Fix launcher is null on login command call
- Fix indexing of default resources
- Fix wrong version commit
- Statement is closing to early (leads to closing resultSets)
- Catch and log unexpected thrown exceptions while handling event, fix null recipe values in db
- Fix following bugs:
- errors converting bytes to doubles
- unlogged thrown exceptions in handleEvent
- weird formatted json string from resource
- null and empty neededItems in recipes
- Add name field to skill data type
- fix exploring instant cancel after start

### 💼 Other

- Add exception logging for client init/launching
- init mod just after the server has started
- Add publishing of both mods
- Finally get publishing mods work for gh and modrinth
- Remove ollama log
- Moved doubled code of llmClients into new super class + add embedding gen impl
- Adjust multithreading of response gen
- Add resolutionStrategy for conflicting deps
- Add item dropping to npc capabilities
- Execute chat and cancel actions direct
- Add more sensible archiveBaseName for both mods
- Add missing mixin config
- Final changes v1.0.6
- add auto respawn
- give entity data to the llm
- rename cancel task to stop
- add ability to drop items
- set look dir of npc to the player head if no action is queued
- Add unique tagName
- Remove include of fapi in launcher jar
- Add kotlin plugin to client
- Bump fabric_loader version
- Add better event listener management
- Rename .java to .kt
- Adjust llm clients to superclass changes
- Up gradle version
- Adjust instructions
- remove example json
- Add version specific recipe Identifier creation
- Add templates as json, template resource loading and indexing
- Remove accidentally added main, up stonecutter version
- Add default run configs
- Remove sql syntax error
- Cancel exploring action if another action was added to the queue
- Remove id from template and requirement, up kotlin version
- Add indexing or requirements in multiple threads; interrupt thread on exception
- Rename isOffline param in npc command to isOnline; adjust property logging
- Add action indexing for all actions the llm takes
- rename db action to actionResource
- add own ollama not reachable exception
- Put all both mods and all versions in one gh release
- set the tokens in gradle.properties
- read changelog from md file
- Adjust displayname of modrinth and gh pubblish
- Remove NPCEvent data class, optimize embedding creation on events
- Some adjustments logutils
- Add new action templates/examples
- Removed templates repository
- some events message improvements
- Make project buildable lol
- Rename .java to .kt
- Renamed requirements to recipes; init baritone in main class
- Add blockRepo and indexing of all
- Add relevant blockData to llm prompt
- add miningLevel and toolNeeded to Block db type
- adjust context generation to put also miningLevel and toolNeeded to the context
- Add spotless formatter for java and kotlin files + format all files
- Add discord announcement on publishing
- Add idea project icon
- Update workflow trigger
- Removed unnecessary description in action db type
- Add own ollama4j dep, add json schema to ollama response
- Remove not yet impl actionTypes; add missing jackson lib; remove block processing in context of type air
- I dont know what to write....
- Optimize context string
- remove structure instruction for ollama
- set max number of blocks in world context
- Add custom-lan to use the mod in singleplayer
- Add formatting instructions
- Add some specific example skills
- Merge pull request #9 from sailex428/dev

add mod publishing via gradle task -> main
- Merge pull request #10 from sailex428/dev

### 🚜 Refactor

- Refactor sqlclient, move action specific logik in own class

### 🧪 Testing

- Add deps for testing
- Add some testing for ollamaClient
- Adjustments for test purposes

### ◀️ Revert

- Revert changes

## [1.0.6] - 2024-12-01

### 🐛 Bug Fixes

- Fix classNotDef exception
- Fix version property
- Try to fix build issues
- Finally fixed stonecutter setup
- Try to fix pipe
- Add jar deps to fix build

### 💼 Other

- Up version
- Project restructuring
- Rebase main to restore history
- Project restructuring
- moved launcher into ai-npc repo
- Make project buildable
- multi version still missing
- Adjust task group
- Impl stonecutter for both subprojects
- Remove unnecessary loom config
- Added loom config
- set ide config gen
- set general run dir
- Update to mc version 1.21.3
- Add mc version 1.21/1.21.1
- Merge pull request #8 from sailex428/sync-main-project-restructuring

move launcher into this repo + up to mc 1.21.1 and 1.21.3 -> main

## [1.0.5] - 2024-11-16

### 💼 Other

- Merge branch 'main' into dev
- Merge pull request #7 from sailex428/dev

add better log feedback -> main

## [1.1.0] - 2024-11-16

### 💼 Other

- Up version
- Merge pull request #6 from sailex428/dev

implement ollama client -> main
- Merge remote-tracking branch 'origin/main'

## [1.0.3] - 2024-11-14

### 🚜 Refactor

- Merge pull request #5 from sailex428/dev

refactor config handling + context gen

## [1.0.2] - 2024-11-09

### 🐛 Bug Fixes

- Merge pull request #4 from sailex428/dev

fix chatmessage are not displayed bug -> main

## [1.0.1] - 2024-11-08

### 💼 Other

- Merge pull request #3 from sailex428/dev

add deps to run clients in prod -> main

### 📚 Documentation

- Add some documentation to readme

## [1.0.0] - 2024-11-06

### 🚀 Features

- Implement dynamic configuration update via setconfig command

### 🐛 Bug Fixes

- Add basic impl of OllamaService + fix formatting
- - optimize config handling of OllamaService
- added feedback on command execution
- fix multiple npc with same uuid
- - changed text color of command feedback
- fix multiple players + npcs with same uuids
- Refactor logging and NPC removal functionality

- Moved logging prefix to FeedbackLogger
- Modified NPCManager to use FeedbackLogger.
- Ensured NPC is disconnected from the server when removed.
- Try to fix gradle genSources task executing errors
- Try to fix build issues
- moved ResponseSchema to constants package
- Fix instruction issues
- split default and structure instructions
- add ChatUtils to format messages of the npcs
- Add multithreading to handleMessage and optimize block/entity scanning

- Implemented `ExecutorService` with a fixed thread pool of 3 threads for `handleMessage` method.
- Simplified `getContext` method by removing unnecessary `CompletableFuture`.
- Updated `scanNearbyBlocks` to include only the nearest accessible block of each type.
- Modified `scanNearbyEntities` to include the nearest entity of each mob type and ensure players are always included.
- Try to fix build issues
- Add baritone jar to fix build issues
- Fix logging and missing property bug
- Fix build issues
- Fix chatmessage are not displayed bug + up version

### 💼 Other

- Initial commit
- Format gradle files from groovy to kotlin
- Rename project to ai-npc
- Added github workflow
- Rename client and server init class
- Implement npc creation
- created NPC datatype
- added NPCEntity for connecting npcs to the server.
- added CreateNPCCommand to create npcs with given name and at given position
- added ConfigReader for getting values from properties
- added some specific exceptions
- Removed unnecessary method fetchGameProfile
- Add skin fetching to the npcs
- Limit amount of npc to spawn
- add logic to read int values from config
- add max_count value of npcs to config
- add custom feedback if the max_count of npcs is reached
- Merge remote-tracking branch 'origin/dev' into dev
- Add NPCController and InstructionMessage handling

- Implement NPCController to handle instructions and chat messages
- Add InstructionMessage class with instruction field
- Define RequestType enum for message types
- Add default instruction in Instructions class
- Add do command to tell the npc instructions
- put response generation in separate thread
- Removed client entrypoint
- Clean up deps
- Add functionality to communicate (via chat messages) with the llm.
- added ChatMessageEvent and listener
- added listener manager
- created json response schema for a chat message
- adjust multithreading of response generation (limit number of threads cause of runtime exceptions)
- add NPC datatype to save entity controller uuid in one type
- Add shutdown of open threads on server stop
- Downgrade mc version to 1.20.4 cause of baritone api dep
- Added new Action types to use it as schema type in openai requests
- Moved whole mod to client
- deleted response types
- removed serverside commands to create npcs
- Add script to launch headlessmc client
- Register setconfig command
- Clean up build.gradle.kts
- Rename artifact
- Add license + basic readme; removed icon from mod.json
- Update license cause of baritone dep
- Adjustments in deps
- add baritone to jar
- use vars in depends in mod.json
- remove unused minecraft auth dep
- Include deps into jar + add license in mod.json
- Rename base_name + downgrade target java version
- Add missing deps for prod usage
- Up version
- Add better log feedback
- Update gh pipe
- Merge pull request #1 from sailex428/dev

add basic npc handling -> main
- Merge pull request #2 from sailex428/dev

first alpha version -> main

### 🚜 Refactor

- Refactor config handling
- remove setconfig command
- remove unused config constants
- remove launch script
- Refactor context generation and config reading/validation
- Implement ollama client
- remove old response schema
- refactor instructions
- use ollama4j lib


