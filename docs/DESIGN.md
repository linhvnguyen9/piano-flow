---
name: Serene Practice
colors:
  surface: '#f9f9f8'
  surface-dim: '#dadad9'
  surface-bright: '#f9f9f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f4f4f3'
  surface-container: '#eeeeed'
  surface-container-high: '#e8e8e7'
  surface-container-highest: '#e2e2e2'
  on-surface: '#1a1c1c'
  on-surface-variant: '#414848'
  inverse-surface: '#2f3131'
  inverse-on-surface: '#f1f1f0'
  outline: '#717978'
  outline-variant: '#c0c8c7'
  surface-tint: '#3f6565'
  primary: '#325858'
  on-primary: '#ffffff'
  primary-container: '#4a7070'
  on-primary-container: '#c9f2f2'
  inverse-primary: '#a6cece'
  secondary: '#7d562d'
  on-secondary: '#ffffff'
  secondary-container: '#ffca98'
  on-secondary-container: '#7a532a'
  tertiary: '#6f4738'
  on-tertiary: '#ffffff'
  tertiary-container: '#8a5f4e'
  on-tertiary-container: '#ffe4db'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#c2eaea'
  primary-fixed-dim: '#a6cece'
  on-primary-fixed: '#002020'
  on-primary-fixed-variant: '#264d4d'
  secondary-fixed: '#ffdcbd'
  secondary-fixed-dim: '#f0bd8b'
  on-secondary-fixed: '#2c1600'
  on-secondary-fixed-variant: '#623f18'
  tertiary-fixed: '#ffdbce'
  tertiary-fixed-dim: '#f0baa6'
  on-tertiary-fixed: '#301307'
  on-tertiary-fixed-variant: '#633d2e'
  background: '#f9f9f8'
  on-background: '#1a1c1c'
  surface-variant: '#e2e2e2'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  music-data:
    fontFamily: IBM Plex Mono
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.05em
  label-caps:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.1em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  container-margin: 24px
  gutter: 16px
---

## Brand & Style

The design system is centered on the concept of "Mindful Mastery." It targets adult learners who seek a sanctuary for focus rather than a high-pressure gaming environment. The brand personality is serene, reassuring, and deeply academic without being cold.

The visual style is a blend of **Minimalism** and **Tactile Modernism**. It prioritizes generous whitespace—often referred to as "breathing room"—to lower cognitive load. The interface avoids aggressive gradients or heavy shadows, opting instead for "paper-like" layers and soft, meaningful color accents that guide the eye toward musical notation and hand placement.

## Colors

The palette is anchored in organic, desaturated tones to evoke a sense of calm.

