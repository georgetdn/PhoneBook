# PhoneBook Software Requirements Specification

## Purpose and scope

PhoneBook is an Android app for storing and retrieving contacts through a conversation screen. It saves entries in a Room database on the Android device. The first milestone covers explicit offline commands for finding, adding, editing, and deleting entries, together with in-app Help. The second milestone adds natural-language phone number lookup while preserving offline command access.

A user can ask “What’s Jane Smith’s number?” when the interpretation service is available. The service uses OpenAI to identify the name. PhoneBook searches its own database and displays the result in the conversation. The Android app and its database run on the physical phone or in the Android Emulator. The interpretation service runs separately.

## Functional requirements

The IDs below match the project requirements. The [implementation baseline](#implementation-baseline) records which requirements the current code supports.

| ID | Requirement |
| --- | --- |
| FR-01 | PhoneBook shall provide a conversation screen with a text field and a **Send** button. It shall display each submitted request and its reply and let the user scroll through earlier messages. |
| FR-02 | PhoneBook shall reject blank requests. For an incomplete or invalid command, it shall show an example of valid usage and leave stored entries unchanged. |
| FR-03 | The offline `Find` command shall search by first and last name. PhoneBook shall display all matches with their entry IDs and phone numbers, or report that it found no entries. |
| FR-04 | The offline `Add` command shall accept a first name, last name, and phone number. PhoneBook shall show the assigned entry ID after the save succeeds. |
| FR-05 | An offline edit shall target an entry by ID and allow changes to its first name, last name, and phone number. PhoneBook shall preserve fields outside the edit, update the last-updated timestamp, and report when the ID does not exist. |
| FR-06 | An offline deletion request shall target an entry by ID. PhoneBook shall identify the selected entry and request confirmation before deleting it. Cancellation shall preserve the entry. |
| FR-07 | PhoneBook shall provide in-app Help with command examples and explain how to use `Find` to obtain an entry ID before editing or deleting. |
| FR-08 | For a supported natural-language phone number question, PhoneBook shall use OpenAI through the interpretation service to extract the requested name and shall search the local database. If the question does not identify a complete, unambiguous first and last name, PhoneBook shall request clarification. If the name matches more than one entry, it shall display all matches. |
| FR-09 | If natural-language lookup is unavailable, PhoneBook shall explain the failure and show an example of the offline `Find` command. |

### Command examples

| Request | Expected behavior |
| --- | --- |
| `Find Jane Smith` | Search the local database and list each matching entry with its ID and phone number. |
| `Add Jane Smith +1 202-555-0101` | Save a new entry and reply with its assigned ID. |
| `What's Jane Smith's number?` | Ask the interpretation service for the name, then search the local database. |
| `What's Jane's number?` | Ask the user to provide a complete name when interpretation is available. |

Edit and delete command syntax and the wording of the deletion confirmation are open design decisions. FR-05 and FR-06 specify the required behavior.

## Data requirements

| ID | Requirement |
| --- | --- |
| DR-01 | Each entry shall have a unique ID and a timestamp recording when it was added or last updated. |
| DR-02 | The database shall store first name, last name, phone number, email, street address, apartment or suite, city, state or province, postal code, and notes. The first milestone shall expose name and phone number through the commands and use the ID to identify entries. |
| DR-03 | The database shall store phone numbers and postal codes as text so leading zeros and formatting are preserved. |
| DR-04 | Entries may share the same first and last name. Edits and deletions shall use the entry ID to distinguish them. |
| DR-05 | Saved entries shall remain available after the app is closed and reopened. |

The Room database is `phonebook.db`, and its contact table is `phone_book_entries`. The primary key is `entry_id`. The `update_utc` column records time in milliseconds since the Unix epoch. The database definition lists the columns and the update trigger. [SQLite database definition](PhoneBook.dds.sql)

## Quality and security requirements

| ID | Requirement |
| --- | --- |
| QR-01 | The conversation screen shall remain responsive during database and network operations. |
| QR-02 | PhoneBook shall preserve the visible conversation when the device rotates. Rotation shall never repeat a database write. |
| QR-03 | PhoneBook shall report success for a database operation only after it succeeds. |
| SR-01 | The interpretation service shall receive the user's natural-language question. The app shall keep saved records and local query results on the Android device during lookup. |
| SR-02 | The OpenAI API key shall reside on the server. It shall be excluded from the Android app, source control, and logs. |
| SR-03 | PhoneBook shall validate the name returned by the interpretation service and use a predefined, parameterized Room query for the lookup. |

## Interfaces and operating environment

The Android app uses a Kotlin and XML conversation screen and requires API level 24 or higher in the current build configuration. Offline commands operate on Room and SQLite. Online lookup requires a configured interpretation endpoint and a server with a working OpenAI API key and access to the configured model.

The app sends a JSON `POST /interpret` containing one `question` field. The Node.js service returns HTTP 200 with `status: "lookup"`, `firstName`, and `lastName`, or with `status: "clarify"`. If OpenAI interpretation fails, the service returns HTTP 502 and the app displays the offline fallback. The service reads `OPENAI_API_KEY` from its environment and calls the OpenAI Responses API for structured name extraction.

The development service listens on `127.0.0.1:8787` on the development computer. The emulator debug build defaults to `http://10.0.2.2:8787/interpret`. A USB-connected physical phone can use ADB reverse forwarding with a debug build configured for `http://127.0.0.1:8787/interpret`. A release build requires a configured HTTPS endpoint for natural-language lookup. [Server setup and build options](../server/README.md)

## Implementation baseline

| Requirements | Current code |
| --- | --- |
| FR-01, FR-03, FR-04 | The conversation supports scrolling, `Find`, and `Add`. The current name query compares the stored first and last names exactly and orders matches by ID. Each explicit command takes the first and last name as separate words. |
| FR-02 | The input rejects blanks. Invalid `Find` and `Add` requests show command examples and exit before a write. Input outside the recognized commands is handled as a potential natural-language lookup. |
| FR-05, FR-06 | Room exposes update and delete operations, and the database has a timestamp trigger. The conversation currently answers `Edit` and `Delete` with ID lookup guidance. The user-facing edit, deletion, and confirmation flows remain to be implemented. |
| FR-07 | The typed `Help` command shows examples for `Find`, `Add`, and natural-language lookup. The toolbar Help item has no handler, and the typed reply does not yet explain how to use the ID for edits and deletions. |
| FR-08, FR-09 | The app and Node.js endpoint implement interpretation, clarification, local lookup, and the offline fallback. Live lookup depends on a reachable endpoint and valid OpenAI credentials. |
| DR-01 through DR-05 | The persistent Room schema supplies the ID, timestamp, and all contact fields. Names can repeat, and phone numbers and postal codes use text columns. |
| QR-01 through QR-03 | The app runs requests in a ViewModel coroutine and disables **Send** while a request is in progress. The ViewModel retains the conversation across rotation, and `Add` reports success after Room returns the assigned ID. |
| SR-01 through SR-03 | The interpretation request contains the question alone. The server reads the key from its environment, and the app validates returned names before querying Room. Android backup is enabled in the manifest, and the supplied backup rules contain no explicit database exclusion. |

## Acceptance tests

Use fictional contacts in a test database. Run the offline scenarios with networking disabled where specified. For live natural-language scenarios, configure a reachable server and valid OpenAI credentials. Each expected result is a test criterion.

| ID | Requirements | Scenario and expected result |
| --- | --- | --- |
| AT-01 | FR-01 | Submit enough requests to fill the screen. Each request and reply appears in order, and the user can scroll back to earlier messages. |
| AT-02 | FR-02 | Submit a blank request and incomplete commands such as `Find Jane` and `Add Jane Smith`. PhoneBook rejects the blank request and shows usage examples for the incomplete commands. The stored entries remain unchanged. |
| AT-03 | FR-03, FR-04, DR-01, DR-03, QR-03 | Add Jane Smith with `+1 202-555-0101`, then find her. The successful save shows an ID, and the lookup shows that ID and the number with its original formatting. Verify that the saved row has a timestamp. |
| AT-04 | FR-03, DR-04 | Add another Jane Smith with a different number and find Jane Smith. Both entries appear with distinct IDs. |
| AT-05 | FR-03 | Find a name absent from the database. PhoneBook reports that no entries were found. |
| AT-06 | FR-05, DR-01, DR-04, QR-03 | Edit one of two entries with the same name by ID. Only the selected entry changes, its unrelated fields remain intact, and its timestamp advances. An edit for a missing ID reports that the entry does not exist. |
| AT-07 | FR-06, DR-04, QR-03 | Request deletion by ID and cancel. The identified entry remains. Repeat and confirm. Only that entry is removed, and PhoneBook reports success after removal. |
| AT-08 | FR-07 | Open Help. The user can find examples of the offline commands and follow the instructions to obtain an ID before editing or deleting. |
| AT-09 | FR-03 through FR-06, DR-05 | Disable networking, close and reopen the app, then find a saved contact. The entry remains available. Add, find, edit, and confirm deletion of a test entry while offline. Each operation completes locally. |
| AT-10 | FR-08, SR-03 | Ask “What’s Jane Smith’s number?” with the service available. A single saved match returns its number. Duplicate matches return every matching ID and number. A name absent from the database produces a no-match reply. An incomplete or ambiguous name produces a clarification request. |
| AT-11 | FR-09 | Stop or disconnect the service and repeat the question. PhoneBook reports that natural-language lookup is unavailable and gives a `Find` example. The local command still returns the saved entry. |
| AT-12 | QR-01, QR-02 | Scroll the conversation while a request is pending. Rotate during an `Add`, then find the contact. Earlier messages remain visible, the request produces one reply, and exactly one row was added. |
| AT-13 | SR-01 through SR-03 | Inspect a test lookup request and the app and server configuration. The request contains only the question, the key is available only to the server and does not appear in source, the app, or logs, and a malformed returned name does not trigger a database lookup. |
| AT-14 | DR-02, DR-03 | Use a database test fixture to save an entry with all contact fields filled, including a phone number and postal code with leading zeros. Read the entry back and verify that every value retains its original text. |

The repository includes a Room instrumentation test for insert, lookup, and no-match behavior, and a Node.js test with mocked OpenAI responses for lookup, clarification, failure, and invalid input. These tests cover components of the acceptance scenarios. The edit, deletion, and full Help scenarios depend on their user-facing flows.

## Project references

- [PhoneBook project requirements](../AGENTS.md)
- [SQLite database definition](PhoneBook.dds.sql)
- [Android conversation logic](../app/src/main/java/com/y219/phonebook/ConversationViewModel.kt)
- [Android interpretation client](../app/src/main/java/com/y219/phonebook/NameInterpreter.kt)
- [Room data access](../app/src/main/java/com/y219/phonebook/PhoneBookDao.kt)
- [Node.js interpretation service](../server/interpret.mjs)
