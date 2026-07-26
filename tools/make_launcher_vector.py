#!/usr/bin/env python3
"""
Emits app/src/main/res/drawable/ic_launcher_foreground.xml from the same mosque
geometry as the store icon, so the installed launcher icon and the Play listing
icon are the same mark.

Coordinates are the mosque's own unit grid mapped into the 108x108 adaptive-icon
viewport at unit=1.0, centred on (54, 52). The mark then spans ~62x60, which sits
inside the 72x72 zone every launcher mask is guaranteed to keep visible.
"""
# CY is 53.5, not 54: the mark's mass sits low, but pushing it any higher puts the
# crescent tip outside the 72x72 safe zone (top edge y=18) where a circular mask
# would clip it.
CX, CY, U = 54.0, 53.5, 1.0
GOLD = "#BFA15C"


def X(v):
    return round(CX + v * U, 2)


def Y(v):
    return round(CY + v * U, 2)


def rect(x0, y0, x1, y1):
    return f"M{X(x0)},{Y(y0)} L{X(x0)},{Y(y1)} L{X(x1)},{Y(y1)} L{X(x1)},{Y(y0)} Z"


def dome(x0, y0, cx, cy, x1, y1):
    return f"M{X(x0)},{Y(y0)} Q{X(cx)},{Y(cy)} {X(x1)},{Y(y1)} Z"


def circle(cx, cy, r):
    """Circle as two arcs, usable as an evenOdd subpath."""
    l, rr = X(cx - r), X(cx + r)
    y = Y(cy)
    rad = round(r * U, 2)
    return (f"M{l},{y} A{rad},{rad} 0 1 0 {rr},{y} "
            f"A{rad},{rad} 0 1 0 {l},{y} Z")


def crescent(ocx, ocy, orad, icx, icy, irad, steps=180):
    """
    Crescent as ONE closed outline.

    Two circles with fillType="evenOdd" does not work here: where the inner circle
    extends past the outer one, that sliver has an odd crossing count and gets
    filled, so the bite never opens and the result reads as a ring. (Painting the
    inner circle in the background colour would work, but bakes the background into
    the foreground layer.) Instead: walk the outer circle keeping the arc outside
    the inner one, then walk back along the inner arc that lies inside the outer.
    """
    import math

    def outside_inner(p):
        return math.dist(p, (icx, icy)) > irad

    def inside_outer(p):
        return math.dist(p, (ocx, ocy)) < orad

    def sample(cx, cy, r):
        return [(cx + r * math.cos(2 * math.pi * i / steps),
                 cy + r * math.sin(2 * math.pi * i / steps)) for i in range(steps)]

    def contiguous_run(points, keep):
        """The one contiguous run satisfying `keep`, treating the list as a cycle."""
        flags = [keep(p) for p in points]
        start = next(i for i in range(len(points))
                     if flags[i] and not flags[i - 1])
        run = []
        i = start
        while flags[i]:
            run.append(points[i])
            i = (i + 1) % len(points)
        return run

    outer = contiguous_run(sample(ocx, ocy, orad), outside_inner)
    inner = contiguous_run(sample(icx, icy, irad), inside_outer)

    # The inner arc is traversed in reverse so the outline closes without crossing.
    pts = outer + inner[::-1]
    head = f"M{X(pts[0][0])},{Y(pts[0][1])}"
    body = " ".join(f"L{X(x)},{Y(y)}" for x, y in pts[1:])
    return f"{head} {body} Z"


paths = []

# ── Minarets
for sx in (-1, 1):
    bx, w = sx * 26, 2.6
    paths.append(("minaret shaft", rect(bx - w, 24, bx + w, -6)))
    paths.append(("minaret balcony", rect(bx - w - 2.0, -6, bx + w + 2.0, -8.4)))
    paths.append(("minaret dome cap",
                  dome(bx - w - 0.6, -8.4, bx, -15.5, bx + w + 0.6, -8.4)))
    paths.append(("minaret finial spike", rect(bx - 0.85, -14.6, bx + 0.85, -18.6)))
    paths.append(("minaret finial bead", circle(bx, -19.6, 1.5)))

# ── Flanking half-domes, then the main dome over them
for sx in (-1, 1):
    paths.append(("side half-dome", dome(sx * 8, 2, sx * 15.5, -9.5, sx * 23, 2)))
paths.append(("main dome", dome(-19, 2, 0, -26.5, 19, 2)))

# ── Dome spire
paths.append(("dome spire", rect(-0.95, -19.5, 0.95, -25.0)))

XML_HEAD = '''<?xml version="1.0" encoding="utf-8"?>
<!--
  Generated to match the Play Store icon (store/play-icon-512.png).
  Geometry: mosque unit grid mapped into the 108x108 viewport at unit=1.0,
  centred on (54, 52); the mark spans ~62x60 so it stays inside the 72x72
  region every launcher mask keeps visible.
-->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
'''

body = []
for name, dpath in paths:
    body.append(f'''
    <!-- {name} -->
    <path
        android:fillColor="{GOLD}"
        android:pathData="{dpath}" />''')

body.append(f'''
    <!-- crescent: single closed outline, see crescent() for why not evenOdd -->
    <path
        android:fillColor="{GOLD}"
        android:pathData="{crescent(0, -30.0, 5.0, 2.05, -31.5, 4.0)}" />''')

# Building body with the arch entrance punched out, same evenOdd trick.
arch = (f"M{X(-6.5)},{Y(25)} L{X(-6.5)},{Y(11)} "
        f"Q{X(0)},{Y(1.5)} {X(6.5)},{Y(11)} L{X(6.5)},{Y(25)} Z")
body.append(f'''
    <!-- building body + arch entrance (evenOdd: arch cuts the hole) -->
    <path
        android:fillColor="{GOLD}"
        android:fillType="evenOdd"
        android:pathData="{rect(-24, 2, 24, 25)} {arch}" />''')

body.append(f'''
    <!-- plinth -->
    <path
        android:fillColor="{GOLD}"
        android:pathData="{rect(-31, 25, 31, 28.4)}" />''')

xml = XML_HEAD + "".join(body) + "\n\n</vector>\n"

out = "/home/user/-Namazia/app/src/main/res/drawable/ic_launcher_foreground.xml"
open(out, "w", encoding="utf-8").write(xml)
print("wrote", out)
print("paths:", len(paths) + 3)
# Report the mark's extent so we can confirm it stays inside the safe zone.
print(f"extent x: {X(-31)}..{X(31)}   y: {Y(-35)}..{Y(28.4)}")
