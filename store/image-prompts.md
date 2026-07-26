# Image-generation prompts — Namazia store assets

For use with ChatGPT (DALL·E / GPT image), Midjourney, or any image model.

---

## Read this first — three things that will otherwise waste your time

**1. Never ask the model to render the Dari text.** Image models garble Perso-Arabic:
they draw disconnected, meaningless letterforms. Both prompts below explicitly forbid text.
Generate the artwork clean, then add `اوقات نماز` / `NAMAZIA` yourself in Canva, Figma or
Photoshop using the Vazirmatn font (already in the repo at `app/src/main/res/font/`).

**2. The model will not give you exact Play dimensions.** GPT image outputs 1024×1024,
1024×1536 or 1536×1024. So:
- Icon → generate square 1024×1024, then downscale to **512×512**.
- Banner → generate landscape 1536×1024, then crop to **1024×500**. Ask for the
  composition to sit in a wide central band so the crop does not cut anything important.

**3. Ask for a flat vector look, not an illustration.** Photorealistic or painterly mosques
look wrong at 48px on a home screen and read as clip-art in a listing. The word that does
the most work in these prompts is *flat*.

---

## Colour palette — exact hex codes

These are the values actually used in the app, so assets built from them will match the UI.

### Core brand

| Role | Hex | Notes |
|---|---|---|
| Lapis (primary) | `#1A3A6B` | The app's main colour. Launcher icon background. |
| Lapis light | `#2D5FAA` | Lighter blue, gradient start |
| Lapis deep | `#0A2050` | Darkest blue, gradient end |
| Gold accent | `#BFA15C` | The mosque mark. Launcher icon foreground. |
| Gold soft | `#D9C48A` | Lighter gold for secondary text |
| Moonlight | `#F5F2EB` | Warm off-white app background |

### Gradient pair used in the current assets

| Position | Hex |
|---|---|
| Gradient start (top-left) | `#2C5CA5` |
| Gradient end (bottom-right) | `#091C48` |
| Mosque fill | `#C6A862` |
| Bright gold (highlights) | `#E2C98C` |

### Light theme (for reference / screenshot mockups)

| Role | Hex |
|---|---|
| primary | `#1A3A6B` |
| primaryContainer | `#DDE7FA` |
| onPrimaryContainer | `#0A1F45` |
| secondary | `#2D5FAA` |
| tertiary | `#8A7434` |
| tertiaryContainer | `#F6EDD6` |
| background | `#F5F2EB` |
| surface | `#FFFFFF` |
| onSurface | `#1B1B20` |
| surfaceVariant | `#E8E3D9` |
| onSurfaceVariant | `#4A463E` |
| outline | `#7C786F` |
| outlineVariant | `#D2CCC0` |
| error | `#B3261E` |

### Dark theme

| Role | Hex |
|---|---|
| primary | `#9EBEFF` |
| primaryContainer | `#1B4585` |
| onPrimaryContainer | `#D9E4FF` |
| tertiary | `#D9C48A` |
| background | `#11151E` |
| surface | `#171D29` |
| surfaceVariant | `#262E3D` |
| onSurface | `#E4E2DC` |
| outline | `#8C8A83` |

---

## PROMPT 1 — App icon (square, becomes 512×512)

```
Create a flat vector app icon, perfectly square, 1:1 ratio, full-bleed with no rounded
corners and no border.

BACKGROUND: a smooth diagonal linear gradient running from top-left to bottom-right,
starting at deep royal blue #2C5CA5 and ending at very dark navy #091C48. Add an
extremely subtle soft radial glow of lighter blue behind the centre of the composition
so the shape lifts off the background. No texture, no noise, no pattern.

SUBJECT: a single centred, perfectly bilaterally symmetrical mosque silhouette, filled
in solid warm gold #C6A862 with absolutely no gradient, no shading, no outline and no
highlight on the mosque itself. It must read as one flat gold shape.

The mosque is built from these parts, in this arrangement:
- A large central onion dome, smoothly curved, slightly taller than a half-circle.
- Two smaller half-domes flanking it, one on each side, lower than the central dome and
  partly overlapped by it.
- A rectangular building body below the domes.
- A tall pointed arch doorway centred in the building body, cut out so the blue
  background shows through it — an opening, not a gold shape.
- Two slim vertical minarets, one at each far side, taller than the building body. Each
  minaret has: a narrow shaft, a slightly wider balcony ring near the top, a small dome
  cap above the balcony, then a short thin spire topped with a small solid ball finial.
- A wide flat plinth bar under the whole building, extending slightly past the minarets.
- A crescent moon above the central dome, sitting on a short thin vertical spire that
  rises from the dome's apex. The crescent opens toward the upper right. It is a true
  crescent — a circle with an offset circular bite taken out of it — never a ring or
  a donut.

COMPOSITION: the mosque occupies roughly 60 percent of the canvas width and is centred
horizontally. Leave clear even margins on all four sides. Position it so the crescent tip
and the plinth both stay well inside the middle 70 percent of the canvas, because launcher
masks crop the edges.

STYLE: flat 2D vector, geometric, minimal, crisp clean edges, symmetrical, iconographic.
Think a modern app icon, not an illustration.

STRICTLY DO NOT INCLUDE: any text, any letters, any Arabic or Persian script, any numbers,
any people, any drop shadows, any 3D effect, any bevel, any glossy reflection, any
photorealism, any rounded corners on the canvas, any frame or border, any watermark.
```

