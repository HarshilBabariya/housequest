import os
import subprocess
import sys
import tempfile

import numpy as np
from scipy.signal import butter, sosfilt
from scipy.io import wavfile

SR = 44100
rng = np.random.default_rng(7)


def t_axis(dur):
    return np.arange(int(dur * SR)) / SR


def osc(freq, dur, kind="sine"):
    f = np.broadcast_to(np.asarray(freq, dtype=float), (int(dur * SR),))
    phase = 2 * np.pi * np.cumsum(f) / SR
    if kind == "sine":
        return np.sin(phase)
    if kind == "square":
        return np.sign(np.sin(phase))
    if kind == "saw":
        return 2 * ((phase / (2 * np.pi)) % 1.0) - 1
    if kind == "tri":
        return 2 * np.abs(2 * ((phase / (2 * np.pi)) % 1.0) - 1) - 1
    raise ValueError(kind)


def env(n, a=0.005, d=0.1, s=0.7, r=0.1):
    total = n / SR
    t = np.arange(n) / SR
    e = np.ones(n) * s
    e[t < a] = t[t < a] / a
    dm = (t >= a) & (t < a + d)
    e[dm] = 1 - (1 - s) * (t[dm] - a) / d
    rm = t > total - r
    e[rm] *= np.clip((total - t[rm]) / r, 0, 1)
    return e


def expdecay(n, rate):
    return np.exp(-rate * np.arange(n) / SR)


def noise(dur):
    return rng.uniform(-1, 1, int(dur * SR))


def lp(x, cutoff, order=4):
    return sosfilt(butter(order, cutoff, "low", fs=SR, output="sos"), x)


def hp(x, cutoff, order=2):
    return sosfilt(butter(order, cutoff, "high", fs=SR, output="sos"), x)


def bp(x, lo, hi, order=2):
    return sosfilt(butter(order, [lo, hi], "band", fs=SR, output="sos"), x)


def swept(x, cutoffs, make):
    filtered = [make(x, c) for c in cutoffs]
    pos = np.linspace(0, len(cutoffs) - 1, len(x))
    out = np.zeros(len(x))
    for i, y in enumerate(filtered):
        out += y * np.clip(1 - np.abs(pos - i), 0, 1)
    return out


def place(buf, x, at):
    i = int(at * SR)
    end = min(len(buf), i + len(x))
    buf[i:end] += x[: end - i]
    return buf


def blank(dur):
    return np.zeros(int(dur * SR))


def reverb(x, mix=0.25, decay=0.45):
    out = x.copy()
    for d, g in [(0.029, 0.6), (0.037, 0.55), (0.043, 0.5), (0.053, 0.45), (0.067, 0.4), (0.079, 0.35)]:
        k = int(d * SR)
        tail = np.zeros(len(x))
        gain = g
        shift = k
        while shift < len(x) and gain > 0.02:
            tail[shift:] += x[: len(x) - shift] * gain
            shift += k
            gain *= decay + 0.4
        out += tail * mix / 3
    return out


def bell(freq, dur, decay=3.0, partials=((1, 1.0), (2.76, 0.5), (5.4, 0.25), (8.93, 0.12))):
    n = int(dur * SR)
    out = np.zeros(n)
    for ratio, amp in partials:
        out += amp * osc(freq * ratio, dur) * expdecay(n, decay * (1 + ratio * 0.35))
    out *= env(n, a=0.002, d=0.01, s=1, r=0.05)
    return out


def pluck(freq, dur, damp=0.996):
    n = int(dur * SR)
    period = int(SR / freq)
    buf = rng.uniform(-1, 1, period)
    out = np.zeros(n)
    for i in range(n):
        out[i] = buf[i % period]
        buf[i % period] = damp * 0.5 * (buf[i % period] + buf[(i + 1) % period])
    return out


def drum(f0, f1, dur, decay=18, click=0.3):
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = f1 + (f0 - f1) * np.exp(-t * 30)
    body = osc(f, dur) * expdecay(n, decay)
    c = noise(dur) * expdecay(n, 300) * click
    return body + c


