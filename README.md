![PaperChat Logo](./PaperChatLogo.png)

# PaperChat - AI-Powered Minecraft Plugin

PaperChat is a Minecraft server plugin that integrates AI language models directly into Minecraft. It allows players to send messages to AI assistants and get answers quickly, creating natural conversation experiences powered by Google Gemini and OpenAI models, with the ability to add your own!

Built for Paper/Spigot servers, PaperChat maintains conversation history per player and provides a seamless bridge between Minecraft players and modern AI capabilities.

## Project Philosophy

PaperChat follows the "run it yourself" philosophy. By requiring your own AI API credentials and local deployment, you get:

- No Rate Limits - Your API keys, your usage quotas
- Complete Privacy - Player conversations never leave your infrastructure  
- Full Control - Customize AI behavior, system prompts, and response filtering
- No Dependencies - No external services beyond the AI providers themselves

## Features

- **Multi-Provider AI Support**: Automatic registration of Google Gemini and OpenAI providers with easy extensibility
- **Persistent Conversation History**: Per-player chat sessions with configurable history limits
- **Smart Error Handling**: User-friendly error messages with automatic response recovery
- **Configurable System Prompts**: Define AI behavior and response formatting rules
- **Input Validation**: Character limits
- **Asynchronous Processing**: Non-blocking AI requests to maintain server performance
- **Docker Ready**: Complete containerized deployment with Minecraft server bundled in

## Quick Start

### Docker Deployment (Recommended)

1. **Clone the repository**:
```bash
git clone https://github.com/yourusername/paperchat.git
cd paperchat
```

2. **Configure environment variables**:
```bash
# Create environment file
cp .env.example .env
```
**NOTE**: Edit the `.env` file to set your desired settings.

3. **Start with Docker Compose**:
```bash
# Run the cloud container
docker-compose up --build

# Or build yours and then run
docker-compose up --build
```

### Local Build & Installation

#### Prerequisites
- Java 21 or higher
- Gradle 9.0 or higher
- Paper/Spigot server 1.21.1+

#### Building from Source

1. **Configure and build the plugin**:
```bash
gradle wrapper --gradle-version 9.0
./gradlew clean build
```

2. **Copy to server**:
```bash
cp build/libs/paperchat-*.jar /path/to/your/minecraft/server/plugins/
```

3. **Configure environment variables**:
```bash
export PAPERCHAT_API_KEY="your-api-key-here"
export PAPERCHAT_PROVIDER="google"
export PAPERCHAT_MODEL="gemini-2.5-flash"
```

4. **Start your Minecraft server**

## Available Providers