---

## PROMPT 2A — Banner matched to the accepted icon ← use this one

The icon that was accepted uses slightly different colours from the palette above, sampled
from the file itself:

| Element | Hex |
|---|---|
| Gold (the whole mosque) | `#D3AB4F` |
| Blue, lightest (upper area) | `#154A9E` |
| Blue, darkest (bottom-right) | `#041958` |

Its mosque anatomy also differs from the original spec: the central dome is a **pointed**
onion dome, the doorway is a **pointed** arch, the minaret caps are **bulbous** onion
domes, and the minarets are very tall and thin. The prompt below describes that mosque, so
the banner and the icon read as one set.

**Most reliable method:** attach the icon PNG to the message alongside this prompt and add
the line *"Match the mosque shape, proportions and colours in the attached image exactly."*
Describing a shape in words is always less accurate than showing it.

```
Create a flat vector banner illustration in a wide landscape format, composed so that all
important content sits inside a central horizontal band of roughly 2:1 ratio (it will be
cropped to 1024x500).

BACKGROUND: a smooth gradient of deep saturated royal blue, lightest at the top-left
around #154A9E and darkening toward the bottom-right to #041958. Add a soft, subtle
radial glow of slightly lighter blue behind the mosque so the shape lifts off the
background. Scatter small delicate white stars of varying sizes across the upper
two-thirds, fading out completely toward the bottom — a quiet clear night sky, sparse and
restrained, not a glittery galaxy. No texture, no noise, no pattern, no clouds.

SUBJECT: a single flat mosque silhouette in solid warm gold #D3AB4F, placed in the RIGHT
THIRD of the composition and vertically centred, with clear breathing room above and below
so nothing touches the top or bottom edge. The mosque is one flat solid gold shape: no
gradient on it, no shading, no outline, no highlight.

The mosque, perfectly bilaterally symmetrical, is built from these parts:
- A large central ONION dome that rises to a distinct point at the top, noticeably taller
  than a half-circle, with its widest part low and its sides curving inward to the point.
- Two smaller bulbous rounded half-domes flanking it, one each side, sitting lower than
  the central dome and slightly overlapping it.
- A rectangular building body below the domes, with narrow stepped shoulders at its outer
  top corners.
- A tall POINTED arch doorway centred in the building body, cut out so the blue background
  shows through it — a transparent opening, not a gold shape. The arch rises to a point at
  its apex, not a rounded semicircle.
- Two very tall, very slim minarets standing outside the building body, one at each far
  side, rising well above the central dome. Each minaret has, from bottom to top: a narrow
  vertical shaft; a thin horizontal balcony ring; a bulbous onion-shaped dome cap; a short
  thin spire; and a small solid ball finial at the very top.
- A wide flat plinth bar running along the bottom, extending outward past both minarets,
  with the minarets standing on it.
- A crescent moon above the central dome, on a short thin vertical spire rising from the
  dome's point. The crescent opens toward the upper right. It is a true crescent — a
  circle with an offset circular bite removed — never a ring or a donut.

CRITICAL — NEGATIVE SPACE: leave the LEFT HALF and CENTRE of the banner almost completely
empty, containing only the blue gradient and the stars. This area is reserved for text
that will be added later, so it must stay clean and uncluttered. Do not fill it with any
decoration, ornament, pattern, shape or secondary element.

STYLE: flat 2D vector, minimal, elegant, geometric, crisp clean edges, generous negative
space, calm and reverent.

STRICTLY DO NOT INCLUDE: any text, any letters, any Arabic or Persian script, any numbers,
any logos, any people, any 3D effect, any drop shadow, any bevel, any gloss, any
photorealism, any ornamental pattern, any frame or border, any watermark, no clouds, no
city skyline, no second mosque.
```