def click_wood(dur=0.08, freq=1900):
    n = int(dur * SR)
    return (osc(freq, dur) * 0.7 + bp(noise(dur), 1500, 4000) * 0.6) * expdecay(n, 90)


def brass(freq, dur, bright=2400, vib=0.0):
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = freq * (1 + vib * np.sin(2 * np.pi * 5.5 * t) * np.clip(t / 0.25, 0, 1))
    x = osc(f, dur, "saw") * 0.7 + osc(f * 2, dur, "saw") * 0.2
    x = lp(x, bright)
    return x * env(n, a=0.03, d=0.08, s=0.8, r=0.08)


def normalize(x, peak=0.89):
    x = x - np.mean(x)
    m = np.max(np.abs(x)) or 1
    return x / m * peak


def fade(x, dur=0.04):
    k = int(dur * SR)
    x[-k:] *= np.linspace(1, 0, k)
    x[: int(0.002 * SR)] *= np.linspace(0, 1, int(0.002 * SR))
    return x


# ---------- core sounds ----------

def sfx_cooker_whistle():
    buf = blank(2.1)
    d = 0.7
    n = int(d * SR)
    t = t_axis(d)
    place(buf, bp(noise(d), 3000, 8000) * (t / d) ** 1.5 * 0.45, 0.0)
    at = 0.05
    while at < 0.65:
        place(buf, (bp(noise(0.012), 1500, 5000) * expdecay(int(0.012 * SR), 250)) * 0.5, at)
        at += 0.11 - 0.12 * at
    d = 1.4
    n = int(d * SR)
    t = t_axis(d)
    swell = np.clip(t / 0.15, 0, 1) * np.exp(-np.clip(t - 0.9, 0, None) * 6)
    tone = osc(2250 + 120 * t, d) * (0.6 + 0.4 * np.sin(2 * np.pi * 27 * t)) + osc(4500 + 240 * t, d) * 0.15
    steam = bp(noise(d), 2000, 6000) * 0.35
    place(buf, (tone * 0.7 + steam) * swell, 0.62)
    return buf


def sfx_doorbell():
    buf = blank(1.9)
    place(buf, bell(659.3, 1.2, 2.2), 0.0)
    place(buf, bell(523.3, 1.5, 1.8), 0.45)
    return reverb(buf, 0.3)


def ting(freq, dur=0.6):
    return bell(freq, dur, 5.5, ((1, 1), (2.4, 0.35), (4.1, 0.15)))


def coins(buf, start, count, spread, gain=0.25):
    for i in range(count):
        place(buf, osc(rng.uniform(3200, 6800), 0.05) * expdecay(int(0.05 * SR), 60) * gain, start + i * spread * rng.uniform(0.6, 1.4))
    return buf


def sfx_debit_ting():
    buf = blank(1.0)
    place(buf, ting(1975.5), 0.0)
    place(buf, ting(1318.5, 0.8) * 0.9, 0.14)
    return reverb(buf, 0.2)


def sfx_credit_ting():
    buf = blank(1.2)
    place(buf, ting(1318.5), 0.0)
    place(buf, ting(1975.5, 0.9), 0.14)
    coins(buf, 0.3, 10, 0.05)
    return reverb(buf, 0.2)


def sfx_microwave():
    buf = blank(1.1)
    for i in range(3):
        d = 0.17
        place(buf, lp(osc(1050, d, "square"), 4000) * env(int(d * SR), 0.003, 0.01, 0.9, 0.01) * 0.5, i * 0.29)
    return buf


def sfx_card_declined():
    buf = blank(1.1)
    for at, f, d in ((0.0, 466, 0.22), (0.26, 311, 0.5)):
        x = lp(osc(f, d, "square") * 0.6 + osc(f / 2, d, "saw") * 0.3, 2500)
        place(buf, x * env(int(d * SR), 0.005, 0.02, 0.85, 0.04), at)
    return buf


