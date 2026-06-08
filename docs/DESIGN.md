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

- **Primary (Sage Teal):** Used for primary actions, progress indicators, and active piano keys. It provides a clear but non-distracting focal point.
- **Secondary (Honey Amber):** Reserved for moments of achievement, secondary highlights, or "warm" feedback.
- **Surface Strategy:** In light mode, surfaces use a "Paper" tint (#FAF9F6) to reduce eye strain compared to pure white. In dark mode, a "Warm Charcoal" (#1C1B1A) maintains the organic feel, avoiding the harshness of true black.
- **Status:** Errors use a desaturated terracotta to remain visible without feeling alarming or loud.

## Typography

This design system utilizes a dual-font approach to distinguish between instructional UI and musical data.

- **Plus Jakarta Sans:** Chosen for its friendly, humanist terminals. It is used for all narrative and instructional text to keep the tone approachable.
- **IBM Plex Mono:** A clean, technical monospace used exclusively for musical notes, chord names (e.g., Cmaj7), and finger numbering. This ensures that characters like 'B' and '8' or 'I' and '1' are never confused during practice.
- **Hierarchy:** Use larger font sizes with ample line height to ensure readability from a distance (e.g., when the device is placed on a piano music stand).

## Layout & Spacing

The design system follows an **8pt linear grid** to maintain a rhythmic, predictable flow.

- **Layout Model:** A fluid grid with fixed side margins of 24px on mobile and 48px on tablet.
- **Vertical Rhythm:** Content blocks should be separated by `xl` (32px) spacing to prevent the UI from feeling cluttered.
- **Touch Targets:** All interactive elements (buttons, keys, toggles) must maintain a minimum 48x48px hit area to accommodate adult dexterity and quick movements between the screen and the piano keys.

## Elevation & Depth

To maintain the "serene" aesthetic, depth is communicated through **Tonal Layers** and **Soft Ambient Shadows** rather than sharp borders.

- **Level 0 (Base):** The primary background color.
- **Level 1 (Cards):** Use a slightly lighter/darker surface color (depending on mode) with a very soft, 10% opacity shadow (Blur: 12px, Y: 4px).
- **Level 2 (Modals/Sheets):** Elevated with a more pronounced blur (Blur: 20px) to indicate temporary focus.
- **Piano Keys:** White keys use a subtle inner stroke (1px) to define edges; active keys use a soft outer glow (4px) in the Primary color to simulate the "light-up" feedback of a digital learning piano.

## Shapes

The shape language is "soft-organic." Hard corners are avoided to reduce the visual "sharpness" of the app.

- **Cards:** 16px corner radius (`rounded-lg`).
- **Interactive Elements:** Buttons and chips use a full-pill radius to appear inviting and tactile.
- **Bottom Sheets:** Use a distinctive 24px top-only radius to create a soft "drawer" feel when pulled up for lesson details.

## Components

- **Buttons:**
    - *Primary:* Solid Sage Teal (#4A7070) with white text. Full-pill shape.
    - *Secondary:* Transparent background with a 1.5px Sage Teal border.
- **Pill Chips:** Used for difficulty levels (Easy, Intermediate). Always full-pill. Info chips use a pale version of the primary color with dark text.
- **Piano Keyboard:**
    - *White Keys:* Off-white fill, 1px neutral-300 stroke.
    - *Black Keys:* Dark Ink fill.
    - *Active State:* Primary color fill + a soft 400ms "glow" transition to highlight the note to be played.
- **Labeled Stepper:** Uses a progress line connecting circles. Completed steps turn Primary; the active step has a Secondary (Honey) ring to indicate the current focus.
- **Toggle/Switch:** Large, tactile "thumb" with a 4px offset from the track edge. Use Primary color for the "On" state.
- **Icons:** 2px stroke weight, rounded caps and joins. Avoid filled icons unless used as a primary status indicator.