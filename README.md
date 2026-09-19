# AnonChat Bot

A Telegram chatbot for anonymous one-to-one conversations. Users register through Telegram, select another registered user, create a named chat, and exchange messages without exposing their identity to the other participant.

## Features

- Telegram-based onboarding with `/start`
- Contact-based user registration
- Anonymous one-to-one chat creation
- Named conversations and a list of existing chats
- Text-message forwarding between participants
- Chat exit and deletion flows
- Blocking/ban functionality
- Confirmation steps for destructive actions
- PostgreSQL persistence for profiles and chats
- Duplicate-chat and invalid-operation handling
- Telegram reply keyboards and callback interactions

## User flow

1. A user starts the bot and shares their own contact to create a profile.
2. The user selects a contact and requests a new anonymous chat.
3. The bot verifies that the contact is registered and that no blocked or duplicate relationship exists.
4. The user names the conversation; the chat becomes active.
5. Messages are forwarded to the other participant by the bot without revealing the sender's identity.
6. Participants can leave, delete, or ban the other participant.

## Architecture

```text
src/main/java/com/example/anaonchatbot/
├── domains/       # Telegram profile and anonymous-chat entities
├── exceptions/    # Domain-specific errors
├── jdbc/          # Profile and chat database operations
├── repository/    # Spring Data repositories
└── telegram/      # Update handling, commands, keyboards, and chat state
```

The main update handler is implemented as a Telegram long-polling bot. It routes incoming updates through conversation states such as contact setup, chat selection, active chatting, chat creation, and confirmation. JDBC services handle persistence while Spring Data repositories provide database-backed entity operations.

## Technology stack

- Java 17
- Spring Boot 3.1
- TelegramBots 6.5
- Spring Data JDBC
- Spring Data JPA
- PostgreSQL
- Maven
- Lombok

## Running locally

### Prerequisites

- JDK 17
- PostgreSQL
- A Telegram bot token created through BotFather
- Maven, or use the included Maven wrapper

### Configuration

Configure the following values in a local, untracked configuration file or through environment-specific configuration:

- Telegram bot token
- Bot/database connection settings
- PostgreSQL URL, username, and password

Never commit the Telegram token or database credentials.

### Start the bot

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
mvnw.cmd spring-boot:run
```

### Build and test

```bash
./mvnw test
./mvnw package
```

The bot must be running and connected to a configured PostgreSQL database before Telegram interactions can be tested end to end.

## Available interactions

- `/start` — initialise the user and show the main menu
- `/chat` — begin the chat flow
- `/allchats` — browse existing conversations
- `/deletechat` — delete a conversation
- **New chat** — select a registered contact and assign a name
- **Exit chat** — leave the active conversation
- **Ban** — block the other participant after confirmation

The bot accepts text messages during an active conversation and uses Telegram keyboards for navigation and confirmation.

## Engineering highlights

- Integrated an external messaging API with a persistent Spring Boot backend.
- Implemented a stateful conversation flow for Telegram updates.
- Added validation for self-messaging, missing users, duplicate chats, and blocked users.
- Separated Telegram interaction logic from JDBC and repository layers.
- Included persistence models for Telegram profiles and anonymous-chat relationships.

## Security and privacy considerations

This project is an educational implementation of anonymous messaging. In a production deployment, it should be extended with explicit data-retention rules, rate limiting, abuse reporting, moderation, stronger state storage, structured logging without sensitive content, and careful handling of phone numbers and Telegram identifiers.

## CV summary

> Built a Java/Spring Boot Telegram chatbot for anonymous one-to-one conversations, integrating the Telegram Bot API with PostgreSQL persistence and implementing registration, stateful chat workflows, message forwarding, duplicate detection, deletion, and blocking.