def sfx_piggy_bank():
    buf = blank(1.3)
    coin = bell(3950, 0.35, 9, ((1, 1), (1.47, 0.6), (2.09, 0.35)))
    place(buf, coin * 0.7, 0.0)
    place(buf, coin * 0.4, 0.13)
    place(buf, coin * 0.2, 0.21)
    place(buf, bell(880, 0.6, 7, ((1, 1), (2.3, 0.3))) * 0.45, 0.02)
    for i in range(12):
        place(buf, hp(noise(0.004), 3000) * 0.25 * (1 - i / 12), 0.26 + i * 0.03)
    for i, f in enumerate((1568, 2093, 2637)):
        place(buf, ting(f, 0.5) * 0.3, 0.6 + i * 0.07)
    return reverb(buf, 0.25)


def sfx_cash_register():
    buf = blank(1.7)
    place(buf, (lp(noise(0.08), 1500) * expdecay(int(0.08 * SR), 50) + drum(200, 90, 0.08, 40, 0.2)) * 0.8, 0.0)
    d = 0.25
    place(buf, swept(noise(d), [600, 1500, 2500], lambda x, lo: bp(x, lo, lo * 2)) * np.sin(np.pi * t_axis(d) / d) * 0.5, 0.08)
    place(buf, bell(2093, 1.2, 2.5, ((1, 1), (1.5, 0.6), (2.0, 0.4), (3.01, 0.2))) * 0.8, 0.3)
    coins(buf, 0.35, 16, 0.045, 0.3)
    return reverb(buf, 0.25)


def sfx_clock():
    buf = blank(2.5)
    at, gap = 0.0, 0.32
    for i in range(9):
        place(buf, click_wood(0.05, 2600 if i % 2 == 0 else 1900) * 0.8, at)
        at += gap
        gap *= 0.86
    place(buf, bell(392, 1.0, 1.8, ((1, 1), (2.0, 0.5), (3.0, 0.25), (4.2, 0.1))) * 0.7, at + 0.05)
    return reverb(buf, 0.3)


def sfx_stove():
    buf = blank(1.7)
    for i in range(3):
        place(buf, (hp(noise(0.01), 4000) * expdecay(int(0.01 * SR), 300) + osc(5200, 0.01) * 0.3) * 0.8, i * 0.13)
    d = 0.5
    t = t_axis(d)
    whoomp = lp(noise(d), 700) * np.clip(t / 0.06, 0, 1) * np.exp(-t * 5) * 1.2 + drum(110, 50, d, 6, 0.0) * 0.6
    place(buf, whoomp, 0.42)
    d = 0.9
    t = t_axis(d)
    place(buf, bp(noise(d), 1500, 6000) * np.exp(-t * 3) * np.linspace(1, 0, len(t)) * 0.3, 0.55)
    for i, f in enumerate((784, 1046.5, 1318.5)):
        place(buf, ting(f, 0.6) * 0.3, 0.85 + i * 0.09)
    return reverb(buf, 0.25)


# ---------- festival sounds ----------

def pop(gain=1.0):
    d = 0.09
    n = int(d * SR)
    return (lp(noise(d), 4000) * expdecay(n, 45) + drum(140, 60, d, 30, 0.0) * 0.6) * gain


def sfx_diwali():
    buf = blank(2.9)
    d = 0.9
    t = t_axis(d)
    place(buf, bp(noise(d), 2500, 9000) * np.clip(t / 0.2, 0, 1) * 0.3, 0.0)
    for _ in range(25):
        place(buf, hp(noise(0.004), 3000) * rng.uniform(0.2, 0.5), rng.uniform(0.05, 0.9))
    d = 0.6
    t = t_axis(d)
    place(buf, osc(900 + 2200 * (t / d) ** 1.3, d) * np.clip(t / 0.08, 0, 1) * 0.3, 0.35)
    place(buf, (lp(noise(0.5), 2500) * expdecay(int(0.5 * SR), 12) + drum(90, 40, 0.5, 8, 0.0)) * 0.9, 0.95)
    at = 1.25
    while at < 2.45:
        place(buf, pop(rng.uniform(0.5, 0.9)), at)
        at += rng.uniform(0.04, 0.09)
    for _ in range(30):
        place(buf, hp(noise(0.004), 3000) * rng.uniform(0.15, 0.4), rng.uniform(2.3, 2.8))
    return reverb(buf, 0.3)


