# Numbered sources, as given in the brief, starting with 1

Each number is also a constant in `src/Ships.scala` (enum `Source`), so a ship or a data row can cite by number.

1. **USS Missouri (BB-63), "Mighty Mo".** Iowa-class fast battleship; 887 ft; nine 16-inch guns; the Japanese surrender was signed on her deck, 2 September 1945. Not in Vietnam — her sister New Jersey was, September 1968 to April 1969, 5,866 sixteen-inch rounds. Search term as given: "Mighty Mo, use google". Reference: Naval History and Heritage Command, DANFS, Missouri (BB-63).
2. **USS Constellation (CV-64).** https://en.wikipedia.org/wiki/USS_Constellation_(CV-64) — Kitty Hawk class; New York Naval Shipyard; commissioned 27 October 1961; Pierce Arrow 5 August 1964; Sather and Alvarez; seven MiGs on 10 May 1972; decommissioned 2003.
3. **Cambodia.** https://en.wikipedia.org/wiki/Cambodia — neutral under Sihanouk; bombed from March 1969 (Menu) to 15 August 1973; Phnom Penh fell 17 April 1975; 1.5–2 million dead under the Khmer Rouge; Vietnamese invasion December 1978.
4. **Video.** https://www.youtube.com/watch?v=tvMwcKQWmAE
5. **Video.** https://www.youtube.com/watch?v=-N6744h_JWs
6. **Video.** https://www.youtube.com/watch?v=_ecXNJP-ERY
7. **Video.** https://www.youtube.com/watch?v=e4FQj0we3nE&t=2693s (the brief points at 44:53)
8. **Video.** https://www.youtube.com/watch?v=B1IZ-NTLORI

Sources 4–8 were asked to be transcribed. They are not transcribed here: this environment has no way to hear or fetch a video's audio or captions, and a transcript written without hearing the video would be an invention. Each is listed by URL with its number so a real transcript can be dropped into `sources/transcripts/N.md` and cited as Source.N in the code.

The enum stops at 4 because that is how many were given; "in perpetuity" is handled not by an enum, which is closed by definition, but by `Combinations` in the Scala, which generates variations without end from the closed sets.
