# Applications Bot

This is a simple Discord bot that you can use for an applications system for your Discord server. It's expected to
work as follows:

1. Add the bot to the server you require applications to join
2. Create a second server with the bot on it, that users can join to learn how to apply
3. Set the `TOKEN`, `GUILD_ID` and `PUBLIC_GUILD_ID` env vars (either directly or in a `.env` file)
4. Start the bot and use the slash commands to set up the channel to post applications to
5. When someone posts an application, click on the buttons to approve or deny it
6. When approved, the user will be sent a single-use invite to join your server
