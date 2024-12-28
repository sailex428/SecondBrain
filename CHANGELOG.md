### Changes
- Added attack action to NPC capabilities
- Integrated attack and damage events for NPCs. so NPCs can perform skills based on combat interactions.
- Added cloth configuration and custom lan mod directly to the JAR, so its no longer needed to manually add them to the NPC client. 
- Defined a response JSON schema for communication with Ollama and OpenAI servers to ensure that actions are structured and more actions are actually executed compared to previous versions.
- NPCs can now access recipes, previously made skills, conversations and blocks (names/IDs).
  - Added an SQLite client that creates a local database. On NPC initialization, all recipes, block data, and sample skills are indexed.
  - Embeddings are generated for specific fields to use similarity search (RAG) for querying relevant data from the database based on player requests.
- Renamed isOffline parameter to isOnline in the NPC command to avoid confusion.
#### Security Enhancements:
- API key can no longer be set via setconfig to prevent plaintext transmission over TCP.
- NPC client can now be verified on online dedicated servers using username and password in the config file.
- (For local servers, the device code login method is still used for authentication.)

