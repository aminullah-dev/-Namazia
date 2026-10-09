#!/usr/bin/env python3
"""
Play Store graphics for Namazia.

  store/play-icon-512.png            512x512   store app icon
  store/play-feature-1024x500.png   1024x500   feature graphic (banner)

Both are drawn from one mosque routine so the two assets, and the launcher icon,
read as the same mark. Text uses Raqm (harfbuzz shaping + fribidi), which handles
Perso-Arabic joining and RTL ordering correctly from raw strings.
"""
import os
import random

from PIL import Image, ImageDraw, ImageFilter, ImageFont

REPO = "/home/user/-Namazia"
OUT = os.path.join(REPO, "store")
FONT_DIR = os.path.join(REPO, "app/src/main/res/font")

LAPIS_LIGHT = (44, 92, 165)
LAPIS_DEEP = (9, 28, 72)
GOLD = (198, 168, 98)
GOLD_BRIGHT = (226, 201, 140)
WHITE = (255, 255, 255)

SS = 4  # supersample, then downsample with LANCZOS for clean edges


def font(name, size):
    return ImageFont.truetype(
        os.path.join(FONT_DIR, name), size, layout_engine=ImageFont.Layout.RAQM
    )


def qpts(p0, p1, p2, steps=120):
    out = []
    for i in range(steps + 1):
        t = i / steps
        u = 1 - t
        out.append((u * u * p0[0] + 2 * u * t * p1[0] + t * t * p2[0],
                    u * u * p0[1] + 2 * u * t * p1[1] + t * t * p2[1]))
    return out


def diagonal_gradient(size, c0, c1):
    small = Image.new("RGB", (96, 96))
    px = small.load()
    for y in range(96):
        for x in range(96):
            t = (x / 95 * 0.55 + y / 95 * 0.45)
            px[x, y] = tuple(round(c0[i] + (c1[i] - c0[i]) * t) for i in range(3))
    return small.resize(size, Image.BICUBIC)


def draw_mosque(img, cx, cy, unit, color, bg_for_arch):
    """
    Mosque mark. `unit` is the size of one grid step; the whole mark is about
    68 units wide and 62 tall, centred on (cx, cy).

    Refined from the first pass: minarets get a balcony, a dome cap and a slim
    finial instead of a bare triangle, which was reading as an arrow. The crescent
    sits on the main dome's spire rather than floating beside it.
    """
    d = ImageDraw.Draw(img)

    def P(x, y):
        return (cx + x * unit, cy + y * unit)

    def poly(pts, fill=color):
        d.polygon([P(*p) for p in pts], fill=fill)

    # ── Minarets (mirrored)
    for sx in (-1, 1):
        bx = sx * 26          # centre line of this minaret
        w = 2.6               # half-width of the shaft
        # shaft
        poly([(bx - w, 24), (bx - w, -6), (bx + w, -6), (bx + w, 24)])
        # balcony ring
        poly([(bx - w - 2.0, -6), (bx - w - 2.0, -8.4), (bx + w + 2.0, -8.4), (bx + w + 2.0, -6)])
        # dome cap above the balcony
        d.polygon([P(*p) for p in qpts((bx - w - 0.6, -8.4), (bx, -15.5), (bx + w + 0.6, -8.4))],
                  fill=color)
        # finial: spike topped with a bead. The spike is deliberately chunky —
        # a hairline vanishes at 512px and leaves the bead looking like a stray dot.
        poly([(bx - 0.85, -14.6), (bx - 0.85, -18.6), (bx + 0.85, -18.6), (bx + 0.85, -14.6)])
        r = 1.5 * unit
        c = P(bx, -19.6)
        d.ellipse([c[0] - r, c[1] - r, c[0] + r, c[1] + r], fill=color)

    # ── Flanking half-domes, drawn before the main dome so it overlaps them
    for sx in (-1, 1):
        d.polygon([P(*p) for p in qpts((sx * 8, 2), (sx * 15.5, -9.5), (sx * 23, 2))], fill=color)

    # ── Main dome
    d.polygon([P(*p) for p in qpts((-19, 2), (0, -26.5), (19, 2))], fill=color)

    # dome spire + crescent
    poly([(-0.95, -19.5), (-0.95, -25.0), (0.95, -25.0), (0.95, -19.5)])
    cr = 5.0 * unit
    cc = P(0, -30.0)
    d.ellipse([cc[0] - cr, cc[1] - cr, cc[0] + cr, cc[1] + cr], fill=color)
    # bite the crescent out with the backdrop colour, offset up-right
    ir = 4.0 * unit
    ic = (cc[0] + 2.05 * unit, cc[1] - 1.5 * unit)
    d.ellipse([ic[0] - ir, ic[1] - ir, ic[0] + ir, ic[1] + ir], fill=bg_for_arch)

    # ── Building body
    poly([(-24, 2), (-24, 25), (24, 25), (24, 2)])

    # ── Arch entrance, punched back out in the backdrop colour
    arch = [P(-6.5, 25), P(-6.5, 11)]
    arch += [P(*p) for p in qpts((-6.5, 11), (0, 1.5), (6.5, 11))]
    arch += [P(6.5, 25)]
    d.polygon(arch, fill=bg_for_arch)

    # ── Plinth
    poly([(-31, 25), (-31, 28.4), (31, 28.4), (31, 25)])


