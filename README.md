<h1 align="center" style="font-weight: normal;"><b>SecondBrain</b></h1>
<p align="center"><img src="https://raw.githubusercontent.com/sailex428/SecondBrain/refs/heads/main/logo.png" alt="mod-logo"></p>

A Fabric mod that brings intelligent NPCs to your minecraft world. Create player-like characters controlled by LLMs that respond to your chat messages and perform basic player actions.

**This mod works fully serverside. It must also be installed on a client for setup.**

> **State of Development**: (19.10.25) Fixing issues executing NPC tasks, Updating it to 1.21.1 and the latest mc version

## Requirements
      
- **Minecraft Version**: 1.20.1 (the latest version of this mod only supports 1.20.1 currently)
- **Running Ollama server OR Player2 App OR an OpenAi Key**

## Mod Installation

1. **Download the Mod**:
   - Get the latest version of the `SecondBrain` mod from the [Modrinth](https://modrinth.com/mod/secondbrain) page.
2. **Install the Mod**:
   - Place `secondbrain.jar` in your `mods` folder on your minecraft fabric server and client.
3. **Launch Minecraft**:
   - Start the Minecraft client and the server, and you're ready!

## Usage with Ollama/OpenAi

>Note: Player must be an operator to execute the following commands

### GUI
- Open the gui with the `/secondbrain` command and create/spawn, despawn/delete or edit the NPCs there.
- Also edit the base configuration there.


### ~~Commands (Deprecated)~~
>~~Warning: This command uses default ollama/openai settings!~~

1. **~~Spawn NPCs~~**:
   - ~~Use the `/secondbrain add <npcname> <openai|ollama>` (currently only ollama is supported) command to create an NPC. (Example: `/secondbrain add sailex428 OLLAMA)~~
2. **~~Remove NPCs~~**:
   - ~~Use the `/secondbrain remove <npcname>` command to remove an NPC from the game world.~~

**Interact with NPCs**:
- Just write in the chat to interact with the NPC.

## Setup Ollama
### Ollama (Local LLM)
1. **Download Ollama**:
   - Visit [Ollamas Website](https://ollama.com/) and download the installer for your operating system.

2. **Install and Run Ollama**:
   - Follow the setup instructions to install.
   - Start Ollama and ensure it's running in the background.

3. **Connect to the Mod**:
   - Use the gui to create/edit an NPC and put in the address to your ollama server (complete url with http://...; 
     if you're running your ollama on your local pc then you can just use the one that's already typed in)

## Setup OpenAi (Paid)
1. **Create API Key**
   - Visit [OpenAi's website](https://platform.openai.com/api-keys), sign up/log in and create a API key
   - Copy the created key
2. **Use the Key in the mod**
   - Open the GUI and create an NPC with the LLM Type `OPENAI` and paste key into the Key field. (The key wont be ever shared with any other clients that has access to the GUI, also not your client. It will be saved in a secondbrain config file on the server)

## Usage with Player2 App

>The mod will automatically sync the selected Characters from the player2 App to NPCs in your singleplayer world.
If you updated a description/name in the player2 App you can sync these changes with the command `/secondbrain player2 SYNC`
It's not possible to change any configs from the Character with this mod.

**Interact with NPCs**
- Write messages in the chat or directly to a specific NPC
- Press and hold alt (win) or opt (macOS) key to talk directly to the LLM/AI
- Activate Text-To-Speech for any NPC in the config gui to hear the LLM/AI output

## Setup Player2 (Free, Limited)
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
- [automatone](https://github.com/Ladysnake/Automatone)
- [player2](https://player2.game)

Thank you to the developers of these projects for their amazing work!