def dhol_pattern(buf, pattern, start, step):
    for i, hit in enumerate(pattern):
        at = start + i * step
        if hit == "D":
            place(buf, drum(160, 70, 0.4, 9, 0.35), at)
        elif hit == "t":
            place(buf, (drum(520, 380, 0.12, 30, 0.6) * 0.55), at)
        elif hit == "c":
            place(buf, click_wood() * 0.7, at)
    return buf


def sfx_holi():
    buf = blank(2.5)
    dhol_pattern(buf, "D.tt.tD.t.", 0.0, 0.1)
    for at in (0.95, 1.2):
        d = 0.22
        place(buf, swept(noise(d), [5000, 3000, 1800], lambda x, lo: bp(x, lo, lo * 1.6)) * expdecay(int(d * SR), 8) * 0.6, at)
    d = 0.7
    t = t_axis(d)
    splash = drum(120, 60, d, 14, 0.0) * 0.7 + lp(noise(d), 3500) * np.clip(t / 0.01, 0, 1) * np.exp(-t * 7) * 1.1
    place(buf, splash, 1.55)
    for _ in range(14):
        place(buf, osc(rng.uniform(1400, 3200), 0.03) * expdecay(int(0.03 * SR), 90) * 0.25, rng.uniform(1.7, 2.3))
    return reverb(buf, 0.2)


def sfx_tiranga():
    buf = blank(2.4)
    seq = [(523.3, 0.16), (659.3, 0.16), (784, 0.16), (1046.5, 0.5), (784, 0.2), (1046.5, 0.9)]
    at = 0.0
    for f, d in seq:
        place(buf, brass(f, d, 3800, vib=0.006 if d > 0.4 else 0) * 0.7, at)
        at += d + 0.02
    place(buf, drum(110, 50, 0.6, 6, 0.3) * 0.6, 0.52)
    return reverb(buf, 0.35)


def sfx_christmas():
    buf = blank(2.3)
    for i in range(16):
        at = i * 0.075
        jingle = sum(osc(f * rng.uniform(0.98, 1.02), 0.09) * a for f, a in ((4200, 0.5), (5650, 0.35), (7300, 0.25)))
        place(buf, jingle * expdecay(int(0.09 * SR), 40) * (0.3 + 0.2 * (i % 2)), at)
    for i, f in enumerate((784, 987.8, 1174.7, 1568)):
        place(buf, bell(f, 1.0, 2.6) * 0.45, 1.0 + i * 0.14)
    return reverb(buf, 0.35)


def sfx_new_year():
    buf = blank(2.5)
    for i in range(3):
        place(buf, osc(880, 0.11) * env(int(0.11 * SR), 0.003, 0.02, 0.8, 0.02) * 0.5, i * 0.32)
    place(buf, osc(1760, 0.35) * env(int(0.35 * SR), 0.003, 0.05, 0.7, 0.1) * 0.5, 0.96)
    place(buf, (hp(noise(0.12), 800) * expdecay(int(0.12 * SR), 35)) * 0.9, 1.3)
    d = 0.7
    n = int(d * SR)
    t = t_axis(d)
    horn = osc(380 + 160 * np.clip(t / 0.2, 0, 1), d, "saw") * (1 + 0.5 * osc(31, d, "square")) * 0.25
    place(buf, lp(horn, 3000) * env(n, 0.02, 0.05, 0.9, 0.1), 1.45)
    for _ in range(25):
        place(buf, osc(rng.uniform(2500, 6000), 0.05) * expdecay(int(0.05 * SR), 50) * 0.15, rng.uniform(1.3, 2.3))
    return reverb(buf, 0.25)


