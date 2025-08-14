# Fabric Example Mod

## Features

- Add custom command aliases in-game with `/addcommand <alias> <target command...>`.
- Aliases are stored in `config/modid_aliases.json` and loaded on server start.

## Usage

```
/addcommand greet say Hello world!
/greet
```
This will run `/say Hello world!` when you type `/greet`.

## Setup

For setup instructions please see the [fabric documentation page](https://docs.fabricmc.net/develop/getting-started/setting-up-a-development-environment) that relates to the IDE that you are using.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
