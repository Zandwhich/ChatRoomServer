# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Server-side of a Java chat room application. Listens on a TCP socket for client connections and broadcasts JSON messages between all connected participants. Companion client repo: [ChatRoomClient](https://github.com/Zandwhich/ChatRoomClient).

## Build & Run

This is an IntelliJ IDEA project (JDK 11 / Temurin) with no build tool (no Maven/Gradle). Source root is `src/`.

**Compile from CLI:**
```
javac -d out -sourcepath src src/com/company/Main.java
```

**Run:**
```
java -cp out com.company.Main
```

The server binds to port 1024. No tests exist in the project.

## Architecture

Three classes in `com.company`, plus a vendored JSON library:

- **Main** — entry point; creates a `Controller` and calls `run()`.
- **Controller** — the server core. Opens a `ServerSocket`, accepts client connections in a loop, reads each client's initial handshake (JSON with a `name` field), wraps the connection in a `Participant`, and manages broadcasting. All outgoing messages are JSON built with `json_simple.JSONObject`. Each participant is assigned a random colour on join (a random hue, kept until they disconnect) that is serialized alongside their name; system messages (join/leave) are always grey.
- **Participant** — wraps a client `Socket` with `DataInputStream`/`DataOutputStream` (Java's `writeUTF`/`readUTF` framing). Contains an inner class `InputThread` that continuously reads incoming JSON messages from this client and calls back into `Controller.sendMessage()` to broadcast.

## Wire Protocol

Messages use `DataInputStream.readUTF()` / `DataOutputStream.writeUTF()` (length-prefixed UTF-8). Payloads are JSON:

- **Client → Server handshake:** `{"name": "<username>"}`
- **Client → Server message:** `{"name": "<username>", "message": "<text>"}`
- **Server → Client named message:** `{"name": {"text": "<username>", "color": {"red": <0-255>, "green": <0-255>, "blue": <0-255>}}, "message": {"text": "<text>", "color": {"red": 0, "green": 0, "blue": 0}}}` — the name carries the participant's assigned colour; the message body is black.
- **Server → Client system message:** `{"message": {"text": "<text>", "color": {"red": 128, "green": 128, "blue": 128}}}` — always grey.

JSON keys are defined as constants on `Controller` (`NAME_KEY`, `MESSAGE_KEY`, `TEXT_KEY`, `TIME_KEY`, `COLOR_KEY`, etc.).

## Vendored Dependency

`src/json_simple/` is a vendored copy of [json-simple](https://code.google.com/archive/p/json-simple/). It is not managed by any package manager.