def radial_glow(size, center, radius, color, strength):
    """Soft light behind the mark so it lifts off the flat gradient."""
    w, h = size
    layer = Image.new("L", (w, h), 0)
    dl = ImageDraw.Draw(layer)
    dl.ellipse([center[0] - radius, center[1] - radius,
                center[0] + radius, center[1] + radius], fill=strength)
    layer = layer.filter(ImageFilter.GaussianBlur(radius * 0.45))
    tint = Image.new("RGB", (w, h), color)
    return layer, tint


# ─────────────────────────────────────────────────────────────
def build_icon(path):
    n = 512 * SS
    img = diagonal_gradient((n, n), LAPIS_LIGHT, LAPIS_DEEP)

    mask, tint = radial_glow((n, n), (n / 2, n * 0.54), n * 0.36, (120, 165, 235), 70)
    img = Image.composite(tint, img, mask)

    # Sample the backdrop where the arch and crescent cut-outs land.
    bg = img.getpixel((int(n / 2), int(n * 0.62)))

    # Nudged up: the mark's visual mass sits low (building + plinth), so geometric
    # centring reads as bottom-heavy.
    draw_mosque(img, cx=n / 2, cy=n * 0.485, unit=n / 108 * 1.02,
                color=GOLD, bg_for_arch=bg)

    img = img.resize((512, 512), Image.LANCZOS)
    # Play's spec asks for a 32-bit PNG: add a fully opaque alpha channel. The icon
    # must stay a full-bleed square — Play applies its own rounded mask and shadow.
    img.convert("RGBA").save(path, "PNG", optimize=True)
    return path


def build_feature(path):
    W, H = 1024 * SS, 500 * SS
    img = diagonal_gradient((W, H), LAPIS_LIGHT, LAPIS_DEEP)
    d = ImageDraw.Draw(img, "RGBA")

    # Night sky, thinning toward the horizon
    random.seed(11)
    for _ in range(260):
        x, y = random.uniform(0, W), random.uniform(0, H * 0.8)
        r = random.uniform(0.9, 2.4) * SS
        a = int(random.uniform(35, 170) * (1 - y / (H * 0.95)))
        if a > 0:
            d.ellipse([x - r, y - r, x + r, y + r], fill=(255, 255, 255, a))

    # Horizon wash instead of the bumpy arcade from the first pass
    band = Image.new("RGBA", (W, int(H * 0.2)), (255, 255, 255, 0))
    bd = ImageDraw.Draw(band)
    for i in range(band.height):
        bd.line([(0, i), (W, i)], fill=(255, 255, 255, int(13 * (i / band.height))))
    img.paste(Image.alpha_composite(
        img.crop((0, H - band.height, W, H)).convert("RGBA"), band).convert("RGB"),
        (0, H - band.height))

    d = ImageDraw.Draw(img, "RGBA")
    mask, tint = radial_glow((W, H), (W * 0.775, H * 0.52), H * 0.44, (120, 165, 235), 58)
    img = Image.composite(tint, img, mask)

    bg = img.getpixel((int(W * 0.775), int(H * 0.72)))
    # Held clear of the bottom edge: Play crops the feature graphic in some
    # placements, and a plinth flush to the edge is the first thing to be clipped.
    draw_mosque(img, cx=W * 0.775, cy=H * 0.44, unit=H / 108 * 0.98,
                color=GOLD, bg_for_arch=bg)

    d = ImageDraw.Draw(img, "RGBA")

    # Text block, right-aligned so it reads into the mark (RTL)
    right = W * 0.585
    f_title = font("vazirmatn_bold.ttf", int(94 * SS))
    f_latin = font("vazirmatn_medium.ttf", int(38 * SS))
    f_sub = font("vazirmatn_regular.ttf", int(30 * SS))

    def right_text(y, text, f, fill):
        w = d.textlength(text, font=f)
        d.text((right - w, y), text, font=f, fill=fill)

    # Short gold rule above the wordmark
    d.rounded_rectangle([right - 132 * SS, H * 0.225,
                         right, H * 0.225 + 6 * SS],
                        radius=int(3 * SS), fill=GOLD)

    right_text(H * 0.275, "اوقات نماز", f_title, WHITE)
    right_text(H * 0.545, "NAMAZIA", f_latin, GOLD_BRIGHT)
    right_text(H * 0.665, "اذان، قبله و تقویم برای افغانستان", f_sub, (255, 255, 255, 210))

    img = img.resize((1024, 500), Image.LANCZOS)
    img.save(path, "PNG", optimize=True)
    return path


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    for p in (build_icon(os.path.join(OUT, "play-icon-512.png")),
              build_feature(os.path.join(OUT, "play-feature-1024x500.png"))):
        im = Image.open(p)
        print(f"{os.path.basename(p):30s} {im.size[0]}x{im.size[1]}  "
              f"{os.path.getsize(p) / 1024:.0f} KB  {im.mode}")
