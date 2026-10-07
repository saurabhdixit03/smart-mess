# Smart Mess Postman

Keep one canonical collection and one environment. Import both JSON files. Select **Smart Mess Local**, start the development backend, and run one regression folder at a time. Never run the entire collection as one automated test.

- **01–14:** manual API reference and integration workflows; authenticate and set the required IDs first. A newly registered owner must configure initial prices with PUT `/api/meal-pricing` (omit `effectiveDate`) before publishing menus.
- **15–19, 22–24:** automated regression folders. Each fresh-owner fixture checks missing pricing, blocked menu availability and rejected publishing, then explicitly saves half ₹60 / full ₹80 / extra roti ₹10 before creating customers. Folder 15 additionally checks that future-only pricing does not unlock today's menu. Existing price updates and collection snapshot checks remain.
- **20–21:** Cashfree sandbox checkpoints. Run before/after the required browser checkout, following the existing folder instructions. These depend on billing fixtures and gateway configuration.

The environment keeps all variable names but clears all values except `baseUrl`. Tokens, IDs, test credentials, emails and gateway results are populated during local runs. Do not commit the environment after exporting live run values without clearing them again. Mutation scripts restrict execution to localhost.

## Validation status

The earlier regression folders passed before automatic pricing was removed. This consolidated collection preserves those scripts and adds explicit setup and pricing guards. JSON parsing and JavaScript syntax are checked; the updated HTTP assertions have not yet been run against your backend. Rerun automated folders locally before calling this version verified. Backend Maven tests (40), client lint and build passed separately on 2026-10-07.

## Cleanup

Replace the repository's entire old `postman` directory with this directory. Archive the old directory outside the repository first. This removes the overlapping standalone exports and duplicate environments while preserving the reference, automated tests, Cashfree checkpoints, Settings and Dashboard/Insights coverage in the canonical collection.

## Evening-run fixture correction

Folders 17 and 24 now set the fresh foreign mess response cutoffs to 23:59:59 immediately before publishing its dinner menu. This fixes the reported evening publish rejection and subsequent missing-menu responses without changing application business rules. Run within one India calendar day and before 23:59:59. The corrected requests still require a local rerun.