- [**Google's**](https://cloud.google.com/ai) Models
- [**OpenAI**](https://openai.com/) Models
- [**Hack Club's**](https://ai.hackclub.com/) Models

## Configuration

PaperChat is configured through either environment variables:

| Variable | Description | Value |
|----------|-------------|-------|
| `PAPERCHAT_API_KEY` | Your AI provider API key | `your-api-key` (required only by your provider. If an AI provider does not require an API key, you can leave this blank) |
| `PAPERCHAT_MAX_HISTORY` | Max number of past requests to be saved per player | Conversation history limit |
| `PAPERCHAT_PROVIDER` | One of the available AI providers | AI provider (see more at [**Available Providers**](#available-providers)) |
| `PAPERCHAT_MODEL` | Any LLM that a provider provides | Model to use |
| `PAPERCHAT_TEMPERATURE` | Response creativity (0.0-1.0) | 0.7 |
| `PAPERCHAT_TIMEOUT` | Request timeout in seconds | 30 |
| `PAPERCHAT_MAX_INPUT_CHARACTERS` | Maximum input message length | 100 |
| `PAPERCHAT_MAX_OUTPUT_TOKENS` | Maximum response length | 4096 |
| `PAPERCHAT_SYSTEM_PROMPT` | Custom system prompt for AI behavior | Your custom prompt (leaving empty uses default system prompt) |

Or `config.yml` file located in the `data/plugins/PaperChat/` directory:

```yaml
ai:
  api-key: ""  # You can leave it empty if your provider does NOT require an API key
  provider: "google"
  model: "gemini-2.5-flash"
  temperature: 0.7
  timeout: 30
  max-output-tokens: 4096
  system-prompt: ""  # Include your custom system prompt here

# NOTE: setting a system prompt here will overwrite the current prompt
# If left empty, the default system prompt will be used

# Chat Configuration
chat:
  max-history: 5
  max-input-characters: 100
```

Configuration in `config.yml` matches the environment variables. If both are set, `config.yml` file takes precedence.

### System Prompt Configuration

The default system prompt sets the context to Minecraft with strict JSON formatting. You can customize it:

```bash
export PAPERCHAT_SYSTEM_PROMPT="Your custom AI behavior instructions here"
```

## Usage

### Basic Commands

Send a message to another player through AI:
```
/paperchat <target_player> <your_message>
/paperchat Steve "How do I build a redstone clock?"
/paperchat Alex What's the best way to mine diamonds?
```

### Permissions

- `paperchat.use` - Allows players to use the /paperchat command

### Response Flow

1. Player sends command with target and message
2. AI processes the message with conversation history context
3. Response is formatted and sent to the target player
4. Both the original message and AI response are stored in conversation history

## Architecture

PaperChat follows clean architecture principles:

```
src/main/java/lt/domax/paperchat/
├── domain/
│   ├── ai/            # AI provider abstractions and registry
│   ├── chat/          # Chat services and session management
│   ├── config/        # Configuration management
│   └── player/        # Player data management
├── infrastructure/
│   └── commands/      # Minecraft command implementations
├── resources/
│   ├── plugin.yml     # Plugin metadata
│   └── META-INF       # Java metadata
│       └── services/  # Service provider configurations
└── PaperChat.java     # Main plugin class
```

### AI Provider System

- **Abstract Provider Class**: Base for all AI implementations
- **Service Provider Interface**: Automatic registration using Java SPI
- **Registry Pattern**: Centralized provider management
- **Graceful Fallback**: Automatic error recovery and user-friendly messages

## Development

### Adding New AI Providers

1. **Create provider class**:
```java
@AIProvider("yourprovider")
public class YourProvider extends Provider {
    // Implementation
}
```

2. **Add to service registry**:
Add your provider class name to:
```
src/main/resources/META-INF/services/lt.domax.paperchat.domain.ai.Provider
```

## API Credentials Setup

### Google Gemini

1. Visit [Google AI Studio](https://ai.google.dev/)
2. Create a new API key
3. Set `PAPERCHAT_API_KEY` to your key
4. Set `PAPERCHAT_PROVIDER` to `google`
5. Set `PAPERCHAT_MODEL` to your desired Gemini model (e.g., `gemini-2.5-flash`)

### OpenAI

1. Visit [OpenAI API](https://platform.openai.com/api-keys)
2. Create a new API key
3. Set `PAPERCHAT_API_KEY` to your key
4. Set `PAPERCHAT_PROVIDER` to `openai`
5. Set `PAPERCHAT_MODEL` to your desired OpenAI model (e.g., `gpt-5`)

### Hack Club's OpenAI

1. Visit [Hack Club's AI](https://ai.hackclub.com/)
2. Set `PAPERCHAT_API_KEY` to be empty
3. Set `PAPERCHAT_PROVIDER` to `hackclub/openai`
4. Set `PAPERCHAT_MODEL` to your desired Hack Club model (e.g., `openai/gpt-oss-20b`)

## Troubleshooting

### Common Issues

**Plugin fails to load or compile**:
- Check Java version (requires 21+)
- Verify API key is set correctly
- Check server logs for detailed error messages

**AI responses not working**:
- Verify API key has sufficient credits/quota
- Check network connectivity from server
- Review timeout settings

**Performance issues**:
- Reduce `PAPERCHAT_MAX_HISTORY` value
- Increase `PAPERCHAT_TIMEOUT` for slow networks
- Monitor API usage and rate limits

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes following the existing architecture
4. Submit a pull request

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.

## Security

- API keys are never logged or exposed
- All AI requests use HTTPS
- Player data is stored locally only
- No external data transmission beyond AI providers

## Support

For issues, feature requests, or questions:
- Open an issue on GitHub
- Review configuration options

---

Built with modern Java practices for reliable Minecraft server integration. _Happy chatting!_
