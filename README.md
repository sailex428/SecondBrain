<h1 align="center" style="font-weight: normal;"><b>SecondBrain</b></h1>
<p align="center"><img src="logo.png" alt="mod-logo"></p>

A Fabric mod that brings intelligent NPCs to your minecraft world. Create player-like characters controlled by LLMs that respond to your chat messages and perform player actions.

> **Note**: This project is under active development. Future updates will expand the NPCs capabilities.

## Requirements
      
- **Minecraft Version**: 1.20.1/1.21.1/1.21.8
- **Dependencies**: [fapi](https://github.com/FabricMC/fabric), [owo-lib](https://modrinth.com/mod/owo-lib) on client and serverside
- **Running Ollama server OR Player2 App**

## Mod Installation

1. **Download the Mod**:
   - Get the latest version of the `SecondBrain` mod from the [Modrinth](https://modrinth.com/mod/secondbrain) page.
2. **Install the Mod**:
   - Place `secondbrain.jar` in your `mods` folder on your minecraft fabric server and client.
3. **Launch Minecraft**:
   - Start the Minecraft client and the server, and you're ready!

## Usage with Ollama

>Note: Player must be an operator to execute the following commands

### GUI (New)
- Open the gui with the `/secondbrain` command and create/spawn, despawn/delete or edit the NPCs there.
- Also edit the base configuration there.

### Commands (Deprecated)
>Warning: This command uses default ollama/openai settings!
1. **Spawn NPCs**:
   - Use the `/secondbrain add <npcname> <openai|ollama>` (currently only ollama is supported) command to create an NPC. (Example: `/secondbrain add sailex428 OLLAMA)
2. **Remove NPCs**:
   - Use the `/secondbrain remove <npcname>` command to remove an NPC from the game world.

**Interact with NPCs**:
- Just write in the chat to interact with the NPC.
- 
## Setup Ollama  (Local LLM)
1. **Download Ollama**:
   - Visit [Ollamas Website](https://ollama.com/) and download the installer for your operating system.

2. **Install and Run Ollama**:
   - Follow the setup instructions to install.
   - Start Ollama and ensure it's running in the background.

3. **Connect to the Mod**:
   - Use the gui to create/edit an NPC and put in the address to your ollama server (complete url with http://...; 
     if you're running your ollama on your local pc then you can just use the one that's already typed in)

## Usage with Player2 App

>The mod will automatically sync the selected Characters from the player2 App to NPCs in your singleplayer world.
If you updated a description/name in the player2 App you can sync these changes with the command `/secondbrain player2 SYNC`
It's not possible to change any configs from the Character with this mod.

**Interact with NPCs**
- Write messages in the chat or directly to a specific NPC
- Press and hold alt (win) or opt (macOS) key to talk directly to the LLM/AI
- Activate Text-To-Speech for any NPC in the config gui to hear the LLM/AI output

## Setup Player2
1. **Download Player2 App**
   - Visit [player2's website](https://player2.game) and download the App for your operating system.

2. **Start the App and select Characters**
   - Start the downloaded App and select the Characters you wanna use as NPCs

## License

This project is licensed under the [LGPL-3.0](https://github.com/sailex428/SecondBrain/blob/main/LICENSE.md)
## [Disclaimer](https://github.com/sailex428/SecondBrain/blob/main/DISCLAIMER.md)

## Credits
This project utilizes components from the following projects:
- [fabric-carpet](https://github.com/gnembon/fabric-carpet)
- [secondbrain-engine](https://github.com/sailex428/SecondBrainEngine)
- [player2](https://player2.game)

Thank you to the developers of these projects for their amazing work!