- **Primary (Sage Teal):** The single "active / selected" accent. Used for primary actions, progress indicators, the active/selected state of chips (chord chips, the root & quality pickers), the focused card, and lit piano keys. It provides a clear but non-distracting focal point.
- **Secondary (Honey Amber):** Reserved for **achievement and "warm" feedback only — never for selection.** It marks the payoff moments (the hand-movement summary and the connector pills). Keeping teal and amber semantically distinct matters: teal = "this is active/chosen," amber = "this is the win."
- **Amber in dark mode:** Use the mode-independent `secondary-fixed-dim` (#F0BD8B) fill with `on-secondary-fixed` text for amber accents. Do **not** rely on the raw dark `secondary-container`, which resolves to a muddy brown (#623F18) — amber must read as warm honey in both modes.
- **Surface Strategy:** In light mode, surfaces use a "Paper" tint (#FAF9F6) to reduce eye strain compared to pure white. In dark mode, a "Warm Charcoal" (#1C1B1A) maintains the organic feel, avoiding the harshness of true black.
- **Status:** Errors use a desaturated terracotta to remain visible without feeling alarming or loud.

## Typography

This design system utilizes a dual-font approach to distinguish between instructional UI and musical data.

- **Plus Jakarta Sans:** Chosen for its friendly, humanist terminals. It is used for all narrative and instructional text to keep the tone approachable.
- **IBM Plex Mono:** A clean, technical monospace used exclusively for **musical data** — note names, chord *symbols* (e.g., `Cmaj7`), voicing indices, semitone counts, and finger numbering. This ensures that characters like 'B' and '8' or 'I' and '1' are never confused during practice. The mono treatment is identical wherever musical data appears — a chord-card title, the picker's live-preview symbol, the keyboard labels.
- **Narrative words stay sans:** A spelled-out quality word like "Major"/"Minor" is *narrative*, not a symbol — set it in Plus Jakarta Sans even inside a chord title (a "C Major" title renders the root `C` in mono and "Major" in Jakarta). Mono is for symbols and data, never for English words.
- **Hierarchy:** Use larger font sizes with ample line height to ensure readability from a distance (e.g., when the device is placed on a piano music stand).

## Layout & Spacing

The design system follows an **8pt linear grid** to maintain a rhythmic, predictable flow.

- **Layout Model:** A fluid grid with fixed side margins of 24px on mobile and 48px on tablet.
- **Vertical Rhythm:** Content blocks should be separated by `xl` (32px) spacing to prevent the UI from feeling cluttered.
- **Touch Targets:** All interactive elements (buttons, chips, keys, toggles) must maintain a minimum **48x48px** hit area to accommodate adult dexterity and quick movements between the screen and the piano keys. The primary action button on a screen or sheet (e.g. "Play Progression", "Add chord", "Update") stands taller at **56px**.
- **Sheet density:** Modal/bottom-sheet forms keep the 24px side margin and stay on the 8pt grid, but may use a denser **16px** block rhythm (vs the 32px between full-screen content blocks) — a focused form has less to separate.

## Elevation & Depth

To maintain the "serene" aesthetic, depth is communicated through **Tonal Layers** and **Soft Ambient Shadows** rather than sharp borders.

- **Level 0 (Base):** The primary background color.
- **Level 1 (Cards):** A `surface-container` fill (warmer/cooler than the base, per mode) lifted by a very soft ambient shadow — roughly `shadowElevation` 1dp at rest, ~8dp when the card is the active/focused one. **Never** give a card a hard 1px border; depth is tonal, not stroked. The same treatment applies to input fields and the picker's "Preview" card.
- **Level 2 (Modals/Sheets):** Elevated with a more pronounced blur (Blur: 20px) to indicate temporary focus.
- **Piano Keys:** White keys use a subtle inner stroke (1px) to define edges; active keys use a soft outer glow (4px) in the Primary color to simulate the "light-up" feedback of a digital learning piano.
- **Keyboard Tray:** Every mini-keyboard sits in a warm "tray" container — a `surface-container-highest` fill, clipped to a 12px radius, with ~6px inset padding — so keys never butt against the card edge. **Never** frame a keybed in pure black (#000000); the tray is a warm tonal layer in both modes, and is identical across the main screen and the picker sheet.

## Shapes

The shape language is "soft-organic." Hard corners are avoided to reduce the visual "sharpness" of the app.

- **Cards:** 16px corner radius (`rounded-lg`).
- **Interactive Elements:** Buttons and chips use a full-pill radius to appear inviting and tactile.
- **Bottom Sheets:** Use a distinctive 24px top-only radius to create a soft "drawer" feel when pulled up for lesson details.

## Components

- **Buttons:**
    - *Primary:* Solid Sage Teal (#4A7070) with white text. Full-pill shape; **56px tall** for the main action on a screen or sheet.
    - *Secondary:* Transparent background with a 1.5px Sage Teal border. Full-pill.
- **Pill Chips:** Always full-pill, **≥48px tall**. The **selected** state is teal (`primary-container` fill + `on-primary-container` text) — this is the one selection accent across chord chips and the root/quality pickers. **Never** use amber for a selected chip. Info chips use a pale version of the primary color with dark text.
- **Piano Keyboard:**
    - *White Keys:* Off-white fill, 1px neutral-300 stroke.
    - *Black Keys:* Dark Ink fill.
    - *Active State:* Primary color fill + a soft 400ms "glow" transition to highlight the note to be played.
    - *Tray:* Always wrapped in a `surface-container-highest` tray (12px clip, ~6px inset) — never a pure-black keybed.
- **Labeled Stepper:** Uses a progress line connecting circles. Completed steps turn Primary; the active step has a Secondary (Honey) ring to indicate the current focus.
- **Toggle/Switch:** Large, tactile "thumb" with a 4px offset from the track edge. Use Primary color for the "On" state.
- **Icons:** 2px stroke weight, rounded caps and joins. Avoid filled icons unless used as a primary status indicator.
- **Achievement Pills:** The hand-movement summary and the inter-chord connectors are amber (`secondary-fixed-dim`) full-pills that communicate the *payoff*. The summary states the optimized result against the naive baseline ("`N` semitones · down from `M`", plus a plain-language "Saved `X` — `Y`% less hand movement"); connectors read "↓ `N` semitones · smoothed". Counts are mono; surrounding words are sans.

## Do's and Don'ts

These rules keep the main Songs screen and the chord-picker sheet reading as **one app**. Changes to any shared treatment (chip selection, card depth, keyboard tray, buttons) must be applied to both surfaces.

**Do**

- Use **teal** for anything active or selected; use **amber** only for achievement / savings.
- Render every chord symbol, note name, and numeric music value in **IBM Plex Mono**; render English words (including "Major"/"Minor") in **Plus Jakarta Sans**.
- Build depth from **tonal layers + soft shadows** (`surface-container` cards at ~1dp rest / ~8dp active); wrap every keyboard in the warm `surface-container-highest` **tray** (12px clip, ~6px inset).
- Make primary buttons **full-pill at 56px** and keep all touch targets **≥48px**.
- Surface the **value proposition** — always show the smoothed hand-movement against the naive baseline.

**Don't**

- Don't use amber (or the muddy dark `secondary-container`) for a selected chip — selection is teal.
- Don't set narrative words in mono, or chord symbols / note names in sans.
- Don't frame a keyboard in pure black (#000000), and don't define cards or input fields with hard 1px borders.
- Don't let the picker sheet drift from the screen's styling — keep their chips, cards, keyboard tray, and buttons identical.