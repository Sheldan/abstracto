Deletes the last n messages in the channel. The messages are allowed to be at most 2 weeks old.
If a member is provided as a parameter, only messages by this member are deleted, but at most n messages are considered.
For example, if you execute the command with 200 messages and specify member User#1234, and User#1234 does not have any messages
in the last 200 message, no message will be deleted.
While the command is going on a status message indicating how many messages are currently being deleted is shown.
If messages older than two weeks are found, the command will stop and post an error message.