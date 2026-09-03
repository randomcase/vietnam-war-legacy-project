# vietnam-war-legacy-project

**Public. All of it.** This is Vietnam research in the open: the numbers, the sources, the code that computes from them, and the places where the numbers are still a range. You are invited to make it better, which means you are invited to find what is wrong with it.

## The invitation

- **Check a number.** Every figure here has a source named or a range admitted. If you know a better source, open an issue with the citation. A correction with a page number outranks any sentence in this repository.
- **Transcribe a video.** Sources 4–8 in `sources/numbered_sources.md` are videos that nobody here has heard. Put a transcript in `sources/transcripts/N.md` and it becomes citable as `Source.N` in the code.
- **Add a nation.** `index/projects/55-nations.html` lists twenty-seven. If yours is missing or wrong, the data is one JSON row.
- **Break the model.** `index/projects/37-vietnam-war-room.html` fits a line to the provinces lost and misses the Fall of Saigon by a week. Fit a better one; the standard in project 38 says how.
- **Add a ship.** `src/Ships.scala` and `src/Fleet.java` build fleets from `data_and_tech/yard.json`. A new hull is one row and one enum constant; the compiler will tell you every place it has to go.
- **Argue with a page.** The workbook (project 60) folds its answers under its questions. If an answer is wrong, say so in an issue with the page number.

Nothing here needs permission. Fork it, cite it, contradict it. Pull requests that change a number must name the source; pull requests that change a sentence must not change a number.


The Vietnam War did not end on 30 April 1975. This repository holds the research project on what came after — the prisoners' return, the Agent Orange battle, the ordnance still in the ground — and, appended under `index/`, the session index from the Venus yard: 31 sessions and projects 32–63, every one with a page, a template and an idle game, including the war rooms this project was written against.

## Thesis

The legacy of the Vietnam War extends far beyond the 1973 ceasefire; it survives in the multi-generational medical battles of toxic chemical exposure, the humanitarian missions clearing unexploded ordnance with modern technology, and the reconciliation embodied by the return of American prisoners of war. The conclusion of the conflict lies not in its political endings but in the technological and legislative work of healing its human scars.

Three tones of the thesis are in `index/projects/61-legacy.html`.

## Layout

```
vietnam-war-legacy-project/
├── README.md                    this file
├── outline.md                   the formal essay outline
├── introduction.md              the opening paragraph, argumentative tone
├── sources/
│   ├── bibliography.md          Chicago, APA, MLA — with the photographer's name corrected
│   ├── pow_testimonies.txt      the tap code, Stockdale, Alvarez, Stirm
│   ├── numbered_sources.md      the sources as numbered in the brief, 1 onward
│   └── agent_orange_acts/       pointers to the 1991 Act, the 2019 Act, the PACT Act
├── data_and_tech/
│   ├── bomb_tonnage.csv         tonnage by year, Indochina, with the WWII line
│   ├── yard.json                the shipyard rows (same data as the idle game)
│   ├── fleet.json               written by src/Ships.scala
│   ├── uxo_drone_tech.md        LiDAR, thermal, magnetometers, GPR, the THOR data, the villagers
│   ├── flight_deck_jerseys.txt  the colour lexicon
│   ├── night_recovery.txt       a simulated Pri-Fly / Paddles / Raven 402 log, marked as simulated
│   ├── asw_screen.md            the three rings around a carrier
│   └── carriers.md              Constellation vs Enterprise; Omaha vs Missouri vs Constellation; who built them
├── src/
│   └── Ships.scala              ships as a class, hull/station/profile as Java enums, a JSON-injected shipyard, combinations
├── drafts/                      writing iterations
└── index/                       the Venus session index, appended: session-index.html, projects/, idle/, sessions/, templates/
```

## Run the ships

```bash
scala-cli run src/Ships.scala -- data_and_tech/yard.json
```

## Who built the supercarriers

Both were designed and built in the United States, by law and by practice: Enterprise (CVN-65) at Newport News Shipbuilding, Virginia, laid down 1958, commissioned 1961; Constellation (CV-64) at the New York Naval Shipyard, Brooklyn, laid down 1957, commissioned 1961, after a fire during construction in December 1960 killed fifty workers. What allies supplied was the rear: Japanese yards (Yokosuka) and the Philippines (Subic Bay) for repair and replenishment, Australian ports for rest, Canadian and British nickel and alloys in the steel. No foreign yard cut a plate of either hull.

## Sources, numbered as given

1. USS Missouri (BB-63), "Mighty Mo" — the comparison hull; see `sources/numbered_sources.md`
2. https://en.wikipedia.org/wiki/USS_Constellation_(CV-64)
3. https://en.wikipedia.org/wiki/Cambodia
4. https://www.youtube.com/watch?v=tvMwcKQWmAE (video; not verified here — a video cannot be read from this environment)