---

## PROMPT 2 — Banner, original palette version

```
Create a flat vector banner illustration in a wide landscape format, composed so that all
important content sits inside a central horizontal band of roughly 2:1 ratio (it will be
cropped to 1024x500).

BACKGROUND: a smooth diagonal linear gradient from deep royal blue #2C5CA5 at the top-left
to very dark navy #091C48 at the bottom-right, evoking a clear night sky. Scatter small,
subtle white stars of varying sizes across the upper two-thirds, fading out completely
toward the bottom. The stars must be delicate and sparse — a quiet night sky, not a
glittery galaxy. Add a very faint pale glow along the bottom edge suggesting a distant
horizon.

SUBJECT: place a single flat mosque silhouette in solid warm gold #C6A862 in the RIGHT
THIRD of the composition, vertically centred, with clear breathing room above and below
it so nothing touches the top or bottom edge. Behind it, a soft subtle radial glow of
lighter blue.

The mosque is built from these parts:
- A large central onion dome, smoothly curved.
- Two smaller half-domes flanking it, lower and partly overlapped by the central dome.
- A rectangular building body below the domes.
- A tall pointed arch doorway centred in the body, cut out so the blue background shows
  through it.
- Two slim minarets, one each side, taller than the body. Each has a narrow shaft, a
  wider balcony ring near the top, a small dome cap, then a thin spire with a small ball
  finial.
- A wide flat plinth bar under the building.
- A crescent moon above the central dome on a short thin spire, opening toward the upper
  right. A true crescent — a circle with an offset circular bite removed — never a ring.

The mosque is one flat solid gold shape: no gradient on it, no shading, no outline.

CRITICAL — NEGATIVE SPACE: leave the LEFT HALF and CENTRE of the banner almost completely
empty, containing only the gradient and stars. This empty area is reserved for text that
will be added later, so it must stay clean and uncluttered. Do not fill it with any
decoration, ornament, pattern or shape.

STYLE: flat 2D vector, minimal, elegant, geometric, crisp edges, generous negative space.
A calm, reverent mood.

STRICTLY DO NOT INCLUDE: any text, any letters, any Arabic or Persian script, any numbers,
any logos, any people, any 3D effect, any drop shadow, any photorealism, any heavy
ornamental pattern, any frame or border, any watermark, no clouds, no city skyline.
```

---

## PROMPT 3 — Just the mosque on transparency (most flexible option)

If you want full control over the layout, ask for the mark alone and composite it yourself.

```
Create a flat vector mosque icon on a fully transparent background, as a single solid
shape filled in warm gold #C6A862 — no gradient, no shading, no outline, no shadow.

The mosque, perfectly bilaterally symmetrical, consists of: a large central onion dome;
two lower half-domes flanking it, partly overlapped by the central dome; a rectangular
building body; a tall pointed arch doorway centred in the body and cut out as a
transparent opening; two slim minarets at the far sides, each with a narrow shaft, a wider
balcony ring near the top, a small dome cap, and a thin spire topped with a small ball
finial; a wide flat plinth bar beneath the building; and a crescent moon above the central
dome on a short thin spire, opening toward the upper right — a true crescent, being a
circle with an offset circular bite removed, never a ring.

Flat 2D vector, geometric, minimal, crisp clean edges, centred, with even margins.

Do not include any text, letters, Arabic or Persian script, numbers, people, shadows, 3D
effects, gloss, photorealism, frames or watermarks. Background must be transparent.
```

---

## After you get the images

1. **Icon** — downscale to exactly **512×512**, export **32-bit PNG**, keep it a full square
   with no rounded corners (Play adds its own mask and shadow). Must be under 1 MB.
2. **Banner** — crop to exactly **1024×500**, export PNG or JPEG, under 15 MB.
3. **Add the text to the banner** in the left/centre empty area, right-aligned toward the
   mosque so it reads into the mark:
   - `اوقات نماز` — Vazirmatn Bold, white `#FFFFFF`, large
   - `NAMAZIA` — Vazirmatn Medium, gold `#D9C48A`
   - `اذان، قبله و تقویم برای افغانستان` — Vazirmatn Regular, white at about 82% opacity
   - Optionally a short gold `#BFA15C` rule above the title
4. **Check the icon at real size.** View it at 48×48 pixels. If the minaret finials or the
   crescent turn to mush, the mark is too detailed — ask for a simpler version with thicker
   strokes. This is the test that matters; an icon is almost always seen small.

If you want the launcher icon inside the app to match whatever you choose, send me the
final image and I will redraw `ic_launcher_foreground.xml` to match it.