def sfx_uttarayan():
    buf = blank(2.2)
    d = 1.1
    n = int(d * SR)
    t = t_axis(d)
    flutter = bp(noise(d), 600, 2400) * (0.5 + 0.5 * np.sign(np.sin(2 * np.pi * (14 + 10 * t) * t))) * np.sin(np.pi * t / d) * 0.6
    place(buf, flutter, 0.0)
    for at in (0.9, 1.35):
        dd = 0.35
        tt = t_axis(dd)
        place(buf, osc(1200 + 1400 * tt / dd, dd) * np.sin(np.pi * tt / dd) * 0.35, at)
    dhol_pattern(buf, "D.t.D", 1.25, 0.11)
    return reverb(buf, 0.25)


def sfx_navratri():
    buf = blank(2.5)
    pattern = "DccDccDcDcc."
    for i, h in enumerate(pattern):
        at = i * 0.17
        if h == "D":
            place(buf, drum(150, 68, 0.38, 9, 0.35), at)
            place(buf, click_wood(0.07, 2100) * 0.6, at)
        elif h == "c":
            place(buf, click_wood(0.07, 1800 + (i % 2) * 250) * 0.75, at)
    place(buf, bell(1046.5, 0.9, 3) * 0.3, 1.9)
    return reverb(buf, 0.2)


def sfx_rakhi():
    buf = blank(2.4)
    place(buf, bell(440, 2.2, 1.2, ((1, 1), (2.4, 0.4), (4.1, 0.2), (6.8, 0.08))) * 0.6, 0.0)
    for i, f in enumerate((523.3, 587.3, 659.3, 784, 880, 1046.5)):
        place(buf, pluck(f, 0.7) * 0.4, 0.35 + i * 0.13)
    return reverb(buf, 0.3)


def sfx_ganesh():
    buf = blank(2.5)
    for i in range(18):
        at = i * 0.07
        place(buf, hp(noise(0.05), 1200) * expdecay(int(0.05 * SR), 55) * (0.35 + 0.25 * (i % 3 == 0)), at)
    dhol_pattern(buf, "D..D..D.DD", 0.0, 0.14)
    place(buf, bell(880, 1.3, 1.6) * 0.55, 1.4)
    return reverb(buf, 0.25)


def sfx_sparkler():
    buf = blank(2.4)
    d = 2.4
    t = t_axis(d)
    swell = np.clip(t / 0.35, 0, 1) * np.clip((d - t) / 0.9, 0, 1)
    place(buf, bp(noise(d), 2800, 9000) * swell * 0.22, 0.0)
    for _ in range(140):
        place(buf, hp(noise(0.003), 4000) * rng.uniform(0.05, 0.18), rng.uniform(0.1, 2.1))
    for _ in range(6):
        f = rng.uniform(2400, 3600)
        place(buf, ting(f, 0.5) * 0.12, rng.uniform(0.3, 1.7))
    return reverb(buf, 0.2)


SOUNDS = {name: fn for name, fn in globals().items() if name.startswith("sfx_") and callable(fn)}


def main(out_dir):
    os.makedirs(out_dir, exist_ok=True)
    tmp = tempfile.mkdtemp()
    for name, fn in SOUNDS.items():
        x = fade(normalize(fn()))
        wav = os.path.join(tmp, name + ".wav")
        wavfile.write(wav, SR, (x * 32767).astype(np.int16))
        ogg = os.path.join(out_dir, name + ".ogg")
        subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-ac", "1", "-c:a", "libvorbis", "-q:a", "4", ogg], check=True)
        print(name, f"{len(x) / SR:.2f}s", os.path.getsize(ogg))


if __name__ == "__main__":
    main(sys.argv[1])
