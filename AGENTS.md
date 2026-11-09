# PhoneBook project instructions

## Purpose and milestones

PhoneBook is an Android app for storing and retrieving phone book entries. Users submit requests and see replies in a conversation screen. Entries are stored on the device in a SQLite database using Room.

1. Support explicit commands for finding, adding, editing, and deleting entries. All core commands must work offline.
2. Add natural-language lookup through OpenAI so a user can ask “What’s Jane Smith’s number?” and retrieve the number saved on the device. Keep the core commands available offline.

## Functional requirements

| ID | Requirement |
| --- | --- |
| FR-01 | Provide a conversation screen with a text field and a **Send** button. Display each submitted request and its reply, and allow users to scroll through earlier messages. |
| FR-02 | Reject blank requests. Show an example when a command is incomplete or invalid. Invalid requests must leave stored entries unchanged. |
| FR-03 | Find entries by first and last name. Show all matches with each entry’s ID and phone number, or report that no entries were found. |
| FR-04 | Add an entry when the user supplies a first name, last name, and phone number. Show the assigned ID after the save succeeds. |
| FR-05 | Edit an entry’s name and phone number by ID. Preserve the values of fields outside the edit and update the entry’s last-updated timestamp. Report when the ID does not exist. |
| FR-06 | Identify the entry selected for deletion and ask for confirmation before deleting it. Keep the entry if the user cancels. |
| FR-07 | Provide in-app Help with command examples and explain how to find an entry’s ID before editing or deleting it. |
| FR-08 | For supported natural-language lookups, use OpenAI to extract the requested name, then search the local database. Ask for clarification when the request does not identify a complete, unambiguous first and last name. If a complete name matches multiple stored entries, show all matches. |
| FR-09 | If natural-language lookup is unavailable, explain the failure and show how to use the local `Find` command. |

## Data requirements

- Each entry must have a unique ID and a timestamp recording when it was added or last updated.
- Store first name, last name, phone number, email, street address, apartment or suite, city, state or province, postal code, and notes. The first milestone exposes name and phone number through the app’s commands and uses the ID to identify entries.
- Store phone numbers and postal codes as text to preserve leading zeros and formatting.
- Allow entries to share a name. Target edits and deletions by entry ID.
- Keep saved entries available after the app is closed and reopened.
- See the [PhoneBook database definition](Documentation/PhoneBook.dds.sql).

## Quality and security requirements

- Keep the conversation screen responsive during database and network operations.
- Preserve the visible conversation when the device rotates. Rotation must never repeat a database write.
- Show a success reply only after the corresponding database operation succeeds.
- Send the user’s natural-language question to the interpretation service. Keep stored records and local query results on the device.
- Store the OpenAI API key on the server. Keep it out of the Android app, source control, and logs.
- Validate the name returned by the interpretation service and use the app’s predefined Room query for the lookup.

## Acceptance tests

| Test | Expected result |
| --- | --- |
| Add Jane Smith with `+1 202-555-0101`, then find her. | The lookup shows the assigned ID and the saved number. |
| Add another Jane Smith with a different number, then search for Jane Smith. | The search shows both entries with distinct IDs. |
| Search for a name with no saved entry. | The app reports that no entries were found. |
| Edit one of the two Jane Smith entries by ID. | Only the selected entry changes. |
| Request deletion and cancel it. Request deletion again and confirm it. | The entry survives cancellation and is removed after confirmation. |
| Close and reopen the app with networking disabled. | Previously saved entries are still available through local commands. |
| Ask “What’s Jane Smith’s number?” with the interpretation service available. | The app displays the saved number for a single match or all matching entries if the name is shared. |
| Repeat the question with the interpretation service unavailable. | The app reports the failure and shows how to use `Find Jane Smith`. That command still works. |
